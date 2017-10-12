/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.config.distributed.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.oodt.config.Constants;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;
import static org.apache.oodt.config.Constants.ZPaths.NAMESPACE;
import static org.apache.oodt.config.Constants.ZPaths.SEPARATOR;

/**
 * A set of utility methods to be used to do complex operations on zookeeper using {@link CuratorFramework}
 *
 * @author Imesha Sudasingha
 */
public class CuratorUtils {

    private static final Logger logger = LoggerFactory.getLogger(CuratorUtils.class);

    private CuratorUtils() {
    }

    /**
     * Loads zookeeper related properties from ZK_PROPERTIES_FILE.
     *
     * @throws IOException if properties file could not be read
     */
    public static void loadZookeeperProperties() throws IOException {
        System.getProperties().load(new FileInputStream(System.getProperty(ZK_PROPERTIES_FILE)));
    }

    /**
     * Create a ZNode in the given path if not exists, with the given data
     *
     * @param curatorFramework {@link CuratorFramework} client
     * @param zNodePath        the path in which the ZNode to be created
     */
    public static void createZNodeIfNotExists(CuratorFramework curatorFramework, String zNodePath, CreateMode mode, byte[] content) throws Exception {
        try {
            String path = curatorFramework.create().creatingParentsIfNeeded().withMode(mode).forPath(zNodePath, content);
            logger.debug("Created ZNode at path : {}", path);
        } catch (Exception e) {
            if (e instanceof KeeperException && KeeperException.Code.NODEEXISTS.equals(((KeeperException) e).code())) {
                // This is a desired behaviour, trying to create an already existing znode.
                logger.debug("Attempting to create an already existing ZNode at path {} : {}", zNodePath, e);
            } else {
                logger.error("Error when creating ZNode for path {} : {}", zNodePath, e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Gets the <code>String</code> value of the data in the given znode path
     *
     * @param client {@link CuratorFramework} client
     * @param path   ZNode path to be checked
     * @return ZNode's data as a string or null
     * @throws Exception zookeeper errors and etc
     */
    public static String getIfExists(CuratorFramework client, String path) throws Exception {
        return client.checkExists().forPath(path) != null ? new String(client.getData().forPath(path)) : null;
    }

    /**
     * Builds a {@link CuratorFramework} instance with given connect string. Will use the {@link CuratorUtils#logger}
     * for logging.
     *
     * @param connectString zookeeper connect string
     * @return CuratorFramework instance created
     */
    public static CuratorFramework newCuratorFrameworkClient(String connectString) {
        return newCuratorFrameworkClient(connectString, logger);
    }

    /**
     * Builds a {@link CuratorFramework} instance using the given connectString.
     *
     * @param connectString connection string to connect to zookeeper
     * @param logger        {@link Logger} instance of the calling class
     * @return Newly created CuratorFramework instance.
     */
    public static CuratorFramework newCuratorFrameworkClient(String connectString, Logger logger) {
        int connectionTimeoutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "15000"));
        int sessionTimeoutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "60000"));
        int retryInitialWaitMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "1000"));
        int maxRetryCount = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "3"));

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .namespace(NAMESPACE)
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(retryInitialWaitMs, maxRetryCount))
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs);

        /*
         * If authorization information is available, those will be added to the client. NOTE: These auth info are
         * for access control, therefore no authentication will happen when the client is being started. These
         * info will only be required whenever a client is accessing an already create ZNode. For another client of
         * another node to make use of a ZNode created by this node, it should also provide the same auth info.
         */
        if (System.getProperty(Constants.Properties.ZK_USERNAME) != null && System.getProperty(Constants.Properties.ZK_PASSWORD) != null) {
            String authenticationString = System.getProperty(Constants.Properties.ZK_USERNAME) + ":" + System.getProperty(Constants.Properties.ZK_PASSWORD);
            builder.authorization("digest", authenticationString.getBytes())
                    .aclProvider(new ACLProvider() {
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        public List<ACL> getAclForPath(String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }

        CuratorFramework client = builder.build();
        logger.debug("CuratorFramework client built successfully with connectString: {}, sessionTimeout: {} and connectionTimeout: {}",
                connectString, sessionTimeoutMs, connectionTimeoutMs);

        return client;
    }

    /**
     * Get the leaf nodes in a given sub tree starting from a given ZNode.
     *
     * @param client          {@link CuratorFramework} instance
     * @param parentZNodePath root ZNode of the sub tree
     * @return List of leaf nodes
     * @throws Exception zookeeper exceptions
     */
    public static List<String> getLeafZNodePaths(CuratorFramework client, String parentZNodePath) throws Exception {
        List<String> leafZNodePaths = new ArrayList<>();

        List<String> childNodes = client.getChildren().forPath(parentZNodePath);
        if (childNodes != null && childNodes.size() > 0) {
            for (String child : childNodes) {
                String childZNodePath = parentZNodePath + SEPARATOR + child;
                leafZNodePaths.addAll(getLeafZNodePaths(client, childZNodePath));
            }
        } else {
            // Then, current ZNode path is a leaf node
            leafZNodePaths.add(parentZNodePath);
        }

        return leafZNodePaths;
    }

    /**
     * Delete all the child ZNodes under a given ZNode.
     *
     * @param client          {@link CuratorFramework} instance
     * @param parentZNodePath ZNode path of which all the children are to be deleted
     * @throws Exception
     */
    public static void deleteChildNodes(CuratorFramework client, String parentZNodePath) throws Exception {
        if (client.checkExists().forPath(parentZNodePath) != null) {
            List<String> children = client.getChildren().forPath(parentZNodePath);
            for (String child : children) {
                String zNodePath = parentZNodePath + SEPARATOR + child;
                logger.debug("Deleting child ZNode '{}' at {}", child, zNodePath);
                client.delete().deletingChildrenIfNeeded().forPath(zNodePath);
            }
            logger.debug("Deleted children ZNodes of {}", parentZNodePath);
        } else {
            logger.warn("ZNode - {} doesn't exist. Nothing to delete", parentZNodePath);
        }
    }
}

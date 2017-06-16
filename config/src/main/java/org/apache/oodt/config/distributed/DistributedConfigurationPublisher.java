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

package org.apache.oodt.config.distributed;

import org.apache.commons.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.utils.CuratorUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;
import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

/**
 * The class to publish configuration to Zookeeper
 *
 * @author Imesha Sudasingha
 */
public class DistributedConfigurationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DistributedConfigurationPublisher.class);

    private Map<String, String> propertiesFiles;
    private String connectString;
    private CuratorFramework client;
    private ZPaths zPaths;

    public DistributedConfigurationPublisher(String component, Map<String, String> propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
        this.zPaths = new ZPaths(component);

        if (System.getProperty(ZK_PROPERTIES_FILE) == null && System.getProperty(Constants.Properties.ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires system properties " + ZK_PROPERTIES_FILE + " or " + ZK_CONNECT_STRING + " to be set");
        }

        if (System.getProperty(ZK_PROPERTIES_FILE) != null) {
            try {
                CuratorUtils.loadZookeeperProperties();
            } catch (IOException e) {
                logger.error("Error occurred when loading properties from properties file");
            }
        }

        if (System.getProperty(Constants.Properties.ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires a proper connect string to connect to zookeeper ensemble");
        }

        connectString = System.getProperty(Constants.Properties.ZK_CONNECT_STRING);
        logger.info("Using zookeeper connect string : {}", connectString);

        startZookeeper();
    }

    /**
     * Creates a {@link CuratorFramework} instance and start it. This method will wait a maximum amount of
     * {@link Constants.Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
     */
    private void startZookeeper() {
        int connectionTimeoutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "15000"));
        int sessionTimeoutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "60000"));
        int retryInitialWaitMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "1000"));
        int maxRetryCount = Integer.parseInt(System.getProperty(Constants.Properties.ZK_CONNECTION_TIMEOUT, "3"));
        int startupTimeOutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_STARTUP_TIMEOUT, "30000"));

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
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

        client = builder.build();
        logger.debug("CuratorFramework client built successfully with connectString: {}, sessionTimeout: {} and connectionTimeout: {}",
                connectString, sessionTimeoutMs, connectionTimeoutMs);

        client.start();
        logger.info("Curator framework start operation invoked");

        try {
            logger.info("Waiting to connect to zookeeper, startupTimeout : {}", startupTimeOutMs);
            client.blockUntilConnected(startupTimeOutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            logger.error("Interrupted while waiting to connect zookeeper (connectString : {}) : {}", ex, connectString);
        }

        if (!client.getZookeeperClient().isConnected()) {
            throw new IllegalStateException("Could not connect to ZooKeeper : " + connectString);
        }

        logger.info("CuratorFramework client started successfully");
    }

    public void publishConfiguration() throws Exception {
        for (Map.Entry<String, String> entry : propertiesFiles.entrySet()) {
            logger.info("Publishing configuration {} - {}", entry.getKey(), entry.getValue());
            URL resource = Thread.currentThread().getContextClassLoader().getResource(entry.getKey());

            String content;
            try {
                content = FileUtils.readFileToString(new File(resource.toURI()));
            } catch (IOException | URISyntaxException e) {
                logger.error("Unable to read file : {}", entry.getKey(), e);
                continue;
            }

            String zNodePath = zPaths.getPropertiesZNodePath(entry.getValue());
            if (client.checkExists().forPath(zNodePath) != null) {
                byte[] bytes = client.getData().forPath(zNodePath);
                String existingData = new String(bytes);
                if (content.equals(existingData)) {
                    logger.info("{} already exists in zookeeper at {}", entry.getKey(), entry.getValue());
                    continue;
                } else {
                    Stat stat = client.setData().forPath(zNodePath, content.getBytes());
                    if (stat != null) {
                        logger.info("Published {} to {}", entry.getKey(), entry.getValue());
                    }
                }
            } else {
                client.create().creatingParentContainersIfNeeded().forPath(zNodePath, content.getBytes());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            logger.warn("Component name and at least one properties file needs to be given configuration publishing");
            throw new IllegalArgumentException("Requires at least two arguments, component and one properties file");
        }

        logger.debug("Starting publishing configuration. Component : {}", args[0]);

        String[] fileMapping = Arrays.copyOfRange(args, 1, args.length);
        Map<String, String> propertiesFiles = new HashMap<>();
        for (String entry : fileMapping) {
            String[] strings = entry.split("=");
            propertiesFiles.put(strings[0], strings[1]);
        }

        DistributedConfigurationPublisher distributedConfigurationPublisher = new DistributedConfigurationPublisher(args[0], propertiesFiles);
        try {
            distributedConfigurationPublisher.publishConfiguration();
        } catch (Exception e) {
            logger.error("Error occurred when publishing configuration to zookeeper", e);
        }

        logger.info("Published configuration successfully");
    }
}

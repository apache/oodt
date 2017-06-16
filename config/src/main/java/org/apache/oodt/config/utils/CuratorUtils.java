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

package org.apache.oodt.config.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

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

}

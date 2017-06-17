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

import org.apache.curator.framework.CuratorFramework;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.Constants.Properties;
import org.apache.oodt.config.utils.CuratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;
import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

/**
 * Distributed configuration manager implementation. This class make use of a {@link CuratorFramework} instance to connect
 * to zookeeper
 *
 * @author Imesha Sudasingha.
 */
public class DistributedConfigurationManager extends ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(DistributedConfigurationManager.class);

    /** Variables required to connect to zookeeper */
    private String connectString;
    private CuratorFramework client;

    public DistributedConfigurationManager(String component) {
        super(component);

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
     * {@link Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
     */
    private void startZookeeper() {
        client = CuratorUtils.getCuratorFrameworkClient(connectString, logger);

        client.start();
        logger.info("Curator framework start operation invoked");

        int startupTimeOutMs = Integer.parseInt(System.getProperty(Properties.ZK_STARTUP_TIMEOUT, "30000"));
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

    @Override
    public void loadProperties() {
        // todo Implement the logic with Curator
    }
}

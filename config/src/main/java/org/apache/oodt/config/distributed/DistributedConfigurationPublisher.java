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
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.distributed.utils.CuratorUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;
import static org.apache.oodt.config.Constants.Properties.ZK_PROPERTIES_FILE;

/**
 * The class to publish configuration to Zookeeper. When using distributed configuration with OODT, configuration per
 * each component type needs to be stored in zookeeper beforehand. This class, provides the means to do that.
 *
 * @author Imesha Sudasingha
 */
public class DistributedConfigurationPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DistributedConfigurationPublisher.class);

    private Map<String, String> propertiesFiles;
    private Map<String, String> configFiles;
    private String connectString;
    private CuratorFramework client;
    private ZNodePaths zNodePaths;
    private String componentName;

    public DistributedConfigurationPublisher(String componentName) {
        this.componentName = componentName;
        this.zNodePaths = new ZNodePaths(componentName);

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
        client = CuratorUtils.getCuratorFrameworkClient(connectString, logger);

        client.start();
        logger.info("Curator framework start operation invoked");

        int startupTimeOutMs = Integer.parseInt(System.getProperty(Constants.Properties.ZK_STARTUP_TIMEOUT, "30000"));
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
        logger.debug("Publishing properties files : {}", propertiesFiles);
        publishConfiguration(propertiesFiles, true);
        logger.info("Properties files published successfully");

        logger.debug("Publishing config files : {}", configFiles);
        publishConfiguration(configFiles, false);
        logger.info("Config files published successfully");

        // TODO: 6/17/17 Verify whether the given configuration are published correctly
    }

    private void publishConfiguration(Map<String, String> fileMapping, boolean isProperties) throws Exception {
        for (Map.Entry<String, String> entry : fileMapping.entrySet()) {
            logger.debug("Publishing configuration {} - {}", entry.getKey(), entry.getValue());
            URL resource = Thread.currentThread().getContextClassLoader().getResource(entry.getKey());

            String content;
            try {
                content = FileUtils.readFileToString(new File(resource.toURI()));
            } catch (IOException | URISyntaxException e) {
                logger.error("Unable to read file : {}", entry.getKey(), e);
                continue;
            }

            String zNodePath = isProperties ? zNodePaths.getPropertiesZNodePath(entry.getValue()) : zNodePaths.getConfigurationZNodePath(entry.getValue());
            if (client.checkExists().forPath(zNodePath) != null) {
                byte[] bytes = client.getData().forPath(zNodePath);
                String existingData = new String(bytes);
                if (content.equals(existingData)) {
                    logger.warn("{} already exists in zookeeper at {}", entry.getKey(), entry.getValue());
                } else {
                    Stat stat = client.setData().forPath(zNodePath, content.getBytes());
                    if (stat != null) {
                        logger.info("Published configuration file {} to {}", entry.getKey(), entry.getValue());
                    }
                }
            } else {
                /*
                 * Creating these ZNodes with parent 'Containers' is important since containers are automatically deleted
                 * when no child node is present under them.
                 */
                client.create().creatingParentContainersIfNeeded().forPath(zNodePath, content.getBytes());
            }
        }
    }

    public Map<String, String> getPropertiesFiles() {
        return propertiesFiles;
    }

    public void setPropertiesFiles(Map<String, String> propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
    }

    public Map<String, String> getConfigFiles() {
        return configFiles;
    }

    public void setConfigFiles(Map<String, String> configFiles) {
        this.configFiles = configFiles;
    }

    public ZNodePaths getZNodePaths() {
        return zNodePaths;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("Spring configuration file needs to be given as an argument");
            return;
        }

        logger.info("Starting publishing configuration. Spring conf : {}", args[0]);

        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(args[0]);
            Map distributedConfigurationPublisher = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);
            for (Object bean : distributedConfigurationPublisher.values()) {
                DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;

                logger.debug("Publishing configuration for : {}", publisher.getComponentName());
                publisher.publishConfiguration();
                logger.info("Published configuration for : {}", publisher.getComponentName());
            }
        } catch (BeansException e) {
            logger.error("Error occurred when obtaining configuration publisher beans", e);
            return;
        } catch (Exception e) {
            logger.error("Error occurred when publishing configuration to zookeeper", e);
            return;
        }

        logger.info("Published configuration successfully");
    }

    public String getComponentName() {
        return componentName;
    }
}

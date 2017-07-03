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

package org.apache.oodt.config.distributed.cli;

import org.apache.commons.io.FileUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.distributed.ZNodePaths;
import org.apache.oodt.config.distributed.utils.CuratorUtils;
import org.apache.zookeeper.data.Stat;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
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

        if (System.getProperty(ZK_PROPERTIES_FILE) == null && System.getProperty(ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires system properties " + ZK_PROPERTIES_FILE + " or " + ZK_CONNECT_STRING + " to be set");
        }

        if (System.getProperty(ZK_PROPERTIES_FILE) != null) {
            try {
                CuratorUtils.loadZookeeperProperties();
            } catch (IOException e) {
                logger.error("Error occurred when loading properties from properties file");
            }
        }

        if (System.getProperty(ZK_CONNECT_STRING) == null) {
            throw new IllegalArgumentException("Zookeeper requires a proper connect string to connect to zookeeper ensemble");
        }

        connectString = System.getProperty(ZK_CONNECT_STRING);
        logger.info("Using zookeeper connect string : {}", connectString);

        startZookeeper();
    }

    /**
     * Creates a {@link CuratorFramework} instance and start it. This method will wait a maximum amount of
     * {@link Constants.Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
     */
    private void startZookeeper() {
        client = CuratorUtils.newCuratorFrameworkClient(connectString, logger);

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

    public void destroy() {
        logger.debug("Destroying configuration publisher");
        try {
            client.close();
        } catch (Exception e) {
            logger.error("Error occurred when trying to close Curator client : {}", e);
        }

        logger.info("Configuration publisher destroyed");
    }

    /**
     * Publishes the configuration files specified to zookeeper. If an exception is thrown while configuration being
     * published, no further publishing attempts will be carried on. Error will be reported to user.
     *
     * @throws Exception Zookeeper errors
     */
    public void publishConfiguration() throws Exception {
        logger.debug("Publishing properties files : {}", propertiesFiles);
        publishConfiguration(propertiesFiles, true);
        logger.info("Properties files published successfully");

        logger.debug("Publishing config files : {}", configFiles);
        publishConfiguration(configFiles, false);
        logger.info("Config files published successfully");
    }

    /**
     * Verified whether the actual content of the local files specified to be published are 100% similar to the ones that
     * has been published and stored in zookeeper at the moment.
     *
     * @return true | if content are up to date and similar
     */
    public boolean verifyPublishedConfiguration() {
        try {
            return verifyPublishedConfiguration(propertiesFiles, true) && verifyPublishedConfiguration(configFiles, false);
        } catch (Exception e) {
            logger.error("Error occurred when checking published config", e);
            return false;
        }
    }

    /**
     * Removes all the nodes from zookeeper where the configuration corresponding to component {@link #componentName} is
     * stored
     *
     * @throws Exception zookeeper errors
     */
    public void clearConfiguration() throws Exception {
        logger.debug("Clearing configuration from zookeeper");
        CuratorUtils.deleteChildNodes(client, zNodePaths.getPropertiesZNodePath());
        CuratorUtils.deleteChildNodes(client, zNodePaths.getConfigurationZNodePath());
        logger.info("Configuration cleared!");
    }

    private void publishConfiguration(Map<String, String> fileMapping, boolean isProperties) throws Exception {
        for (Map.Entry<String, String> entry : fileMapping.entrySet()) {
            String filePath = entry.getKey();
            String relativeZNodePath = entry.getValue();
            logger.info("Publishing configuration {} - {}", filePath, relativeZNodePath);

            String content = getFileContent(filePath);

            String zNodePath = isProperties ? zNodePaths.getPropertiesZNodePath(relativeZNodePath) : zNodePaths.getConfigurationZNodePath(relativeZNodePath);
            if (client.checkExists().forPath(zNodePath) != null) {
                byte[] bytes = client.getData().forPath(zNodePath);
                String existingData = new String(bytes);
                if (existingData.equals(content)) {
                    logger.warn("{} already exists in zookeeper at {}", filePath, relativeZNodePath);
                } else {
                    Stat stat = client.setData().forPath(zNodePath, content.getBytes());
                    if (stat != null) {
                        logger.info("Published configuration file {} to {}", filePath, relativeZNodePath);
                    } else {
                        logger.warn("Unable to publish configuration file {} to {}", filePath, relativeZNodePath);
                    }
                }
            } else {
                /*
                 * Creating these ZNodes with parent 'Containers' is important since containers are automatically deleted
                 * when no child node is present under them.
                 */
                client.create().creatingParentContainersIfNeeded().forPath(zNodePath, content.getBytes());
                logger.info("Replaced old published configuration at {} with content of file : {}", relativeZNodePath, filePath);
            }
        }
    }

    private boolean verifyPublishedConfiguration(Map<String, String> fileMapping, boolean isProperties) throws Exception {
        boolean noError = true;
        for (Map.Entry<String, String> entry : fileMapping.entrySet()) {
            String filePath = entry.getKey();
            String relativeZNodePath = entry.getValue();
            logger.info("Checking published configuration for {} - {}", filePath, relativeZNodePath);

            String originalContent = getFileContent(filePath);

            String zNodePath = isProperties ? zNodePaths.getPropertiesZNodePath(relativeZNodePath) : zNodePaths.getConfigurationZNodePath(relativeZNodePath);
            if (client.checkExists().forPath(zNodePath) == null) {
                logger.error("File : {} hasn't been published to ZNode : {}", filePath, relativeZNodePath);
                noError = false;
                continue;
            }

            String publishedContent = new String(client.getData().forPath(zNodePath));
            if (!publishedContent.equals(originalContent)) {
                logger.error("Content of local file : {} and content published to {} are not similar", filePath, relativeZNodePath);
                noError = false;
                continue;
            }

            logger.info("{} - {} configuration checked and OK", filePath, relativeZNodePath);
        }

        return noError;
    }

    private String getFileContent(String file) {
        String content;
        try {
            content = FileUtils.readFileToString(new File(file));
        } catch (IOException e) {
            logger.error("Unable to read file : {}", file, e);
            throw new IllegalArgumentException("Unable to read content of the file : " + file);
        }

        return content;
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

    public static void main(String[] args) throws Exception {
        CmdLineOptions cmdLineOptions = new CmdLineOptions();
        CmdLineParser parser = new CmdLineParser(cmdLineOptions);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("There's an error in your command");
            parser.printUsage(System.err);
            return;
        }

        if (cmdLineOptions.getConnectString() == null && System.getProperty(ZK_CONNECT_STRING) == null) {
            System.err.println("Zookeeper connect string is not found");
            parser.printUsage(System.err);
            return;
        } else {
            System.setProperty(ZK_CONNECT_STRING, cmdLineOptions.getConnectString());
        }

        System.out.println("Starting configuration publishing");

        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext(Constants.CONFIG_PUBLISHER_XML);
            Map distributedConfigurationPublisher = applicationContext.getBeansOfType(DistributedConfigurationPublisher.class);

            for (Object bean : distributedConfigurationPublisher.values()) {
                DistributedConfigurationPublisher publisher = (DistributedConfigurationPublisher) bean;
                System.out.println(String.format("\nProcessing commands for component : %s", publisher.getComponentName()));

                if (cmdLineOptions.isPublish()) {
                    System.out.println(String.format("Publishing configuration for : %s", publisher.getComponentName()));
                    publisher.publishConfiguration();
                    System.out.println(String.format("Published configuration for : %s", publisher.getComponentName()));
                    System.out.println();
                }

                if (cmdLineOptions.isVerify()) {
                    System.out.println(String.format("Verifying configuration for : %s", publisher.getComponentName()));
                    if (publisher.verifyPublishedConfiguration()) {
                        System.out.println("OK... Configuration verified");
                        System.out.println(String.format("Verified configuration for : %s", publisher.getComponentName()));
                    } else {
                        System.err.println("ERROR... Published configuration doesn't match the local files. Please check above logs");
                    }
                    System.out.println();
                }

                if (cmdLineOptions.isClear()) {
                    System.out.println(String.format("Clearing configuration for : %s", publisher.getComponentName()));
                    publisher.clearConfiguration();
                    System.out.println(String.format("Cleared configuration for : %s", publisher.getComponentName()));
                    System.out.println();
                }

                publisher.destroy();
            }
        } catch (BeansException e) {
            logger.error("Error occurred when obtaining configuration publisher beans", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error occurred when publishing configuration to zookeeper", e);
            throw e;
        }

        logger.info("Exiting CLI ...");
    }

    public String getComponentName() {
        return componentName;
    }
}

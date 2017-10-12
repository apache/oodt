/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigEventType;
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.distributed.utils.CuratorUtils;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.oodt.config.Constants.DEFAULT_PROJECT;
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

    private Component component;
    private String project;

    public DistributedConfigurationPublisher(Component component) {
        this(component, DEFAULT_PROJECT);
    }

    public DistributedConfigurationPublisher(Component component, String project) {
        this.component = component;
        this.project = project;
        this.zNodePaths = new ZNodePaths(this.project, this.component.getName());

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

        try {
            logger.debug("Creating ZNode paths");
            zNodePaths.createZNodes(client);
        } catch (Exception e) {
            logger.error("Error occurred when creating initial ZNode paths", e);
            throw new IllegalStateException("Unable to create ZNode paths", e);
        }
    }

    /**
     * Creates a {@link CuratorFramework} instance and start it. This method will wait a maximum amount of {@link
     * Constants.Properties#ZK_STARTUP_TIMEOUT} milli-seconds until the client connects to the zookeeper ensemble.
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
     * Verified whether the actual content of the local files specified to be published are 100% similar to the ones
     * that has been published and stored in zookeeper at the moment.
     *
     * @return true | if content are up to date and similar
     */
    public boolean verifyPublishedConfiguration() {
        try {
            return verifyPublishedConfiguration(propertiesFiles, true) &&
                    verifyPublishedConfiguration(configFiles, false);
        } catch (Exception e) {
            logger.error("Error occurred when checking published config", e);
            return false;
        }
    }

    /**
     * Removes all the nodes from zookeeper where the configuration corresponding to component {@link #component} is
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

    /**
     * Notifies the watching {@link org.apache.oodt.config.ConfigurationManager}s about the configuration change
     *
     * @param type {@link ConfigEventType}
     * @throws Exception
     */
    public void notifyConfigEvent(ConfigEventType type) throws Exception {
        logger.info("Notifying event: '{}' to configuration managers of {}", type, component);
        client.setData().forPath(zNodePaths.getNotificationsZNodePath(), type.toString().getBytes());
    }

    /**
     * Publishes configuration from local files to zookeeper.
     *
     * @param fileMapping  source file to ZNode path mappings
     * @param isProperties if true, files will be stored under {@link ZNodePaths#propertiesZNodePath}
     * @throws Exception
     */
    private void publishConfiguration(Map<String, String> fileMapping, boolean isProperties) throws Exception {
        for (Map.Entry<String, String> entry : fileMapping.entrySet()) {
            String filePath = entry.getKey();
            String relativeZNodePath = entry.getValue();
            logger.debug("Publishing configuration {} to {}", filePath, relativeZNodePath);

            String content = getFileContent(filePath);

            String zNodePath = isProperties ? zNodePaths.getPropertiesZNodePath(relativeZNodePath) :
                    zNodePaths.getConfigurationZNodePath(relativeZNodePath);
            if (client.checkExists().forPath(zNodePath) != null) {
                byte[] bytes = client.getData().forPath(zNodePath);
                String existingData = new String(bytes);
                if (existingData.equals(content)) {
                    logger.warn("{} already exists in zookeeper at {}", filePath, relativeZNodePath);
                } else {
                    Stat stat = client.setData().forPath(zNodePath, content.getBytes());
                    if (stat != null) {
                        logger.info("Replaced old published configuration at {} with content of file : {}", zNodePath, filePath);
                    } else {
                        logger.warn("Unable to replace published configuration at {} with file: {}", zNodePath, filePath);
                    }
                }
            } else {
                /*
                 * Creating these ZNodes with parent 'Containers' is important since containers are automatically deleted
                 * when no child node is present under them.
                 */
                client.create().creatingParentContainersIfNeeded().forPath(zNodePath, content.getBytes());
                logger.info("Published configuration file {} to {}", filePath, zNodePath);
            }
        }
    }

    /**
     * Verifies whether the content in local files given by keys of the <pre>fileMapping</pre> are identical to the
     * configuration stored in zookeeper under ZNode paths given by <pre>${prefix}/{fileMapping.value}</pre>
     *
     * @param fileMapping  src file to znode path mappings
     * @param isProperties if true, treated as properties files and will look under {@link
     *                     ZNodePaths#propertiesZNodePath}
     * @return true, if all the configuration verification was successful and no error was detected.
     * @throws Exception
     */
    private boolean verifyPublishedConfiguration(Map<String, String> fileMapping, boolean isProperties) throws Exception {
        boolean noError = true;
        for (Map.Entry<String, String> entry : fileMapping.entrySet()) {
            String filePath = entry.getKey();
            String relativeZNodePath = entry.getValue();
            logger.debug("Checking published configuration for {} - {}", filePath, relativeZNodePath);

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

        if (!noError) {
            logger.warn("There are errors in configuration publishing");
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

    public Component getComponent() {
        return component;
    }

    public String getProject() {
        return project;
    }
}

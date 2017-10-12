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
import org.apache.oodt.config.distributed.utils.CuratorUtils;
import org.apache.zookeeper.CreateMode;

import static org.apache.oodt.config.Constants.DEFAULT_PROJECT;
import static org.apache.oodt.config.Constants.ZPaths.COMPONENTS_PATH_NAME;
import static org.apache.oodt.config.Constants.ZPaths.CONFIGURATION_PATH_NAME;
import static org.apache.oodt.config.Constants.ZPaths.NOTIFICATIONS_PATH;
import static org.apache.oodt.config.Constants.ZPaths.PROJECTS_PATH_NAME;
import static org.apache.oodt.config.Constants.ZPaths.PROPERTIES_PATH_NAME;
import static org.apache.oodt.config.Constants.ZPaths.SEPARATOR;

/**
 * Class responsible for handling all the Zookeeper ZNode paths related to configuration
 *
 * @author Imesha Sudasingha
 */
public class ZNodePaths {

    /** ZNode for distinct components. /projects/${project}/components/${component} */
    private String componentZNodePath;
    private String componentZNodeRoot;

    /** ZNode path for properties files. /projects/${project}/components/${component}/properties */
    private String propertiesZNodePath;
    private String propertiesZNodeRoot;

    /** ZNode path for other configuration files. /projects/${project}/components/${component}/configuration */
    private String configurationZNodePath;
    private String configurationZNodeRoot;

    /** ZNode to be watched for configuration changes. /projects/${project}/components/${component}/notifications */
    private String notificationsZNodePath;

    /**
     * Creates the ZNode path structure accordingly to the <pre>componentName</pre> and <pre>propertiesFileNames</pre>
     * given.
     *
     * @param componentName Name of the OODT component
     */
    public ZNodePaths(String project, String componentName) {
        if (project == null) {
            project = DEFAULT_PROJECT;
        }

        if (componentName == null) {
            throw new IllegalArgumentException("Component name cannot be null");
        }

        /* ZNode for distinct projects. /projects/${project} */
        String projectZNodePath = SEPARATOR + PROJECTS_PATH_NAME + SEPARATOR + project;
        String projectZNodeRoot = projectZNodePath + SEPARATOR;

        componentZNodePath = projectZNodeRoot + COMPONENTS_PATH_NAME + SEPARATOR + componentName;
        componentZNodeRoot = componentZNodePath + SEPARATOR;

        propertiesZNodePath = componentZNodeRoot + PROPERTIES_PATH_NAME;
        propertiesZNodeRoot = propertiesZNodePath + SEPARATOR;

        configurationZNodePath = componentZNodeRoot + CONFIGURATION_PATH_NAME;
        configurationZNodeRoot = configurationZNodePath + SEPARATOR;

        notificationsZNodePath = componentZNodeRoot + NOTIFICATIONS_PATH;
    }

    /**
     * Creates the initial ZNode structure in zookeeper. Supposed to be called by the {@link
     * DistributedConfigurationPublisher}.
     *
     * @param client {@link CuratorFramework} instance
     * @throws Exception
     */
    public void createZNodes(CuratorFramework client) throws Exception {
        CuratorUtils.createZNodeIfNotExists(client, propertiesZNodePath, CreateMode.PERSISTENT, new byte[1]);
        CuratorUtils.createZNodeIfNotExists(client, configurationZNodePath, CreateMode.PERSISTENT, new byte[1]);
        CuratorUtils.createZNodeIfNotExists(client, notificationsZNodePath, CreateMode.PERSISTENT, new byte[1]);
    }

    public String getComponentZNodePath() {
        return componentZNodePath;
    }

    public String getPropertiesZNodePath() {
        return propertiesZNodePath;
    }

    public String getConfigurationZNodePath() {
        return configurationZNodePath;
    }

    public String getPropertiesZNodePath(String subPath) {
        return propertiesZNodeRoot + (subPath.startsWith(SEPARATOR) ? subPath.substring(1) : subPath);
    }

    public String getConfigurationZNodePath(String subPath) {
        return configurationZNodeRoot + (subPath.startsWith(SEPARATOR) ? subPath.substring(1) : subPath);
    }

    public String getLocalConfigFilePath(String zNodePath) {
        return zNodePath.substring(configurationZNodeRoot.length());
    }

    public String getLocalPropertiesFilePath(String zNodePath) {
        return zNodePath.substring(propertiesZNodeRoot.length());
    }

    public String getNotificationsZNodePath() {
        return notificationsZNodePath;
    }
}

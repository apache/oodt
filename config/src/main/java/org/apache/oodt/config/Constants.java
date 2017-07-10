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

package org.apache.oodt.config;

import java.io.File;

/**
 * Constants to be used by the config package
 *
 * @author Imesha Sudasingha
 */
public class Constants {

    private Constants() {
    }

    public static final String SEPARATOR = File.separator;

    /** Node name to be used when the configuration manager is the standalone version */
    public static final String STANDALONE_NODE_NAME = "local";

    /** Default environment name to be used */
    public static final String DEFAULT_ENVIRONMENT = "default";

    /** The XML file name in which the configuration to be published to zookeeper is defined */
    public static final String CONFIG_PUBLISHER_XML = "etc" + SEPARATOR + "config-publisher.xml";

    public static class Properties {
        /** The system property to be set in order to enable distributed configuration management */
        public static final String ENABLE_DISTRIBUTED_CONFIGURATION = "org.apache.oodt.config.distributed";

        /** System property, which will holf the location of the zookeeper properties file */
        public static final String ZK_PROPERTIES_FILE = "org.apache.oodt.config.zkProperties";

        /** Property name to fetch connect string to connect to a zookeeper ensemble. */
        public static final String ZK_CONNECT_STRING = "org.apache.oodt.config.zk.connectString";

        public static final String ZK_CONNECTION_TIMEOUT = "org.apache.oodt.config.zk.connectionTimeoutMs";
        public static final String ZK_SESSION_TIMEOUT = "org.apache.oodt.config.zk.sessionTimeoutMs";

        public static final String ZK_RETRY_INITIAL_WAIT = "org.apache.oodt.config.zk.retryInitialWaitMs";
        public static final String ZK_RETRY_MAX_RETRIES = "org.apache.oodt.config.zk.maxRetries";
        public static final String ZK_STARTUP_TIMEOUT = "org.apache.oodt.config.zk.startupTimeoutMs";

        public static final String ZK_USERNAME = "org.apache.oodt.config.zk.username";
        public static final String ZK_PASSWORD = "org.apache.oodt.config.zk.password";
    }

    public enum Component {
        FILE_MANAGER("filemgr", "FILEMGR_HOME"),
        RESOURCE_MANAGER("resmgr", "RESMGR_HOME");

        String name;
        String home;

        Component(String name, String home) {
            this.name = name;
            this.home = home;
        }

        public String getName() {
            return name;
        }

        public String getHome() {
            return home;
        }
    }

    public static class ZPaths {
        /** Separator for ZNode paths */
        public static final String SEPARATOR = "/";

        /** Namespace to be used when creating ZNodes in Zookeeper */
        public static final String NAMESPACE = "oodt";

        /** Where OODT components related configuration are stored */
        public static final String COMPONENTS_PATH_NAME = "components";

        /** Where properties files are stored inside each component */
        public static final String PROPERTIES_PATH_NAME = "properties";

        /** Where other configuration files will be stored */
        public static final String CONFIGURATION_PATH_NAME = "configuration";
    }
}

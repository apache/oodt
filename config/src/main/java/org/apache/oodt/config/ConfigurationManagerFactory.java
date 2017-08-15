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

import org.apache.oodt.config.Constants.Env;
import org.apache.oodt.config.distributed.DistributedConfigurationManager;
import org.apache.oodt.config.standalone.StandaloneConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.oodt.config.Constants.Env.CONNECT_STRING;
import static org.apache.oodt.config.Constants.Properties.ENABLE_DISTRIBUTED_CONFIGURATION;
import static org.apache.oodt.config.Constants.Properties.ZK_CONNECT_STRING;

/**
 * Factory class to be used to get the {@link ConfigurationManager} instances accordingly.
 *
 * @author Imesha Sudasingha
 */
public class ConfigurationManagerFactory {

    /** Logger instance for this class */
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagerFactory.class);

    private ConfigurationManagerFactory() {
    }

    /**
     * Returns the {@link ConfigurationManager} to be used by the calling class. Whether to use the standalone version
     * or the distributed version of the configuration manager will be determined by the value of the property
     * <pre>org.apache.oodt.config.zookeeper == true</pre>
     *
     * @param component       Name of the OODT component, to which the created configuration manager instance will be
     *                        providing configuration support
     * @param propertiesFiles List of <pre>.properties</pre> files which are to be used in the case of standalone
     *                        configuration management
     * @return ConfigurationManager instance to used by the corresponding component.
     */
    public static ConfigurationManager getConfigurationManager(Component component, List<String> propertiesFiles) {
        String enableDistributed = System.getProperty(ENABLE_DISTRIBUTED_CONFIGURATION);
        boolean isDistributed;
        if (enableDistributed == null) {
            String env = System.getenv(Env.ENABLE_DISTRIBUTED_CONFIGURATION);
            isDistributed = Boolean.parseBoolean(env);
        } else {
            isDistributed = Boolean.parseBoolean(enableDistributed);
        }

        if (isDistributed) {
            String connectString = System.getProperty(ZK_CONNECT_STRING);
            if (connectString == null) {
                connectString = System.getenv(CONNECT_STRING);
            }

            if (connectString == null) {
                throw new IllegalArgumentException(
                        String.format("%s environment variable need to be set for distributed configuration management", CONNECT_STRING));
            }

            System.setProperty(ZK_CONNECT_STRING, connectString);

            logger.info("Using distributed configuration management for {}", component);
            return new DistributedConfigurationManager(component);
        }

        logger.info("Using standalone configuration management for {}", component);
        return new StandaloneConfigurationManager(component, propertiesFiles);
    }
}

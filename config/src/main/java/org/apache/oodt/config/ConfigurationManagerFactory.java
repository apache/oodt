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

import org.apache.oodt.config.distributed.DistributedConfigurationManager;
import org.apache.oodt.config.standalone.StandaloneConfigurationManager;

import java.util.List;
import java.util.logging.Logger;

import static org.apache.oodt.config.Constants.Properties.ENABLE_DISTRIBUTED_CONFIGURATION;

/**
 * Factory class to be used to get the {@link ConfigurationManager} instances accordingly.
 *
 * @author Imesha Sudasingha
 */
public class ConfigurationManagerFactory {

    /** Logger instance for this class */
    private static final Logger logger = Logger.getLogger(ConfigurationManagerFactory.class.getName());

    private ConfigurationManagerFactory() {
    }

    /**
     * Returns the {@link ConfigurationManager} to be used by the calling class. Whether to use the standalone version or
     * the distributed version of the configuration manager will be determined by the value of the property
     * <pre>org.apache.oodt.config.zookeeper == true</pre>
     *
     * @param component       Name of the OODT component, to which the created configuration manager instance will be providing
     *                        configuration support
     * @param propertiesFiles List of <pre>.properties</pre> files which are to be used in the case of standalone configuration
     *                        management
     * @return ConfigurationManager instance to used by the corresponding component.
     */
    public static ConfigurationManager getConfigurationManager(String component, List<String> propertiesFiles) {
        if (System.getProperty(ENABLE_DISTRIBUTED_CONFIGURATION) != null) {
            return new DistributedConfigurationManager(component);
        }
        return new StandaloneConfigurationManager(component, propertiesFiles);
    }
}

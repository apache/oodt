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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The abstract class to define functions of the configuration managers.
 *
 * @author Imesha Sudasingha
 */
public abstract class ConfigurationManager {

    protected Component component;
    protected String project;
    private Set<ConfigurationListener> configurationListeners = new HashSet<>(1);

    public ConfigurationManager(Component component) {
        this(component, Constants.DEFAULT_PROJECT);
    }

    public ConfigurationManager(Component component, String project) {
        this.component = component;
        this.project = project;
    }

    /**
     * Loads configuration required for {@link #component}. If distributed configuration management is enabled, this
     * will download configuration from zookeeper. Else, this will load properties files specified.
     *
     * @throws Exception
     */
    public abstract void loadConfiguration() throws Exception;

    /**
     * Clears loaded configuration. Invocation of this method will remove the downloaded configuration files to be
     * deleted in the distributed configuration management scenario. Any child class that is extending this class should
     * implement this operation on their own.
     */
    public abstract void clearConfiguration();

    public synchronized void addConfigurationListener(ConfigurationListener listener) {
        configurationListeners.add(listener);
    }

    public synchronized void removeConfigurationListener(ConfigurationListener listener) {
        configurationListeners.remove(listener);
    }

    protected synchronized void notifyConfigurationChange(ConfigEventType type) {
        for (ConfigurationListener listener : configurationListeners) {
            listener.configurationChanged(type);
        }
    }

    public Component getComponent() {
        return component;
    }

    /**
     * Returns a list of file paths which are the locations of the files stored locally corresponding to configuration.
     * In distributed configuration management scenario, this stands for the files downloaded and stored in local file
     * system.
     *
     * @return list of locally stored files
     */
    public abstract List<String> getSavedFiles();

    public String getProject() {
        return project;
    }
}

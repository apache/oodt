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

/**
 * An enum to represent available OODT components and their corresponding names to be used when setting up configuration
 * management.
 *
 * @author Imesha Sudasingha
 */
public enum Component {
    FILE_MANAGER("filemgr", "FILEMGR_HOME"),
    RESOURCE_MANAGER("resmgr", "RESMGR_HOME"),
    WORKFLOW_MANAGER("wmgr", "WORKFLOW_HOME");

    /** Shorthand name of the component. Will be used when creating ZNodes in zookeeper */
    String name;
    /**
     * Environment variable of ${COMPONENT_HOME} which will be set by the executing script when running that component.
     * For example, ${FILEMGR_HOME} will be set by the {@link #FILE_MANAGER} bash script.
     */
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

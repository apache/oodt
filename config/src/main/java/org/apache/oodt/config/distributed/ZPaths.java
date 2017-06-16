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

import java.util.List;

import static org.apache.oodt.config.Constants.ZPaths.*;

/**
 * Class responsible for handling all the Zookeeper ZNode paths related to configuration
 *
 * @author Imesha Sudasingha
 */
public class ZPaths {

    /** ZNode for distinct components. /components/${component} */
    public String componentZNodePath;
    public String componentZNodeRoot;

    /** ZNode path for properties files. /components/${component}/properties */
    public String propertiesZNodePath;
    public String propertiesZNodeRoot;

    /**
     * Creates the ZNode path structure accordingly to the <pre>componentName</pre> and <pre>propertiesFileNames</pre> given.
     *
     * @param componentName Name of the OODT component
     */
    public ZPaths(String componentName) {
        if (componentName == null) {
            throw new IllegalArgumentException("Component name cannot be null");
        }

        componentZNodePath = SEPARATOR + COMPONENTS_PATH_NAME + SEPARATOR + componentName;
        componentZNodeRoot = componentZNodePath + SEPARATOR;

        propertiesZNodePath = componentZNodeRoot + PROPERTIES_PATH_NAME;
        propertiesZNodeRoot = propertiesZNodePath + SEPARATOR;
    }

    public String getComponentZNodePath() {
        return componentZNodePath;
    }

    public String getPropertiesZNodePath() {
        return propertiesZNodePath;
    }

    public String getPropertiesZNodePath(String subPath) {
        return propertiesZNodeRoot + (subPath.startsWith(SEPARATOR) ? subPath.substring(1) : subPath);
    }
}

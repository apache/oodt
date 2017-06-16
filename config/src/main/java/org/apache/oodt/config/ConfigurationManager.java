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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The abstract class to define functionalities of the configuration managers.
 *
 * @author Imesha Sudasingha
 */
public abstract class ConfigurationManager {

    protected String component;
    protected List<String> propertiesFiles;

    public ConfigurationManager(String component, List<String> propertiesFiles) {
        this.component = component;
        this.propertiesFiles = propertiesFiles != null ? propertiesFiles : new ArrayList<String>();
    }

    /**
     * Retrieves a given property from the underlying configuration storage. For example, If we want to get the
     * value of the property org.foo.bar, we have to call this method with <pre>org.foo.bar</pre> as the parameter.
     *
     * @param key Name of the property to be retrieved.
     * @return Value of the requested property | null
     */
    public abstract String getProperty(String key);

    public abstract void loadProperties() throws IOException;

    public abstract File getPropertiesFile(String filePath) throws FileNotFoundException;
}

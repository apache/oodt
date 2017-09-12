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

package org.apache.oodt.config.standalone;

import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConfigurationManager} implementation to be used with standalone configuration management.
 *
 * @author Imesha Sudasingha
 */
public class StandaloneConfigurationManager extends ConfigurationManager {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneConfigurationManager.class);

    private List<String> propertiesFiles;

    public StandaloneConfigurationManager(Component component, List<String> propertiesFiles) {
        super(component);
        this.propertiesFiles = propertiesFiles != null ? propertiesFiles : new ArrayList<String>();
    }

    /** {@inheritDoc} */
    @Override
    public void loadConfiguration() throws Exception {
        for (String file : propertiesFiles) {
            logger.debug("Loading properties from file : {}", file);
            System.getProperties().load(new FileInputStream(new File(file)));
            logger.debug("Properties loaded from file : {}", file);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearConfiguration() {

    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSavedFiles() {
        return new ArrayList<>();
    }
}

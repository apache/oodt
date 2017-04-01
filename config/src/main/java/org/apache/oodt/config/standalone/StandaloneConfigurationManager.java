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

import org.apache.oodt.config.ConfigurationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link ConfigurationManager} implementation to be used with standalone configuration management.
 *
 * @author Imesha Sudasingha
 */
public class StandaloneConfigurationManager extends ConfigurationManager {

  /** Logger instance for logging */
  private static final Logger logger = Logger.getLogger(StandaloneConfigurationManager.class.getName());

  public StandaloneConfigurationManager(String component, List<String> propertiesFiles, List<String> otherFiles) {
    super(component, propertiesFiles, otherFiles);
  }

  /** {@inheritDoc} */
  @Override
  public String getProperty(String key) {
    return System.getProperty(key);
  }

  /** {@inheritDoc} */
  @Override
  public void loadProperties() throws IOException {
    for (String file : propertiesFiles) {
      System.getProperties().load(new FileInputStream(new File(file)));
    }
  }

  /** {@inheritDoc} */
  @Override
  public File getPropertiesFile(String filePath) throws FileNotFoundException {
    File file = new File(filePath);
    if (!propertiesFiles.contains(filePath) || !file.exists()) {
      throw new FileNotFoundException("Couldn't find properties file located at: " + filePath);
    }

    return file;
  }

  /** {@inheritDoc} */
  @Override
  public File getConfigurationFile(String filePath) throws FileNotFoundException {
    File file = new File(filePath);
    if (!otherFiles.contains(filePath) || !file.exists()) {
      throw new FileNotFoundException("Couldn't find properties file located at: " + filePath);
    }

    return file;
  }
}

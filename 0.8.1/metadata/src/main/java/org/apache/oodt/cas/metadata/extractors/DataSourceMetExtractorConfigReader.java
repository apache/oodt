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
package org.apache.oodt.cas.metadata.extractors;

// JDK imports
import java.io.File;

// OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;

/**
 * {@link MetExtractorConfigReader} which reads configuration from a Java Properties file.
 *
 * @author bfoster@apache.org (Brian Foster)
 */
public class DataSourceMetExtractorConfigReader implements MetExtractorConfigReader {

  @Override
  public DataSourceMetExtractorConfig parseConfigFile(File configFile)
      throws MetExtractorConfigReaderException {
    try {
      DataSourceMetExtractorConfig config = new DataSourceMetExtractorConfig();
      config.load(configFile.toURI().toURL().openStream());
      return config;
    } catch (Exception e) {
      throw new MetExtractorConfigReaderException(
          String.format("Failed to parse '%s'", configFile), e);
    }
  }
}

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

//JDK imports
import java.io.File;
import java.io.FileInputStream;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;
import org.apache.oodt.pcs.input.PGEConfigFileReader;
import org.apache.oodt.pcs.input.PGEConfigurationFile;

/**
 * 
 * Reads in a {@link FilenameTokenConfig} using the {@link PGEConfigFileReader}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FilenameTokenConfigReader implements MetExtractorConfigReader {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.metadata.MetExtractorConfigReader#parseConfigFile
   * (java.io.File)
   */
  public MetExtractorConfig parseConfigFile(File file)
      throws MetExtractorConfigReaderException {
    FilenameTokenConfig conf = new FilenameTokenConfig();
    PGEConfigFileReader reader = new PGEConfigFileReader();
    PGEConfigurationFile confFile;

    try {
      confFile = reader.read(new FileInputStream(file));
    } catch (Exception e) {
      throw new MetExtractorConfigReaderException(e.getMessage());
    }

    conf.setConf(confFile);
    return conf;
  }

}

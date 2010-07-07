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

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.pcs.input.PGEConfigurationFile;

/**
 * 
 * Wraps a {@link PGEConfigurationFile} as a {@link MetExtractorConfig}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FilenameTokenConfig implements MetExtractorConfig {

  private PGEConfigurationFile conf;

  public FilenameTokenConfig() {
    this.conf = null;
  }

  public FilenameTokenConfig(PGEConfigurationFile conf) {
    this.conf = conf;
  }

  /**
   * @return the conf
   */
  public PGEConfigurationFile getConf() {
    return conf;
  }

  /**
   * @param conf
   *          the conf to set
   */
  public void setConf(PGEConfigurationFile conf) {
    this.conf = conf;
  }

}

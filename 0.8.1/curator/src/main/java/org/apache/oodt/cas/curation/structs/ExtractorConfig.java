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

package org.apache.oodt.cas.curation.structs;

import java.io.File;
import java.util.List;

public class ExtractorConfig {
  
  public final static String PROP_CLASS_NAME = "extractor.classname";

  public final static String PROP_CONFIG_FILES = "extractor.config.files";
  
  private final List<File> configFiles;

  private final String className;

  private final String identifier;
  
  public ExtractorConfig(String identifier, String className,
      List<File> configFiles) {
    this.configFiles = configFiles;
    this.className = className;
    this.identifier = identifier;
  }
  
  public List<File> getConfigFiles() {
    return this.configFiles;
  }
  
  public String getClassName() {
    return this.className;
  }
  
  public String getIdentifier() {
    return this.identifier;
  }
  
}

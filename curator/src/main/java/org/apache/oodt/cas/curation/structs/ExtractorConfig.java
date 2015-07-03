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
/**
 * A class holding the configuration for metadata extractors
 * 
 * @author starchmd - cleanup only, original author unspecified
 */
public class ExtractorConfig {

    public final static String PROP_CLASS_NAME = "extractor.classname";
    public final static String PROP_CONFIG_FILES = "extractor.config.files";
    public final static String PROP_FILLER = "extractor.filler";

    private final List<File> configFiles;
    private final String className;
    private final String identifier;
    private final String filler;
    /**
     * Creates a new extractor configuration object
     * @param identifier - name of this extractor
     * @param className - class name of extractor
     * @param configFiles - list of config file for this extractor (Note: only the first is used)
     * @param filler - fill string for unextracted fields
     */
    public ExtractorConfig(String identifier, String className, List<File> configFiles, String filler) {
      this.configFiles = configFiles;
      this.className = className;
      this.identifier = identifier;
      this.filler = filler;
    }
    /**
     * Gets the list of configuration files (Note: only the first, index 0, is used)
     * @return config files
     */
    public List<File> getConfigFiles() {
      return this.configFiles;
    }
    /**
     * Accessor - get class name of this extractor
     * @return class name
     */
    public String getClassName() {
      return this.className;
    }
    /**
     * Accessor - get identifier (i.e. name) of this extractor
     * @return identifier
     */
    public String getIdentifier() {
      return this.identifier;
    }
    /**
     * Accessor - get the filler string
     * @return filler
     */
    public String getFiller() {
        return this.filler;
    }
}

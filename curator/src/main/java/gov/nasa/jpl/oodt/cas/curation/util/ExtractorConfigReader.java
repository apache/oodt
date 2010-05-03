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

package gov.nasa.jpl.oodt.cas.curation.util;


import gov.nasa.jpl.oodt.cas.curation.structs.ExtractorConfig;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ExtractorConfigReader {
  public static ExtractorConfig readFromDirectory(File directory,
      String configId) throws FileNotFoundException, IOException {
    File propsFileDir = new File(directory, configId);
    Properties props = new Properties();
    props
        .load(new FileInputStream(new File(propsFileDir,
        "config.properties")));
    
    String identifier = configId;
    String className = props.getProperty(ExtractorConfig.PROP_CLASS_NAME);
    List<File> files = new ArrayList<File>();
    String[] fileList = props.getProperty(ExtractorConfig.PROP_CONFIG_FILES)
        .split(",");
    for (int i = 0; i < fileList.length; i++) {
      files.add(new File(PathUtils.replaceEnvVariables(fileList[0])));
    }
    
    return new ExtractorConfig(identifier, className, files);
  }
}

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

package org.apache.oodt.cas.curation.util;


import org.apache.oodt.cas.curation.structs.ExtractorConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.Iterator;

public class ExtractorConfigWriter {
  
  public static void saveToDirectory(ExtractorConfig config, File dir)
      throws IOException {
    Properties props = new Properties();
    props.setProperty(ExtractorConfig.PROP_CLASS_NAME, config.getClassName());
    File configDir = new File(dir, config.getIdentifier());
    configDir.mkdirs();
    StringBuilder files = new StringBuilder();
    for (Iterator<File> i = config.getConfigFiles().iterator(); i.hasNext();) {
      File file = i.next();
      files.append(file.toURI());
      if (i.hasNext()) {
        files.append(",");
      }
    }
    props.setProperty(ExtractorConfig.PROP_CONFIG_FILES, files.toString());
    OutputStream os = new FileOutputStream(new File(configDir, "config.properties"));
    try {
      props
          .store(os, "");
    }
    finally{
      os.close();
    }
  }

}

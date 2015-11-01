/**
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

package org.apache.oodt.cas.pge.writers;

//JDK imports
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

//APACHE imports

/**
 * 
 * Simple template based replacement writer, replaces $variables inside of a
 * text file template with information computed by Apache OODT (e.g., from
 * environment variables, from input workflow or file metadata, derived
 * metadata, etc etc.)
 * 
 * Multi-valued metadata keys are expanded to joined strings delimited by the
 * 2nd varargs argument to
 * {@link #createConfigFile(String, Metadata, Object...)}. The 1st varargs
 * argument to {@link #createConfigFile(String, Metadata, Object...)} is the
 * template file to use as a basis.
 * 
 */
public class MetadataKeyReplacerTemplateWriter extends
    DynamicConfigFileWriter {

  private static final String DEFAULT_SEPARATOR = ",";

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.pge.writers.DynamicConfigFileWriter#generateFile(java
   * .lang.String, org.apache.oodt.cas.metadata.Metadata,
   * java.util.logging.Logger, java.lang.Object[])
   */
  @Override
  public File generateFile(String filePath, Metadata metadata, Logger logger,
      Object... args) throws IOException {
    String templateFile = (String) args[0];
    String processedTemplate = FileUtils
        .readFileToString(new File(templateFile));
    String separator = args.length == 2 ? (String) args[1] : DEFAULT_SEPARATOR;

    for (String key : metadata.getAllKeys()) {
      String replaceVal;
      if (metadata.isMultiValued(key)) {
        List<String> values = metadata.getAllMetadata(key);
        replaceVal = StringUtils.join(values, separator);
      } else {
        replaceVal = metadata.getMetadata(key);
      }
      processedTemplate = processedTemplate.replaceAll("\\$" + key, replaceVal);
    }

    File configFile = new File(filePath);
    FileUtils.writeStringToFile(configFile, processedTemplate);
    return configFile;
  }

}

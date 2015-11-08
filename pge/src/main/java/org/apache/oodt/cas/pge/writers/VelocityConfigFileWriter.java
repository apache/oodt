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
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

//APACHE imports

/**
 * 
 * A PGE input file writer based on Apache Velocity and Paul Ramirez's need to
 * make my code better.
 * 
 * First var args parameter to
 * {@link #createConfigFile(String, Metadata, Object...)} is the template
 * directory full path. Second var args parameter to
 * {@link #createConfigFile(String, Metadata, Object...)} is the template file
 * name.
 * 
 */
public class VelocityConfigFileWriter extends DynamicConfigFileWriter {

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
    File configFile = new File(filePath);
    VelocityMetadata velocityMetadata = new VelocityMetadata(metadata);
    // Velocity requires you to set a path of where to look for
    // templates. This path defaults to . if not set.
    int slashIndex = ((String) args[0]).lastIndexOf('/');
    String templatePath = ((String) args[0]).substring(0, slashIndex);
    Velocity.setProperty("file.resource.loader.path", templatePath);
    // Initialize Velocity and set context up
    Velocity.init();
    VelocityContext context = new VelocityContext();
    context.put("metadata", velocityMetadata);
    context.put("env", System.getenv());
    // Load template from templatePath
    String templateName = ((String) args[0]).substring(slashIndex);
    Template template = Velocity.getTemplate(templateName);
    // Fill out template and write to file
    StringWriter sw = new StringWriter();
    template.merge(context, sw);
    FileUtils.writeStringToFile(configFile, sw.toString());
    return configFile;
  }

}

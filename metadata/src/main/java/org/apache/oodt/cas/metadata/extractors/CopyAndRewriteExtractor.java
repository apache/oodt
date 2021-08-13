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
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.CmdLineMetExtractor;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A {@link MetExtractor} that takes in a configuration file that is a java
 * properties object with the following properties defined:
 * 
 * <ul>
 * <li>numRewriteFields - The number of fields to rewrite within the original
 * metadata file.</li>
 * <li>rewriteFieldN - The name(s) of the fields to rewrite in the original
 * {@link Metadata} file.</li>
 * <li>orig.met.file.path - he original path to the {@link Metadata} XML file
 * to draw the original properties from.</li>
 * <li>fieldN.pattern - The string specification that details which fields to
 * replace and to use in building the new field value.</li>
 * </ul>
 * 
 * </p>.
 */
public class CopyAndRewriteExtractor extends CmdLineMetExtractor {
  private static Logger LOG = Logger.getLogger(CopyAndRewriteExtractor.class.getName());
  private final static String FILENAME = "Filename";

  private final static String FILE_LOCATION = "FileLocation";

  private static CopyAndRewriteConfigReader reader = new CopyAndRewriteConfigReader();

  /**
   * Default Constructor.
   * 
   */
  public CopyAndRewriteExtractor() {
      super(reader);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java.io.File)
   */
  public Metadata extrMetadata(File file) throws MetExtractionException {
      if (this.config == null) {
          throw new MetExtractionException(
                  "No config file defined: unable to copy and rewrite metadata!");
      }

      Metadata met;
      
      try {
          met = new SerializableMetadata(new File(PathUtils
                  .replaceEnvVariables(((CopyAndRewriteConfig) this.config)
                          .getProperty("orig.met.file.path"))).toURI().toURL()
                  .openStream());
      } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw new MetExtractionException(
                  "error parsing original met file: ["
                          + ((CopyAndRewriteConfig) this.config)
                                  .getProperty("orig.met.file.path")
                          + "]: Message: " + e.getMessage());
      }

      addDefaultFields(file, met);

      // now override
      int numOverrideFields = Integer
              .parseInt(((CopyAndRewriteConfig) this.config)
                      .getProperty("numRewriteFields"));

      LOG.log(Level.FINE, "Extracting metadata: num rewrite fields: ["
              + numOverrideFields + "]");

      for (int i = 0; i < numOverrideFields; i++) {
          String rewriteFieldName = ((CopyAndRewriteConfig) this.config)
                  .getProperty("rewriteField" + (i + 1));
          String rewriteFieldStr = ((CopyAndRewriteConfig) this.config)
                  .getProperty(rewriteFieldName + ".pattern");
          LOG.log(Level.FINE, "Rewrite string: [" + rewriteFieldStr + "]");
          rewriteFieldStr = PathUtils.replaceEnvVariables(rewriteFieldStr,
                  met);
          met.replaceMetadata(rewriteFieldName, rewriteFieldStr);
      }

      return met;

  }

  public static void main(String[] args) throws Exception {
      processMain(args, new CopyAndRewriteExtractor());
  }

  private void addDefaultFields(File file, Metadata met) {
      met.replaceMetadata(FILENAME, file.getName());
      met.replaceMetadata(FILE_LOCATION, file.getParentFile()
              .getAbsolutePath());
  }

}


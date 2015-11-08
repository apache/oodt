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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.metadata.AbstractMetExtractor;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.exec.EnvUtilities;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Metadata extraction interface for standalone met extractors that have the
 * following command line interface:<br>
 * 
 * <code>/path/to/met_extractor &lt;file&gt; &lt;config file&gt;</code>
 * </p>.
 */
public abstract class CmdLineMetExtractor extends AbstractMetExtractor {

  public CmdLineMetExtractor(MetExtractorConfigReader reader) {
      super(reader);
  }

  protected static void processMain(String[] args,
          CmdLineMetExtractor extractor) throws Exception {
      String usage = "Usage: " + extractor.getClass().getName()
              + " <file> <configfile>";
      String extractFilePath, configFilePath;

      if (args.length < 2) {
          System.err.println(usage);
          System.exit(1);
      }

      extractFilePath = args[0].replaceAll("\\\\", "");
      configFilePath = args[1];

      Metadata met = extractor.extractMetadata(new File(extractFilePath),
              configFilePath);
      XMLUtils.writeXmlToStream(new SerializableMetadata(met).toXML(),
              getMetFileOutputStream(extractFilePath));
  }

  protected static void processMain(String[] args,
          CmdLineMetExtractor extractor, OutputStream os) throws Exception {
      String usage = "Usage: " + extractor.getClass().getName()
              + " <file> <configfile>";
      String extractFilePath, configFilePath;

      if (args.length < 2) {
          System.err.println(usage);
          System.exit(1);
      }

      extractFilePath = args[0].replaceAll("\\\\", "");
      configFilePath = args[1];

      Metadata met = extractor.extractMetadata(new File(extractFilePath),
              configFilePath);
      XMLUtils.writeXmlToStream(new SerializableMetadata(met).toXML(), os);

  }

  private static FileOutputStream getMetFileOutputStream(String filePath) {
      Properties envVars = EnvUtilities.getEnv();
      String cwd = envVars.getProperty("PWD");
      if (cwd == null) {
          throw new RuntimeException(
                  "Unable to get current working directory: failing!");
      }

      if (!cwd.endsWith("/")) {
          cwd += "/";
      }

      String metFilePath = cwd
              + new File(filePath).getName().replaceAll("\\\\", "") + ".met";

      // try and remove the met file, if it already exists
      // for some reason below, the writeXmlFile method in
      // XMLUtils doesn't overwrite, and throws an Exception
      File metFile = new File(metFilePath);
      if (!metFile.delete()) {
          LOG.log(Level.WARNING, "Attempt to overwrite met file: ["
                  + metFilePath + "] unsuccessful!");
      }

      try {
          return new FileOutputStream(metFile);
      } catch (FileNotFoundException e) {
          LOG.log(Level.WARNING, "Could not create met file: [" + metFile
                  + "]: Reason " + e.getMessage(), e);
          return null;
      }
  }

}


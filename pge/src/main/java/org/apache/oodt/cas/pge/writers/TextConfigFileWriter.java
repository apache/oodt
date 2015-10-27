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
package org.apache.oodt.cas.pge.writers;

//Google static imports
import com.google.common.annotations.VisibleForTesting;

import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.oodt.cas.pge.util.XmlHelper.fillIn;

//OODT static imports
//JDK imports
//Google imports
//OODT imports

/**
 * Text {@link SciPgeConfigFileWriter} which takes a template with envReplace
 * metadata that gets dynamically filled out and written to a file.
 *
 * @author bfoster (Brian Foster)
 */
public class TextConfigFileWriter extends DynamicConfigFileWriter {

   private static final int TEMPLATE_INDEX = 0;

   @Override
   public File generateFile(String filePath, Metadata metadata, Logger logger,
         Object... customArgs) throws IOException {
      checkArgument(customArgs.length > 0,
            TextConfigFileWriter.class.getCanonicalName()
                  + " has no args specified");
      String template = checkNotNull((String) customArgs[TEMPLATE_INDEX],
            "Must specify Text file template in args at index = '"
                  + TEMPLATE_INDEX + "'");

      try {
         return writeTextFile(filePath, fillIn(template, metadata));
      } catch (Exception e) {
         throw new IOException(e);
      }
   }

   @VisibleForTesting
   protected File writeTextFile(String file, String text) throws IOException {
      File outputFile = new File(file);
      PrintStream ps = null;
      try {
         ps = new PrintStream(new FileOutputStream(outputFile));
         ps.println(text);
         return outputFile;
      } catch (Exception e) {
         throw new IOException("Failed to write text file '" + file + "' : "
               + e.getMessage(), e);
      } finally {
         try {
            ps.close();
         } catch (Exception ignore) {
         }
      }
   }
}

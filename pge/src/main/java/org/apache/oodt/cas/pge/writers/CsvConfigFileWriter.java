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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * CSV {@link SciPgeConfigFileWriter} which takes a comma separted list of
 * {@link String}s as the first index of the args and generates a CSV file from
 * the metadata fields listed. For example, if the list of {@link String}s given
 * is:
 *
 * <pre>
 *    InputFiles,IsText
 * </pre>
 *
 * And:
 *
 * <pre>
 *    InputFiles=File1.txt,File2.dat,File3.xml
 *    IsText=true,false,true
 * </pre>
 *
 * Then the CSV file create will look like:
 *
 * <pre>
 *    InputFiles,IsText
 *    File1.text,true
 *    File2.dat,false
 *    File3.xml,true
 * </pre>
 *
 * @author bfoster (Brian Foster)
 */
public class CsvConfigFileWriter extends DynamicConfigFileWriter {

   private static final int HEADER_INDEX = 0;
   private static final int DELIM_INDEX = 0;

   private static final String DEFAULT_DELIM = ",";

   @Override
   public File generateFile(String filePath, Metadata metadata, Logger logger,
         Object... customArgs) throws IOException {
      checkArgument(customArgs.length > 0,
            CsvConfigFileWriter.class.getCanonicalName()
                  + " has no args specified");
      List<String> header = checkNotNull(
            Lists.newArrayList(Splitter.on(",").split(
                  (String) customArgs[HEADER_INDEX])),
            "Must specify CSV header in args at index = '" + HEADER_INDEX + "'");
      String delim = DEFAULT_DELIM;
      if (customArgs.length > DELIM_INDEX) {
         delim = (String) customArgs[DELIM_INDEX];
      }

      return writeCsvFile(filePath, header, generateRows(header, metadata),
            delim);
   }

   @VisibleForTesting
   protected List<List<String>> generateRows(List<String> header,
         Metadata meatadata) {
      List<List<String>> rows = Lists.newArrayList();
      int index = 0;
      TOP: while (true) {
         for (String columnName : header) {
            List<String> values = meatadata.getAllMetadata(columnName);
            if (values.size() <= index) {
               break TOP;
            }
            List<String> row = rows.get(index);
            if (row == null) {
               row = Lists.newArrayList();
            }
            row.add(values.get(index));
            rows.set(index, row);
         }
      }
      return rows;
   }

   @VisibleForTesting
   protected File writeCsvFile(String file, List<String> header,
         List<List<String>> rows, String delim) throws IOException {
      File outputFile = new File(file);
      PrintStream ps = null;
      try {
         ps = new PrintStream(new FileOutputStream(outputFile));
         ps.println(Joiner.on(delim).join(header));
         for (List<String> row : rows) {
            ps.println(Joiner.on(delim).join(row));
         }
         return outputFile;
      } catch (Exception e) {
         throw new IOException("Failed to write CSV file '" + file + "' : "
               + e.getMessage(), e);
      } finally {
         try {
            ps.close();
         } catch (Exception ignore) {
         }
      }
   }
}

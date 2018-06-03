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
package org.apache.oodt.cas.filemgr.cli.action;

//OODT static imports
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.FILENAME;
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.PRODUCT_NAME;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * A {@link CmdLineAction} which dumps out metadata for a given {@link Product}.
 * 
 * @author bfoster (Brian Foster)
 */
public class DumpMetadataCliAction extends FileManagerCliAction {

   private String productId;
   private File outputDir;

   @Override
   public void execute(final ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productId, "Must specify productId");

         Product product = client.getProductById(productId);
         if (product == null) {
            throw new Exception("FileManager returned null product");
         }
         Metadata metadata = client.getMetadata(product);
         if (metadata == null) {
            throw new Exception("FileManager returned null metadata");
         }
         if (outputDir != null) {
            if (outputDir.exists()) {
               XMLUtils.writeXmlFile(
                     new SerializableMetadata(metadata).toXML(), new File(
                           outputDir.getAbsoluteFile(),
                           generateFilename(metadata)).getAbsolutePath());
            } else {
               throw new Exception("Output dir '" + outputDir
                     + "' does not exist");
            }
         } else {
            OutputStream os = new OutputStream() {
               private StringBuffer sb = new StringBuffer("");

               @Override
               public void write(int character) throws IOException {
                  sb.append((char) character);
               }

               @Override
               public void close() throws IOException {
                  super.close();
                  printer.println(sb.toString());
               }
            };
            XMLUtils.writeXmlToStream(
                  new SerializableMetadata(metadata).toXML(),
                  os); 
            os.close();
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get metadata for product '" + productId + "' : "
                     + e.getMessage(), e);
      }
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }

   public void setOutputDir(File outputDir) {
      this.outputDir = outputDir;
   }

   private String generateFilename(Metadata metadata) {
      String filename = metadata.getMetadata(FILENAME) != null ? metadata
            .getMetadata(FILENAME) : metadata.getMetadata(PRODUCT_NAME);
      return filename + ".met";
   }
}

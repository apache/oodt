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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;

//OODT imports
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * This class provides some basic support for actions revolving around files. It
 * allows action to support file selection based on configuration, metadata, or
 * the current file being handled by the crawler. In addition, it supports the
 * usage of FileSettings to provide conformance amongst extending actions.
 * Finally, it captures a flag for indicating whether the action should fail
 * should the file not exist.
 * 
 * @author pramirez (Paul Ramirez)
 * @author mattmann (Chris Mattmann)
 * 
 */
public abstract class FileBasedAction extends CrawlerAction {
   protected String file;
   protected String fileKey;
   protected FileSettings fileSettings;
   protected boolean failMissingFile;

   public FileBasedAction() {
      fileSettings = new FileSettings();
      this.failMissingFile = false;
   }

   public void setFilePrefix(String filePrefix) {
      fileSettings.setFilePrefix(filePrefix);
   }

   public void setFileSuffix(String fileSuffix) {
      fileSettings.setFileSuffix(fileSuffix);
   }

   public void setFileExtension(String fileExtension) {
      fileSettings.setFileExtension(fileExtension);
   }

   public void setKeepExistingExtension(boolean keepExistingExtension) {
      fileSettings.setKeepExistingExtension(keepExistingExtension);
   }

   public void setFile(String file) {
      this.file = file;
   }

   public void setFileKey(String fileKey) {
      this.fileKey = fileKey;
   }

   public void setFailMissingFile(boolean failMissingFile) {
      this.failMissingFile = failMissingFile;
   }

   public File getSelectedFile(File product, Metadata metadata) {
      File selectedFile;
      if (file == null && fileKey == null) {
         selectedFile = product;
      } else if (file != null) {
         selectedFile = new File(file);
      } else {
         selectedFile = new File(metadata.getMetadata(fileKey));
      }
      return selectedFile;
   }

   public boolean performAction(File product, Metadata metadata)
         throws CrawlerActionException {
      File selectedFile = this.getSelectedFile(product, metadata);
      String selectedFileString = this.fileSettings
            .getPreparedFileString(selectedFile);
      File actionFile = new File(selectedFileString);

      if (failMissingFile && !actionFile.exists()) {
         LOG.severe("File does not exist: " + actionFile.getAbsolutePath());
         return false;
      }

      if (actionFile.exists()) {
         return this.performFileAction(actionFile, metadata);
      } else {
         LOG.fine("File does not exist: " + actionFile.getAbsolutePath());
      }

      return true;
   }

   public abstract boolean performFileAction(File actionFile, Metadata metadata)
       ;
}

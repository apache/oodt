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

/**
 * This class captures settings used in FileBased actions and the FileExists
 * precondition. It specifies how to prepare a file string and provides
 * consistent features across the aforementioned. This preparation supports such
 * converting from something such as image.jpg to cool_image_ancillary.png. In
 * this scenario the filePrefix="cool_", fileSuffix="_ancillary",
 * fileExtension="png", and keepExistingExtension=true. This conversion could
 * take place by passing calling the getPreparedFileString method.
 * 
 * @author pramirez (Paul Ramirez)
 * @author mattmann (Chris Mattmann)
 * 
 */
public class FileSettings {
   private String filePrefix;
   private String fileSuffix;
   private String fileExtension;
   private boolean keepExistingExtension;

   public FileSettings() {
      this.keepExistingExtension = true;
   }

   public void setFilePrefix(String filePrefix) {
      this.filePrefix = filePrefix;
   }

   public void setFileSuffix(String fileSuffix) {
      this.fileSuffix = fileSuffix;
   }

   public void setFileExtension(String fileExtension) {
      this.fileExtension = fileExtension;
   }

   public void setKeepExistingExtension(boolean keepExistingExtension) {
      this.keepExistingExtension = keepExistingExtension;
   }

   public String getPreparedFileString(File file) {
      StringBuilder fileString = new StringBuilder();

      if (file.getParent() != null) {
         fileString.append(file.getParent());
         fileString.append(System.getProperties().getProperty("file.separator",
               "/"));
      }

      if (filePrefix != null) {
         fileString.append(filePrefix);
      }

      String existingExtension = "";
      String filename = file.getName();
      int existingIndex = filename.lastIndexOf(".");
      if (existingIndex != -1) {
         existingExtension = filename.substring(existingIndex);
         filename = filename.substring(0, existingIndex);
      }

      fileString.append(filename);

      if (fileSuffix != null) {
         fileString.append(fileSuffix);
      }

      if (keepExistingExtension) {
         fileString.append(existingExtension);
      }

      if (fileExtension != null) {
         fileString.append(".");
         fileString.append(fileExtension);
      }

      return fileString.toString();
   }
}

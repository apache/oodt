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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.util.logging.Level;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//File.Utils import to handle NFS mounted directories
import org.apache.commons.io.FileUtils;


/**
 * Moves a {@link Product} file as a reponse to a Crawler lifecycle phase
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class MoveFile extends CrawlerAction {

   private String file;
   private String toDir;
   private String fileExtension;
   private boolean createToDir;

   public MoveFile() {
      super();
      this.createToDir = false;
   }

   public boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException {
      String mvFile = file;
      try {
         if (mvFile == null) {
            mvFile = product.getAbsolutePath();
            if (this.fileExtension != null) {
               mvFile += "." + this.fileExtension;
            }
         }
         File srcFile = new File(mvFile);
         File toFile = new File(toDir + "/" + srcFile.getName());
         if (createToDir) {
            toFile.getParentFile().mkdirs();
         }
         LOG.log(Level.INFO, "Moving file " + srcFile.getAbsolutePath()
               + " to " + toFile.getAbsolutePath());
         if(!srcFile.renameTo(toFile)) {//If the file failed to copy
        	 LOG.log(Level.INFO, "Moving failed, possibly because ingest dir is nfs mounted. Retrying to move " + srcFile.getAbsolutePath()
                     + " to " + toFile.getAbsolutePath());
        	 FileUtils.copyFileToDirectory(srcFile, toFile.getParentFile());
        	 FileUtils.forceDelete(srcFile); //Need to delete the old file
        	 return true; //File copied on second attempt
         }
         else {
            return true; //File copied
         }
      } catch (Exception e) {
         throw new CrawlerActionException("Failed to move file from " + mvFile
               + " to " + this.toDir + " : " + e.getMessage(), e);
      }
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.isTrue(file == null || fileExtension == null,
               "Must specify either file or fileExtension");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public void setCreateToDir(boolean createToDir) {
      this.createToDir = createToDir;
   }

   public void setFile(String file) {
      this.file = file;
   }

   @Required
   public void setToDir(String toDir) {
      this.toDir = toDir;
   }

   public String getToDir() {
      return toDir;
   }

   public void setFileExtension(String fileExtension) {
      this.fileExtension = fileExtension;
   }
}

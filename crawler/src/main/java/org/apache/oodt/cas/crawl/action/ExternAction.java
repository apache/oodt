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

//OODT static imports
import static org.apache.oodt.cas.metadata.util.PathUtils.doDynamicReplacement;

//JDK imports
import java.io.File;
import java.util.logging.Level;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.exec.ExecUtils;

/**
 * Execute some external command as a {@link CrawlerAction} response.
 * 
 * @author bfoster (Brian Foster)
 */
public class ExternAction extends CrawlerAction {

   private String executeCommand;
   private String workingDir;

   @Override
   public boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException {
      try {
         String envReplacedExecuteCommand = doDynamicReplacement(
               executeCommand, productMetadata);
         return ExecUtils.callProgram(
               envReplacedExecuteCommand,
               LOG,
               new File(workingDir != null ? doDynamicReplacement(workingDir,
                     productMetadata) : product.getParent())) == 0;
      } catch (Exception e) {
         LOG.log(Level.SEVERE, "Failed to execute extern command '"
               + executeCommand + "' : " + e.getMessage(), e);
         return false;
      }
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.notNull(executeCommand, "Must specify executeCommand");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public void setExecuteCommand(String executeCommand) {
      this.executeCommand = executeCommand;
   }

   public void setWorkingDir(String workingDir) {
      this.workingDir = workingDir;
   }
}

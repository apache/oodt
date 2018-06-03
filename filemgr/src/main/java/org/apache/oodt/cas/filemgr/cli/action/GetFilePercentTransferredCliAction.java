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

//JDK imports
import java.io.File;
import java.net.URI;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which get percent transferred for a given data file.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetFilePercentTransferredCliAction extends FileManagerCliAction {

   private String origRef;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(origRef, "Must specify origRef");

         Reference ref = new Reference();
         ref.setOrigReference(getUri(origRef).toString());

         printer.println("Reference: [origRef=" + origRef + ",transferPct=" + client.getRefPctTransferred(ref) + "]");
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get percent transfered for" + " file '" + origRef
                     + "' : " + e.getMessage(), e);
      }
   }

   public void setOrigRef(String origRef) {
      this.origRef = origRef;
   }

   private URI getUri(String filePath) {
      if (new File(filePath).exists()) {
         return new File(filePath).toURI();
      } else {
         return URI.create(filePath);
      }
   }
}

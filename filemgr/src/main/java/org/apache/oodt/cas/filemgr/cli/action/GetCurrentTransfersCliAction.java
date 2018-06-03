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
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which get list of current {@link Product} file
 * transfers.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetCurrentTransfersCliAction extends FileManagerCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         List<FileTransferStatus> statuses = client.getCurrentFileTransfers();
         if (statuses == null) {
            throw new NullPointerException(
                  "FileManager returned null file transfers");
         }
         for (FileTransferStatus status : statuses) {
            printer.println("File Transfer: [ref={orig="
                  + status.getFileRef().getOrigReference() + ",ds="
                  + status.getFileRef().getDataStoreReference() + "},product="
                  + status.getParentProduct().getProductName() + ",fileSize="
                  + status.getFileRef().getFileSize() + ",amtTransferred="
                  + status.getBytesTransferred() + ",pct="
                  + status.computePctTransferred() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get list of current file transfers : "
                     + e.getMessage(), e);
      }
   }
}

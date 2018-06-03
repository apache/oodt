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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which checks if the File Manager has a given
 * {@link org.apache.oodt.cas.filemgr.structs.Product} ingested.
 * 
 * @author bfoster (Brian Foster)
 */
public class HasProductCliAction extends FileManagerCliAction {

   private String productName;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productName, "Must specify productName");

         printer.println("hasProduct: Result: " + client.hasProduct(productName));
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to check for product '"
               + productName + "' : " + e.getMessage(), e);
      }
   }

   public void setProductName(String productName) {
      this.productName = productName;
   }
}

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

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;

/**
 * A {@link CmdLineAction} which gets information about a {@link Product}
 * by ID.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class AbstractGetProductCliAction extends FileManagerCliAction {

   @Override
   public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
      try {
         Product p = getProduct();
         printer.println("Product:");
         printer.println(" - ID: " + p.getProductId());
         printer.println(" - Name: " + p.getProductName());
         printer.println(" - ProductType: " + p.getProductType().getName());
         printer.println(" - Structure: " + p.getProductStructure());
         printer.println(" - Status: " + p.getTransferStatus());
         if (p.getRootRef() != null) {
            printer.println(" - RootRef: " + p.getRootRef().getDataStoreReference());
         }
         if (!p.getProductReferences().isEmpty()) {
            printer.println(" - References: ");
            for (Reference ref : p.getProductReferences()) {
               printer.println("    - " + ref.getDataStoreReference()
                     + " (" + ref.getFileSize() + ")");
            }
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get product info : "
               + e.getMessage(), e);
      }
   }

   protected abstract Product getProduct() throws Exception;
}

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

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * Retrieves files for a given {@link Product}.
 *
 * @author bfoster (Brian Foster)
 */
public class RetrieveFilesCliAction extends FileManagerCliAction {

   private String productId;
   private String productName;
   private DataTransfer dt;
   private File destination;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         dt.setFileManagerUrl(client.getFileManagerUrl());
         Product product;
         if (productId != null) {
            product = client.getProductById(productId);
         } else if (productName != null) {
            product = client.getProductByName(productName);
         } else {
              throw new Exception("Must specify either productId or productName");
         }
         if (product != null) {
            product.setProductReferences(client.getProductReferences(product));
            dt.retrieveProduct(product, destination);
         } else {
            throw new Exception("Product was not found");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to retrieve files for product : " + e.getMessage(), e);
      }
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }

   public void setProductName(String productName) {
      this.productName = productName;
   }

   public void setDataTransferFactory(DataTransferFactory dtFactory) {
      dt = dtFactory.createDataTransfer();
   }

   public void setDestination(File destination) {
      this.destination = destination;
   }
}

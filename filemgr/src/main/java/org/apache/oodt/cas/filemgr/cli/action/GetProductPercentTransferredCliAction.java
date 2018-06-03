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
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which get a {@link Product}'s data file transfers
 * percent complete.
 *
 * @author bfoster (Brian Foster)
 */
public class GetProductPercentTransferredCliAction extends FileManagerCliAction {

   private String productId;
   private String productTypeName;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productId, "Must specify productid");
         Validate.notNull(productTypeName, "Must specify productTypeName");

         Product product = new Product();
         // TODO(bfoster): Not sure why ProductType is needed here.
         ProductType pt = client.getProductTypeByName(productTypeName);
         if (pt == null) {
            throw new Exception("FileManager returned null ProductType");
         }
         product.setProductType(pt);
         product.setProductId(productId);

         printer.println("Product: [id=" + productId + ", transferPct="
               + client.getProductPctTransferred(product) + "]");
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get percent transferred"
               + " for product id '" + productId + "' and ProductType name '"
               + productTypeName + "' : " + e.getMessage(), e);
      }
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }

   public void setProductTypeName(String productTypeName) {
      this.productTypeName = productTypeName;
   }
}

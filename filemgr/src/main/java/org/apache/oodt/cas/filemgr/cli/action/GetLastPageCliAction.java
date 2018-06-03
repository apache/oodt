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
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which gets the last page of {@link Product}s of a
 * given {@link ProductType}.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetLastPageCliAction extends FileManagerCliAction {

   private String productTypeName;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productTypeName, "Must specify productTypeName");

         ProductType type = client.getProductTypeByName(productTypeName);
         if (type == null) {
            throw new Exception("FileManager returned null ProductType");
         }
         ProductPage lastPage = client.getLastPage(type);
         if (lastPage == null) {
            throw new Exception("FileManager returned null ProductPage");
         }
         printer.println("Page: [num=" + lastPage.getPageNum()
               + ", totalPages=" + lastPage.getTotalPages() + ", pageSize="
               + lastPage.getPageSize() + "]");
         printer.println("Products:");

         if (lastPage.getPageProducts() == null) {
            throw new NullPointerException(
                  "FileManager returned null page Products");
         }
         for (Product p : lastPage.getPageProducts()) {
            printer.println("Product: [id=" + p.getProductId() + ",name="
                  + p.getProductName() + ",type="
                  + p.getProductType().getName() + ",structure="
                  + p.getProductStructure() + ", transferStatus="
                  + p.getTransferStatus() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get last page of "
               + "product for ProductType name '" + productTypeName + "' : "
               + e.getMessage(), e);
      }
   }

   public void setProductTypeName(String productTypeName) {
      this.productTypeName = productTypeName;
   }
}

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
 * A {@link CmdLineAction} which gets the previous page of {@link Product}s for
 * a given {@link ProductType}.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetPrevPageCliAction extends FileManagerCliAction {

   private String productTypeName;
   private int currentPageNum;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productTypeName, "Must specify productTypeName");
         Validate.notNull(currentPageNum, "Must specify currentPageNum");

         ProductType type = client.getProductTypeByName(productTypeName);
         if (type == null) {
            throw new Exception("FileManager returned null ProductType");
         }
         ProductPage firstPage = client.getFirstPage(type);
         if (firstPage == null) {
            throw new Exception("FileManager returned null first ProductPage");
         }
         ProductPage currentPage = new ProductPage();
         currentPage.setPageNum(currentPageNum);
         currentPage.setPageSize(firstPage.getPageSize());
         currentPage.setTotalPages(firstPage.getTotalPages());
         ProductPage prevPage = client.getPrevPage(type, currentPage);
         if (prevPage == null) {
            throw new Exception("FileManager returned null previous ProductPage");
         }
         printer.println("Page: [num=" + prevPage.getPageNum()
               + ", totalPages=" + prevPage.getTotalPages() + ", pageSize="
               + prevPage.getPageSize() + "]");
         printer.println("Products:");

         if (prevPage.getPageProducts() == null) {
            throw new NullPointerException(
                  "FileManager returned null page Products");
         }
         for (Product p : prevPage.getPageProducts()) {
            printer.println("Product: [id=" + p.getProductId() + ",name="
                  + p.getProductName() + ",type="
                  + p.getProductType().getName() + ",structure="
                  + p.getProductStructure() + ", transferStatus="
                  + p.getTransferStatus() + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get previous page of products"
                     + " for ProductType name '" + productTypeName
                     + "' and current " + " page '" + currentPageNum + "' : "
                     + e.getMessage(), e);
      }
   }

   public void setProductTypeName(String productTypeName) {
      this.productTypeName = productTypeName;
   }

   public void setCurrentPageNum(int currentPageNum) {
      this.currentPageNum = currentPageNum;
   }
}

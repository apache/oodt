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
package org.apache.oodt.cas.filemgr.system;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

/**
 * A Mock {@link XmlRpcFileManagerClient}.
 *
 * @author bfoster (Brian Foster)
 */
public class MockXmlRpcFileManagerClient extends XmlRpcFileManagerClient {

   private MethodCallDetails lastMethodCallDetails;

   public MockXmlRpcFileManagerClient() throws ConnectionException,
         MalformedURLException {
      super(new URL("http://localhost:9000"), false);
   }

   public MethodCallDetails getLastMethodCallDetails() {
      return lastMethodCallDetails;
   }

   @Override
   public String addProductType(ProductType productType) {
      lastMethodCallDetails = new MethodCallDetails("addProductType",
            Lists.newArrayList((Object) productType));
      return "ProductTypeId";
   }

   @Override
   public Product getProductById(String productId) {
      lastMethodCallDetails = new MethodCallDetails("getProductById",
            Lists.newArrayList((Object) productId));
      Product p = new Product();
      p.setProductId(productId);
      p.setProductName("TestProductName");
      p.setProductReferences(Lists.newArrayList(
            new Reference("file:/orig/file", "file:/ds/file", 3)));
      p.setProductStructure(Product.STRUCTURE_FLAT);
      p.setTransferStatus(Product.STATUS_RECEIVED);
      ProductType pt = new ProductType();
      pt.setName("TestProductType");
      p.setProductType(pt);
      return p;
   }

   @Override
   public Product getProductByName(String productName) {
      lastMethodCallDetails = new MethodCallDetails("getProductByName",
            Lists.newArrayList((Object) productName));
      Product p = new Product();
      p.setProductId("TestProductId");
      p.setProductName(productName);
      p.setProductReferences(Lists.newArrayList(
            new Reference("file:/orig/file", "file:/ds/file", 3)));
      p.setProductStructure(Product.STRUCTURE_FLAT);
      p.setTransferStatus(Product.STATUS_RECEIVED);
      ProductType pt = new ProductType();
      pt.setName("TestProductType");
      p.setProductType(pt);
      return p;
   }

   @Override
   public List<Reference> getProductReferences(Product product) {
      lastMethodCallDetails = new MethodCallDetails("getProductReferences",
            Lists.newArrayList((Object) product));
      return Lists.newArrayList();
   }

   @Override
   public boolean removeProduct(Product product) {
      lastMethodCallDetails = new MethodCallDetails("removeProduct",
            Lists.newArrayList((Object) product));
      return true;
   }

   @Override
   public boolean removeFile(String file) {
      lastMethodCallDetails = new MethodCallDetails("removeFile",
            Lists.newArrayList((Object) file));
      return true;
   }

   @Override
   public byte[] retrieveFile(String filePath, int offset, int numBytes)
      throws DataTransferException {
      lastMethodCallDetails = new MethodCallDetails("removeFile",
            Lists.newArrayList((Object) filePath, offset, numBytes));
      return new byte[0];
   }

   @Override
   public FileTransferStatus getCurrentFileTransfer() {
      lastMethodCallDetails = new MethodCallDetails("getCurrentFileTransfer",
            Lists.newArrayList());
      FileTransferStatus status = new FileTransferStatus();
      status.setBytesTransferred(10);
      status.setFileRef(new Reference("file:/orig/file", "file:/ds/file", 4));
      Product p = new Product();
      p.setProductId("TestProductId");
      status.setParentProduct(p);
      return status;
   }

   @Override
   public List<FileTransferStatus> getCurrentFileTransfers() {
      lastMethodCallDetails = new MethodCallDetails("getCurrentFileTransfers",
            Lists.newArrayList());
      FileTransferStatus status = new FileTransferStatus();
      status.setBytesTransferred(10);
      status.setFileRef(new Reference("file:/orig/file", "file:/ds/file", 4));
      Product p = new Product();
      p.setProductId("TestProductId");
      status.setParentProduct(p);
      return Lists.newArrayList(status);
   }

   @Override
   public double getRefPctTransferred(Reference ref) {
      lastMethodCallDetails = new MethodCallDetails("getRefPctTransferred",
            Lists.newArrayList((Object) ref));
      return 0.6;
   }

   @Override
   public ProductType getProductTypeByName(String productTypeName) {
      lastMethodCallDetails = new MethodCallDetails("getProductTypeByName",
            Lists.newArrayList((Object) productTypeName));
      ProductType pt = new ProductType();
      pt.setName(productTypeName);
      return pt;
   }

   @Override
   public ProductPage getFirstPage(ProductType type) {
      lastMethodCallDetails = new MethodCallDetails("getFirstPage",
            Lists.newArrayList((Object) type));
      ProductPage pp = new ProductPage();
      pp.setNumOfHits(0);
      pp.setPageProducts(new ArrayList<Product>());
      pp.setPageSize(10);
      pp.setTotalPages(0);
      pp.setPageNum(1);
      return pp;
   }

   @Override
   public ProductPage getLastPage(ProductType type) {
      lastMethodCallDetails = new MethodCallDetails("getLastPage",
            Lists.newArrayList((Object) type));
      ProductPage pp = new ProductPage();
      pp.setNumOfHits(0);
      pp.setPageProducts(new ArrayList<Product>());
      pp.setPageSize(10);
      pp.setTotalPages(0);
      pp.setPageNum(1);
      return pp;
   }

   @Override
   public ProductPage getNextPage(ProductType type, ProductPage curPage) {
      lastMethodCallDetails = new MethodCallDetails("getNextPage",
            Lists.newArrayList((Object) type, curPage));
      ProductPage pp = new ProductPage();
      pp.setNumOfHits(0);
      pp.setPageProducts(new ArrayList<Product>());
      pp.setPageSize(10);
      pp.setTotalPages(0);
      pp.setPageNum(curPage.getPageNum() + 1);
      return pp;
   }

   @Override
   public ProductPage getPrevPage(ProductType type, ProductPage curPage) {
      lastMethodCallDetails = new MethodCallDetails("getPrevPage",
            Lists.newArrayList((Object) type, curPage));
      ProductPage pp = new ProductPage();
      pp.setNumOfHits(0);
      pp.setPageProducts(new ArrayList<Product>());
      pp.setPageSize(10);
      pp.setTotalPages(0);
      pp.setPageNum(curPage.getPageNum() - 1);
      return pp;
   }

   @Override
   public int getNumProducts(ProductType type) {
      lastMethodCallDetails = new MethodCallDetails("getNumProducts",
            Lists.newArrayList((Object) type));
      return 0;
   }

   @Override
   public double getProductPctTransferred(Product product) {
      lastMethodCallDetails = new MethodCallDetails("getProductPctTransferred",
            Lists.newArrayList((Object) product));
      return 0.6;
   }

   @Override
   public boolean hasProduct(String productName) {
      lastMethodCallDetails = new MethodCallDetails("hasProduct",
            Lists.newArrayList((Object) productName));
      return true;
   }

   @Override
   public String ingestProduct(Product p, Metadata m, boolean clientTransfer) {
      lastMethodCallDetails = new MethodCallDetails("ingestProduct",
            Lists.newArrayList((Object) p, m, clientTransfer));
      return "TestProductId";
   }

   @Override
   public List<ProductType> getProductTypes() {
      lastMethodCallDetails = new MethodCallDetails("getProductTypes",
            Lists.newArrayList());
      ProductType pt = new ProductType();
      pt.setName("TestProductType");
      return Lists.newArrayList(pt);
   }

   @Override
   public List<QueryResult> complexQuery(ComplexQuery query) {
      lastMethodCallDetails = new MethodCallDetails("complexQuery",
            Lists.newArrayList((Object) query));
      return Lists.newArrayList();
   }

   @Override
   public Metadata getMetadata(Product p) {
      lastMethodCallDetails = new MethodCallDetails("getMetadata",
            Lists.newArrayList((Object) p));
      return new Metadata();
   }

   public class MethodCallDetails {
      private String methodName;
      private List<Object> args;

      public MethodCallDetails(String methodName, List<Object> args) {
         this.methodName = methodName;
         this.args = args;
      }

      public String getMethodName() {
         return methodName;
      }

      public List<Object> getArgs() {
         return args;
      }
   }
}

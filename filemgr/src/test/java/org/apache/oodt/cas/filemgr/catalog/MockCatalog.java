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
package org.apache.oodt.cas.filemgr.catalog;

//JDK imports
import java.util.List;
import java.util.Map;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A Mock {@link Catalog}.
 *
 * @author bfoster (Brian Foster)
 */
public class MockCatalog implements Catalog {

   Map<String, Product> products;

   public MockCatalog() {
      products = Maps.newHashMap();
   }

   @Override
   public ProductPage getFirstPage(ProductType type) {
      return null;
   }

   @Override
   public ProductPage getLastProductPage(ProductType type) {
      return null;
   }

   @Override
   public ProductPage getNextPage(ProductType type, ProductPage currentPage) {
      return null;
   }

   @Override
   public ProductPage getPrevPage(ProductType type, ProductPage currentPage) {
      return null;
   }

   @Override
   public void addMetadata(Metadata m, Product product) throws CatalogException {      
   }

   @Override
   public void removeMetadata(Metadata m, Product product)
         throws CatalogException {      
   }

   @Override
   public void addProduct(Product product) throws CatalogException {   
      products.put(product.getProductId(), product);
   }

   @Override
   public void modifyProduct(Product product) throws CatalogException {
      products.put(product.getProductId(), product);
   }

   @Override
   public void removeProduct(Product product) throws CatalogException {
      products.remove(product.getProductId());
   }

   @Override
   public void setProductTransferStatus(Product product)
         throws CatalogException {
      Product ingestedProduct = products.get(product.getProductId());
      ingestedProduct.setTransferStatus(product.getTransferStatus());
   }

   @Override
   public void addProductReferences(Product product) throws CatalogException {
      Product ingestedProduct = products.get(product.getProductId());
      List<Reference> references = ingestedProduct.getProductReferences();
      if (references == null) {
         references = Lists.newArrayList();
      }
      references.addAll(product.getProductReferences());
      ingestedProduct.setProductReferences(references);
   }

   @Override
   public Product getProductById(String productId) throws CatalogException {
      return products.get(productId);
   }

   @Override
   public Product getProductByName(String productName) throws CatalogException {
      for (Product product : products.values()) {
         if (product.getProductName().equals(productName)) {
            return product;
         }
      }
      return null;
   }

   @Override
   public List<Reference> getProductReferences(Product product)
         throws CatalogException {
      return null;
   }

   @Override
   public List<Product> getProducts() throws CatalogException {
      return Lists.newArrayList(products.values());
   }

   @Override
   public List<Product> getProductsByProductType(ProductType type)
         throws CatalogException {
      return null;
   }

   @Override
   public Metadata getMetadata(Product product) throws CatalogException {
      return null;
   }

   @Override
   public Metadata getReducedMetadata(Product product, List<String> elements)
         throws CatalogException {
      return null;
   }

   @Override
   public List<String> query(Query query, ProductType type)
         throws CatalogException {
      return null;
   }

   @Override
   public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
         throws CatalogException {
      return null;
   }

   @Override
   public List<Product> getTopNProducts(int n) throws CatalogException {
      return null;
   }

   @Override
   public List<Product> getTopNProducts(int n, ProductType type)
         throws CatalogException {
      return null;
   }

   @Override
   public ValidationLayer getValidationLayer() {
      return null;
   }

   @Override
   public int getNumProducts(ProductType type) throws CatalogException {
      return 0;
   }
}

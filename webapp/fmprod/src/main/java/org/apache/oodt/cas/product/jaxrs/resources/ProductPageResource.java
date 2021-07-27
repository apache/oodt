/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.product.jaxrs.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A JAX-RS resource representing a {@link ProductPage}.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement(name = "productPage")
@XmlType(
    propOrder = {
      "pageSize",
      "pageNum",
      "totalPages",
      "totalProducts",
      "numOfHits",
      "metadataResource",
      "productResources"
    })
@XmlAccessorType(XmlAccessType.NONE)
public class ProductPageResource {

  private MetadataResource metadataResource;
  private List<ProductResource> productResources = new ArrayList<ProductResource>();
  private int pageSize;
  private int pageNum;
  private int totalPages;
  private int totalProducts;
  private long numOfHits;

  /* The file manager's working directory for this resource, used for example
   when creating zip archives.
  */
  private File workingDir;

  /** Default constructor required by JAXB. */
  public ProductPageResource() {}

  /**
   * Constructor that sets the ProductPage, metadata and working directory for the ProductPage
   * resource.
   *
   * @param page the productpage for the dataset
   * @param productMetaDataList the metadata for the dataset
   * @param productReferencesList the References for the dataset
   * @param workingDir the working directory for creating temporary files to attach to responses
   */
  public ProductPageResource(
      ProductPage page,
      List<Metadata> productMetaDataList,
      List<List<Reference>> productReferencesList,
      File workingDir) {

    this.pageSize = page.getPageSize();
    this.pageNum = page.getPageNum();
    this.totalPages = page.getTotalPages();
    this.numOfHits = page.getNumOfHits();

    this.workingDir = workingDir;

    List<Product> pageProducts = page.getPageProducts();
    for (int i = 0; i < pageProducts.size(); i++) {

      Product product = pageProducts.get(i);
      Metadata metadata = productMetaDataList.get(i);
      List<Reference> references = productReferencesList.get(i);

      this.productResources.add(
          new ProductResource(product, metadata, productReferencesList.get(0), workingDir));
    }
  }
    
  /**
   * Sets the total no. of products in the repository
   *
   * @param total no. of products to be set
   */
  public void setTotalProducts(int totalProducts){
    this.totalProducts = totalProducts;
  }

  /**
   * Adds a {@link ProductResource} to the list of product resources for the dataset.
   *
   * @param resource the resource to add to the dataset.
   */
  public void addProductResource(ProductResource resource) {
    productResources.add(resource);
  }

  /**
   * Gets the working directory for the dataset.
   *
   * @return the working directory
   */
  public File getWorkingDir() {
    return workingDir;
  }

  @XmlElement(name = "pageSize")
  public int getPageSize() {
    return this.pageSize;
  }

  @XmlElement(name = "pageNum")
  public int getPageNum() {
    return pageNum;
  }

  @XmlElement(name = "totalPages")
  public int getTotalPages() {
    return totalPages;
  }
    
  /**
   * Gets the total no. of products in the repository
   *
   * @return the total no. of products 
   */
  @XmlElement(name = "totalProducts")
  public int getTotalProducts() {
    return totalProducts;
  }

  @XmlElement(name = "numOfHits")
  public long getNumOfHits() {
    return numOfHits;
  }

  /**
   * Gets the product resources for the dataset.
   *
   * @return the productResources
   */
  @XmlElementWrapper(name = "products")
  @XmlElement(name = "product")
  public List<ProductResource> getProductResources() {
    return productResources;
  }

  /**
   * Gets the metadata resource for the dataset.
   *
   * @return the metadata resource
   */
  @XmlElement(name = "metadata")
  public MetadataResource getMetadataResource() {
    return metadataResource;
  }
}

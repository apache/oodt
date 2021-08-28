/**
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

package org.apache.oodt.cas.webcomponents.filemgr;

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FileManagerConn {

  private FileManagerClient fm;

  private static final Logger LOG = LoggerFactory.getLogger(FileManagerConn.class);

  public FileManagerConn(String fmUrlStr) {
    this.initFm(fmUrlStr);
  }

  public String getProdReceivedTime(Product p) {
    Metadata met = getMetadata(p);
    String prodReceivedTime = met
        .getMetadata("CAS." + CoreMetKeys.PRODUCT_RECEVIED_TIME);
    return prodReceivedTime != null && !prodReceivedTime.equals("")
        ? prodReceivedTime : "UNKNOWN";
  }

  public List<Reference> getProductReferences(Product p) {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    List<Reference> refs = new Vector<Reference>();
    try {
      refs = fm.getProductReferences(p);
    } catch (Exception e) {
      LOG.warn("Unable to get references for product: [{}]: {}", p.getProductName(), e.getMessage(), e);
    }

    return refs;
  }

  public ProductType safeGetProductTypeByName(String name) {
    if (!isConnected())
      return ProductType.blankProductType();
    try {
      return fm.getProductTypeByName(name);
    } catch (Exception e) {
      LOG.warn("Unable to get product type by name: [{}]: {}", name, e.getMessage(), e);
      return null;
    }
  }

  public Product safeGetProductById(String id) {
    if (!isConnected())
      return Product.getDefaultFlatProduct("", "");
    try {
      return fm.getProductById(id);
    } catch (Exception e) {
      LOG.warn("Unable to get product by id: [{}]: {}", id, e.getMessage(), e);
      return null;
    }
  }

  public Metadata getMetadata(Product p) {
    if (!isConnected())
      return new Metadata();
    Metadata met = null;
    try {
      met = fm.getMetadata(p);
    } catch (CatalogException e) {
      LOG.warn("Unable to get metadata and display product received time for: [{}]: {}", p.getProductName(), e.getMessage(), e);
    }

    return met;
  }

  public List<Element> safeGetElementsForProductType(ProductType type) {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    try {
      return fm.getElementsByProductType(type);
    } catch (Exception e) {
      LOG.warn("Unable to obtain elements for product type: [{}]: {}", type.getName(), e.getMessage(), e);
      return new Vector<Element>();
    }
  }

  public List<ProductType> safeGetProductTypes() {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    List<ProductType> types = new Vector<ProductType>();
    try {
      types = this.fm.getProductTypes();
    } catch (RepositoryManagerException e) {
      LOG.warn("Unable to obtain product types: {}", e.getMessage(), e);
    }
    return types;
  }

  public FileManagerClient getFm() {
    return this.fm;
  }

  public void initFm(String urlStr) {
    try {
      this.fm = RpcCommunicationFactory.createClient(new URL(urlStr));
    } catch (Exception e) {
      LOG.warn("Unable to connect to the file manager at: [{}]: {}", urlStr, e.getMessage(), e);
      this.fm = null;
    }
  }

  private boolean isConnected() {
    if (this.fm == null) {
      LOG.warn("File Manager Connection is null: Default objects for Products, Product Types, References, etc., will be returned.");
      return false;
    } else
      return true;
  }

}

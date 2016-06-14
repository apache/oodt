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

package org.apache.oodt.cas.filemgr.browser.model;

import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CasDB {

  public static final int INT = 20;
  URL filemgrUrl;
  XmlRpcFileManagerClient client;
  public Results results;
  private static Logger LOG = Logger.getLogger(CasDB.class.getName());
  private static String freeTextBlock = "__FREE__";

  public CasDB() {
    filemgrUrl = null;
  }

  public boolean connect(String cas) {
    try {
      filemgrUrl = new URL(cas);
      client = new XmlRpcFileManagerClient(filemgrUrl);
    } catch (Exception e) {
      filemgrUrl = null;
      return false;
    }
    return true;
  }

  public boolean disconnect() {
    filemgrUrl = null;
    return true;
  }

  public boolean isConnected() {
    return filemgrUrl != null;
  }

  public String[] getAvailableTypes() {
    String[] types = null;
    if (filemgrUrl != null) {
      try {
        Vector<ProductType> v = (Vector<ProductType>) client.getProductTypes();
        types = new String[v.size()];

      } catch (RepositoryManagerException e) {
        LOG.log(Level.SEVERE, e.getMessage());
      }

    } else {
      types = new String[1];
      types[0] = "";
    }

    return types;
  }

  public String[] getAvailableElements(String productTypeName) {
    String[] elements = null;
    if (filemgrUrl != null) {
      try {
        ProductType pt = client.getProductTypeByName(productTypeName);
        Vector<Element> v = (Vector<Element>) client
            .getElementsByProductType(pt);
        elements = new String[v.size()];

      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
      }
    } else {
      elements = new String[1];
      elements[0] = "";
    }
    return elements;
  }

  public String getElementID(String elementName) {
    String elementID = "";
    Element e;
    try {
      e = client.getElementByName(elementName);
      elementID = e.getElementId();
    } catch (ValidationLayerException ignored) {
    }
    return elementID;
  }

  public boolean issueQuery(org.apache.oodt.cas.filemgr.structs.Query query,
      String productType) {
    results = new Results();
    try {
      ProductType type = client.getProductTypeByName(productType);
      Vector<Product> products = (Vector<Product>) client.query(query, type);
      int maxVal = INT;
      if (products.size() < maxVal) {
        maxVal = products.size();
      }
      for (int i = 0; i < maxVal; i++) {
        Metadata m = client.getMetadata(products.get(i));
        results.addProduct(m);
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public boolean createQuery(String queryText, String productType) {

    results = new Results();
    if (queryText.trim().equals("*")) {
      ProductType type;
      try {
        type = client.getProductTypeByName(productType);
        Vector<Product> products = (Vector<Product>) client
            .getProductsByProductType(type);
        int maxVal = INT;
        if (products.size() < maxVal) {
          maxVal = products.size();
        }
        for (int i = 0; i < maxVal; i++) {
          Metadata m = client.getMetadata(products.get(i));
          results.addProduct(m);
        }
      } catch (Exception e) {
        return false;
      }

    } else {

      QueryBuilder qb = new QueryBuilder(this);
      org.apache.oodt.cas.filemgr.structs.Query casQ = qb.ParseQuery(queryText);
      ProductType type;
      try {
        type = client.getProductTypeByName(productType);
        Vector<Product> products = (Vector<Product>) client.query(casQ, type);
        int maxVal = INT;
        if (products.size() < maxVal) {
          maxVal = products.size();
        }
        for (int i = 0; i < maxVal; i++) {
          Metadata m = client.getMetadata(products.get(i));
          results.addProduct(m);
        }
      } catch (Exception e) {
        return false;
      }

    }

    return true;
  }

  public boolean clearQuery() {
    results = null;
    return true;
  }

}

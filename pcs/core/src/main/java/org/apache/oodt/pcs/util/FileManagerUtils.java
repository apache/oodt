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

package org.apache.oodt.pcs.util;

//OODT imports
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.pcs.metadata.PCSConfigMetadata;
import org.apache.oodt.pcs.query.FilenameQuery;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * A set of utility methods for use by the PCS in communicating with the File
 * Manager.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class FileManagerUtils implements PCSConfigMetadata, Serializable {
  /* our log stream */
  private static Logger LOG = Logger
      .getLogger(FileManagerUtils.class.getName());

  private FileManagerClient fmgrClient = null;

  private URL fmUrl;

  public FileManagerUtils(URL fileMgrUrl) {
    try {
      fmgrClient = RpcCommunicationFactory.createClient(fileMgrUrl);
    } catch (ConnectionException e) {
      LOG.log(Level.SEVERE,
          "Unable to connect to file manager: [" + fileMgrUrl.toString() + "]");
      fmgrClient = null;
    }

    this.fmUrl = fileMgrUrl;
  }

  public FileManagerUtils(String fmUrlStr) {
    this(safeGetUrlFromString(fmUrlStr));
  }

  public FileManagerUtils(FileManagerClient client) {
    this.fmgrClient = client;
  }

  public List safeGetTopNProducts(int n) {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    try {
      return this.fmgrClient.getTopNProducts(n);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Exception obtaining top" + n
          + " products: message: " + e.getMessage());
      return null;
    }
  }

  public Product getLatestProductByName(String prodName,
      String productTypeName) {
    return getLatestProductByName(prodName,
        safeGetProductTypeByName(productTypeName));
  }

  public Product getLatestProductByName(String prodName, ProductType type) {
    return getLatestProduct(new FilenameQuery(prodName, this).buildQuery(),
        type);
  }

  public Product getLatestProduct(Query query, String productTypeName) {
    return getLatestProduct(query, safeGetProductTypeByName(productTypeName));
  }

  public List toProductTypeList(List typeNames) {
    List typeList = new Vector();

    if (typeNames != null && typeNames.size() > 0) {
      for (Object typeName1 : typeNames) {
        String typeName = (String) typeName1;
        ProductType type = safeGetProductTypeByName(typeName);
        if (type != null) {
          typeList.add(type);
        }
      }
    }

    return typeList;
  }

  public List queryTypeList(Query query, List typeList) {
    List products = new Vector();

    if (typeList != null && typeList.size() > 0) {
      for (Object aTypeList : typeList) {
        ProductType type = (ProductType) aTypeList;
        List prods = safeIssueQuery(query, type);
        if (prods != null && prods.size() > 0) {
          products.addAll(prods);
        }
      }
    }

    return products;
  }

  public List queryAllTypes(Query query) {
    return queryAllTypes(query, null);
  }

  public List<Product> querySpecifiedTypes(Query query, List<String> typeList) {
    List<Product> products = new Vector<Product>();

    for (String productTypeName : typeList) {
      List<Product> prods = safeIssueQuery(query,
          safeGetProductTypeByName(productTypeName));
      if (prods != null && prods.size() > 0) {
        products.addAll(prods);
      }
    }

    return products;
  }

  public List queryAllTypes(Query query, List excludeTypeList) {
    List productTypes = safeGetProductTypes();
    List products = new Vector();

    if (productTypes != null && productTypes.size() > 0) {
      for (Object productType : productTypes) {
        ProductType type = (ProductType) productType;
        if (excludeTypeList != null
            && excludeTypeList.contains(type.getName())) {
          continue;
        }

        List prods = safeIssueQuery(query, type);
        if (prods != null && prods.size() > 0) {
          products.addAll(prods);
        }
      }
    }

    return products;

  }

  public List queryAndReturnMetadata(Query query, ProductType type) {
    List prods = safeIssueQuery(query, type);

    if (prods == null || (prods.size() == 0)) {
      return new Vector();
    }

    List prodsMet = new Vector(prods.size());

    for (Object prod : prods) {
      Product p = (Product) prod;
      prodsMet.add(safeGetMetadata(p));
    }

    return prodsMet;
  }

  public List safeIssueQuery(Query query, ProductType type) {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    List retProds = null;

    try {
      retProds = this.fmgrClient.query(query, type);
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Exception issuing query: [" + query + "] to file manager at: ["
              + this.fmgrClient.getFileManagerUrl() + "]: Message: "
              + e.getMessage(),
          e);
    }

    return retProds;

  }

  public Element safeGetElementByName(String elemName) {
    if (!isConnected())
      return Element.blankElement();
    Element element = null;

    try {
      element = fmgrClient.getElementByName(elemName);
    } catch (ValidationLayerException e) {
      LOG.log(Level.WARNING,
          "Exception obtaining element definition for element: [" + elemName
              + "]: Message: " + e.getMessage());
    }

    return element;
  }

  public ProductType safeGetProductTypeByName(String productTypeName) {
    if (!isConnected())
      return ProductType.blankProductType();
    ProductType type = null;

    try {
      type = fmgrClient.getProductTypeByName(productTypeName);
    } catch (RepositoryManagerException e) {
      LOG.log(Level.WARNING, "Exception obtaining product type definition"
          + " for type: [" + productTypeName + "]: Message: " + e.getMessage());
    }

    return type;
  }

  public ProductType safeGetProductTypeById(String productTypeId) {
    if (!isConnected())
      return ProductType.blankProductType();
    ProductType type = null;

    try {
      type = fmgrClient.getProductTypeById(productTypeId);
    } catch (RepositoryManagerException e) {
      LOG.log(Level.WARNING, "Exception obtaining product type definition"
          + " for type: [" + productTypeId + "]: Message: " + e.getMessage());
    }

    return type;
  }

  public List safeGetProductReferences(Product product) {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    List refs = null;

    try {
      refs = fmgrClient.getProductReferences(product);
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Exception obtaining product references" + "for product: ["
              + product.getProductName() + "]: Message: " + e.getMessage());
    }

    return refs;
  }

  public Metadata safeGetMetadata(Product product) {
    if (!isConnected())
      return new Metadata();
    Metadata metadata = null;

    try {
      metadata = fmgrClient.getMetadata(product);
    } catch (CatalogException e) {
      LOG.log(Level.WARNING, "Unable to obtain metadata for product: ["
          + product.getProductName() + "] " + "from File Manager at: ["
          + fmgrClient.getFileManagerUrl() + "]: Message: " + e.getMessage());
    }

    return metadata;
  }

  public List safeGetProductTypes() {
    if (!isConnected())
      return Collections.EMPTY_LIST;
    List types = null;

    try {
      types = fmgrClient.getProductTypes();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Unable to retrieve product types from filemgr: ["
          + fmgrClient.getFileManagerUrl() + "]: reason: " + e.getMessage());
    }

    return types;
  }

  public Product getLatestProduct(Query query, ProductType type) {
    if (!isConnected())
      return Product.getDefaultFlatProduct("", "");
    List products;

    try {
      products = fmgrClient.query(query, type);
      if (products != null && products.size() > 0) {
        Product p = (Product) products.get(0);
        p.setProductReferences(safeGetProductReferences(p));
        return p;
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to obtain products: query: [" + query
          + "]: Message: " + e.getMessage(), e);
    }

    return null;

  }

  public Product safeGetProductByName(String prodName) {
    if (!isConnected())
      return Product.getDefaultFlatProduct("", "");
    Product p = null;

    try {
      p = this.fmgrClient.getProductByName(prodName);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error obtaining product: [" + prodName
          + "]: Message: " + e.getMessage(), e);
    }

    return p;
  }

  /**
   * Gets the number of products for the given type.
   * 
   * @param type
   *          The given type.
   * @return The number of products.
   */
  public int safeGetNumProducts(ProductType type) {
    if (!isConnected())
      return -1;
    int numProducts = -1;
    try {
      numProducts = this.fmgrClient.getNumProducts(type);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.warning("Exception getting num products by type: [" + type.getName()
          + "]: " + "Message: " + e.getLocalizedMessage());
    }

    return numProducts;
  }

  /**
   * Get a first page of Products using the pagination API.
   * 
   * @param type
   *          Gets the first page of products for this type.
   * @return The first page of products for this type.
   */
  public ProductPage safeFirstPage(ProductType type) {
    if (!isConnected())
      return ProductPage.blankPage();
    ProductPage page = null;
    try {
      page = this.fmgrClient.getFirstPage(type);
    } catch (Exception e) {
      LOG.info("No products found for: " + type.getName());
    }
    return page;
  }

  public String getFilePath(Product prod) {
    if (!isConnected())
      return "N/A";
    if (prod.getProductReferences() == null) {
      prod.setProductReferences(safeGetProductReferences(prod));
    }

    if (prod.getProductReferences() == null
        || (prod.getProductReferences() != null
            && prod.getProductReferences().size() == 0)) {
      return "N/A";
    }

    // get the first ref
    Reference r = (Reference) prod.getProductReferences().get(0);
    return safeGetFileFromUri(r.getDataStoreReference()).getAbsolutePath();
  }

  public static List toProductNameList(List productList) {
    if (productList == null || (productList.size() == 0)) {
      return new Vector();
    }

    List prodNames = new Vector(productList.size());
    for (Object aProductList : productList) {
      Product p = (Product) aProductList;
      prodNames.add(p.getProductName());
    }

    return prodNames;
  }

  public static boolean check(String propName, String propValue,
      Metadata metadata) {
    if (propValue == null) {
      LOG.log(Level.SEVERE, "PCS: " + propName + ": value: " + null);
      metadata.replaceMetadata("ApplicationSuccess", "false");
      return false;
    } else {
      return true;
    }
  }

  public static URL safeGetUrlFromString(String urlStr) {
    URL url = null;

    try {
      url = new URL(urlStr);
    } catch (MalformedURLException e) {
      LOG.log(Level.SEVERE, "PCS: Unable to generate url from url string: ["
          + urlStr + "]: Message: " + e.getMessage());
    }

    return url;
  }

  public static File safeGetFileFromUri(String uri) {
    File f = null;

    try {
      f = new File(new URI(uri));
    } catch (URISyntaxException e) {
      LOG.log(Level.WARNING,
          "URI syntax exception obtaining file object from uri: [" + uri + "]");
    }

    return f;
  }

  /**
   * @return the fmgrClient
   */
  public FileManagerClient getFmgrClient() {
    return fmgrClient;
  }

  /**
   * @param fmgrClient
   *          the fmgrClient to set
   */
  public void setFmgrClient(FileManagerClient fmgrClient) {
    this.fmgrClient = fmgrClient;
    if (this.fmgrClient != null) {
      this.fmUrl = this.fmgrClient.getFileManagerUrl();
    }
  }

  /**
   * 
   * @return The {@link URL} to the File Manager that this FileManagerUtils
   *         object connects to.
   */
  public URL getFmUrl() {
    return this.fmUrl;
  }

  public static String getDirProductFilePath(Product p, String prodName) {
    return FileManagerUtils.safeGetFileFromUri(
        FileManagerUtils.getRootReference(prodName, p.getProductReferences())
            .getOrigReference())
        .getAbsolutePath();
  }

  public static Reference getRootReference(String productName, List refs) {
    Reference r = null;

    for (Object ref1 : refs) {
      Reference ref = (Reference) ref1;
      if (isRootDir(ref, productName)) {
        r = ref;
      }
    }

    return r;
  }

  public static boolean isRootDir(Reference r, String prodName) {
    if (!r.getOrigReference().endsWith("/")) {
      return false;
    } else {
      String referenceURI = r.getOrigReference();
      int lastFolderInPathStartIdx = referenceURI
          .substring(0, referenceURI.length() - 1).lastIndexOf("/") + 1;
      String lastFolderInPath = referenceURI.substring(lastFolderInPathStartIdx,
          referenceURI.length() - 1);
      return lastFolderInPath.startsWith(prodName);
    }
  }

  private boolean isConnected() {
    if (this.fmgrClient == null) {
      LOG.warning(
          "Not connected to File Manager: Default Products, References, Metadata and other objects will be returned.");
      return false;
    } else
      return true;
  }

}

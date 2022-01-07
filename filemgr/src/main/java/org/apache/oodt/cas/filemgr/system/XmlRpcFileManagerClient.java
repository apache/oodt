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

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.exceptions.FileManagerException;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.XmlRpcStructFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 * @version $Revision$
 * @deprecated replaced by avro-rpc
 *          <p/>
 *          <p> The XML RPC based file manager client. </p>
 */
@Deprecated
public class XmlRpcFileManagerClient implements FileManagerClient {

  /* our xml rpc client */
  private transient XmlRpcClient client = null;

  /* our log stream */
  private static Logger LOG = Logger.getLogger(XmlRpcFileManagerClient.class
          .getName());

  /* file manager url */
  private URL fileManagerUrl = null;

  /* data transferer needed if client is request to move files itself */
  private DataTransfer dataTransfer = null;

  public XmlRpcFileManagerClient(final URL url) throws ConnectionException {
    this(url, true);
  }

  /**
   * <p> Constructs a new XmlRpcFileManagerClient with the given <code>url</code>. </p>
   *
   * @param url            The url pointer to the xml rpc file manager service.
   * @param testConnection Whether or not to check if server at given url is alive.
   */
  public XmlRpcFileManagerClient(final URL url, boolean testConnection)
          throws ConnectionException {
    // set up the configuration, if there is any
    if (System.getProperty("org.apache.oodt.cas.filemgr.properties") != null) {
      String configFile = System
              .getProperty("org.apache.oodt.cas.filemgr.properties");
      LOG.log(Level.INFO,
              "Loading File Manager Configuration Properties from: ["
                      + configFile + "]");
      try {
        System.getProperties().load(
                new FileInputStream(new File(configFile)));
      } catch (Exception e) {
        LOG.log(Level.INFO,
                "Error loading configuration properties from: ["
                        + configFile + "]");
      }

    }

    XmlRpcTransportFactory transportFactory = new XmlRpcTransportFactory() {

      public XmlRpcTransport createTransport()
              throws XmlRpcClientException {

        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
          public boolean retryRequest(
                  IOException exception,
                  int count,
                  HttpContext context){
            if (count < Integer
                    .getInteger(
                            "org.apache.oodt.cas.filemgr.system.xmlrpc.connection.retries",
                            3)) {
              try {
                Thread
                        .sleep(Integer
                                .getInteger(
                                        "org.apache.oodt.cas.filemgr.system.xmlrpc.connection.retry.interval.seconds",
                                        0) * 1000);
                return true;
              } catch (Exception ignored) {
              }
            }
            return false;
          }
        };
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(Integer
                        .getInteger(
                                "org.apache.oodt.cas.filemgr.system.xmlrpc.connectionTimeout.minutes",
                                20) * 60 * 1000)
                .setConnectTimeout(Integer
                        .getInteger(
                                "org.apache.oodt.cas.filemgr.system.xmlrpc.requestTimeout.minutes",
                                60) * 60 * 1000)
                .build();
        Registry<AuthSchemeProvider> r = RegistryBuilder.<AuthSchemeProvider>create().build();
        HttpClient client = HttpClients.custom().setRetryHandler(myRetryHandler).setDefaultAuthSchemeRegistry(r).setDefaultRequestConfig(config).build();

        CommonsXmlRpcTransport transport = new CommonsXmlRpcTransport(url, client);
        transport
                .setConnectionTimeout(Integer
                        .getInteger(
                                "org.apache.oodt.cas.filemgr.system.xmlrpc.connectionTimeout.minutes",
                                20) * 60 * 1000);
        transport
                .setTimeout(Integer
                        .getInteger(
                                "org.apache.oodt.cas.filemgr.system.xmlrpc.requestTimeout.minutes",
                                60) * 60 * 1000);

        return transport;
      }

      public void setProperty(String arg0, Object arg1) {
      }

    };

    client = new XmlRpcClient(url, transportFactory);
    fileManagerUrl = url;

    if (testConnection && !isAlive()) {
      throw new ConnectionException("Exception connecting to filemgr: ["
              + this.fileManagerUrl + "]");
    }

  }

  public boolean refreshConfigAndPolicy() {
    boolean success;

    Vector<Object> argList = new Vector<Object>();
    try {
      success = (Boolean) client.execute("filemgr.refreshConfigAndPolicy",
              argList);
    } catch (XmlRpcException e) {
      LOG.log(Level.WARNING, "XmlRpcException when connecting to filemgr: ["
              + this.fileManagerUrl + "]");
      success = false;
    } catch (IOException e) {
      LOG.log(Level.WARNING, "IOException when connecting to filemgr: ["
              + this.fileManagerUrl + "]");
      success = false;
    }

    return success;
  }

  public boolean isAlive() {
    boolean connected;

    Vector<Object> argList = new Vector<Object>();
    try {
      connected = (Boolean) client.execute("filemgr.isAlive", argList);
    } catch (XmlRpcException e) {
      LOG.log(Level.WARNING,
              "XmlRpcException when connecting to filemgr: ["
                      + this.fileManagerUrl + "]");
      connected = false;
    } catch (IOException e) {
      LOG.log(Level.WARNING, "IOException when connecting to filemgr: ["
              + this.fileManagerUrl + "]");
      connected = false;
    }

    return connected;
  }

  public boolean transferringProduct(Product product)
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.transferringProduct",
              argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    return success;
  }

  public boolean removeProductTransferStatus(Product product)
          throws DataTransferException {
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    Vector<Object> argList = new Vector<Object>();
    argList.add(productHash);

    boolean success;

    try {
      success = (Boolean) client.execute(
              "filemgr.removeProductTransferStatus", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    return success;
  }

  public boolean isTransferComplete(Product product)
          throws DataTransferException {
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    Vector<Object> argList = new Vector<Object>();
    argList.add(productHash);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.isTransferComplete",
              argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    return success;
  }

  public boolean moveProduct(Product product, String newPath)
          throws DataTransferException {
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    Vector<Object> argList = new Vector<Object>();
    argList.add(productHash);
    argList.add(newPath);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.moveProduct", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    return success;
  }

  public boolean modifyProduct(Product product) throws CatalogException {
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);

    Vector<Object> argList = new Vector<Object>();
    argList.add(productHash);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.modifyProduct",
              argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return success;

  }

  public boolean removeProduct(Product product) throws CatalogException {
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);

    Vector<Object> argList = new Vector<Object>();
    argList.add(productHash);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.removeProduct",
              argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return success;

  }

  @SuppressWarnings("unchecked")
  public FileTransferStatus getCurrentFileTransfer()
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();

    Map<String, Object> statusHash;
    FileTransferStatus status = null;

    try {
      statusHash = (Map<String, Object>) client.execute(
              "filemgr.getCurrentFileTransfer", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    if (statusHash != null) {
      status = XmlRpcStructFactory
              .getFileTransferStatusFromXmlRpc(statusHash);
    }

    return status;
  }

  @SuppressWarnings("unchecked")
  public List<FileTransferStatus> getCurrentFileTransfers()
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();

    Vector<Map<String, Object>> statusVector;
    List<FileTransferStatus> statuses = null;

    try {
      statusVector = (Vector<Map<String, Object>>) client.execute(
              "filemgr.getCurrentFileTransfers", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    if (statusVector != null) {
      statuses = XmlRpcStructFactory
              .getFileTransferStatusesFromXmlRpc(statusVector);
    }

    return statuses;
  }

  public double getProductPctTransferred(Product product)
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);
    Double pct;

    try {
      pct = (Double) client.execute("filemgr.getProductPctTransferred",
              argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    if (pct != null) {
      return pct;
    }

    return -1.0;
  }

  public double getRefPctTransferred(Reference reference)
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> refHash = XmlRpcStructFactory
            .getXmlRpcReference(reference);
    argList.add(refHash);
    Double pct;

    try {
      pct = (Double) client.execute("filemgr.getRefPctTransferred",
              argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    if (pct != null) {
      return pct;
    }

    return -1.0;
  }

  @SuppressWarnings("unchecked")
  public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> queryHash = XmlRpcStructFactory
            .getXmlRpcQuery(query);
    Map<String, Object> typeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);
    argList.add(queryHash);
    argList.add(typeHash);
    argList.add(pageNum);

    Map<String, Object> pageHash;

    try {
      pageHash = (Map<String, Object>) client.execute(
              "filemgr.pagedQuery", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
  }

  @SuppressWarnings("unchecked")
  public ProductPage getFirstPage(ProductType type) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

    ProductPage page = null;
    Map<String, Object> pageHash;

    try {
      pageHash = (Map<String, Object>) client.execute(
              "filemgr.getFirstPage", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (pageHash != null) {
      page = XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
    }

    return page;

  }

  @SuppressWarnings("unchecked")
  public ProductPage getLastPage(ProductType type) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

    ProductPage page = null;
    Map<String, Object> pageHash;

    try {
      pageHash = (Map<String, Object>) client.execute(
              "filemgr.getLastPage", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (pageHash != null) {
      page = XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
    }

    return page;
  }

  @SuppressWarnings("unchecked")
  public ProductPage getNextPage(ProductType type, ProductPage currPage)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));
    argList.add(XmlRpcStructFactory.getXmlRpcProductPage(currPage));

    ProductPage page = null;
    Map<String, Object> pageHash;

    try {
      pageHash = (Map<String, Object>) client.execute(
              "filemgr.getNextPage", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (pageHash != null) {
      page = XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
    }

    return page;
  }

  @SuppressWarnings("unchecked")
  public ProductPage getPrevPage(ProductType type, ProductPage currPage)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));
    argList.add(XmlRpcStructFactory.getXmlRpcProductPage(currPage));

    ProductPage page = null;
    Map<String, Object> pageHash;

    try {
      pageHash = (Map<String, Object>) client.execute(
              "filemgr.getPrevPage", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (pageHash != null) {
      page = XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
    }

    return page;
  }

  public String addProductType(ProductType type)
          throws RepositoryManagerException {
    String productTypeId;
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> typeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);
    argList.add(typeHash);

    try {
      productTypeId = (String) client.execute("filemgr.addProductType",
              argList);
    } catch (XmlRpcException e) {
      throw new RepositoryManagerException(e);
    } catch (IOException e) {
      throw new RepositoryManagerException(e);
    }

    return productTypeId;

  }

  public boolean hasProduct(String productName) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(productName);

    boolean hasProduct;

    try {
      hasProduct = (Boolean) client.execute("filemgr.hasProduct",
              argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return hasProduct;

  }

  public int getNumProducts(ProductType type) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

    Integer numProducts;

    try {
      numProducts = (Integer) client.execute("filemgr.getNumProducts",
              argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return numProducts;
  }

  @SuppressWarnings("unchecked")
  public List<Product> getTopNProducts(int n) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(n);

    Vector<Map<String, Object>> topNProducts;

    try {
      topNProducts = (Vector<Map<String, Object>>) client.execute(
              "filemgr.getTopNProducts", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return XmlRpcStructFactory
            .getProductListFromXmlRpc(topNProducts);
  }

  @SuppressWarnings("unchecked")
  public List<Product> getTopNProducts(int n, ProductType type)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(n);
    Map<String, Object> productTypeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);
    argList.add(productTypeHash);

    Vector<Map<String, Object>> topNProducts;

    try {
      topNProducts = (Vector<Map<String, Object>>) client.execute(
              "filemgr.getTopNProducts", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return XmlRpcStructFactory
            .getProductListFromXmlRpc(topNProducts);
  }

  public void setProductTransferStatus(Product product)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);

    try {
      client.execute("filemgr.setProductTransferStatus", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

  }

  public void addProductReferences(Product product) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);

    try {
      client.execute("filemgr.addProductReferences", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }
  }

  public void addMetadata(Product product, Metadata metadata)
          throws CatalogException {

    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));
    argList.add(metadata.getHashTable());

    try {
      client.execute("filemgr.addMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }
  }

  public boolean updateMetadata(Product product, Metadata met)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));
    argList.add(met.getHashTable());

    boolean result;

    try {
      result = (Boolean) client.execute("filemgr.updateMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return result;

  }

  public String catalogProduct(Product product) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));

    String productId;

    try {
      productId = (String) client.execute("filemgr.catalogProduct",
              argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    return productId;
  }

  @SuppressWarnings("unchecked")
  public Metadata getMetadata(Product product) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);

    Map<String, Object> metadata;

    try {
      metadata = (Map<String, Object>) client.execute(
              "filemgr.getMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    Metadata m = new Metadata();
    m.addMetadata(metadata);
    return m;

  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Metadata getReducedMetadata(Product product, List<?> elements)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);
    argList.add(new Vector(elements));

    Map<String, Object> metadata;

    try {
      metadata = (Map<String, Object>) client.execute(
              "filemgr.getReducedMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    Metadata m = new Metadata();
    m.addMetadata(metadata);
    return m;

  }

  public boolean removeFile(String filePath) throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(filePath);

    boolean success;

    try {
      success = (Boolean) client.execute("filemgr.removeFile", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }

    return success;
  }

  public byte[] retrieveFile(String filePath, int offset, int numBytes)
          throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(filePath);
    argList.add(offset);
    argList.add(numBytes);

    try {
      return (byte[]) client.execute("filemgr.retrieveFile", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }
  }

  public void transferFile(String filePath, byte[] fileData, int offset,
                           int numBytes) throws DataTransferException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(filePath);
    argList.add(fileData);
    argList.add(offset);
    argList.add(numBytes);

    try {
      client.execute("filemgr.transferFile", argList);
    } catch (XmlRpcException e) {
      throw new DataTransferException(e);
    } catch (IOException e) {
      throw new DataTransferException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Product> getProductsByProductType(ProductType type)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productTypeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);
    argList.add(productTypeHash);

    Vector<Map<String, Object>> productVector;

    try {
      productVector = (Vector<Map<String, Object>>) client.execute(
              "filemgr.getProductsByProductType", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (productVector == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getProductListFromXmlRpc(productVector);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Element> getElementsByProductType(ProductType type)
          throws ValidationLayerException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> productTypeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);

    argList.add(productTypeHash);

    Vector<Map<String, Object>> elementVector;

    try {
      elementVector = (Vector<Map<String, Object>>) client.execute(
              "filemgr.getElementsByProductType", argList);
    } catch (XmlRpcException e) {
      throw new ValidationLayerException(e);
    } catch (IOException e) {
      throw new ValidationLayerException(e);
    }

    if (elementVector == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getElementListFromXmlRpc(elementVector);
    }
  }

  @SuppressWarnings("unchecked")
  public Element getElementById(String elementId)
          throws ValidationLayerException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(elementId);

    Hashtable<String, Object> elementHash;

    try {
      elementHash = (Hashtable<String, Object>) client.execute(
              "filemgr.getElementById", argList);
    } catch (XmlRpcException e) {
      throw new ValidationLayerException(e);
    } catch (IOException e) {
      throw new ValidationLayerException(e);
    }

    if (elementHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getElementFromXmlRpc(elementHash);
    }
  }

  @SuppressWarnings("unchecked")
  public Element getElementByName(String elementName)
          throws ValidationLayerException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(elementName);

    Hashtable<String, Object> elementHash;

    try {
      elementHash = (Hashtable<String, Object>) client.execute(
              "filemgr.getElementByName", argList);
    } catch (XmlRpcException e) {
      throw new ValidationLayerException(e);
    } catch (IOException e) {
      throw new ValidationLayerException(e);
    }

    if (elementHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getElementFromXmlRpc(elementHash);
    }
  }

  @SuppressWarnings("unchecked")
  public Element getElementByName(String elementName, ProductType type)
          throws ValidationLayerException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(elementName);
    argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

    Hashtable<String, Object> elementHash;

    try {
      elementHash = (Hashtable<String, Object>) client.execute(
              "filemgr.getElementByName", argList);
    } catch (XmlRpcException e) {
      throw new ValidationLayerException(e);
    } catch (IOException e) {
      throw new ValidationLayerException(e);
    }

    if (elementHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getElementFromXmlRpc(elementHash);
    }
  }

  public List<QueryResult> complexQuery(ComplexQuery complexQuery)
          throws CatalogException {
    try {
      Map<String, Object> complexQueryHash = XmlRpcStructFactory
              .getXmlRpcComplexQuery(complexQuery);
      Vector<Object> argList = new Vector<Object>();
      argList.add(complexQueryHash);
      @SuppressWarnings("unchecked")
      Vector<Map<String, Object>> queryResultHashVector = (Vector<Map<String, Object>>) client
              .execute("filemgr.complexQuery", argList);
      return XmlRpcStructFactory
              .getQueryResultsFromXmlRpc(queryResultHashVector);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new CatalogException(e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Product> query(Query query, ProductType type)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    Map<String, Object> queryHash = XmlRpcStructFactory
            .getXmlRpcQuery(query);
    Map<String, Object> typeHash = XmlRpcStructFactory
            .getXmlRpcProductType(type);
    argList.add(queryHash);
    argList.add(typeHash);

    Vector<Map<String, Object>> productVector;

    try {
      productVector = (Vector<Map<String, Object>>) client.execute(
              "filemgr.query", argList);
    } catch (XmlRpcException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new CatalogException(e);

    } catch (IOException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new CatalogException(e);
    }

    if (productVector == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getProductListFromXmlRpc(productVector);
    }
  }

  @SuppressWarnings("unchecked")
  public ProductType getProductTypeByName(String productTypeName)
          throws RepositoryManagerException {
    Hashtable<String, Object> productTypeHash;
    Vector<Object> argList = new Vector<Object>();
    argList.add(productTypeName);

    try {
      productTypeHash = (Hashtable<String, Object>) client.execute(
              "filemgr.getProductTypeByName", argList);
    } catch (XmlRpcException e) {
      throw new RepositoryManagerException(e.getLocalizedMessage());
    } catch (IOException e) {
      throw new RepositoryManagerException(e);
    }

    if (productTypeHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory
              .getProductTypeFromXmlRpc(productTypeHash);
    }
  }

  @SuppressWarnings("unchecked")
  public ProductType getProductTypeById(String productTypeId)
          throws RepositoryManagerException {
    Hashtable<String, Object> productTypeHash;
    Vector<Object> argList = new Vector<Object>();
    argList.add(productTypeId);

    try {
      productTypeHash = (Hashtable<String, Object>) client.execute(
              "filemgr.getProductTypeById", argList);
    } catch (XmlRpcException e) {
      throw new RepositoryManagerException(e);
    } catch (IOException e) {
      throw new RepositoryManagerException(e);
    }

    if (productTypeHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory
              .getProductTypeFromXmlRpc(productTypeHash);
    }
  }

  @SuppressWarnings("unchecked")
  public List<ProductType> getProductTypes()
          throws RepositoryManagerException {
    Vector<Object> argList = new Vector<Object>();

    Vector<Map<String, Object>> productTypeVector;

    try {
      productTypeVector = (Vector<Map<String, Object>>) client
              .execute("filemgr.getProductTypes", argList);
    } catch (XmlRpcException e) {
      throw new RepositoryManagerException(e);
    } catch (IOException e) {
      throw new RepositoryManagerException(e);
    }

    if (productTypeVector == null) {
      return null;
    } else {
      return XmlRpcStructFactory
              .getProductTypeListFromXmlRpc(productTypeVector);
    }
  }

  @SuppressWarnings("unchecked")
  public List<Reference> getProductReferences(Product product)
          throws CatalogException {
    Vector<Object> argList = new Vector<Object>();

    Vector<Map<String, Object>> productReferenceVector;
    Map<String, Object> productHash = XmlRpcStructFactory
            .getXmlRpcProduct(product);
    argList.add(productHash);

    try {
      productReferenceVector = (Vector<Map<String, Object>>) client
              .execute("filemgr.getProductReferences", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (productReferenceVector == null) {
      return null;
    } else {
      return XmlRpcStructFactory
              .getReferencesFromXmlRpc(productReferenceVector);
    }
  }

  @SuppressWarnings("unchecked")
  public Product getProductById(String productId) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();

    Map<String, Object> productHash;
    argList.add(productId);

    try {
      productHash = (Map<String, Object>) client.execute(
              "filemgr.getProductById", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (productHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getProductFromXmlRpc(productHash);
    }
  }

  @SuppressWarnings("unchecked")
  public Product getProductByName(String productName) throws CatalogException {
    Vector<Object> argList = new Vector<Object>();

    Map<String, Object> productHash;
    argList.add(productName);

    try {
      productHash = (Map<String, Object>) client.execute(
              "filemgr.getProductByName", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e);
    } catch (IOException e) {
      throw new CatalogException(e);
    }

    if (productHash == null) {
      return null;
    } else {
      return XmlRpcStructFactory.getProductFromXmlRpc(productHash);
    }
  }

  public String ingestProduct(Product product, Metadata metadata,
                              boolean clientTransfer)
          throws VersioningException, XmlRpcException, FileManagerException {
    try {
      // ingest product
      Vector<Object> argList = new Vector<Object>();
      Map<String, Object> productHash = XmlRpcStructFactory
              .getXmlRpcProduct(product);
      argList.add(productHash);
      argList.add(metadata.getHashTable());
      argList.add(clientTransfer);
      String productId = (String) client.execute("filemgr.ingestProduct",
              argList);

      if (clientTransfer) {
        LOG.log(Level.FINEST,
                "File Manager Client: clientTransfer enabled: "
                        + "transfering product ["
                        + product.getProductName() + "]");

        // we need to transfer the product ourselves
        // make sure we have the product ID
        if (productId == null) {
          throw new Exception("Request to ingest product: "
                  + product.getProductName()
                  + " but no product ID returned from File "
                  + "Manager ingest");
        }

        if (dataTransfer == null) {
          throw new Exception("Request to ingest product: ["
                  + product.getProductName()
                  + "] using client transfer, but no "
                  + "dataTransferer specified!");
        }

        product.setProductId(productId);

        if (!Boolean.getBoolean("org.apache.oodt.cas.filemgr.serverside.versioning")) {
          // version the product
          Versioner versioner = GenericFileManagerObjectFactory
                  .getVersionerFromClassName(product.getProductType()
                          .getVersioner());
          if (versioner != null) {
            versioner.createDataStoreReferences(product, metadata);
          }

          // add the newly versioned references to the data store
          try {
            addProductReferences(product);
          } catch (CatalogException e) {
            LOG
                    .log(
                            Level.SEVERE,
                            "ingestProduct: RepositoryManagerException "
                                    + "when adding Product References for Product : "
                                    + product.getProductName()
                                    + " to RepositoryManager: Message: "
                                    + e);
            throw e;
          }
        } else {
          product.setProductReferences(getProductReferences(product));
        }

        // now transfer the product
        try {
          dataTransfer.transferProduct(product);
          // now update the product's transfer status in the data
          // store
          product.setTransferStatus(Product.STATUS_RECEIVED);

          try {
            setProductTransferStatus(product);
          } catch (CatalogException e) {
            LOG
                    .log(
                            Level.SEVERE,
                            "ingestProduct: RepositoryManagerException "
                                    + "when updating product transfer status for Product: "
                                    + product.getProductName()
                                    + " Message: " + e);
            throw e;
          }
        } catch (Exception e) {
          LOG.log(Level.SEVERE,
                  "ingestProduct: DataTransferException when transfering Product: "
                          + product.getProductName() + ": Message: "
                          + e);
          throw new DataTransferException(e);
        }

      }
      return productId;

      // error versioning file
    } catch (VersioningException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.SEVERE,
              "ingestProduct: VersioningException when versioning Product: "
                      + product.getProductName() + " with Versioner "
                      + product.getProductType().getVersioner()
                      + ": Message: " + e);
      throw new VersioningException(e);
    } catch (XmlRpcException e2) {
      LOG.log(Level.SEVERE, "Failed to ingest product [ name:" + product.getProductName() + "] :" + e2.getMessage()
              + " -- rolling back ingest");
      try {
        Vector<Object> argList = new Vector<Object>();
        Map<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);
        client.execute("filemgr.removeProduct", argList);
      } catch (Exception e1) {
        LOG.log(Level.SEVERE, "Failed to rollback ingest of product ["
                + product + "] : " + e2.getMessage());
      }
      throw e2;
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Failed to ingest product [ id: " + product.getProductId() +
              "/ name:" + product.getProductName() + "] :" + e + " -- rolling back ingest");
      try {
        Vector<Object> argList = new Vector<Object>();
        Map<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);
        client.execute("filemgr.removeProduct", argList);
      } catch (Exception e1) {
        LOG.log(Level.SEVERE, "Failed to rollback ingest of product ["
                + product + "] : " + e);
      }
      throw new FileManagerException("Failed to ingest product [" + product + "] : "
              + e);
    }

  }

  @SuppressWarnings("unchecked")
  public Metadata getCatalogValues(Metadata metadata, ProductType productType)
          throws XmlRpcException, IOException {
    Vector<Object> args = new Vector<Object>();
    args.add(metadata.getHashTable());
    args.add(XmlRpcStructFactory.getXmlRpcProductType(productType));

    Metadata m = new Metadata();
    m.addMetadata((Map<String, Object>) this.client.execute(
            "filemgr.getCatalogValues", args));

    return m;
  }

  @SuppressWarnings("unchecked")
  public Metadata getOrigValues(Metadata metadata, ProductType productType)
          throws XmlRpcException, IOException {
    Vector<Object> args = new Vector<Object>();
    args.add(metadata.getHashTable());
    args.add(XmlRpcStructFactory.getXmlRpcProductType(productType));

    Metadata m = new Metadata();
    m.addMetadata((Map<String, Object>) this.client.execute(
            "filemgr.getOrigValues", args));

    return m;
  }

  @SuppressWarnings("unchecked")
  public Query getCatalogQuery(Query query, ProductType productType)
          throws XmlRpcException, IOException {
    Vector<Object> args = new Vector<Object>();
    args.add(XmlRpcStructFactory.getXmlRpcQuery(query));
    args.add(XmlRpcStructFactory.getXmlRpcProductType(productType));
    return XmlRpcStructFactory
            .getQueryFromXmlRpc((Hashtable<String, Object>) this.client
                    .execute("filemgr.getCatalogQuery", args));
  }

  public static void main(String[] args) {
    CmdLineUtility cmdLineUtility = new CmdLineUtility();
    cmdLineUtility.run(args);
  }

  /**
   * @return Returns the fileManagerUrl.
   */
  public URL getFileManagerUrl() {
    return fileManagerUrl;
  }

  /**
   * @param fileManagerUrl The fileManagerUrl to set.
   */
  public void setFileManagerUrl(URL fileManagerUrl) {
    this.fileManagerUrl = fileManagerUrl;

    // reset the client
    this.client = new XmlRpcClient(fileManagerUrl);
  }

  /**
   * @return Returns the dataTransfer.
   */
  public DataTransfer getDataTransfer() {
    return dataTransfer;
  }

  /**
   * @param dataTransfer The dataTransfer to set.
   */
  public void setDataTransfer(DataTransfer dataTransfer) {
    this.dataTransfer = dataTransfer;
    this.dataTransfer.setFileManagerUrl(this.fileManagerUrl);
  }

  @Override
  public void close() throws IOException {

  }

}

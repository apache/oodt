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

import org.apache.xmlrpc.CommonsXmlRpcTransport;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.util.XmlRpcStructFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
/**
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 * @version $Revision$
 * 
 * <p>
 * The XML RPC based file manager client.
 * </p>
 * 
 */
public class XmlRpcFileManagerClient implements FileManagerClient {

    /* our xml rpc client */
    private XmlRpcClient client = null;

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
     * <p>
     * Constructs a new XmlRpcFileManagerClient with the given <code>url</code>.
     * </p>
     *
     * @param url
     *            The url pointer to the xml rpc file manager service.
     * @param testConnection
     *            Whether or not to check if server at given url is alive.
     */
    public XmlRpcFileManagerClient(final URL url, boolean testConnection)
          throws ConnectionException {

        XmlRpcTransportFactory transportFactory = new XmlRpcTransportFactory() {

            public XmlRpcTransport createTransport()
                    throws XmlRpcClientException {
                HttpClient client = new HttpClient();
                client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                        new HttpMethodRetryHandler() {

                            public boolean retryMethod(HttpMethod method,
                                    IOException e, int count) {
                                if (count < Integer
                                        .getInteger(
                                                "org.apache.oodt.cas.filemgr.system.xmlrpc.connection.retries",
                                                3).intValue()) {
                                    try {
                                        Thread
                                                .sleep(Integer
                                                        .getInteger(
                                                                "org.apache.oodt.cas.filemgr.system.xmlrpc.connection.retry.interval.seconds",
                                                                0).intValue() * 1000);
                                        return true;
                                    } catch (Exception e1) {
                                    }
                                }
                                return false;
                            }

                        });
                CommonsXmlRpcTransport transport = new CommonsXmlRpcTransport(
                        url, client);
                transport
                        .setConnectionTimeout(Integer
                                .getInteger(
                                        "org.apache.oodt.cas.filemgr.system.xmlrpc.connectionTimeout.minutes",
                                        20).intValue() * 60 * 1000);
                transport
                        .setTimeout(Integer
                                .getInteger(
                                        "org.apache.oodt.cas.filemgr.system.xmlrpc.requestTimeout.minutes",
                                        60).intValue() * 60 * 1000);

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
      boolean success = false;
  
      Vector<Object> argList = new Vector<Object>();
      try {
        success = ((Boolean) client.execute("filemgr.refreshConfigAndPolicy",
            argList)).booleanValue();
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
        boolean connected = false;

        Vector<Object> argList = new Vector<Object>();
        try {
            connected = ((Boolean) client.execute("filemgr.isAlive", argList))
                    .booleanValue();
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
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.transferringProduct",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

    public boolean removeProductTransferStatus(Product product)
            throws DataTransferException {
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        Vector<Object> argList = new Vector<Object>();
        argList.add(productHash);

        boolean success = false;

        try {
            success = ((Boolean) client.execute(
                    "filemgr.removeProductTransferStatus", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

    public boolean isTransferComplete(Product product)
            throws DataTransferException {
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        Vector<Object> argList = new Vector<Object>();
        argList.add(productHash);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.isTransferComplete",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

    public boolean moveProduct(Product product, String newPath)
            throws DataTransferException {
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        Vector<Object> argList = new Vector<Object>();
        argList.add(productHash);
        argList.add(newPath);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.moveProduct", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

    public boolean modifyProduct(Product product) throws CatalogException {
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);

        Vector<Object> argList = new Vector<Object>();
        argList.add(productHash);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.modifyProduct",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return success;

    }

    public boolean removeProduct(Product product) throws CatalogException {
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);

        Vector<Object> argList = new Vector<Object>();
        argList.add(productHash);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.removeProduct",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return success;

    }

    @SuppressWarnings("unchecked")
    public FileTransferStatus getCurrentFileTransfer()
            throws DataTransferException {
        Vector<Object> argList = new Vector<Object>();

        Hashtable<String, Object> statusHash = null;
        FileTransferStatus status = null;

        try {
            statusHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getCurrentFileTransfer", argList);
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
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

        Vector<Hashtable<String, Object>> statusVector = null;
        List<FileTransferStatus> statuses = null;

        try {
            statusVector = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.getCurrentFileTransfers", argList);
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
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
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);
        Double pct = null;

        try {
            pct = (Double) client.execute("filemgr.getProductPctTransferred",
                    argList);
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        if (pct != null) {
            return pct.doubleValue();
        }

        return -1.0;
    }

    public double getRefPctTransferred(Reference reference)
            throws DataTransferException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> refHash = XmlRpcStructFactory
                .getXmlRpcReference(reference);
        argList.add(refHash);
        Double pct = null;

        try {
            pct = (Double) client.execute("filemgr.getRefPctTransferred",
                    argList);
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        if (pct != null) {
            return pct.doubleValue();
        }

        return -1.0;
    }

    @SuppressWarnings("unchecked")
    public ProductPage pagedQuery(Query query, ProductType type, int pageNum)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> queryHash = XmlRpcStructFactory
                .getXmlRpcQuery(query);
        Hashtable<String, Object> typeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);
        argList.add(queryHash);
        argList.add(typeHash);
        argList.add(new Integer(pageNum));

        Hashtable<String, Object> pageHash = null;

        try {
            pageHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.pagedQuery", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
    }

    @SuppressWarnings("unchecked")
    public ProductPage getFirstPage(ProductType type) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

        ProductPage page = null;
        Hashtable<String, Object> pageHash = null;

        try {
            pageHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getFirstPage", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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
        Hashtable<String, Object> pageHash = null;

        try {
            pageHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getLastPage", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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
        Hashtable<String, Object> pageHash = null;

        try {
            pageHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getNextPage", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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
        Hashtable<String, Object> pageHash = null;

        try {
            pageHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getPrevPage", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        if (pageHash != null) {
            page = XmlRpcStructFactory.getProductPageFromXmlRpc(pageHash);
        }

        return page;
    }

    public String addProductType(ProductType type)
            throws RepositoryManagerException {
        String productTypeId = null;
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> typeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);
        argList.add(typeHash);

        try {
            productTypeId = (String) client.execute("filemgr.addProductType",
                    argList);
        } catch (XmlRpcException e) {
            throw new RepositoryManagerException(e.getMessage());
        } catch (IOException e) {
            throw new RepositoryManagerException(e.getMessage());
        }

        return productTypeId;

    }

    public boolean hasProduct(String productName) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(productName);

        boolean hasProduct = false;

        try {
            hasProduct = ((Boolean) client.execute("filemgr.hasProduct",
                    argList)).booleanValue();
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return hasProduct;

    }

    public int getNumProducts(ProductType type) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(XmlRpcStructFactory.getXmlRpcProductType(type));

        Integer numProducts = new Integer(-1);

        try {
            numProducts = (Integer) client.execute("filemgr.getNumProducts",
                    argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return numProducts.intValue();
    }

    @SuppressWarnings("unchecked")
    public List<Product> getTopNProducts(int n) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(new Integer(n));

        Vector<Hashtable<String, Object>> topNProducts = null;

        try {
            topNProducts = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.getTopNProducts", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        List<Product> topNProductList = XmlRpcStructFactory
                .getProductListFromXmlRpc(topNProducts);
        return topNProductList;
    }


    @SuppressWarnings("unchecked")
    public List<Product> getTopNProducts(int n, ProductType type)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(new Integer(n));
        Hashtable<String, Object> productTypeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);
        argList.add(productTypeHash);

        Vector<Hashtable<String, Object>> topNProducts = null;

        try {
            topNProducts = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.getTopNProducts", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        List<Product> topNProductList = XmlRpcStructFactory
                .getProductListFromXmlRpc(topNProducts);
        return topNProductList;
    }

    public void setProductTransferStatus(Product product)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);

        try {
            client.execute("filemgr.setProductTransferStatus", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

    }

    public void addProductReferences(Product product) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);

        try {
            client.execute("filemgr.addProductReferences", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }
    }

  public void addMetadata(Product product, Metadata metadata)
      throws CatalogException {

    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));
    argList.add(metadata.getHashtable());

    try {
      client.execute("filemgr.addMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e.getMessage());
    } catch (IOException e) {
      throw new CatalogException(e.getMessage());
    }
  }

  public boolean updateMetadata(Product product, Metadata met)
      throws CatalogException {
    Vector<Object> argList = new Vector<Object>();
    argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));
    argList.add(met.getHashtable());

    boolean result = false;

    try {
      result = (Boolean) client.execute("filemgr.updateMetadata", argList);
    } catch (XmlRpcException e) {
      throw new CatalogException(e.getMessage());
    } catch (IOException e) {
      throw new CatalogException(e.getMessage());
    }

    return result;

  }

    public String catalogProduct(Product product) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(XmlRpcStructFactory.getXmlRpcProduct(product));

        String productId = null;

        try {
            productId = (String) client.execute("filemgr.catalogProduct",
                    argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        return productId;
    }

    @SuppressWarnings("unchecked")
    public Metadata getMetadata(Product product) throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);

        Hashtable<String, Object> metadata = null;

        try {
            metadata = (Hashtable<String, Object>) client.execute(
                    "filemgr.getMetadata", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        Metadata m = new Metadata();
        m.addMetadata(metadata);
        return m;

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Metadata getReducedMetadata(Product product, List<?> elements)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);
        argList.add(new Vector(elements));

        Hashtable<String, Object> metadata = null;

        try {
            metadata = (Hashtable<String, Object>) client.execute(
                    "filemgr.getReducedMetadata", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        Metadata m = new Metadata();
        m.addMetadata(metadata);
        return m;

    }

    public boolean removeFile(String filePath) throws DataTransferException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(filePath);

        boolean success = false;

        try {
            success = ((Boolean) client.execute("filemgr.removeFile", argList))
                    .booleanValue();
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }

        return success;
    }

   public byte[] retrieveFile(String filePath, int offset, int numBytes)
         throws DataTransferException {
      Vector<Object> argList = new Vector<Object>();
      argList.add(filePath);
      argList.add(new Integer(offset));
      argList.add(new Integer(numBytes));

      try {
         return (byte[]) client.execute("filemgr.retrieveFile", argList);
      } catch (XmlRpcException e) {
         throw new DataTransferException(e.getMessage());
      } catch (IOException e) {
         throw new DataTransferException(e.getMessage());
      }
   }

    public void transferFile(String filePath, byte[] fileData, int offset,
            int numBytes) throws DataTransferException {
        Vector<Object> argList = new Vector<Object>();
        argList.add(filePath);
        argList.add(fileData);
        argList.add(new Integer(offset));
        argList.add(new Integer(numBytes));

        try {
            client.execute("filemgr.transferFile", argList);
        } catch (XmlRpcException e) {
            throw new DataTransferException(e.getMessage());
        } catch (IOException e) {
            throw new DataTransferException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Product> getProductsByProductType(ProductType type)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> productTypeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);
        argList.add(productTypeHash);

        Vector<Hashtable<String, Object>> productVector = null;

        try {
            productVector = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.getProductsByProductType", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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
        Hashtable<String, Object> productTypeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);

        argList.add(productTypeHash);

        Vector<Hashtable<String, Object>> elementVector = null;

        try {
            elementVector = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.getElementsByProductType", argList);
        } catch (XmlRpcException e) {
            throw new ValidationLayerException(e.getMessage());
        } catch (IOException e) {
            throw new ValidationLayerException(e.getMessage());
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

        Hashtable<String, Object> elementHash = null;

        try {
            elementHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getElementById", argList);
        } catch (XmlRpcException e) {
            throw new ValidationLayerException(e.getMessage());
        } catch (IOException e) {
            throw new ValidationLayerException(e.getMessage());
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

        Hashtable<String, Object> elementHash = null;

        try {
            elementHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getElementByName", argList);
        } catch (XmlRpcException e) {
            throw new ValidationLayerException(e.getMessage());
        } catch (IOException e) {
            throw new ValidationLayerException(e.getMessage());
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
            Hashtable<String, Object> complexQueryHash = XmlRpcStructFactory
                    .getXmlRpcComplexQuery(complexQuery);
            Vector<Object> argList = new Vector<Object>();
            argList.add(complexQueryHash);
            @SuppressWarnings("unchecked")
            Vector<Hashtable<String, Object>> queryResultHashVector = (Vector<Hashtable<String, Object>>) client
                    .execute("filemgr.complexQuery", argList);
            return XmlRpcStructFactory
                    .getQueryResultsFromXmlRpc(queryResultHashVector);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CatalogException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Product> query(Query query, ProductType type)
            throws CatalogException {
        Vector<Object> argList = new Vector<Object>();
        Hashtable<String, Object> queryHash = XmlRpcStructFactory
                .getXmlRpcQuery(query);
        Hashtable<String, Object> typeHash = XmlRpcStructFactory
                .getXmlRpcProductType(type);
        argList.add(queryHash);
        argList.add(typeHash);

        Vector<Hashtable<String, Object>> productVector = null;

        try {
            productVector = (Vector<Hashtable<String, Object>>) client.execute(
                    "filemgr.query", argList);
        } catch (XmlRpcException e) {
            e.printStackTrace();
            throw new CatalogException(e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            throw new CatalogException(e.getMessage());
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
        Hashtable<String, Object> productTypeHash = new Hashtable<String, Object>();
        Vector<Object> argList = new Vector<Object>();
        argList.add(productTypeName);

        try {
            productTypeHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getProductTypeByName", argList);
        } catch (XmlRpcException e) {
            throw new RepositoryManagerException(e.getLocalizedMessage());
        } catch (IOException e) {
            throw new RepositoryManagerException(e.getMessage());
        }

        if (productTypeHash == null) {
            return null;
        } else
            return XmlRpcStructFactory
                    .getProductTypeFromXmlRpc(productTypeHash);
    }

    @SuppressWarnings("unchecked")
    public ProductType getProductTypeById(String productTypeId)
            throws RepositoryManagerException {
        Hashtable<String, Object> productTypeHash = new Hashtable<String, Object>();
        Vector<Object> argList = new Vector<Object>();
        argList.add(productTypeId);

        try {
            productTypeHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getProductTypeById", argList);
        } catch (XmlRpcException e) {
            throw new RepositoryManagerException(e.getMessage());
        } catch (IOException e) {
            throw new RepositoryManagerException(e.getMessage());
        }

        if (productTypeHash == null) {
            return null;
        } else
            return XmlRpcStructFactory
                    .getProductTypeFromXmlRpc(productTypeHash);
    }

    @SuppressWarnings("unchecked")
    public List<ProductType> getProductTypes()
            throws RepositoryManagerException {
        Vector<Object> argList = new Vector<Object>();

        Vector<Hashtable<String, Object>> productTypeVector = null;

        try {
            productTypeVector = (Vector<Hashtable<String, Object>>) client
                    .execute("filemgr.getProductTypes", argList);
        } catch (XmlRpcException e) {
            throw new RepositoryManagerException(e.getMessage());
        } catch (IOException e) {
            throw new RepositoryManagerException(e.getMessage());
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

        Vector<Hashtable<String, Object>> productReferenceVector = null;
        Hashtable<String, Object> productHash = XmlRpcStructFactory
                .getXmlRpcProduct(product);
        argList.add(productHash);

        try {
            productReferenceVector = (Vector<Hashtable<String, Object>>) client
                    .execute("filemgr.getProductReferences", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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

        Hashtable<String, Object> productHash = null;
        argList.add(productId);

        try {
            productHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getProductById", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
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

        Hashtable<String, Object> productHash = null;
        argList.add(productName);

        try {
            productHash = (Hashtable<String, Object>) client.execute(
                    "filemgr.getProductByName", argList);
        } catch (XmlRpcException e) {
            throw new CatalogException(e.getMessage());
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        }

        if (productHash == null) {
            return null;
        } else {
            return XmlRpcStructFactory.getProductFromXmlRpc(productHash);
        }
    }

    public String ingestProduct(Product product, Metadata metadata,
            boolean clientTransfer) throws Exception {
        try {
            // ingest product
            Vector<Object> argList = new Vector<Object>();
            Hashtable<String, Object> productHash = XmlRpcStructFactory
                    .getXmlRpcProduct(product);
            argList.add(productHash);
            argList.add(metadata.getHashtable());
            argList.add(Boolean.valueOf(clientTransfer));
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
                  versioner.createDataStoreReferences(product, metadata);
                  
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
                                              + e.getMessage());
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
                                                + " Message: " + e.getMessage());
                        throw e;
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE,
                            "ingestProduct: DataTransferException when transfering Product: "
                                    + product.getProductName() + ": Message: "
                                    + e.getMessage());
                    throw new DataTransferException(e);
                }

            }
            return productId;

            // error versioning file
        } catch (VersioningException e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE,
                "ingestProduct: VersioningException when versioning Product: "
                        + product.getProductName() + " with Versioner "
                        + product.getProductType().getVersioner()
                        + ": Message: " + e.getMessage());
            throw new VersioningException(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.log(Level.SEVERE, "Failed to ingest product [" + product
                    + "] : " + e.getMessage() + " -- rolling back ingest");
            try {
                Vector<Object> argList = new Vector<Object>();
                Hashtable<String, Object> productHash = XmlRpcStructFactory
                        .getXmlRpcProduct(product);
                argList.add(productHash);
                client.execute("filemgr.removeProduct", argList);
            } catch (Exception e1) {
                LOG.log(Level.SEVERE, "Failed to rollback ingest of product ["
                        + product + "] : " + e.getMessage());
            }
            throw new Exception("Failed to ingest product [" + product + "] : "
                    + e.getMessage());
        }

    }

    @SuppressWarnings("unchecked")
    public Metadata getCatalogValues(Metadata metadata, ProductType productType)
            throws XmlRpcException, IOException {
        Vector<Object> args = new Vector<Object>();
        args.add(metadata.getHashtable());
        args.add(XmlRpcStructFactory.getXmlRpcProductType(productType));

        Metadata m = new Metadata();
        m.addMetadata((Hashtable<String, Object>) this.client.execute(
                "filemgr.getCatalogValues", args));

        return m;
    }

    @SuppressWarnings("unchecked")
    public Metadata getOrigValues(Metadata metadata, ProductType productType)
            throws XmlRpcException, IOException {
        Vector<Object> args = new Vector<Object>();
        args.add(metadata.getHashtable());
        args.add(XmlRpcStructFactory.getXmlRpcProductType(productType));

        Metadata m = new Metadata();
        m.addMetadata((Hashtable<String, Object>) this.client.execute(
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
    /**
     * @return Returns the fileManagerUrl.
     */
    public URL getFileManagerUrl() {
        return fileManagerUrl;
    }

    /**
     * @param fileManagerUrl
     *            The fileManagerUrl to set.
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
     * @param dataTransfer
     *            The dataTransfer to set.
     */
    public void setDataTransfer(DataTransfer dataTransfer) {
        this.dataTransfer = dataTransfer;
        this.dataTransfer.setFileManagerUrl(this.fileManagerUrl);
    }

}

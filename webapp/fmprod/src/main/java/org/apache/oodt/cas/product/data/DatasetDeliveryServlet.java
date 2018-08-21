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


package org.apache.oodt.cas.product.data;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//JDK imports

/**
 * Delivers back entire {@link ProductType}s (or <code>Dataset</code>s) as
 * zipped up packages.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class DatasetDeliveryServlet extends HttpServlet implements DataDeliveryKeys {

  /* our log stream */
  private static final Logger LOG = Logger
      .getLogger(DatasetDeliveryServlet.class.getName());

  /* serial version UID */
  private static final long serialVersionUID = -6692665690674186105L;
  public static final int INT = 512;

  /* our file manager client */
  private FileManagerClient client;

  /* the working dir in which to create huge zip files */
  private String workingDirPath;

  /* indicates the product has been zipped up already */
  private final static String ALREADY_ZIPPED = "00ga";

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPut(req, res);
  }

  /**
   * 
   * Method requires a single parameter, <code>typeID</code>, specifying a valid
   * {@link ProductType} within the filemgr. All {@link Product}s associated
   * with the given <code>typeID</code> are packaged up (as zip files), and then
   * aggregated and zipped and delivered as an entire collection called a
   * <code>Dataset</code>.
   * 
   * @param req
   *          Servlet request.
   * @param res
   *          Servlet response
   * @throws ServletException
   *           If any error occurs.
   * @throws IOException
   *           If an I/O error occurs.
   */
  protected void doPut(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    String typeID = req.getParameter("typeID");
    ProductType type;

    if (typeID == null) {
      throw new IllegalArgumentException("No typeID parameter specified!");
    }

    try {
      type = client.getProductTypeById(typeID);
    } catch (Exception e) {
      throw new ServletException("Unable to deduce product type: [" + typeID
          + "]: Message: " + e.getMessage());
    }

    // create a temporary product dir: we'll use working dir + typeName
    String productDirPath = workingDirPath + type.getName();
    if (!new File(productDirPath).mkdirs()) {
      LOG.log(Level.WARNING,
          "mkdirs returned false for temporary dataset dir: [" + productDirPath
              + "]: errors may follow");
    }

    // use the pagination API to iterate over each product
    // for each product, zip it up
    // after you zip up all products then create the dataset zip

    ProductPage page;

    try {
      page = client.getFirstPage(type);
      if (page == null || (page.getPageProducts() == null) || (page.getPageProducts().size() == 0)) {
        throw new ServletException("No products for dataset: ["
            + type.getName() + "]");
      }

      Map productHash = new ConcurrentHashMap();

      do {
        for (Product product : page.getPageProducts()) {
          if (alreadyZipped(product, productHash)) {
            continue;
          }

          Metadata metadata;
          product.setProductReferences(client.getProductReferences(product));
          metadata = client.getMetadata(product);
          DataUtils.createProductZipFile(product, metadata, productDirPath);
          productHash.put(product.getProductName(), ALREADY_ZIPPED);
        }

        page = client.getNextPage(type, page);

      } while ((page != null && !page.isLastPage())
          && (page.getPageProducts() != null && page.getPageProducts().size() > 0));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new ServletException(e.getMessage());
    }

    // now that all product zips have been created, create the dataset
    // zip
    String datasetZipFilePath;
    File datasetZipFile = null;
    InputStream in = null;
    OutputStream o2 = null;

    try {
      datasetZipFilePath = DataUtils.createDatasetZipFile(type, productDirPath);

      datasetZipFile = new File(datasetZipFilePath);
      String datasetZipFileName = datasetZipFile.getName();

      res.addHeader(CONTENT_DISPOSITION_HDR, "attachment; filename=\""
          + datasetZipFileName + "\"");

      // add the content length for the zip file size
      res.addHeader(CONTENT_LENGTH_HDR, String.valueOf(new File(
          datasetZipFilePath).length()));

      // now we need to read the zip file in, and write it to the output
      // stream
      in = new FileInputStream(datasetZipFile);

      // Call java.io.File.delete() on the file, while still open.
      if (!datasetZipFile.delete()) {
        throw new RuntimeException("Unable to delete streaming file: ["
            + datasetZipFile.getAbsolutePath() + "] while delivering!");
      }

      // 3. Deliver the data.
      o2 = res.getOutputStream();
      byte[] buf = new byte[INT];
      int n;
      while ((n = in.read(buf)) != -1) {
        o2.write(buf, 0, n);
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception delivering dataset: Message: "
          + e.getMessage());
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception ignore) {
        }

      }

      if (o2 != null) {
        try {
          o2.close();
        } catch (Exception ignore) {
        }

      }

      // now try and remove the tmp working directory for the
      // dataset zip
      if (datasetZipFile != null) {
        datasetZipFile.getParentFile().delete();
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    try {
      String fileMgrURL;
      try {
        fileMgrURL = PathUtils.replaceEnvVariables(config.getServletContext().getInitParameter(
            "filemgr.url") );
      } catch (Exception e) {
        throw new ServletException("Failed to get filemgr url : " + e.getMessage(), e);
      }
      client = RpcCommunicationFactory.createClient(new URL(fileMgrURL));
    } catch (MalformedURLException ex) {
      throw new ServletException(ex);
    } catch (ConnectionException ex) {
      throw new ServletException(ex);
    }

    workingDirPath = config.getServletContext().getInitParameter(
        "filemgr.working.dir");

    // need the working dir to be specified, else throw exception
    if (workingDirPath == null) {
      throw new ServletException("no servlet working dir path specified!");
    } else {
      // clean it
      this.workingDirPath += this.workingDirPath.endsWith("/") ? "" : "/";
    }

  }

  public void destroy() {
    if (client != null) {
      try {
        client.close();
      } catch (IOException ignored) { }
    }
  }

  private boolean alreadyZipped(Product p, Map hash) {
    return hash.containsKey(p.getProductName());
  }

}

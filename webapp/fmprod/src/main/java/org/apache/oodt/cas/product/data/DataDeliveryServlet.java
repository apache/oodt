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
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Data Delivery Servlet.
 * 
 * The Data Delivery Serlvet retrieves the product data from the file manager.
 */
public class DataDeliveryServlet extends HttpServlet implements
    DataDeliveryKeys {

  public static final int INT = 512;

  /** Client i/f to filemgr server. */
  private FileManagerClient client;

  /** our log stream */
  private static final Logger LOG = Logger.getLogger(DataDeliveryServlet.class
          .getName());

  /** our working dir path. */
  private String workingDirPath;

  /** serial version UID. */
  private static final long serialVersionUID = -955613407495060869L;

  /** {@inheritDoc} */
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      String fileMgrURL;
      try {
        fileMgrURL = PathUtils.replaceEnvVariables(config.getServletContext().getInitParameter("filemgr.url") );
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

  /** {@inheritDoc} */
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPut(req, res);
  }

  /**
   * Handle a request for product data. Request parameters include a mandatory
   * <code>productID</code> identifying what product to retrieve, and an
   * optional <code>refIndex</code> in order to select which reference file to
   * send. By default it sends the zeroth reference.
   * 
   * In addition, an optional <code>format</code> option can be specified to
   * indicate that the product data be zipped up and delivered back as a
   * zip file.
   * 
   * @param req
   *          Servlet request
   * @param res
   *          Servlet response
   * @throws ServletException
   *           If an error occurs
   * @throws IOException
   *           If an I/O error occurs
   */
  public void doPut(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      String productID = req.getParameter("productID");
      if (productID == null) {
        throw new IllegalArgumentException("productID is required");
      }
      String refIndex = req.getParameter("refIndex");
      if (refIndex == null) {
        refIndex = "0";
      }
      int index = Integer.parseInt(refIndex);
      String format = req.getParameter("format");

      if (format == null) {
        // then just deliver back the particular product file
        deliverProductFile(req, res, index, productID);
      } else {
        // make sure it's application/x-zip
        if (!format.equals(FORMAT_ZIP)) {
          throw new IllegalArgumentException("unknown product return format: "
              + format);
        }

        try {
          deliverProductAsZip(req, res, productID);
        } catch (Exception e) {
          throw new ServletException(e);
        }
      }

    } catch (IllegalArgumentException ex) {
      throw new ServletException(ex);
    } catch (CatalogException ex) {
      throw new ServletException(ex);
    }
  }

  private void deliverProductAsZip(HttpServletRequest req,
      HttpServletResponse res, String productID) throws CatalogException {
    Product product = client.getProductById(productID);
    product.setProductReferences(client.getProductReferences(product));
    Metadata metadata = client.getMetadata(product);

    // we'll be delivering a zip
    res.addHeader(CONTENT_TYPE_HDR, FORMAT_ZIP);

    String productZipFilePath;
    File productZipFile;
    InputStream in = null;
    OutputStream o2 = null;

    try {
      productZipFilePath = DataUtils.createProductZipFile(product, metadata,
          workingDirPath);
      productZipFile = new File(productZipFilePath);
      String productZipFileName = productZipFile.getName();

      res.addHeader(CONTENT_DISPOSITION_HDR, "attachment; filename=\""
          + productZipFileName + "\"");

      // add the content length for the zip file size
      res.addHeader(CONTENT_LENGTH_HDR, String.valueOf(new File(
          productZipFilePath).length()));

      // now we need to read the zip file in, and write it to the output
      // stream
      in = new FileInputStream(productZipFile);

      // Call java.io.File.delete() on the file, while still open.
      if (!productZipFile.delete()) {
        throw new RuntimeException("Unable to delete streaming file: ["
            + productZipFile.getAbsolutePath() + "] while delivering!");
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
      LOG.log(Level.WARNING, "Exception delivering data!: Message: "
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

    }

  }

  private void deliverProductFile(HttpServletRequest req,
      HttpServletResponse res, int index, String productID)
      throws CatalogException, IOException {
    Product product = null; 
    List refs = null;
    
    try{
      product = client.getProductById(productID);
      refs = client.getProductReferences(product);      
    }
    catch(Exception e){
      LOG.warning("Unable to deliver product: ID: ["+productID+"]: "
          + "Message: "+e.getMessage()+" throwing 404.");
      res.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    
    Reference ref = (Reference) refs.get(index);
    res.addHeader(CONTENT_LENGTH_HDR, String.valueOf(ref.getFileSize()));
    String contentType = (ref.getMimeType() != null
        && ref.getMimeType().getName() != null && !ref.getMimeType().getName()
        .equals("")) ? ref.getMimeType().getName() : DataUtils
        .guessTypeFromName(ref.getDataStoreReference());
    res.addHeader(CONTENT_TYPE_HDR, contentType);
    try {
      res.addHeader(CONTENT_DISPOSITION_HDR, "attachment; filename=\""
          + new File(new URI(ref.getDataStoreReference())).getName() + "\"");
    } catch (URISyntaxException e) {
      LOG.log(Level.WARNING,
          "Unable to sense filename from data store URI: Message: "
              + e.getMessage());
    }
    URL url = new URL(ref.getDataStoreReference());
    URLConnection c = url.openConnection();
    InputStream in = c.getInputStream();
    OutputStream out = res.getOutputStream();
    byte[] buf = new byte[INT];
    int n;
    while ((n = in.read(buf)) != -1) {
      out.write(buf, 0, n);
    }
    in.close();
    out.close();
  }
}

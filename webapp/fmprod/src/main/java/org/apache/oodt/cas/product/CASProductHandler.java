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


package org.apache.oodt.cas.product;


import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.product.data.DataUtils;
import org.apache.oodt.product.LargeProductQueryHandler;
import org.apache.oodt.product.ProductException;
import org.apache.oodt.xmlquery.Header;
import org.apache.oodt.xmlquery.LargeResult;
import org.apache.oodt.xmlquery.XMLQuery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.PRODUCT_ID;
import static org.apache.oodt.cas.product.CASProductHandlerMetKeys.CAS_PROFILE_ID;
import static org.apache.oodt.cas.product.CASProductHandlerMetKeys.FILE_HEADER;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class CASProductHandler implements LargeProductQueryHandler {

  private static Logger LOG = Logger.getLogger(CASProductHandler.class.getName());
    private FileManagerClient fm;

    public CASProductHandler() throws MalformedURLException,
            ConnectionException {
        this.fm = RpcCommunicationFactory.createClient(new URL(System.getProperty("fm.url", "http://localhost:9000")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.product.QueryHandler#query(org.apache.xmlquery.XMLQuery)
     */
    public XMLQuery query(XMLQuery query) throws ProductException {
        // we'll only accept queries for ProductId=some_id
        String kwdQuery = query.getKwdQueryString();
        String[] kwdQueryToks = kwdQuery.split("=");
        if ((kwdQueryToks.length != 2)) {
            throw new ProductException(
                    "Malformed query: CASProductHandler only accepts queries of the "
                            + "form " + PRODUCT_ID
                            + " = <some product id>: your query was: "
                            + kwdQuery);
        }

        String prodId = kwdQueryToks[1];
        Product product;
        try {
            product = this.fm.getProductById(prodId);
            product.setProductReferences(this.fm.getProductReferences(product));
        } catch (CatalogException e) {
            throw new ProductException(
                    "Exception querying file manager for product: [" + prodId
                            + "]: Message: " + e.getMessage());
        }

        try {
            addResultsFromProductId(query, product);
        } catch (URISyntaxException e) {
            throw new ProductException(
                    "URI Syntax Exception deciphering product: ["
                            + product.getProductName() + "]: Message: "
                            + e.getMessage());
        }
        return query;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.product.LargeProductQueryHandler#close(java.lang.String)
     */
    public void close(String id) {
        // doesn't really have to do anything b/c we're not going
        // to keep anything open

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.product.LargeProductQueryHandler#retrieveChunk(java.lang.String,
     *      long, int)
     */
    public byte[] retrieveChunk(String id, long offset, int length)
            throws ProductException {

        InputStream in = null;
        byte[] buf = null;

        try {
            URL url = new URL(id);
            URLConnection c = url.openConnection();
            in = c.getInputStream();

            buf = new byte[length];
            int numRead;
            long numSkipped;
            numSkipped = in.skip(offset);
            if (numSkipped != offset) {
                throw new ProductException("Was not able to skip: [" + offset
                        + "] bytes into product: num actually skipped: ["
                        + numSkipped + "]");
            }

            numRead = in.read(buf, 0, length);

            if (numRead != length) {
                throw new ProductException("Was not able to read: [" + length
                        + "] bytes from product: num actually read: ["
                        + numRead + "]");
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new ProductException(
                    "IO exception retrieving chunk of product: [" + id
                            + "]: Message: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }

            }
        }

        return buf;

    }

    private void addResultsFromProductId(XMLQuery query, Product product)
            throws URISyntaxException {

        if (product != null && product.getProductReferences() != null
                && product.getProductReferences().size() > 0) {
          for (Reference r : product.getProductReferences()) {
            query.getResults().add(toResult(r));
          }
        }

    }

    private LargeResult toResult(Reference r) throws URISyntaxException {
        String mimeType = r.getMimeType() != null ? r.getMimeType().getName()
                : DataUtils.guessTypeFromName(r.getDataStoreReference());
      return new LargeResult(r.getDataStoreReference(),
              mimeType, CAS_PROFILE_ID, new File(new URI(r
                      .getDataStoreReference())).getName(),
              Collections.singletonList(new Header(FILE_HEADER, mimeType,
                      null /* unit */)), r.getFileSize());
    }

}

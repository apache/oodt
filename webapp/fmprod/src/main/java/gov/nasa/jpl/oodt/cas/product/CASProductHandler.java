//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Iterator;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.filemgr.structs.Reference;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import gov.nasa.jpl.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import gov.nasa.jpl.oodt.cas.product.data.DataUtils;
import jpl.eda.product.LargeProductQueryHandler;
import jpl.eda.product.ProductException;
import jpl.eda.xmlquery.Header;
import jpl.eda.xmlquery.LargeResult;
import jpl.eda.xmlquery.XMLQuery;
import static gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys.*;
import static gov.nasa.jpl.oodt.cas.product.CASProductHandlerMetKeys.*;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class CASProductHandler implements LargeProductQueryHandler {

    private XmlRpcFileManagerClient fm;

    public CASProductHandler() throws MalformedURLException,
            ConnectionException {
        this.fm = new XmlRpcFileManagerClient(new URL(System.getProperty(
                "fm.url", "http://localhost:9000")));
    }

    /*
     * (non-Javadoc)
     * 
     * @see jpl.eda.product.QueryHandler#query(jpl.eda.xmlquery.XMLQuery)
     */
    public XMLQuery query(XMLQuery query) throws ProductException {
        // we'll only accept queries for ProductId=some_id
        String kwdQuery = query.getKwdQueryString();
        String[] kwdQueryToks = kwdQuery.split("=");
        if (kwdQueryToks == null
                || (kwdQueryToks != null && kwdQueryToks.length != 2)) {
            throw new ProductException(
                    "Malformed query: CASProductHandler only accepts queries of the "
                            + "form " + PRODUCT_ID
                            + " = <some product id>: your query was: "
                            + kwdQuery);
        }

        String prodId = kwdQueryToks[1];
        Product product = null;
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
     * @see jpl.eda.product.LargeProductQueryHandler#close(java.lang.String)
     */
    public void close(String id) throws ProductException {
        // doesn't really have to do anything b/c we're not going
        // to keep anything open

    }

    /*
     * (non-Javadoc)
     * 
     * @see jpl.eda.product.LargeProductQueryHandler#retrieveChunk(java.lang.String,
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
            e.printStackTrace();
            throw new ProductException(
                    "IO exception retrieving chunk of product: [" + id
                            + "]: Message: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore) {
                }

                in = null;
            }
        }

        return buf;

    }

    private void addResultsFromProductId(XMLQuery query, Product product)
            throws URISyntaxException {

        if (product != null && product.getProductReferences() != null
                && product.getProductReferences().size() > 0) {
            for (Iterator<Reference> i = product.getProductReferences()
                    .iterator(); i.hasNext();) {
                Reference r = i.next();
                query.getResults().add(toResult(r));
            }
        }

    }

    private LargeResult toResult(Reference r) throws URISyntaxException {
        String mimeType = r.getMimeType() != null ? r.getMimeType().getName()
                : DataUtils.guessTypeFromName(r.getDataStoreReference());
        LargeResult result = new LargeResult(r.getDataStoreReference(),
                mimeType, CAS_PROFILE_ID, new File(new URI(r
                        .getDataStoreReference())).getName(),
                Collections.singletonList(new Header(FILE_HEADER, mimeType,
                        null /* unit */)), r.getFileSize());
        return result;
    }

}

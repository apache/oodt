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

package org.apache.oodt.cas.filemgr.datatransfer;

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;

//JDK imports
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An abstract base class for data transfers that uses an in-memory ConcurrentHashMap to
 * keep track of data transfer status information.
 * </p>
 * 
 */
public class TransferStatusTracker {
    /* ConcurrentHashMap containing a list of current product transfers */
    protected ConcurrentHashMap<String, Product> currentProductTransfers = new ConcurrentHashMap<String, Product>();

    /* our catalog object */
    private Catalog catalog = null;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(TransferStatusTracker.class
            .getName());

    /**
     * <p>
     * Default Constructor
     * </p>.
     * 
     * @param catalog
     *            The Catalog interface handed to it by the File Manager. It
     *            needs this object to look up reference information.
     */
    public TransferStatusTracker(Catalog catalog) {
        this.catalog = catalog;
    }

    public FileTransferStatus getCurrentFileTransfer() {
        List<FileTransferStatus> transfers = getCurrentFileTransfers();

        if (transfers != null && transfers.size() > 0) {
            return (FileTransferStatus) transfers.get(0);
        } else {
            return null;
        }
    }

    public void transferringProduct(Product product) {
        currentProductTransfers.put(product.getProductId(), product);
    }

    public List<FileTransferStatus> getCurrentFileTransfers() {
        List<FileTransferStatus> currTransfers = new Vector<FileTransferStatus>();

        for (Map.Entry<String, Product> productId : currentProductTransfers.entrySet()) {
            Product p = productId.getValue();

            // get its references
            List<Reference> refs = quietGetReferences(p);

            if (refs != null && refs.size() > 0) {
                for (Reference r : refs) {
                    long bytesTransferred = getBytesTransferred(r);

                    if (bytesTransferred > 0
                        && bytesTransferred < r.getFileSize() && !isDir(r)) {
                        FileTransferStatus status = new FileTransferStatus();
                        status.setBytesTransferred(bytesTransferred);
                        status.setFileRef(r);
                        status.setParentProduct(p);
                        currTransfers.add(status);
                    }
                }
            }
        }

        return currTransfers;
    }

    public double getPctTransferred(Product product) {
        // get its references
        List<Reference> refs = quietGetReferences(product);
        long totalBytesTransferred = 0L;
        long totalProductSize = 0L;

        if (refs.size() > 0) {
            for (Reference r : refs) {
                long bytesTransferred = getBytesTransferred(r);

                if (!isDir(r)) {
                    // only add this if > 0
                    if (bytesTransferred > 0) {
                        totalBytesTransferred += bytesTransferred;
                    }

                    // add this no matter what
                    totalProductSize += r.getFileSize();
                }

            }
        }

        return ((double) ((1.0 * totalBytesTransferred) / (1.0 * totalProductSize)));

    }

    public double getPctTransferred(Reference ref) {
        long bytesTransferred = getBytesTransferred(ref);
        return ((double) ((1.0 * bytesTransferred) / (1.0 * ref.getFileSize())));
    }

    public void removeProductTransferStatus(Product product) {
        if (currentProductTransfers.get(product.getProductId()) != null) {
            currentProductTransfers.remove(product.getProductId());
        }
    }

    public boolean isTransferComplete(Product product) {
        return getPctTransferred(product) == 1.0;
    }

    private long getBytesTransferred(Reference r) {
        File destFile;

        try {
            destFile = new File(new URI(r.getDataStoreReference()));
            return destFile.length();
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING,
                    "URISyntaxException when checking size of destFile: ["
                            + r.getDataStoreReference() + "]: Message: "
                            + e.getMessage());
            return -1L;
        }
    }

    private List<Reference> quietGetReferences(Product p) {
        List<Reference> refs;

        try {
            refs = catalog.getProductReferences(p);
        } catch (CatalogException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Error retreiving references for product: ["
                    + p.getProductId()
                    + "] from catalog in transfer status tracker: Message: "
                    + e.getMessage());
            refs = new Vector<Reference>();
        }

        return refs;
    }

    private boolean isDir(Reference r) {
        File fileRef;

        try {
            fileRef = new File(new URI(r.getDataStoreReference()));
            return fileRef.isDirectory();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}

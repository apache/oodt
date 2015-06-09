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

package org.apache.oodt.cas.filemgr.structs;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A class to represent the status of a transfer for a file reference belonging
 * to a {@link Product}.
 * </p>
 * 
 */
public class FileTransferStatus {

    /* the file reference that is being transferred */
    private Reference fileRef = null;

    /* the amount of bytes transferred so far */
    private long bytesTransferred = 0L;

    /* the associated Product */
    private Product parentProduct = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public FileTransferStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param fileRef
     *            The file reference being transferred.
     * @param bytesTransferred
     *            The number of bytes transferred so far for this file.
     * @param parentProduct
     *            The parent Product that this file reference belongs to.
     */
    public FileTransferStatus(Reference fileRef, long fileSize,
            long bytesTransferred, Product parentProduct) {
        this.fileRef = fileRef;
        this.bytesTransferred = bytesTransferred;
        this.parentProduct = parentProduct;
    }

    /**
     * @return Returns the bytesTransferred.
     */
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * @param bytesTransferred
     *            The bytesTransferred to set.
     */
    public void setBytesTransferred(long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    /**
     * @return Returns the fileRef.
     */
    public Reference getFileRef() {
        return fileRef;
    }

    /**
     * @param fileRef
     *            The fileRef to set.
     */
    public void setFileRef(Reference fileRef) {
        this.fileRef = fileRef;
    }

    /**
     * @return Returns the parentProduct.
     */
    public Product getParentProduct() {
        return parentProduct;
    }

    /**
     * @param parentProduct
     *            The parentProduct to set.
     */
    public void setParentProduct(Product parentProduct) {
        this.parentProduct = parentProduct;
    }

    /**
     * 
     * @return The percentage of the file that has been transferred so far.
     */
    public double computePctTransferred() {
        return ((double) (bytesTransferred * 1.0) / (fileRef.getFileSize() * 1.0));
    }

}

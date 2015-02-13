/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.oodt.cas.product.jaxrs.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A JAX-RS resource representing a {@link FileTransferStatus}.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name = "transfer")
@XmlType(propOrder = {"productName", "productId", "productTypeName",
  "dataStoreReference", "origReference", "mimeTypeName", "fileSize",
  "totalBytes", "bytesTransferred", "percentComplete", "productReceivedTime"})
@XmlAccessorType(XmlAccessType.NONE)
public class TransferResource
{
  private Reference reference;
  private Product product;
  private Metadata metadata;
  private FileTransferStatus status;



  /**
   * Default constructor required by JAXB.
   */
  public TransferResource()
  {
  }



  /**
   * Constructor that sets the {@link FileTransferStatus} for the resource.
   * @param product the product being transferred
   * @param metadata the metadata for the product being transferred
   * @param status the file transfer status for product
   */
  public TransferResource(Product product, Metadata metadata,
    FileTransferStatus status)
  {
    this.reference = status.getFileRef();
    this.product = product;
    this.metadata = metadata;
    this.status = status;
  }



  /**
   * Gets the product.
   * @return the product
   */
  public Product getProduct()
  {
    return product;
  }



  /**
   * Gets the metadata.
   * @return the metadata
   */
  public Metadata getMetadata()
  {
    return metadata;
  }



  /**
   * Gets the ID of the product.
   * @return the ID of the product
   */
  @XmlElement
  public String getProductId()
  {
    return product.getProductId();
  }



  /**
   * Gets the name of the product.
   * @return the name of the product
   */
  @XmlElement
  public String getProductName()
  {
    return product.getProductName();
  }



  /**
   * Gets the product type name for the product.
   * @return the product type name for the product
   */
  @XmlElement
  public String getProductTypeName()
  {
    return product.getProductType().getName();
  }



  /**
   * Gets the data store reference for the reference.
   * @return the data store reference for the reference
   */
  @XmlElement
  public String getDataStoreReference()
  {
    return reference.getDataStoreReference();
  }



  /**
   * Gets the original reference for the reference.
   * @return the original reference for the reference
   */
  @XmlElement
  public String getOrigReference()
  {
    return reference.getOrigReference();
  }



  /**
   * Gets the name of the MIME type for the reference.
   * @return the name of the MIME type for the reference
   */
  @XmlElement(name = "mimeType")
  public String getMimeTypeName()
  {
    return reference.getMimeType().getName();
  }



  /**
   * Gets the file size for the reference.
   * @return the file size for the reference
   */
  @XmlElement
  public long getFileSize()
  {
    return reference.getFileSize();
  }



  /**
   * Gets the total bytes of the file being transferred.
   * @return the total bytes of the file
   */
  @XmlElement
  public long getTotalBytes()
  {
    return status.getFileRef().getFileSize();
  }



  /**
   * Gets the bytes transferred status of the file transfer.
   * @return the bytes transferred status
   */
  @XmlElement
  public long getBytesTransferred()
  {
    return status.getBytesTransferred();
  }



  /**
   * Gets the percentage complete status of the file transfer.
   * @return the percentage complete status of the file transfer
   */
  @XmlElement
  public double getPercentComplete()
  {
    return status.computePctTransferred() * 100;
  }



  /**
   * Gets the metadata value of the product received time.
   * @return the metadata value of the product received time
   */
  @XmlElement
  public String getProductReceivedTime()
  {
    return metadata.getMetadata("CAS.ProductReceivedTime");
  }
}

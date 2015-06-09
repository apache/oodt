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

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.tika.mime.MimeType;

/**
 * A JAX-RS resource representing a {@link Reference}.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name = "reference")
@XmlType(propOrder = {"productId", "refIndex", "dataStoreReference",
  "origReference", "mimeTypeName", "fileSize"})
@XmlAccessorType(XmlAccessType.NONE)
public class ReferenceResource
{
  // The reference for this resource.
  private Reference reference;

  // The ID of the product that the reference belongs to.
  private String productId;

  // The index of the reference within its product.
  private int refIndex;

  // The file manager's working directory for this resource, used for example
  // when creating zip archives.
  private File workingDir;



  /**
   * Default constructor required by JAXB.
   */
  public ReferenceResource()
  {
  }



  /**
   * Constructor that sets the reference and working directory for the resource.
   * @param productId the ID of the product that the reference belongs to
   * @param refIndex the index of the reference within its product
   * @param reference the reference for the resource
   * @param workingDir the working directory for creating temporary files to
   * attach to responses
   */
  public ReferenceResource(String productId, int refIndex, Reference reference,
    File workingDir)
  {
    this.productId = productId;
    this.refIndex = refIndex;
    this.reference = reference;
    this.workingDir = workingDir;
  }



  /**
   * Gets the reference.
   * @return the reference
   */
  public Reference getReference()
  {
    return reference;
  }



  /**
   * Gets the working directory for the reference.
   * @return the working directory
   */
  public File getWorkingDir()
  {
    return workingDir;
  }



  /**
   * Gets the ID of the product that the reference belongs to.
   * @return the ID of the product that the reference belongs to
   */
  @XmlElement
  public String getProductId()
  {
    return productId;
  }



  /**
   * Gets the index of the reference within its product.
   * @return the index of the reference within its product
   */
  @XmlElement
  public int getRefIndex()
  {
    return refIndex;
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
   * Gets the file size for the reference.
   * @return the file size for the reference
   */
  @XmlElement
  public long getFileSize()
  {
    return reference.getFileSize();
  }



  /**
   * Gets the name of the MIME type for the reference.
   * @return the name of the MIME type for the reference
   */
  @XmlElement(name = "mimeType")
  public String getMimeTypeName()
  {
    MimeType m = reference.getMimeType();
    if (m != null)
    {
      return m.getName();
    }
    return null;
  }



  /**
   * Gets the MIME type for the reference.
   * @return the MIME type for the reference
   */
  public MimeType getMimeType()
  {
    return reference.getMimeType();
  }



  /**
   * Gets the original reference for the reference.
   * @return the original reference for the reference
   */
  @XmlElement(name = "originalReference")
  public String getOrigReference()
  {
    return reference.getOrigReference();
  }
}

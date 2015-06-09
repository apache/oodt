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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A JAX-RS resource representing a {@link Product}.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name = "product")
@XmlType(propOrder = {"productId", "productName", "productStructure",
  "productTypeName", "transferStatus", "metadataResource",
  "referenceResources"})
@XmlAccessorType(XmlAccessType.NONE)
public class ProductResource
{
  private Product product;
  private MetadataResource metadataResource;
  private List<ReferenceResource> referenceResources =
    new ArrayList<ReferenceResource>();

  // The file manager's working directory for this resource, used for example
  // when creating zip archives.
  private File workingDir;



  /**
   * Default constructor required by JAXB.
   */
  public ProductResource()
  {
  }



  /**
   * Constructor that sets the product, metadata, references and
   * working directory for the resource.
   * @param product the product associated with the resource
   * @param metadata the metadata associated with the resource
   * @param references the references associated with the resource
   * @param workingDir the working directory for creating temporary files to
   * attach to responses
   */
  public ProductResource(Product product, Metadata metadata,
    List<Reference> references, File workingDir)
  {
    this.product = product;
    this.metadataResource = new MetadataResource(metadata);
    for (int index = 0; index < references.size(); index++)
    {
      referenceResources.add(new ReferenceResource(product.getProductId(),
        index, references.get(index), workingDir));
    }
    this.workingDir = workingDir;
  }



  /**
   * Gets the working directory for the product.
   * @return the working directory
   */
  public File getWorkingDir()
  {
    return workingDir;
  }



  /**
   * Gets the product's ID.
   * @return the product's ID.
   */
  @XmlElement(name = "id")
  public String getProductId()
  {
    return product.getProductId();
  }



  /**
   * Gets the product's name.
   * @return the product's name
   */
  @XmlElement(name = "name")
  public String getProductName()
  {
    return product.getProductName();
  }



  /**
   * Gets the product's structure.
   * @return a string describing the product's structure
   */
  @XmlElement(name = "structure")
  public String getProductStructure()
  {
    return product.getProductStructure();
  }



  /**
   * Gets the name of the product's type.
   * @return the name of the product's type
   */
  @XmlElement(name = "type")
  public String getProductTypeName()
  {
    return product.getProductType().getName();
  }



  /**
   * Gets the transfer status for the product.
   * @return the transfer status for the product
   */
  @XmlElement(name = "transferStatus")
  public String getTransferStatus()
  {
    return product.getTransferStatus();
  }



  /**
   * Gets the metadata resource for the product.
   * @return the product's metadata resource
   */
  @XmlElement(name = "metadata")
  public MetadataResource getMetadataResource()
  {
    return metadataResource;
  }



  /**
   * Gets the reference resources.
   * @return the reference resources
   */
  @XmlElementWrapper(name = "references")
  @XmlElement(name = "reference")
  public List<ReferenceResource> getReferenceResources()
  {
    return referenceResources;
  }



  /**
   * Gets the product's references.
   * @return the product's references
   */
  public List<Reference> getProductReferences()
  {
    return product.getProductReferences();
  }
}

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

import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * A JAX-RS resource representing a dataset - a set of {@link org.apache.oodt.cas.filemgr.structs.Product products}
 * maintained by the file manager.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name="dataset")
@XmlType(propOrder = {"id", "name", "metadataResource", "productResources"})
@XmlAccessorType(XmlAccessType.NONE)
public class DatasetResource
{
  private String id;
  private String name;
  private MetadataResource metadataResource;
  private List<ProductResource> productResources =
    new ArrayList<ProductResource>();

  // The file manager's working directory for this resource, used for example
  // when creating zip archives.
  private File workingDir;



  /**
   * Default constructor required by JAXB.
   */
  public DatasetResource()
  {
  }



  /**
   * Constructor that sets the name, metadata and working directory for the
   * dataset resource.
   * @param id the ID of the dataset
   * @param name the name of the dataset
   * @param metadata the metadata for the dataset
   * @param workingDir the working directory for creating temporary files to
   * attach to responses
   */
  public DatasetResource(String id, String name, Metadata metadata,
    File workingDir)
  {
    this.id = id;
    this.name = name;
    this.metadataResource = new MetadataResource(metadata);
    this.workingDir = workingDir;
  }



  /**
   * Adds a {@link ProductResource} to the list of product resources for the
   * dataset.
   * @param resource the resource to add to the dataset.
   */
  public void addProductResource(ProductResource resource)
  {
    productResources.add(resource);
  }



  /**
   * Gets the working directory for the dataset.
   * @return the working directory
   */
  public File getWorkingDir()
  {
    return workingDir;
  }



  /**
   * Gets the id of the dataset.
   * @return the id of the dataset
   */
  @XmlElement
  public String getId()
  {
    return id;
  }



  /**
   * Gets the name of the dataset.
   * @return the name of the dataset
   */
  @XmlElement
  public String getName()
  {
    return name;
  }



  /**
   * Gets the product resources for the dataset.
   * @return the productResources
   */
  @XmlElementWrapper(name = "products")
  @XmlElement(name = "product")
  public List<ProductResource> getProductResources()
  {
    return productResources;
  }



  /**
   * Gets the metadata resource for the dataset.
   * @return the metadata resource
   */
  @XmlElement(name = "metadata")
  public MetadataResource getMetadataResource()
  {
    return metadataResource;
  }
}

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

import java.io.Serializable;
import java.util.Collections;
//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A ProductType is a type of content that will be ingested into the data store
 * element of the CAS File Manager. The {@link MetadataStore} also needs to know
 * about the ProductType, because {@link Element}s are associated with them to
 * define how the metadata is stored for a particular product.
 * </p>
 * 
 */
public class ProductType implements Serializable {

    /* the unique ID representing this product type */
    private String productTypeId = null;

    /* the name of this product type */
    private String name = null;

    /* a description of this product type */
    private String description = null;

    /* the path to the repository for this product type */
    private String productRepositoryPath = null;

    /*
     * the name of the class that implements the versioning scheme for this
     * product type
     */
    private String versioner = null;

    /* metadata describing the product type */
    private Metadata typeMetadata = null;

    /* list of {@link ExtractorSpec}s associated with this product type */
    private List<ExtractorSpec> extractors = null;

    private List<TypeHandler> handlers = null;
    
    public ProductType() {
        typeMetadata = new Metadata();
        extractors = new Vector<ExtractorSpec>();
    }

    public ProductType(String id, String name, String description,
            String repository, String versioner) {
        productTypeId = id;
        this.name = name;
        this.description = description;
        productRepositoryPath = repository;
        this.versioner = versioner;
        typeMetadata = new Metadata();
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the productTypeId.
     */
    public String getProductTypeId() {
        return productTypeId;
    }

    /**
     * @param productTypeId
     *            The productTypeId to set.
     */
    public void setProductTypeId(String productTypeId) {
        this.productTypeId = productTypeId;
    }

    /**
     * @return Returns the productRepositoryPath.
     */
    public String getProductRepositoryPath() {
        return productRepositoryPath;
    }

    /**
     * @param productRepositoryPath
     *            The productRepositoryPath to set.
     */
    public void setProductRepositoryPath(String productRepositoryPath) {
        this.productRepositoryPath = productRepositoryPath;
    }

    /**
     * @return Returns the versioner.
     */
    public String getVersioner() {
        return versioner;
    }

    /**
     * @param versioner
     *            The versioner to set.
     */
    public void setVersioner(String versioner) {
        this.versioner = versioner;
    }

    /**
     * @return the typeMetadata
     */
    public Metadata getTypeMetadata() {
        return typeMetadata;
    }

    /**
     * @param typeMetadata
     *            the typeMetadata to set
     */
    public void setTypeMetadata(Metadata typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    /**
     * @return the extractors
     */
    public List<ExtractorSpec> getExtractors() {
        return extractors;
    }

    /**
     * @param extractors
     *            the extractors to set
     */
    public void setExtractors(List<ExtractorSpec> extractors) {
        this.extractors = extractors;
    }

    public List<TypeHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<TypeHandler> handlers) {
        this.handlers = handlers;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.name;
    }
    
    public static ProductType blankProductType(){
      ProductType type = new ProductType();
      type.setDescription("blank");
      type.setExtractors(Collections.EMPTY_LIST);
      type.setHandlers(Collections.EMPTY_LIST);
      type.setName("blank");
      type.setProductRepositoryPath("");
      type.setProductTypeId("");
      type.setTypeMetadata(new Metadata());
      type.setVersioner("");
      return type;
    }


}

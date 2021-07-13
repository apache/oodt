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

import org.apache.oodt.cas.filemgr.structs.ProductType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Vector;

/**
 * A JAX-RS resource representing a list of {@link ProductType}.
 *
 * @author Pavindu 
 */
@XmlRootElement(name = "productTypeList")
public class ProductTypeListResource {
    /* the list of produdct types */
    private List<ProductType> productTypes = new Vector<ProductType>();;

    public ProductTypeListResource() {

    }

    public ProductTypeListResource(List<ProductType> productTypes) {
        this.productTypes = productTypes;
    }

    @XmlElement(name = "productTypes")
    public List<ProductType> getProductTypes(){
        return this.productTypes;
    }

    public void setProductTypes(List<ProductType> productTypes){
        this.productTypes = productTypes;
    }
}

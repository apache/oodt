/**
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

package org.apache.oodt.cas.webcomponents.filemgr.model;

//JDK imports
import java.io.Serializable;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.wicket.model.util.GenericBaseModel;

/**
 * 
 * A {@link Serializable} {@link GenericBaseModel} version of a CAS
 * {@link Product}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ProductModel extends GenericBaseModel<Product> {

  private static final long serialVersionUID = 2389290520013490102L;

  public ProductModel(Product product) {
    this.setObject(product);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.wicket.model.util.GenericBaseModel#createSerializableVersionOf
   * (java.lang.Object)
   */
  @Override
  protected Product createSerializableVersionOf(Product product) {
    SerializableProduct sp = new SerializableProduct();
    sp.setProductId(product.getProductId());
    sp.setProductName(product.getProductName());
    sp.setProductReferences(product.getProductReferences());
    sp.setProductStructure(product.getProductStructure());
    sp.setProductType(product.getProductType());
    sp.setRootRef(product.getRootRef());
    sp.setTransferStatus(product.getTransferStatus());
    return sp;
  }

  class SerializableProduct extends Product implements Serializable {

    private static final long serialVersionUID = 2052147876863984740L;

  }

}

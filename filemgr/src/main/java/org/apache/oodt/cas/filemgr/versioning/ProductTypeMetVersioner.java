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

package org.apache.oodt.cas.filemgr.versioning;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * A simple versioner that lets a user define the filePathSpec needed by the
 * {@link MetadataBasedFileVersioner} in a Product's ProductType metadata. Use
 * the key name filePathSpec, and then provide it for easily configurable use of
 * the {@link MetadataBasedFileVersioner}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ProductTypeMetVersioner extends MetadataBasedFileVersioner {

  public ProductTypeMetVersioner() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.filemgr.versioning.MetadataBasedFileVersioner#
   * createDataStoreReferences(org.apache.oodt.cas.filemgr.structs.Product,
   * org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public void createDataStoreReferences(Product product, Metadata metadata)
      throws VersioningException {
    setFilePathSpec(product.getProductType().getTypeMetadata()
        .getMetadata("filePathSpec"));
    super.createDataStoreReferences(product, metadata);
  }

}

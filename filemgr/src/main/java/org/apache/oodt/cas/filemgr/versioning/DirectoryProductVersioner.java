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
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;

/**
 * A {@link Versioner} for Hierarchical directory products.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class DirectoryProductVersioner extends MetadataBasedFileVersioner {

  /**
   * <p>
   * Default constructor
   * </p>
   */
  public DirectoryProductVersioner() {
    setFlatProducts(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.filemgr.versioning.MetadataBasedFileVersioner#
   * createDataStoreReferences(org.apache.oodt.cas.filemgr.structs.Product,
   * org.apache.oodt.cas.metadata.Metadata)
   */
  public void createDataStoreReferences(Product product, Metadata metadata)
      throws VersioningException {
    // first generate the first correct reference
    super.createDataStoreReferences(product, metadata);

    Reference root = (Reference) product.getProductReferences().get(0);
    if (!root.getDataStoreReference().endsWith("/")) {
      root.setDataStoreReference(root.getDataStoreReference() + "/");
    }

    // now add the heirarchical refs
    VersioningUtils.createBasicDataStoreRefsHierarchical(product
        .getProductReferences());

  }

}

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

//JDK imports
import java.util.logging.Logger;
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;

/**
 * 
 * A Inplace versioner that ingests the file without moving the actual file. It
 * just copies the file's orgin ref to its datastor ref.
 * 
 * @author davoodi
 * 
 */
public class InPlaceVersioner implements Versioner {

  /* our log stream */
  private static final Logger LOG = Logger.getLogger(InPlaceVersioner.class
      .getName());

  /**
     * 
     */
  public InPlaceVersioner() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.versioning.Versioner#createDataStoreReferences(org.
   * apache.oodt.cas.data.structs.Product)
   */
  public void createDataStoreReferences(Product product, Metadata metadata)
      throws VersioningException {
      for (Reference r : product.getProductReferences()) {
        r.setDataStoreReference(r.getOrigReference());
        LOG.log(Level.INFO, "in-place ingestion at datastore path: "
            + r.getDataStoreReference()
            + ".which is the same as the product's origin: "
            + r.getOrigReference());
      }

  }

}

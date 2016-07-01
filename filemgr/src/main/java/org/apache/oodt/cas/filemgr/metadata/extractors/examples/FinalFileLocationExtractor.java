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

package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

//JDK imports
import java.io.File;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.apache.oodt.cas.filemgr.versioning.VersioningUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

/**
 * 
 * Extracts the final <code>FILE_LOCATION</code> met field from the underlying
 * {@link Product} and sets it appropriately in the {@link Product}
 * {@link Metadata}.
 * 
 */
public class FinalFileLocationExtractor extends AbstractFilemgrMetExtractor
    implements CoreMetKeys {

  private boolean replaceLocation;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor
   * #doConfigure()
   */
  @Override
  public void doConfigure() {
    if (this.configuration != null) {
      this.replaceLocation = Boolean.parseBoolean(this.configuration
          .getProperty("replace"));
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor
   * #doExtract(org.apache.oodt.cas.filemgr.structs.Product,
   * org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public Metadata doExtract(Product product, Metadata met)
      throws MetExtractionException {
    Metadata extractMet = new Metadata();
    merge(met, extractMet);
    // get the Versioner
    Versioner versioner = GenericFileManagerObjectFactory
        .getVersionerFromClassName(product.getProductType().getVersioner());
    try {
      versioner.createDataStoreReferences(product, met);
    } catch (VersioningException e) {
      throw new MetExtractionException(
          "Unable to generate final FileLocation: Reason: " + e.getMessage());
    }

    Reference r = product.getProductReferences().get(0);
    String finalLocation = VersioningUtils.getAbsolutePathFromUri(r
        .getDataStoreReference());
    if (this.replaceLocation) {
      extractMet.replaceMetadata(FILE_LOCATION,
          new File(finalLocation).getParent());
    } else {
      extractMet
          .addMetadata(FILE_LOCATION, new File(finalLocation).getParent());
    }

    this.scrubRefs(product);

    return extractMet;

  }

  private void scrubRefs(Product p) {
    if (p.getProductReferences() == null) {
      return;
    }

    for (Reference r : p.getProductReferences()) {
      r.setDataStoreReference("");
    }
  }

}

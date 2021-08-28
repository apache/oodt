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

package org.apache.oodt.cas.filemgr.versioning;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.commons.util.DateConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//JDK imports
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A simple versioning scheme that versions {@link Product}s with their
 * production date time.
 * </p>
 * 
 */
public class DateTimeVersioner implements Versioner {

    /* our log stream */
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeVersioner.class);

    /**
     * <p>
     * Default Constructor
     * </p>
     */
    public DateTimeVersioner() {
        super();
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.versioning.Versioner#createDataStoreReferences(org.apache.
     *      oodt.cas.data.structs.Product)
     */
    public void createDataStoreReferences(Product product, Metadata metadata)
            throws VersioningException {
        // first, we need to know if its heirarchical, or flat
        if (product.getProductStructure() == null) {
            LOG.warn("DateTimeVersioner: Product Structure must be defined in order to version!");
            return;
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyddMM.HHmmss");
        String productionDateTime = metadata
                .getMetadata("CAS.ProductReceivedTime");
        if (productionDateTime == null) { // generate it ourselves then
            productionDateTime = dateFormatter.format(new Date());
        } else {
            Date prodDateTime;
            try {
                prodDateTime = DateConvert.isoParse(productionDateTime);
            } catch (ParseException e) {
                LOG.warn("Unable to parse production date time CAS.ProductReceivedTime: {}: {}. generating it ourselves", productionDateTime, e.getMessage(), e);
                prodDateTime = new Date();
            }
            productionDateTime = dateFormatter.format(prodDateTime);
        }

        if (product.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
            // if its flat, just return references that include
            // productTypeRepo/productName/fileName.productionDateTime
            // we'll use the format: yyyy.dd.MM.HH.mm.ss

          for (Reference r : product.getProductReferences()) {
            String dataStoreRef;

            try {
              dataStoreRef = new File(new URI(product.getProductType()
                                                     .getProductRepositoryPath())).toURI().toURL()
                                                                                  .toExternalForm()
                             + "/"
                             + product.getProductName()
                             + "/"
                             + new File(new URI(r.getOrigReference())).getName()
                             + "." + productionDateTime;
              LOG.info("DateTimeVersioner: Generated dataStoreRef: {} from original ref: {}", dataStoreRef, r.getOrigReference());
              r.setDataStoreReference(dataStoreRef);
            } catch (URISyntaxException e) {
              LOG.warn("DateTimeVersioner: URISyntaxException while generating DataStore ref for orig ref: {}: {}", r.getOrigReference(), e.getMessage(), e);
              // try and keep generating
            } catch (MalformedURLException e) {
              LOG.warn("DateTimeVersioner: MalformedURLException while getting URL for product repository path: {}: {}", product.getProductType().getProductRepositoryPath(), e.getMessage(), e);
              // try and keep generating
            }

          }
        } else if (product.getProductStructure().equals(Product.STRUCTURE_STREAM)) {
                VersioningUtils.createBasicDataStoreRefsStream(product.getProductName(), product.getProductType().getProductRepositoryPath(),
                        product.getProductReferences(), productionDateTime);
        } else if (product.getProductStructure().equals(
                Product.STRUCTURE_HIERARCHICAL)) {
            // if its heirarchical, then we'll version the files within the
            // directory
            // first, we need to get a list of all the references from the
            // original dir
            Reference origDirRef = (Reference) product.getProductReferences()
                    .get(0);

            try {
                String dataStoreRef = new File(new URI(product.getProductType()
                        .getProductRepositoryPath())).toURI().toURL().toExternalForm()
                        + URLEncoder.encode(product.getProductName(), "UTF-8")
                        + "/";
                LOG.info("DateTimeVersioner: generated DataStore ref: {} from origDirRef: {}", dataStoreRef, origDirRef.getOrigReference());
                origDirRef.setDataStoreReference(dataStoreRef);
            } catch (MalformedURLException e) {
                String msg = String.format("DateTimeVersioner (product: %s): MalformedURLException while generating initial DataStore ref for origRef: %s: %s", product.getProductName(), origDirRef.getOrigReference(), e.getMessage());
                LOG.warn(msg, e);
                throw new VersioningException(msg, e);
            } catch (URISyntaxException e) {
                String msg = String.format("DateTimeVersioner (product: %s): URISyntaxException while creating file ref from original dir URI: %s: %s", product.getProductName(), origDirRef.getOrigReference(), e.getMessage());
                LOG.warn(msg, e);
                throw new VersioningException(msg, e);
            } catch (UnsupportedEncodingException e) {
                String msg = String.format("DateTimeVersioner (product: %s): UnsupportedEncodingException while generating initial DataStore ref for origRef: %s: %s", product.getProductName(), origDirRef.getOrigReference(), e.getMessage());
                LOG.warn(msg, e);
                throw new VersioningException(msg, e);
            }

            // create the basic data store refs
            VersioningUtils.createBasicDataStoreRefsHierarchical(product
                    .getProductReferences());

            // add the production date time
            addProductDateTimeToReferences(product.getProductReferences(),
                    productionDateTime);

        }
    }

    private void addProductDateTimeToReferences(List<Reference> references,
            String productionDateTime) {
      for (Reference r : references) {
        if (!r.getOrigReference().endsWith("/")) {
          r.setDataStoreReference(r.getDataStoreReference() + "."
                                  + productionDateTime);
        }
      }
    }

}

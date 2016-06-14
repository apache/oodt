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

package org.apache.oodt.cas.filemgr.metadata.extractors;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.commons.util.DateConvert;

//JDK imports
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * The core {@link FilemgrMetExtractor} providing the {@link CoreMetKeys}
 * for this {@link Product}.
 * </p>.
 */
public class CoreMetExtractor extends AbstractFilemgrMetExtractor implements
        CoreMetKeys {

    private boolean namespaceAware = false;

    private String elementNs;

    private static final String nsSeparator = ".";

    private List<String> nsReplaceElements;

    public CoreMetExtractor() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.
     *      AbstractFilemgrMetExtractor#doExtract(org.apache.oodt.cas.filemgr.structs.Product,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public Metadata doExtract(Product product, Metadata met)
            throws MetExtractionException {
        Metadata extractMet = new Metadata();
        /* copy through original metadata */
        merge(met, extractMet);
        File prodFile = null;
        if (!product.getProductStructure().equals(Product.STRUCTURE_STREAM))
        {
            prodFile = getProductFile(product);
        }
        //GUID used when no file is associated with product i.e. stream
        String guid=UUID.randomUUID().toString();
        extractMet
                .addMetadata(isNsReplace(PRODUCT_ID) ? elementNs + nsSeparator
                        + PRODUCT_ID : PRODUCT_ID, product.getProductId());
        addMetadataIfUndefined(met, extractMet,
                isNsReplace(FILENAME) ? elementNs + nsSeparator + FILENAME
                        : FILENAME, (prodFile == null)?guid:prodFile.getName());
        addMetadataIfUndefined(met, extractMet,
                isNsReplace(FILE_LOCATION) ? elementNs + nsSeparator
                        + FILE_LOCATION : FILE_LOCATION, (prodFile==null)?guid:prodFile
                        .getParentFile().getAbsolutePath());
        addMetadataIfUndefined(met, extractMet,
                isNsReplace(PRODUCT_NAME) ? elementNs + nsSeparator
                        + PRODUCT_NAME : PRODUCT_NAME, product.getProductName());
        addMetadataIfUndefined(met, extractMet,
                isNsReplace(PRODUCT_STRUCTURE) ? elementNs + nsSeparator
                        + PRODUCT_STRUCTURE : PRODUCT_STRUCTURE, product
                        .getProductStructure());
        extractMet.addMetadata(isNsReplace(PRODUCT_RECEVIED_TIME) ? elementNs
                + nsSeparator + PRODUCT_RECEVIED_TIME : PRODUCT_RECEVIED_TIME,
                DateConvert.isoFormat(new Date()));
        addMetadataIfUndefined(met, extractMet,
                isNsReplace(PRODUCT_TYPE) ? elementNs + nsSeparator
                        + PRODUCT_TYPE : PRODUCT_TYPE, product.getProductType()
                        .getName());

        return extractMet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor#doConfigure()
     */
    public void doConfigure() {
        if (this.configuration != null) {
            namespaceAware = Boolean.valueOf(
                this.configuration.getProperty("nsAware"));

            if (namespaceAware) {
                elementNs = this.configuration.getProperty("elementNs");
                String replaceElemStr = this.configuration
                        .getProperty("elements");
                nsReplaceElements = Arrays.asList(replaceElemStr.split(","));
            }
        }
    }

    private boolean isNsReplace(String elemName) {
      return !(this.nsReplaceElements == null || (this.nsReplaceElements.size() == 0)) && namespaceAware
             && this.nsReplaceElements.contains(elemName);

    }

}

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

//JDK imports

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An abstract base class providing functionality to any sub-classing
 * {@link FilemgrMetExtractor}s.
 * </p>.
 */
public abstract class AbstractFilemgrMetExtractor implements
        FilemgrMetExtractor {
  private static Logger LOG = Logger.getLogger(AbstractFilemgrMetExtractor.class.getName());
    protected Properties configuration;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.
     *      FilemgrMetExtractor#extractMetadata(org.apache.oodt.cas.filemgr.structs.Product,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public Metadata extractMetadata(Product product, Metadata met)
            throws MetExtractionException {
        validateProduct(product, met);
        return doExtract(product, met);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor#configure(java.util.Properties)
     */
    public void configure(Properties props) {
        this.configuration = props;
        doConfigure();
    }

    public abstract Metadata doExtract(Product product, Metadata met)
            throws MetExtractionException;

    public abstract void doConfigure();

    protected void validateProduct(Product product, Metadata met)
            throws MetExtractionException {
        if (product.getProductType() == null || ((product
                                                      .getProductType().getName() == null || (product
                                                                                                  .getProductType()
                                                                                                  .getName()
                                                                                                  .equals(""))))) {
            throw new MetExtractionException("Product Type undefined");
        }

        if (product.getProductReferences() == null || (product
                                                           .getProductReferences().size() == 0)) {
            throw new MetExtractionException("Product references undefined");
        }


    }

    protected void addMetadataIfUndefined(Metadata origMet, Metadata destMet,
            String key, String val) {
        if (!origMet.containsKey(key)) {
            destMet.addMetadata(key, val);
        }
    }

    protected void merge(Metadata src, Metadata dest) {
        dest.addMetadata(src.getMap());
    }

    protected File getProductFile(Product product)
            throws MetExtractionException {
        File prodFile;

        if (product.getProductStructure()
                .equals(Product.STRUCTURE_HIERARCHICAL)) {
            try {
                prodFile = new File(getRootRefPath(product
                        .getProductReferences(), product.getProductType()
                        .getProductRepositoryPath()));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, e.getMessage());
                throw new MetExtractionException("URI exception parsing: ["
                        + product.getRootRef().getOrigReference() + "]");
            }
        } else {
            try {
                prodFile = new File(new URI(((Reference) product
                        .getProductReferences().get(0)).getOrigReference()));
            } catch (Exception e) {
                throw new MetExtractionException("URI exception parsing: ["
                        + ((Reference) product.getProductReferences().get(0))
                                .getOrigReference() + "]");

            }
        }

        return prodFile;
    }

    protected String getRootRefPath(List<Reference> refs,
            String productTypeRepoPath) throws URISyntaxException {
        return new File(new URI(refs.get(0)
                .getOrigReference())).getAbsolutePath();
    }

}

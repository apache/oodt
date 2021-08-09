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

//JDK imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A basic versioner that doesn't do anything special: it just creates data
 * store refs from product refs using the product name and product repo path.
 * </p>
 * 
 */
public class BasicVersioner implements Versioner {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(BasicVersioner.class.getName());

    /**
     * 
     */
    public BasicVersioner() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.versioning.Versioner#createDataStoreReferences(org.apache.oodt.cas.data.structs.Product)
     */
    public void createDataStoreReferences(Product product, Metadata metadata)
            throws VersioningException {

        String productName = product.getProductName();
        String productRepoPath = product.getProductType()
                .getProductRepositoryPath();

        if (product.getProductStructure()
                .equals(Product.STRUCTURE_HIERARCHICAL)) {

            if (product.getProductReferences() == null || (product.getProductReferences().size() == 0)) {
                throw new VersioningException(
                        "Hierarchical product and references not set!");
            }

            // get the first reference, it tells us what directory to move it
            // to
            // TODO: fix that hack :-)
            Reference r = (Reference) product.getProductReferences().get(0);

            String dataStoreRef;

            try {
                dataStoreRef = new File(new URI(productRepoPath)).toURI().toURL()
                        .toExternalForm();
                if(!dataStoreRef.endsWith("/")){
                  dataStoreRef+="/";
                }
                
                dataStoreRef+= URLEncoder.encode(productName, "UTF-8") + "/";
                LOG.log(Level.INFO, "BasicVersioner: generated DataStore ref: "
                        + dataStoreRef + " from origRef: "
                        + r.getOrigReference());
                r.setDataStoreReference(dataStoreRef);
                VersioningUtils.createBasicDataStoreRefsHierarchical(product
                        .getProductReferences());
            } catch (URISyntaxException e) {
                LOG.log(Level.WARNING,
                        "BasicVersioner: URISyntaxException while generating initial "
                                + "data store ref for origRef: "
                                + r.getOrigReference());
                throw new VersioningException(e);
            } catch (MalformedURLException e) {
                LOG.log(Level.WARNING,
                        "BasicVersioner: MalformedURLException while generating initial "
                                + "data store ref for origRef: "
                                + r.getOrigReference());
                throw new VersioningException(e);
            } catch (UnsupportedEncodingException e) {
                LOG.log(Level.WARNING,
                        "BasicVersioner: UnsupportedEncodingException while generating "
                                + "initial data store ref for origRef: "
                                + r.getOrigReference());
                throw new VersioningException(e);
            }

        } else if (product.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
            // just use the VersioningUtils
            VersioningUtils.createBasicDataStoreRefsFlat(productName,
                    productRepoPath, product.getProductReferences());
        } else if (product.getProductStructure().equals(Product.STRUCTURE_STREAM)) {
            VersioningUtils.createBasicDataStoreRefsStream(productName,
                    productRepoPath, product.getProductReferences(),"");
        } else {
            throw new VersioningException("Unsupported product structure: "
                    + product.getProductStructure());
        }

    }

}

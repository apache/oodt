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

package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//OODT static imports
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.*;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An example {@link FilemgrMetExtractor} to extract out a Product's 
 * Mime Type.
 * </p>.
 */
public class MimeTypeExtractor extends AbstractFilemgrMetExtractor {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor#doConfigure()
     */
    public void doConfigure() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor#doExtract(org.apache.oodt.cas.filemgr.structs.Product,
     *      org.apache.oodt.cas.metadata.Metadata)
     */
    public Metadata doExtract(Product product, Metadata met)
            throws MetExtractionException {
        Metadata extractMet = new Metadata();
        merge(met, extractMet);

        if (product.getProductStructure().equals(Product.STRUCTURE_FLAT)) {
            Reference prodRef = (Reference) product.getProductReferences().get(
                    0);

            extractMet.addMetadata(MIME_TYPE, prodRef.getMimeType().getName());
            extractMet.addMetadata(MIME_TYPE, prodRef.getMimeType()
                    .getType().getType());
            extractMet.addMetadata(MIME_TYPE, prodRef.getMimeType()
                    .getType().getSubtype());
        }

        return extractMet;
    }

}

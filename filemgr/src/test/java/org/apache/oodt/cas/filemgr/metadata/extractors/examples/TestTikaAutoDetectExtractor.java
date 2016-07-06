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

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URL;

import static junit.framework.Assert.*;


@RunWith(JUnit4.class)
public class TestTikaAutoDetectExtractor {

    @Test
    public void test() throws MetExtractionException {
        TikaAutoDetectExtractor tikaExtractor = new TikaAutoDetectExtractor();

        Metadata emptyMetadata = new Metadata();
        Reference ref = new Reference();
        URL file = this.getClass().getResource("/test.txt");
        ref.setOrigReference(file.toString());
        ref.setDataStoreReference(file.toString());

        Product product = new Product();
        product.getProductReferences().add(ref);
        product.setProductStructure(Product.STRUCTURE_FLAT);

        Metadata outputMetadata = tikaExtractor.doExtract(product, emptyMetadata);

        assertNotNull(outputMetadata);
        assertTrue(outputMetadata.getAllKeys().size() > 0);
        assertTrue(outputMetadata.containsKey("X-Parsed-By"));
        assertFalse(outputMetadata.getMetadata("X-Parsed-By").equals("org.apache.tika.parser.EmptyParser"));
    }
}

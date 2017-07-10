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

//JDK imports
import java.net.URL;
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.FileAttributesMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author adhulipala
 * @version $Revision$
 * @since OODT-847
 * <p>
 *  Test suite for the {@link FileAttributesExtractor}.
 * </p>.
 */
public class TestFileAttributesExtractor extends TestCase implements FileAttributesMetKeys {

    private final FileAttributesExtractor fileAttributesExtractor = new FileAttributesExtractor();

    public void testExtractBasicFileAttributes() {
        // Define Reference & Product
        URL refUrl = this.getClass().getResource("/ingest/test.txt");
        Reference ref = new Reference();
        ref.setOrigReference(refUrl.toString());
        ref.setDataStoreReference(refUrl.toString());

        Product product = new Product();
        product.getProductReferences().add(ref);
        product.setProductStructure(Product.STRUCTURE_FLAT);

        // Define configuration to extract basic file attributes
        Properties config = new Properties();
        config.setProperty("attributes", "*");

        // Configure Extractor
        fileAttributesExtractor.configure(config);

        // Extract file attributes metadata
        Metadata metadata = new Metadata();

        try {
            metadata = fileAttributesExtractor.doExtract(product, metadata);

        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        // Test
        assertNotNull(metadata);
        assertTrue(metadata.containsKey(IS_SYMBOLIC_LINK));
        assertTrue(metadata.containsKey(CREATION_TIME));
        assertTrue(metadata.containsKey(LAST_MODIFIED_TIME));
        assertTrue(metadata.containsKey(IS_OTHER));
        assertTrue(metadata.containsKey(IS_DIRECTORY));
        assertTrue(metadata.containsKey(FILE_KEY));
        assertTrue(metadata.containsKey(LAST_ACCESS_TIME));
        assertTrue(metadata.containsKey(IS_REGULAR_FILE));
        assertTrue(metadata.containsKey(SIZE));
    }

    public void testExtractPosixFileAttributes() {
        // Define Reference & Product
        URL refUrl = this.getClass().getResource("/ingest/test.txt");
        Reference ref = new Reference();
        ref.setOrigReference(refUrl.toString());
        ref.setDataStoreReference(refUrl.toString());

        Product product = new Product();
        product.getProductReferences().add(ref);
        product.setProductStructure(Product.STRUCTURE_FLAT);

        // Define configuration to extract basic file attributes
        Properties config = new Properties();
        config.setProperty("attributes", "posix:*");

        // Configure Extractor
        fileAttributesExtractor.configure(config);

        // Extract file attributes metadata
        Metadata metadata = new Metadata();

        try {
            metadata = fileAttributesExtractor.doExtract(product, metadata);
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        // Test
        assertNotNull(metadata);
        assertTrue(metadata.containsKey(OWNER));
        assertTrue(metadata.containsKey(PERMISSIONS));
        assertTrue(metadata.containsKey(GROUP));
    }

    public void testExtractSpecificFileAttributes() {
        // Define Reference & Product
        URL refUrl = this.getClass().getResource("/ingest/test.txt");
        Reference ref = new Reference();
        ref.setOrigReference(refUrl.toString());
        ref.setDataStoreReference(refUrl.toString());

        Product product = new Product();
        product.getProductReferences().add(ref);
        product.setProductStructure(Product.STRUCTURE_FLAT);

        // Define configuration to extract basic file attributes
        Properties config = new Properties();
        config.setProperty("attributes", "posix:size,owner,creationTime");

        // Configure Extractor
        fileAttributesExtractor.configure(config);

        // Extract file attributes metadata
        Metadata metadata = new Metadata();

        try {
            metadata = fileAttributesExtractor.doExtract(product, metadata);
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        System.out.println(metadata.getAllKeys());
        System.out.println(metadata.containsKey(OWNER));
        System.out.println(metadata.containsKey(IS_SYMBOLIC_LINK));

        // Test presence of expected attributes
        assertNotNull(metadata);
        assertTrue(metadata.containsKey(OWNER));
        assertTrue(metadata.containsKey(SIZE));
        assertTrue(metadata.containsKey(CREATION_TIME));

        // Test absence of other attributes
        assertFalse(metadata.containsKey(IS_SYMBOLIC_LINK));
        assertFalse(metadata.containsKey(LAST_MODIFIED_TIME));
        assertFalse(metadata.containsKey(IS_OTHER));
        assertFalse(metadata.containsKey(IS_DIRECTORY));
        assertFalse(metadata.containsKey(FILE_KEY));
        assertFalse(metadata.containsKey(LAST_ACCESS_TIME));
        assertFalse(metadata.containsKey(IS_REGULAR_FILE));

    }
}

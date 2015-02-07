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
import java.util.List;
import java.util.Properties;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link CoreMetExtractor}.
 * </p>.
 */
public class TestCoreMetExtractor extends TestCase implements CoreMetKeys {

    private CoreMetExtractor extractor = new CoreMetExtractor();
    
    public TestCoreMetExtractor(){
        Properties config = new Properties();
        config.setProperty("nsAware", String.valueOf(false));
        config.setProperty("elementNs", "CAS");
        extractor.configure(config);
    }

    public void testExtract() {
        Reference ref = new Reference();
        ref.setOrigReference("file:/foo/bar/file.ext");
        ref.setDataStoreReference("file:/foo/bar/final/file.ext");

        List refs = new Vector();
        refs.add(ref);

        Product product = new Product();
        product.setProductId("1");
        product.setProductName("foo");
        product.setProductReferences(refs);
        product.setProductStructure(Product.STRUCTURE_FLAT);
        product.setRootRef(ref);

        ProductType type = new ProductType();
        type.setName("FooType");
        product.setProductType(type);

        Metadata met = new Metadata();
        try {
            met = extractor.doExtract(product, met);
        } catch (MetExtractionException e) {
            fail(e.getMessage());
        }

        assertNotNull(met);
        assertTrue(met.containsKey(FILENAME));
        assertTrue(met.containsKey(FILE_LOCATION));
        assertTrue(met.containsKey(PRODUCT_TYPE));
        assertTrue(met.containsKey(PRODUCT_STRUCTURE));
        assertTrue(met.containsKey(PRODUCT_ID));
        assertTrue(met.containsKey(PRODUCT_NAME));
        assertTrue(met.containsKey(PRODUCT_RECEVIED_TIME));

        assertEquals(met.getMetadata(FILENAME), "file.ext");
        assertEquals(met.getMetadata(FILE_LOCATION), "/foo/bar");
        assertEquals(met.getMetadata(PRODUCT_TYPE), "FooType");
        assertEquals(met.getMetadata(PRODUCT_STRUCTURE), Product.STRUCTURE_FLAT);
        assertEquals(met.getMetadata(PRODUCT_ID), "1");
        assertEquals(met.getMetadata(PRODUCT_NAME), "foo");

    }
    
    public void testExtractDirectoryProduct(){
      Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
      p.setProductId("1");
      p.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
      p.setProductName("somedir");
      p.getProductType().setProductRepositoryPath("file:///archive/dirs");
      p.getProductType().setName("GenericFile");
      p.getProductType().setVersioner(
          "org.apache.oodt.cas.filemgr.versioning.DirectoryProductVersioner");
      p.getProductReferences()
          .add(new Reference("file:///tmp/somedir", null, 4L));
      p.getProductReferences().add(
          new Reference("file:///tmp/somedir/file1.txt", null, 8L));
      p.getProductReferences().add(
          new Reference("file:///tmp/somedir/file2.txt", null, 8L));

      Metadata met = new Metadata();
      try {
          met = extractor.doExtract(p, met);
      } catch (MetExtractionException e) {
          fail(e.getMessage());
      }

      assertNotNull(met);
      assertTrue(met.containsKey(FILENAME));
      assertTrue(met.containsKey(FILE_LOCATION));
      assertTrue(met.containsKey(PRODUCT_TYPE));
      assertTrue(met.containsKey(PRODUCT_STRUCTURE));
      assertTrue(met.containsKey(PRODUCT_ID));
      assertTrue(met.containsKey(PRODUCT_NAME));
      assertTrue(met.containsKey(PRODUCT_RECEVIED_TIME));

      assertEquals(met.getMetadata(FILENAME), "somedir");
      assertEquals(met.getMetadata(FILE_LOCATION), "/tmp");
      assertEquals(met.getMetadata(PRODUCT_TYPE), "GenericFile");
      assertEquals(met.getMetadata(PRODUCT_STRUCTURE), Product.STRUCTURE_HIERARCHICAL);
      assertEquals(met.getMetadata(PRODUCT_ID), "1");
      assertEquals(met.getMetadata(PRODUCT_NAME), "somedir");
      
      
    }
}

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
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.extractors.examples.FilenameRegexMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * @author nchung
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link FilenameRegexMetExtractor}.
 * </p>.
 */
public class TestFilenameRegexMetExtractor extends TestCase {

   public void testExtract() {
      Properties config = new Properties();
      config.setProperty("filenamePattern", "(\\w*)_(\\d*)\\.txt");
      config.setProperty("metadataKeys", "Name,ID");
      FilenameRegexMetExtractor extractor = new FilenameRegexMetExtractor();
      extractor.configure(config);

      Reference ref = new Reference();
      ref.setOrigReference("file:/foo/bar/foobar_001.txt");
      ref.setDataStoreReference("file:/foo/bar/final/foobar_001.txt");

      Product product = new Product();
      product.getProductReferences().add(ref);
      product.setProductStructure(Product.STRUCTURE_FLAT);

      Metadata met = new Metadata();
      try {
         met = extractor.doExtract(product, met);
      } catch (MetExtractionException e) {
         fail(e.getMessage());
      }

      assertTrue(met.containsKey("Name"));
      assertTrue(met.containsKey("ID"));

      assertEquals("foobar", met.getMetadata("Name"));
      assertEquals("001", met.getMetadata("ID"));
   }
}

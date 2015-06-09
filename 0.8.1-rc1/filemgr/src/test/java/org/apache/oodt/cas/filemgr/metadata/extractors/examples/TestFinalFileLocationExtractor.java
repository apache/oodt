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
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test suite for the {@link FinalFileLocationExtractor}.
 * 
 * @since OODT-72
 * 
 */
public class TestFinalFileLocationExtractor extends TestCase {

  public void testExtract() {
    String expectedFinalLocation = "/archive/somefile.txt";
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.setProductName("somefile.txt");
    p.getProductType().setProductRepositoryPath("file:///archive");
    p.getProductType().setVersioner(
        "org.apache.oodt.cas.filemgr.versioning.BasicVersioner");
    p.getProductReferences().add(
        new Reference("file:///tmp/somefile.txt", null, 0L));
    Properties config = new Properties();
    config.setProperty("replace", "false");
    Metadata met = new Metadata();
    met.addMetadata(CoreMetKeys.FILE_LOCATION, "/tmp");
    FinalFileLocationExtractor extractor = new FinalFileLocationExtractor();
    extractor.configure(config);
    Metadata extractMet = new Metadata();

    try {
      extractMet = extractor.doExtract(p, met);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(extractMet);
    assertTrue(extractMet.containsKey(CoreMetKeys.FILE_LOCATION));
    assertEquals(2, extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
    assertEquals("/tmp", extractMet.getMetadata(CoreMetKeys.FILE_LOCATION));
    assertEquals("expected final location: [" + expectedFinalLocation
        + "] is not equal to generated location: ["
        + extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).get(1) + "]",
        expectedFinalLocation,
        extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).get(1));

    // ensure that the data store ref is blank
    assertEquals("", p.getProductReferences().get(0).getDataStoreReference());

    // reconfigure to replace
    config.setProperty("replace", "true");
    extractor.configure(config);

    try {
      extractMet = extractor.doExtract(p, met);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(extractMet);
    assertTrue(extractMet.containsKey(CoreMetKeys.FILE_LOCATION));
    assertEquals(1, extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
    assertEquals(expectedFinalLocation,
        extractMet.getMetadata(CoreMetKeys.FILE_LOCATION));

    // ensure that the data store ref is blank
    assertEquals("", p.getProductReferences().get(0).getDataStoreReference());

  }

  /**
   * @since OODT-200
   */
  public void testExtractHierarchical() {
    String expectedFinalLocation = "/archive/dirs";
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
    p.setProductName("somedir");
    p.getProductType().setProductRepositoryPath("file:///archive/dirs");
    p.getProductType().setVersioner(
        "org.apache.oodt.cas.filemgr.versioning.DirectoryProductVersioner");
    p.getProductReferences()
        .add(new Reference("file:///tmp/somedir", null, 4L));
    p.getProductReferences().add(
        new Reference("file:///tmp/somedir/file1.txt", null, 8L));
    p.getProductReferences().add(
        new Reference("file:///tmp/somedir/file2.txt", null, 8L));
    Properties config = new Properties();
    config.setProperty("replace", "false");
    Metadata met = new Metadata();
    met.addMetadata(CoreMetKeys.FILE_LOCATION, "/tmp");
    FinalFileLocationExtractor extractor = new FinalFileLocationExtractor();
    extractor.configure(config);
    Metadata extractMet = new Metadata();

    try {
      extractMet = extractor.doExtract(p, met);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(extractMet);
    assertTrue(extractMet.containsKey(CoreMetKeys.FILE_LOCATION));
    assertEquals(2, extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
    assertEquals("/tmp", extractMet.getMetadata(CoreMetKeys.FILE_LOCATION));
    assertEquals("expected final location: [" + expectedFinalLocation
        + "] is not equal to generated location: ["
        + extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).get(1) + "]",
        expectedFinalLocation,
        extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).get(1));

    // ensure that the data store ref is blank
    assertEquals("", p.getProductReferences().get(0).getDataStoreReference());

    // reconfigure to replace
    config.setProperty("replace", "true");
    extractor.configure(config);

    try {
      extractMet = extractor.doExtract(p, met);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(extractMet);
    assertTrue(extractMet.containsKey(CoreMetKeys.FILE_LOCATION));
    assertEquals(1, extractMet.getAllMetadata(CoreMetKeys.FILE_LOCATION).size());
    assertEquals(expectedFinalLocation,
        extractMet.getMetadata(CoreMetKeys.FILE_LOCATION));

    // ensure that the data store ref is blank
    assertEquals("", p.getProductReferences().get(0).getDataStoreReference());

  }

}

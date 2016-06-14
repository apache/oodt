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


package org.apache.oodt.cas.filemgr.validation;

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.*;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Vector;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Unit tests for the XMLValidationLayer.
 * </p>
 * 
 */
public class TestXMLValidationLayer extends TestCase {

    /* our test URIs containing elements.xml and product-type-element-map.xml */
    private List testDirUris = new Vector();

    /* our XML Validation layer for testing */
    private XMLValidationLayer validationLayer = null;

    private static final String expectedProductIdDefinition = "A Product's unique identifier within the CAS namespace.";

    private static final String expectedProductNameDefinition = "\n          A Product's name within the CAS namespace.\n        ";

    private static final String expectedProductReceivedTime = "The ISO 8601 formatted time that the Product was received.";

    private static final String expectedFilenameDefinition = "The names of the files that represent this product.";

    private static final String CAS_NS = "CAS";

    public TestXMLValidationLayer() {
    }

    /**
     * @since OODT-195
     */
    public void testModifyElement() {
      Element elem = new Element();
      String elemName = "TestFilename";
      elem.setElementName(elemName);
      elem.setElementId("urn:oodt:Filename");
      try {
        validationLayer.modifyElement(elem);
      } catch (Exception e) {
        fail(e.getMessage());
      }
  
      ProductType type = new ProductType();
      type.setProductTypeId("urn:oodt:GenericFile");
      List<Element> retrievedElems = null;
      try {
        retrievedElems = validationLayer.getElements(type);
      } catch (Exception e) {
        fail(e.getMessage());
      }
      assertNotNull(retrievedElems);
      boolean found = false;
      for (Element e : retrievedElems) {
        if (e.getElementName().equals(elemName)) {
          found = true;
        }
      }
      assertTrue(
          "Unable to find updated element: ["+elemName+"]: Set contains : ["
              + retrievedElems + "]", found);
  }    

    /**
     * @since OODT-220
     * 
     */
    public void testReadProperDescriptionTrimImplicitTrue() {
        Element elem = null;
        try {
            elem = validationLayer.getElementByName(CAS_NS + "." + PRODUCT_ID);
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        assertNotNull(elem);
        assertNotNull(elem.getDescription());
        assertEquals(expectedProductIdDefinition, elem.getDescription());
    }

    /**
     * @since OODT-220
     * 
     */
    public void testReadBadFormattedDescriptionTrimImplicitTrue() {
        Element elem = null;
        try {
            elem = validationLayer.getElementByName(FILENAME);
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        assertNotNull(elem);
        assertNotNull(elem.getDescription());
        assertEquals(expectedFilenameDefinition, elem.getDescription());
    }

    /**
     * @since OODT-220
     * 
     */
    public void testReadDescriptionTrimExplicitFalse() {
        Element elem = null;
        try {
            elem = validationLayer
                    .getElementByName(CAS_NS + "." + PRODUCT_NAME);
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        assertNotNull(elem);
        assertNotNull(elem.getDescription());
        assertEquals(expectedProductNameDefinition, elem.getDescription());

    }

    /**
     * @since OODT-220
     * 
     */
    public void testReadDescriptionTrimExplicitTrue() {
        Element elem = null;
        try {
            elem = validationLayer.getElementByName(CAS_NS + "."
                    + PRODUCT_RECEVIED_TIME);
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        assertNotNull(elem);
        assertNotNull(elem.getDescription());
        assertEquals(expectedProductReceivedTime, elem.getDescription());

    }

    public void testGetElements() {
        List elementList = null;

        try {
            elementList = validationLayer.getElements();
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        // should be 4 elements
        assertNotNull(elementList);
        assertEquals("There aren't exactly 4 elements in the test samples!", 4,
                elementList.size());

        // try and find one of them
        // find produuct received time
        boolean hasReceivedTime = false;
      for (Object anElementList : elementList) {
        Element element = (Element) anElementList;
        if (element.getElementName().equals("CAS.ProductReceivedTime")) {
          hasReceivedTime = true;
        }
      }

        if (!hasReceivedTime) {
            fail("Didn't load the CAS.ProductReceivedTime element!");
        }
    }

    public void testGetElementsForProductType() {
        List elementList = null;
        ProductType type = new ProductType();

        type.setProductTypeId("urn:oodt:GenericFile");
        type.setName("Generic File");

        try {
            elementList = validationLayer.getElements(type);
        } catch (ValidationLayerException e) {
            fail(e.getMessage());
        }

        // should be exactly 4 elements
        assertNotNull(elementList);
        assertEquals(
                "There aren't exactly 4 elements for the product type [Generic File]!",
                4, elementList.size());
    }




    /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    try {
      File tempDir = File.createTempFile("ignore", "txt").getParentFile();
      // copy the val layer policy into the temp dir
      URL url = this.getClass().getResource("/vallayer");
      for (File f : new File(url.getFile())
          .listFiles(new FileFilter() {

            public boolean accept(File pathname) {
              return pathname.isFile();
            }
          })) {
        FileUtils.copyFileToDirectory(f, tempDir);
      }

      testDirUris.add(tempDir.toURI().toString());
      validationLayer = new XMLValidationLayer(testDirUris);

    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    deleteAllFiles(new File(new URI((String) testDirUris.get(0)))
        .getAbsolutePath());
    testDirUris.clear();
  }

  private void deleteAllFiles(String startDir) {
    File startDirFile = new File(startDir);
    File[] delFiles = startDirFile.listFiles();

    if (delFiles != null && delFiles.length > 0) {
      for (File delFile : delFiles) {
        delFile.delete();
      }
    }

    startDirFile.delete();

  }    
}

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


package gov.nasa.jpl.oodt.cas.filemgr.validation;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.Element;
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import static gov.nasa.jpl.oodt.cas.filemgr.metadata.CoreMetKeys.*;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;

//JDK imports
import java.io.File;
import java.util.Iterator;
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

    /**
     * 
     */
    public TestXMLValidationLayer() {
        testDirUris.add(new File("./src/testdata/vallayer").toURI().toString());
        validationLayer = new XMLValidationLayer(testDirUris);
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
        for (Iterator i = elementList.iterator(); i.hasNext();) {
            Element element = (Element) i.next();
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

}

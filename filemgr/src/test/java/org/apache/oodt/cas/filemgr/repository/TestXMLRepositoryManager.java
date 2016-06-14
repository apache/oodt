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


package org.apache.oodt.cas.filemgr.repository;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.commons.exec.EnvUtilities;

//JDK imports
import java.io.File;
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
 * Test case class for the XMLRepositoryManager.
 * </p>
 * 
 */
public class TestXMLRepositoryManager extends TestCase {

    /* URI pointers to our test product-types.xml directories */
    private List productTypeDirUris = new Vector();

    /* our repository manager to test */
    private XMLRepositoryManager repositoryManager = null;

    private static final String expectedGenericFileBlankMetDesc = "The default product type for any kind of file, with blank metadata.";

    private static final String expectedGenericFileDesc = "The default product type for any kind of file, with real metadata.";

    private static final String expectedGenericFileNoMetDesc = "\n      The default product type for any kind of file, without metadata.\n    ";

    private static final String expectedVersionerClassName = "org.apache.oodt.cas.filemgr.versioning.BasicVersioner";

    private static final String expectedRepPath = "file://"
            + EnvUtilities.getEnv("HOME") + "/files";

    /**
     * 
     */
    public TestXMLRepositoryManager() {
        URL url = this.getClass().getResource("/repomgr");
        productTypeDirUris.add(new File(url.getFile()).toURI().toString());
        try {
            repositoryManager = new XMLRepositoryManager(productTypeDirUris);
        } catch (InstantiationException e) {
            fail(e.getMessage());
        }
    }

    public void testRepoPathEnvVarReplace() {
        List productTypes = null;

        try {
            productTypes = repositoryManager.getProductTypes();
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        ProductType type = ((ProductType) productTypes.get(0));
        String expectedPath = "file://" + System.getenv("HOME") + "/files";
        assertEquals("The repo path: [" + type.getProductRepositoryPath()
                + "] is " + "not equal to the exptected path: [" + expectedPath
                + "]", expectedPath, type.getProductRepositoryPath());
    }

    public void testGetProductTypes() {
        List productTypes = null;

        try {
            productTypes = repositoryManager.getProductTypes();
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        // should be exactly 3 product types
        assertNotNull(productTypes);
        assertEquals(3, productTypes.size());
    }

    public void testGetProductTypeByName() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeByName("GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        // should be not null and name should be generic file
        assertNotNull(type);
        assertEquals("The type name: [" + type.getName()
                + "] is not \"GenericFile\"", type.getName(), "GenericFile");
    }

    public void testGetProductTypeById() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeById("urn:oodt:GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        // should be not null and should be generic file
        assertNotNull(type);
        assertEquals("The type id: [" + type.getProductTypeId()
                + "] is not \"urn:oodt:GenericFile\"", type.getProductTypeId(),
                "urn:oodt:GenericFile");

    }

    /**
     * @since OODT-69
     * 
     */
    public void testReadProductTypeWithMetadataWithBlankMet() {
        ProductType type = null;

        try {
            type = repositoryManager
                    .getProductTypeByName("GenericFileWithBlankMet");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getTypeMetadata());

    }

    /**
     * @since OODT-69
     * 
     */
    public void testReadProductTypeWithMetadataWithNoMet() {
        ProductType type = null;

        try {
            type = repositoryManager
                    .getProductTypeByName("GenericFileNoTypeMetadata");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getTypeMetadata());
	assertEquals(0, type.getTypeMetadata().getKeys().size());
    }

    /**
     * @since OODT-69
     * 
     */
    public void testReadProductTypeWithMetadata() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeByName("GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getTypeMetadata());

        assertTrue(type.getTypeMetadata().containsKey("Creator"));
        assertTrue(type.getTypeMetadata().containsKey("Contributor"));
        assertEquals(2, type.getTypeMetadata().getMap().keySet().size());
        assertNotNull(type.getTypeMetadata().getAllMetadata("Creator"));
        assertEquals(2, type.getTypeMetadata().getAllMetadata("Creator").size());
        assertNotNull(type.getTypeMetadata().getAllMetadata("Contributor"));
        assertEquals(1, type.getTypeMetadata().getAllMetadata("Contributor")
                .size());

        List contribValues = type.getTypeMetadata().getAllMetadata(
                "Contributor");
        assertEquals("File Manager Client", (String) contribValues.get(0));

        List creatorValues = type.getTypeMetadata().getAllMetadata("Creator");
        boolean hasFirstCreator = false, hasSecondCreator = false;

        for (Object creatorValue : creatorValues) {
            String val = (String) creatorValue;
            if (val.equals("Chris Mattmann")) {
                hasFirstCreator = true;
            } else if (val.equals("Paul Ramirez")) {
                hasSecondCreator = true;
            }
        }

        assertTrue(hasFirstCreator && hasSecondCreator);
    }

    /**
     * @since OODT-219
     * 
     */
    public void testReadBadFormattedDescriptionTrimImplicitTrue() {
        ProductType type = null;

        try {
            type = repositoryManager
                    .getProductTypeByName("GenericFileWithBlankMet");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getDescription());
        assertEquals(expectedGenericFileBlankMetDesc, type.getDescription());
    }

    /**
     * @since OODT-219
     * 
     */
    public void testReadDescriptionTrimExplicitFalse() {
        ProductType type = null;

        try {
            type = repositoryManager
                    .getProductTypeByName("GenericFileNoTypeMetadata");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getDescription());
        assertEquals(expectedGenericFileNoMetDesc, type.getDescription());

    }

    /**
     * @since OODT-219
     * 
     */
    public void testReadDescriptionTrimExplicitTrue() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeByName("GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getDescription());
        assertEquals(expectedGenericFileDesc, type.getDescription());

    }

    /**
     * @since OODT-219
     * 
     */
    public void testReadVersionerClass() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeByName("GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getVersioner());
        assertEquals(expectedVersionerClassName, type.getVersioner());

    }

    /**
     * @since OODT-219
     * 
     */
    public void testReadRepoPath() {
        ProductType type = null;

        try {
            type = repositoryManager.getProductTypeByName("GenericFile");
        } catch (RepositoryManagerException e) {
            fail(e.getMessage());
        }

        assertNotNull(type);
        assertNotNull(type.getProductRepositoryPath());
        assertEquals(expectedRepPath, type.getProductRepositoryPath());
    }
}

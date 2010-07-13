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


package gov.nasa.jpl.oodt.cas.filemgr.util;

//OODT imports
import java.io.File;
import java.io.FileInputStream;

import gov.nasa.jpl.oodt.cas.filemgr.catalog.Catalog;
import gov.nasa.jpl.oodt.cas.filemgr.datatransfer.DataTransfer;
import gov.nasa.jpl.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import gov.nasa.jpl.oodt.cas.filemgr.repository.RepositoryManager;
import gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayer;
import gov.nasa.jpl.oodt.cas.filemgr.versioning.Versioner;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class TestGenericFileManagerObjectStructFactory extends TestCase {

    private static final String dataTransferServiceFactory = "gov.nasa.jpl.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

    private static final String validationServiceFactory = "gov.nasa.jpl.oodt.cas.filemgr.validation.XMLValidationLayerFactory";

    private static final String catalogServiceFactory = "gov.nasa.jpl.oodt.cas.filemgr.catalog.DataSourceCatalogFactory";

    private static final String repositoryServiceFactory = "gov.nasa.jpl.oodt.cas.filemgr.repository.XMLRepositoryManagerFactory";

    private static final String versionerClass = "gov.nasa.jpl.oodt.cas.filemgr.versioning.BasicVersioner";

    private static final String extractorClass = "gov.nasa.jpl.oodt.cas.filemgr.metadata.extractors.CoreMetExtractor";

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        // set the log levels
        System.setProperty("java.util.logging.config.file", new File(
                "./src/main/resources/logging.properties").getAbsolutePath());

        // first load the example configuration
        try {
            System.getProperties().load(
                    new FileInputStream("./src/main/resources/filemgr.properties"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testGetDataTransferServiceFromFactory() {
        DataTransfer transferer = GenericFileManagerObjectFactory
                .getDataTransferServiceFromFactory(dataTransferServiceFactory);
        assertNotNull(transferer);
    }

    public void testGetRepositoryManagerServiceFromFactory() {
        RepositoryManager repMgr = GenericFileManagerObjectFactory
                .getRepositoryManagerServiceFromFactory(repositoryServiceFactory);
        assertNotNull(repMgr);

    }

    public void testGetCatalogServiceFromFactory() {
        Catalog cat = GenericFileManagerObjectFactory
                .getCatalogServiceFromFactory(catalogServiceFactory);
        assertNotNull(cat);

    }

    public void testGetValidationLayerFromFactory() {
        ValidationLayer valLayer = GenericFileManagerObjectFactory
                .getValidationLayerFromFactory(validationServiceFactory);
        assertNotNull(valLayer);

    }

    public void testGetVersionerFromClassName() {
        Versioner versioner = GenericFileManagerObjectFactory
                .getVersionerFromClassName(versionerClass);
        assertNotNull(versioner);

    }

    public void testGetExtractorFromClassName() {
        FilemgrMetExtractor extractor = GenericFileManagerObjectFactory
                .getExtractorFromClassName(extractorClass);
        assertNotNull(extractor);

    }

}

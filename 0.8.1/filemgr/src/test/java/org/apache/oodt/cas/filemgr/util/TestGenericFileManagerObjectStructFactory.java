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


package org.apache.oodt.cas.filemgr.util;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.filemgr.versioning.Versioner;

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

    private static final String dataTransferServiceFactory = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

    private static final String validationServiceFactory = "org.apache.oodt.cas.filemgr.validation.XMLValidationLayerFactory";

    private static final String catalogServiceFactory = "org.apache.oodt.cas.filemgr.catalog.DataSourceCatalogFactory";

    private static final String repositoryServiceFactory = "org.apache.oodt.cas.filemgr.repository.XMLRepositoryManagerFactory";

    private static final String versionerClass = "org.apache.oodt.cas.filemgr.versioning.BasicVersioner";

    private static final String extractorClass = "org.apache.oodt.cas.filemgr.metadata.extractors.CoreMetExtractor";

    private Properties initialProperties = new Properties(
        System.getProperties());

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        Properties properties = new Properties(System.getProperties());

      // set the log levels
        URL loggingPropertiesUrl = this.getClass().getResource(
            "/test.logging.properties");
        properties.setProperty("java.util.logging.config.file", new File(
            loggingPropertiesUrl.getFile()).getAbsolutePath());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource(
                "/filemgr.properties");
            properties.load(new FileInputStream(
                filemgrPropertiesUrl.getFile()));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        System.setProperties(properties);
    }

    protected void tearDown() throws Exception {
        System.setProperties(initialProperties);
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

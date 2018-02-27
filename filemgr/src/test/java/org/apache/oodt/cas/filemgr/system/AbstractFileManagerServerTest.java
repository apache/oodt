/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.filemgr.system;

import junit.framework.TestCase;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class AbstractFileManagerServerTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(AbstractFileManagerServerTest.class.getName());

    protected static final int FM_PORT = 50001;
    protected static final String transferServiceFacClass =
            "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

    protected FileManagerServer fileManagerServer;
    private String luceneCatLoc;
    private Properties initialProperties = new Properties(System.getProperties());


    @Override
    public void setUp() throws Exception {
        super.setUp();
        startFileManagerServer();
        ingestTestFile();
    }

    private void startFileManagerServer() {
        Properties properties = new Properties(System.getProperties());

        // first make sure to load properties for the file manager
        // and make sure to load logging properties as well

        // set the log levels
        URL loggingPropertiesUrl = this.getClass().getResource("/test.logging.properties");
        properties.setProperty("java.util.logging.config.file", new File(loggingPropertiesUrl.getFile())
                .getAbsolutePath());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource("/filemgr.properties");
            properties.load(new FileInputStream(new File(filemgrPropertiesUrl.getFile())));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // override the catalog to use: we'll use lucene
        try {
            URL ingestUrl = this.getClass().getResource("/ingest");
            luceneCatLoc = new File(ingestUrl.getFile()).getCanonicalPath() + "/cat";
        } catch (Exception e) {
            fail(e.getMessage());
        }

        properties.setProperty("filemgr.catalog.factory", "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
        properties.setProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath", luceneCatLoc);

        // now override the repo mgr policy
        URL fmpolicyUrl = this.getClass().getResource("/ingest/fmpolicy");
        try {
            properties.setProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs",
                    "file://" + new File(fmpolicyUrl.getFile()).getCanonicalPath());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // now override the val layer ones
        properties.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
                "file://" + new File(fmpolicyUrl.getFile()).getAbsolutePath());

        // set up mime repo path
        URL mimeTypesUrl = this.getClass().getResource("/mime-types.xml");
        properties.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
                new File(mimeTypesUrl.getFile()).getAbsolutePath());

        // override expand product met
        properties.setProperty("org.apache.oodt.cas.filemgr.metadata.expandProduct",
                Boolean.toString(shouldExpandProduct()));

        System.setProperties(properties);

        setProperties();

        try {
            fileManagerServer = newFileManagerServer(FM_PORT);
            fileManagerServer.startUp();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void tearDown() throws Exception {
        fileManagerServer.shutdown();
        fileManagerServer = null;

        // blow away lucene cat
        deleteAllFiles(luceneCatLoc);

        // blow away test file
        deleteAllFiles("/tmp/test.txt");

        // Reset the System properties to initial values.
        System.setProperties(initialProperties);
        super.tearDown();
    }

    protected void deleteAllFiles(String startDir) {
        File startDirFile = new File(startDir);
        File[] delFiles = startDirFile.listFiles();

        if (delFiles != null && delFiles.length > 0) {
            for (File delFile : delFiles) {
                delFile.delete();
            }
        }

        startDirFile.delete();
    }

    @SuppressWarnings("Duplicates")
    private void ingestTestFile() {
        Metadata prodMet;
        StdIngester ingester = new StdIngester(transferServiceFacClass);

        try {
            URL ingestUrl = this.getClass().getResource("/ingest");
            URL refUrl = this.getClass().getResource("/ingest/test.txt");
            URL metUrl = this.getClass().getResource("/ingest/test.txt.met");

            prodMet = new SerializableMetadata(new FileInputStream(new File(metUrl.getFile())));

            // now add the right file location
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
            prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
            ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(refUrl.getFile()), prodMet);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    protected abstract void setProperties();

    protected abstract FileManagerServer newFileManagerServer(int port) throws Exception;

    protected abstract boolean shouldExpandProduct();
}

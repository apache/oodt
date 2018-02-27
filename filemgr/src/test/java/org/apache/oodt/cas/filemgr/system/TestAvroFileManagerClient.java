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

import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class TestAvroFileManagerClient extends AbstractFileManagerServerTest {

    /**
     * @since OODT-161
     */
    public void testGetReducedMetadata() {
        List<String> arrayListElems = new ArrayList<>();
        List<String> vectorElemList = new Vector<>();
        List<String> linkedListElemList = new LinkedList<>();

        arrayListElems.add(CoreMetKeys.FILENAME);
        vectorElemList.add(CoreMetKeys.FILENAME);
        linkedListElemList.add(CoreMetKeys.FILENAME);

        try {
            FileManagerClient fmc = new AvroFileManagerClient(new URL("http://localhost:" + FM_PORT));

            Metadata reducedMet;
            List pTypes = fmc.getProductTypes();
            assertNotNull(pTypes);
            assertTrue(pTypes.size() > 0);
            ProductType genericFileType = fmc.getProductTypeByName("GenericFile");
            assertNotNull(genericFileType);
            List products = fmc.getProductsByProductType(genericFileType);
            assertNotNull(products);
            assertTrue(products.size() > 0);
            Product product = (Product) products.get(0);
            assertNotNull(product);

            reducedMet = fmc.getReducedMetadata(product, arrayListElems);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getMap().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, vectorElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getMap().keySet().size(), 1);

            reducedMet = fmc.getReducedMetadata(product, linkedListElemList);
            assertNotNull(reducedMet);
            assertTrue(reducedMet.containsKey(CoreMetKeys.FILENAME));
            assertEquals(reducedMet.getMap().keySet().size(), 1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testIngest() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl = this.getClass().getResource("/ingest/test.txt");

        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(
                ingestUrl.getFile()).getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");

        StdIngester ingester = new StdIngester(transferServiceFacClass);
        String productId = ingester.ingest(
                new URL("http://localhost:" + FM_PORT),
                new File(refUrl.getFile()), prodMet);
        FileManagerClient fmc = new AvroFileManagerClient(new URL("http://localhost:" + FM_PORT));
        Metadata m = fmc.getMetadata(fmc.getProductById(productId));
        assertEquals(m.getMetadata("Filename"), "test.txt");
        deleteAllFiles("/tmp/test-type");
    }

    /**
     * todo This is an incomplete test. No verification done after the file is deleted
     *
     * @throws Exception
     */
    public void testRemoveFile() throws Exception {
        Path tmpFilePath = Paths.get("/tmp", "test-delete.txt");
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl = this.getClass().getResource("/ingest/test-delete.txt");
        Files.copy(Paths.get(refUrl.toURI()), tmpFilePath, REPLACE_EXISTING);

        try {
            Metadata prodMet = new Metadata();
            prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
            prodMet.addMetadata(CoreMetKeys.FILENAME, "test-delete.txt");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile");
            prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");

            StdIngester ingester = new StdIngester(transferServiceFacClass);
            String productId = ingester.ingest(
                    new URL("http://localhost:" + FM_PORT),
                    new File(refUrl.getFile()), prodMet);
            FileManagerClient fmc = new AvroFileManagerClient(new URL("http://localhost:" + FM_PORT));
            Metadata m = fmc.getMetadata(fmc.getProductById(productId));
            assertEquals(m.getMetadata("Filename"), "test-delete.txt");
            String loc = m.getMetadata("FileLocation");
            fmc.removeFile(loc + "/" + m.getMetadata("Filename"));
            fmc.getProductById(productId);
        } finally {
            Files.copy(tmpFilePath, Paths.get(refUrl.toURI()));
        }
        deleteAllFiles("/tmp/test-type");
    }

    /**
     * @since OODT-404
     */
    public void testMetadataPersistence() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl = this.getClass().getResource("/ingest/test-file-3.txt");
        URL metUrl = this.getClass().getResource("/ingest/test-file-3.txt.met");

        Metadata prodMet;
        StdIngester ingester = new StdIngester(transferServiceFacClass);
        prodMet = new SerializableMetadata(new FileInputStream(metUrl.getFile()));
        // now add the right file location
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
        String productId = ingester.ingest(
                new URL("http://localhost:" + FM_PORT),
                new File(refUrl.getFile()), prodMet);
        FileManagerClient fmc = new AvroFileManagerClient(new URL("http://localhost:" + FM_PORT));

        Metadata m = fmc.getMetadata(fmc.getProductById(productId));
        assertEquals(m.getAllMetadata("TestElement").size(), 4);
        assertEquals(m.getMetadata("TestElement"), "fe");
    }

    public void testComplexQuery() throws Exception {
        URL ingestUrl = this.getClass().getResource("/ingest");
        URL refUrl1 = this.getClass().getResource("/ingest/test-file-1.txt");
        URL refUrl2 = this.getClass().getResource("/ingest/test-file-2.txt");

        StdIngester ingester = new StdIngester(transferServiceFacClass);

        //ingest first file
        Metadata prodMet = new Metadata();
        prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
        prodMet.addMetadata(CoreMetKeys.FILENAME, "test-file-1.txt");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile1");
        prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(refUrl1.getFile()), prodMet);

        //ingest second file
        prodMet.replaceMetadata(CoreMetKeys.FILENAME, "test-file-2.txt");
        prodMet.replaceMetadata(CoreMetKeys.PRODUCT_NAME, "TestFile2");
        ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(refUrl2.getFile()), prodMet);

        //perform complex query
        ComplexQuery complexQuery = new ComplexQuery();
        List<String> reducedMetadata = new Vector<String>();
        reducedMetadata.add(CoreMetKeys.FILENAME);
        complexQuery.setReducedMetadata(reducedMetadata);
        List<String> productTypeNames = new Vector<String>();
        productTypeNames.add("GenericFile");
        complexQuery.setReducedProductTypeNames(productTypeNames);
        complexQuery.setSortByMetKey(CoreMetKeys.FILENAME);
        complexQuery.setToStringResultFormat("$" + CoreMetKeys.FILENAME);
        complexQuery.addCriterion(SqlParser.parseSqlWhereClause("Filename != 'test.txt'"));
        FileManagerClient fmc = new AvroFileManagerClient(new URL("http://localhost:" + FM_PORT));
        List<QueryResult> queryResults = fmc.complexQuery(complexQuery);
        assertEquals("[test-file-1.txt, test-file-2.txt]", queryResults.toString());
    }

    @Override
    protected void setProperties() {
        System.setProperty("filemgr.server", "org.apache.oodt.cas.filemgr.system.rpc.AvroFileManagerServerFactory");
        System.setProperty("filemgr.client", "org.apache.oodt.cas.filemgr.system.rpc.AvroFileManagerClientFactory");
    }

    @Override
    protected FileManagerServer newFileManagerServer(int port) throws Exception {
        return new AvroFileManagerServer(port);
    }

    @Override
    protected boolean shouldExpandProduct() {
        return false;
    }
}

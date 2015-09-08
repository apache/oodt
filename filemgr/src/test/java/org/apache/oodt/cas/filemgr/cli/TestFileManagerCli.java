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
package org.apache.oodt.cas.filemgr.cli;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
//JUnit imports
import junit.framework.TestCase;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.cli.util.OptionPropertyRegister;
import org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.system.MockFileManagerClient;
import org.apache.oodt.cas.filemgr.system.MockFileManagerClient.MethodCallDetails;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

/**
 * Tests File Manager Clients Command-line interface.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestFileManagerCli extends TestCase {

   private CmdLineUtility cmdLineUtility;
   private MockFileManagerClient client;
   private Properties initialProperties = new Properties(
     System.getProperties());

   @Override
   public void setUp() throws Exception {
      super.setUp();

      Properties properties = new Properties(System.getProperties());

      properties.setProperty("org.apache.oodt.cas.cli.debug", "true");

      URL actionsUrl = this.getClass().getResource("/cmd-line-actions.xml");
      properties.setProperty("org.apache.oodt.cas.cli.action.spring.config",
        "file:" + new File(actionsUrl.getFile()).getAbsolutePath());

      URL optionsUrl = this.getClass().getResource("/cmd-line-options.xml");
      properties.setProperty("org.apache.oodt.cas.cli.option.spring.config",
        "file:" + new File(optionsUrl.getFile()).getAbsolutePath());

      System.setProperties(properties);

      cmdLineUtility = new CmdLineUtility();
      UseMockClientCmdLineActionStore actionStore = new UseMockClientCmdLineActionStore();
      client = actionStore.getClient();
      cmdLineUtility.setActionStore(actionStore);
   }

   @Override
   public void tearDown() throws Exception {
      OptionPropertyRegister.clearRegister();
      System.setProperties(initialProperties);
   }

   public void testAddProductType() throws MalformedURLException,
         ConnectionException {
      String productTypeName = "TestProductType";
      String productTypeDesc = "ProductTypeDesc";
      String productTypeRepo = "ProductTypeRepo";
      String productTypeVersioner = "ProductTypeVersioner";
      String productTypeId = "MyProductTypeId";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --addProductType"
                  + " --typeName " + productTypeName + " --typeId "+productTypeId+" --typeDesc "
                  + productTypeDesc + " --repository " + productTypeRepo
                  + " --versionClass " + productTypeVersioner).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("addProductType", methodCallDetails.getMethodName());
      assertEquals(1, methodCallDetails.getArgs().size());
      ProductType pt = (ProductType) methodCallDetails.getArgs().get(0);
      assertEquals(productTypeName, pt.getName());
      assertEquals(productTypeDesc, pt.getDescription());
      assertEquals(productTypeRepo, pt.getProductRepositoryPath());
      assertEquals(productTypeVersioner, pt.getVersioner());
   }

   public void testDeleteProductById() {
      String productId = "TestProductId";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --deleteProductById"
                  + " --productId " + productId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("removeProduct", methodCallDetails.getMethodName());
      assertEquals(1, methodCallDetails.getArgs().size());
      assertEquals(productId,
            ((Product) methodCallDetails.getArgs().get(0)).getProductId());
   }

   public void testDeleteProductByName() {
      String productName = "TestProductName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --deleteProductByName"
                  + " --productName " + productName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("removeProduct", methodCallDetails.getMethodName());
      assertEquals(1, methodCallDetails.getArgs().size());
      assertEquals(productName,
            ((Product) methodCallDetails.getArgs().get(0)).getProductName());
   }

   public void testGetCurrentTransfer() {
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getCurrentTransfer")
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getCurrentFileTransfer", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().size());
   }

   public void testGetCurrentTransfers() {
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getCurrentTransfers")
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getCurrentFileTransfers", methodCallDetails.getMethodName());
      assertEquals(0, methodCallDetails.getArgs().size());
   }

   public void testGetFilePercentTransferred() {
     URL refUrl = this.getClass().getResource("/ingest/test.txt");
     String refPath = new File(refUrl.getFile()).getAbsolutePath();
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getFilePctTransferred"
                  + " --origRef " + refPath).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getRefPctTransferred", methodCallDetails.getMethodName());
      assertTrue(((Reference) methodCallDetails.getArgs().get(0))
            .getOrigReference().endsWith("test.txt"));
   }

   public void testGetFirstPage() {
      String productTypeName = "ProductTypeName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getFirstPage"
                  + " --productTypeName " + productTypeName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getFirstPage", methodCallDetails.getMethodName());
      assertEquals(productTypeName, ((ProductType) methodCallDetails.getArgs()
            .get(0)).getName());
   }

   public void testGetLastPage() {
      String productTypeName = "ProductTypeName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getLastPage"
                  + " --productTypeName " + productTypeName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getLastPage", methodCallDetails.getMethodName());
      assertEquals(productTypeName, ((ProductType) methodCallDetails.getArgs()
            .get(0)).getName());
   }

   public void testGetNextPage() {
      String productTypeName = "ProductTypeName";
      int curPage = 1;
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getNextPage"
                  + " --productTypeName " + productTypeName + " --curPage " + curPage)
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNextPage", methodCallDetails.getMethodName());
      assertEquals(productTypeName, ((ProductType) methodCallDetails.getArgs()
            .get(0)).getName());
      assertEquals(curPage,
            ((ProductPage) methodCallDetails.getArgs().get(1)).getPageNum());
   }

   public void testGetPrevPage() {
      String productTypeName = "ProductTypeName";
      int curPage = 1;
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getPrevPage"
                  + " --productTypeName " + productTypeName + " --curPage " + curPage)
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getPrevPage", methodCallDetails.getMethodName());
      assertEquals(productTypeName, ((ProductType) methodCallDetails.getArgs()
            .get(0)).getName());
      assertEquals(curPage,
            ((ProductPage) methodCallDetails.getArgs().get(1)).getPageNum());
   }

   public void testGetNumProducts() {
      String productTypeName = "ProductTypeName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getNumProducts"
                  + " --productTypeName " + productTypeName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getNumProducts", methodCallDetails.getMethodName());
      assertEquals(productTypeName, ((ProductType) methodCallDetails.getArgs()
            .get(0)).getName());
   }

   public void testGetProductById() {
      String productId = "TestProductId";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getProductById"
                  + " --productId " + productId).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductById", methodCallDetails.getMethodName());
      assertEquals(productId, methodCallDetails.getArgs().get(0));
   }

   public void testGetProductByName() {
      String productName = "TestProductName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getProductByName"
                  + " --productName " + productName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductByName", methodCallDetails.getMethodName());
      assertEquals(productName, methodCallDetails.getArgs().get(0));
   }

   public void testGetProductPercnetTransferred() {
      String productId = "TestProductId";
      String productTypeName = "TestProductType";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getProductPctTransferred"
                  + " --productId " + productId + " --productTypeName " + productTypeName)
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductPctTransferred",
            methodCallDetails.getMethodName());
      assertEquals(productId,
            ((Product) methodCallDetails.getArgs().get(0)).getProductId());
   }

   public void testGetProductTypeByName() {
      String productTypeName = "TestProductType";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --getProductTypeByName"
                  + " --productTypeName " + productTypeName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductTypeByName", methodCallDetails.getMethodName());
      assertEquals(productTypeName, methodCallDetails.getArgs().get(0));
   }

   public void testHasProduct() {
      String productName = "TestProductName";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --hasProduct"
                  + " --productName " + productName).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("hasProduct", methodCallDetails.getMethodName());
      assertEquals(productName, methodCallDetails.getArgs().get(0));
   }

   public void testIngestProduct() {
     URL refUrl = this.getClass().getResource("/ingest/test.txt");
     URL metUrl = this.getClass().getResource("/ingest/test.txt.met");

      String productName = "TestProductName";
      String structure = Product.STRUCTURE_FLAT;
      String productTypeName = "TestProductType";
      String metadataFile = new File(metUrl.getFile()).getAbsolutePath();
      String dataTransferer = InPlaceDataTransferFactory.class
            .getCanonicalName();
      String ref = new File(refUrl.getFile()).getAbsolutePath();
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --ingestProduct"
                  + " --productName " + productName + " --productStructure "
                  + structure + " --productTypeName " + productTypeName
                  + " --metadataFile " + metadataFile + " --refs " + ref
                  + " --clientTransfer --dataTransfer " + dataTransferer)
                  .split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("ingestProduct", methodCallDetails.getMethodName());
      assertEquals(productName,
            ((Product) methodCallDetails.getArgs().get(0)).getProductName());
      assertEquals(structure,
            ((Product) methodCallDetails.getArgs().get(0))
                  .getProductStructure());
      assertEquals(productTypeName,
            ((Product) methodCallDetails.getArgs().get(0)).getProductType()
                  .getName());
      assertTrue(((Product) methodCallDetails.getArgs().get(0))
            .getProductReferences().get(0).getOrigReference().endsWith(ref));

      assertEquals("test.txt",
            ((Metadata) methodCallDetails.getArgs().get(1))
                  .getMetadata("Filename"));
      assertEquals("GenericFile", ((Metadata) methodCallDetails.getArgs()
            .get(1)).getMetadata("ProductType"));
      assertEquals(true, methodCallDetails.getArgs().get(2));
   }

   public void testDumpMetadata() throws IOException {
      String productId = "TestProductId";
      File bogusFile = File.createTempFile("bogus", "bogus");
      File tmpFile = new File(bogusFile.getParentFile(), "CliDumpMetadata");
      tmpFile.mkdirs();
      bogusFile.delete();
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --dumpMetadata"
                  + " --productId " + productId).split(" "));

      OptionPropertyRegister.clearRegister();

      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getMetadata", methodCallDetails.getMethodName());
      assertEquals(productId,
            ((Product) methodCallDetails.getArgs().get(0)).getProductId());
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --dumpMetadata"
                  + " --productId " + productId + " --outputDir " + tmpFile
                  .getAbsolutePath()).split(" "));
      FileUtils.forceDelete(tmpFile);
   }

   public void testLuceneQuery() {
      String query = "ProductId:TestProductId";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --luceneQuery"
                  + " --query " + query).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("complexQuery", methodCallDetails.getMethodName());
      assertEquals("ProductId", ((TermQueryCriteria) ((Query) methodCallDetails
            .getArgs().get(0)).getCriteria().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) ((Query) methodCallDetails.getArgs().get(0))
                  .getCriteria().get(0)).getValue());

      OptionPropertyRegister.clearRegister();

      String reducedMetadataKeys = "ProductId ProductType";
      String outputFormat = "$ProductId";
      String reducedProductTypes = "TestProductType";
      String sortBy = "ProductId";
      String delimiter = ",";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --luceneQuery"
                  + " --query " + query + " --reducedMetadataKeys " + reducedMetadataKeys
                  + " --outputFormat " + outputFormat
                  + " --reducedProductTypes " + reducedProductTypes
                  + " --sortBy " + sortBy
                  + " --delimiter " + delimiter).split(" "));
      methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("complexQuery", methodCallDetails.getMethodName());
      ComplexQuery complexQuery = (ComplexQuery) methodCallDetails.getArgs().get(0);
      assertEquals("ProductId", ((TermQueryCriteria) complexQuery.getCriteria().get(0)).getElementName());
      assertEquals("TestProductId",
            ((TermQueryCriteria) complexQuery.getCriteria().get(0)).getValue());
      assertEquals(Lists.newArrayList(reducedMetadataKeys.split(" ")), complexQuery.getReducedMetadata());
      assertEquals(outputFormat, complexQuery.getToStringResultFormat());
      assertEquals(Lists.newArrayList(reducedProductTypes.split(" ")), complexQuery.getReducedProductTypeNames());
      assertEquals(sortBy, complexQuery.getSortByMetKey());
   }

   public void testRetrieveFilesById() {
      String productId = "TestProductId";
      String destination = "/tmp/toDir";
      String transferer = "org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --retrieveFilesById"
                  + " --productId " + productId + " --destination " + destination
                  + " --transferer " + transferer).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductReferences", methodCallDetails.getMethodName());
      assertEquals(productId, ((Product) methodCallDetails.getArgs().get(0)).getProductId());
   }

   public void testRetrieveFilesByName() {
      String productName = "TestProductName";
      String destination = "/tmp/toDir";
      String transferer = "org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory";
      cmdLineUtility
            .run(("--url http://localhost:9000 --operation --retrieveFilesByName"
                  + " --productName " + productName + " --destination " + destination
                  + " --transferer " + transferer).split(" "));
      MethodCallDetails methodCallDetails = client.getLastMethodCallDetails();
      assertEquals("getProductReferences", methodCallDetails.getMethodName());
      assertEquals(productName, ((Product) methodCallDetails.getArgs().get(0)).getProductName());
   }
}

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
package org.apache.oodt.cas.filemgr.cli.action;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferFactory;
import org.apache.oodt.cas.filemgr.datatransfer.InPlaceDataTransferer;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link IngestProductCliAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestIngestProductCliAction extends TestCase {

   private static final String FILENAME_MET_KEY = "Filename";
   private static final String FILENAME_MET_VAL = "data.dat";
   private static final String NOMINAL_DATE_MET_KEY = "NominalDate";
   private static final String NOMINAL_DATE_MET_VAL = "2011-01-20";

   private static final String PRODUCT_ID = "TestProductId";
   private static final String PRODUCT_NAME = "TestProductName";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";
   private static final String DATA_TRANSFERER_FACTORY = InPlaceDataTransferFactory.class.getCanonicalName();
   private static final String DATA_TRANSFERER = InPlaceDataTransferer.class.getCanonicalName();
   private static final String FLAT_REF_NAME = "flat_ref.txt";
   private static final String HIER_REF_NAME = "hier_ref";
   private static final String SUB_REF_1 = "sub_ref1.txt";
   private static final String SUB_REF_2 = "sub_ref2.txt";

   private DataTransfer clientSetDataTransferer;
   private Product clientSetProduct;
   private Metadata clientSetMetadata;

   private File tmpDir;
   private File metadataFile;
   private File hierRefFile;
   private File flatRefFile;

   @Override
   public void setUp() throws Exception {
      super.setUp();
      clientSetDataTransferer = null;
      clientSetProduct = null;
      clientSetMetadata = null;
      tmpDir = createTmpDir();
      metadataFile = createMetadataFile();
      hierRefFile = createHierarchicalReference();
      flatRefFile = createFlatReference();
   }

   @Override
   public void tearDown() throws Exception {
      FileUtils.forceDelete(tmpDir);
   }

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      IngestProductCliAction cliAction = new MockIngestProductCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_FLAT);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setReferences(Lists.newArrayList(flatRefFile.getAbsolutePath()));
      cliAction.execute(printer); // Should not throw exception.
      cliAction.setDataTransferer(DATA_TRANSFERER_FACTORY);
      cliAction.execute(printer); // Should not throw exception.

      cliAction = new NullPTIngestProductCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_FLAT);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setReferences(Lists.newArrayList(flatRefFile.getAbsolutePath()));
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }
   
   public void testClientTransTrueAndFlatProduct() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockIngestProductCliAction cliAction = new MockIngestProductCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_FLAT);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setDataTransferer(DATA_TRANSFERER_FACTORY);
      cliAction.setReferences(Lists.newArrayList(flatRefFile.getAbsolutePath()));
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("ingestProduct: Result: " + PRODUCT_ID, printer.getPrintedMessages().get(0)); 
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(PRODUCT_NAME, clientSetProduct.getProductName());
      assertEquals(Product.STRUCTURE_FLAT, clientSetProduct.getProductStructure());
      assertEquals(PRODUCT_TYPE_NAME, clientSetProduct.getProductType().getName());
      assertEquals(1, clientSetProduct.getProductReferences().size());
      assertEquals("file:" + flatRefFile.getAbsolutePath(), clientSetProduct.getProductReferences().get(0).getOrigReference());
      assertEquals(DATA_TRANSFERER, clientSetDataTransferer.getClass().getCanonicalName());
      assertEquals(2, clientSetMetadata.getAllKeys().size());
      assertEquals(FILENAME_MET_VAL, clientSetMetadata.getMetadata(FILENAME_MET_KEY));
      assertEquals(NOMINAL_DATE_MET_VAL, clientSetMetadata.getMetadata(NOMINAL_DATE_MET_KEY));
   }

   public void testClientTransFalseAndFlatProduct() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockIngestProductCliAction cliAction = new MockIngestProductCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_FLAT);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setReferences(Lists.newArrayList(flatRefFile.getAbsolutePath()));
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("ingestProduct: Result: " + PRODUCT_ID, printer.getPrintedMessages().get(0)); 
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(PRODUCT_NAME, clientSetProduct.getProductName());
      assertEquals(Product.STRUCTURE_FLAT, clientSetProduct.getProductStructure());
      assertEquals(PRODUCT_TYPE_NAME, clientSetProduct.getProductType().getName());
      assertEquals(1, clientSetProduct.getProductReferences().size());
      assertEquals("file:" + flatRefFile.getAbsolutePath(), clientSetProduct.getProductReferences().get(0).getOrigReference());
      assertNull(clientSetDataTransferer);
      assertEquals(2, clientSetMetadata.getAllKeys().size());
      assertEquals(FILENAME_MET_VAL, clientSetMetadata.getMetadata(FILENAME_MET_KEY));
      assertEquals(NOMINAL_DATE_MET_VAL, clientSetMetadata.getMetadata(NOMINAL_DATE_MET_KEY));
   }

   public void testClientTransTrueAndHierProduct() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockIngestProductCliAction cliAction = new MockIngestProductCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setDataTransferer(DATA_TRANSFERER_FACTORY);
      cliAction.setReferences(Lists.newArrayList(hierRefFile.getAbsolutePath()));
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("ingestProduct: Result: " + PRODUCT_ID, printer.getPrintedMessages().get(0)); 
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(PRODUCT_NAME, clientSetProduct.getProductName());
      assertEquals(Product.STRUCTURE_HIERARCHICAL, clientSetProduct.getProductStructure());
      assertEquals(PRODUCT_TYPE_NAME, clientSetProduct.getProductType().getName());
      assertEquals(3, clientSetProduct.getProductReferences().size());
      Collections.sort(clientSetProduct.getProductReferences(), new Comparator<Reference>() {
         @Override
         public int compare(Reference ref1, Reference ref2) {
            return ref1.getOrigReference().compareTo(ref2.getOrigReference());
         }
      });
      assertEquals("file:" + hierRefFile.getAbsolutePath() + "/", clientSetProduct.getProductReferences().get(0).getOrigReference());
      assertEquals("file:" + new File(hierRefFile, SUB_REF_1).getAbsolutePath(), clientSetProduct.getProductReferences().get(1).getOrigReference());
      assertEquals("file:" + new File(hierRefFile, SUB_REF_2).getAbsolutePath(), clientSetProduct.getProductReferences().get(2).getOrigReference());
      assertEquals(DATA_TRANSFERER, clientSetDataTransferer.getClass().getCanonicalName());
      assertEquals(2, clientSetMetadata.getAllKeys().size());
      assertEquals(FILENAME_MET_VAL, clientSetMetadata.getMetadata(FILENAME_MET_KEY));
      assertEquals(NOMINAL_DATE_MET_VAL, clientSetMetadata.getMetadata(NOMINAL_DATE_MET_KEY));
   }

   public void testClientTransFalseAndHierProduct() throws CmdLineActionException, IOException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockIngestProductCliAction cliAction = new MockIngestProductCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setMetadataFile(metadataFile.getAbsolutePath());
      cliAction.setReferences(Lists.newArrayList(hierRefFile.getAbsolutePath()));
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("ingestProduct: Result: " + PRODUCT_ID, printer.getPrintedMessages().get(0)); 
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(PRODUCT_NAME, clientSetProduct.getProductName());
      assertEquals(Product.STRUCTURE_HIERARCHICAL, clientSetProduct.getProductStructure());
      assertEquals(PRODUCT_TYPE_NAME, clientSetProduct.getProductType().getName());
      assertEquals(3, clientSetProduct.getProductReferences().size());
      Collections.sort(clientSetProduct.getProductReferences(), new Comparator<Reference>() {
         @Override
         public int compare(Reference ref1, Reference ref2) {
            return ref1.getOrigReference().compareTo(ref2.getOrigReference());
         }
      });
      assertEquals("file:" + hierRefFile.getAbsolutePath() + "/", clientSetProduct.getProductReferences().get(0).getOrigReference());
      assertEquals("file:" + new File(hierRefFile, SUB_REF_1).getAbsolutePath(), clientSetProduct.getProductReferences().get(1).getOrigReference());
      assertEquals("file:" + new File(hierRefFile, SUB_REF_2).getAbsolutePath(), clientSetProduct.getProductReferences().get(2).getOrigReference());
      assertNull(clientSetDataTransferer);
      assertEquals(2, clientSetMetadata.getAllKeys().size());
      assertEquals(FILENAME_MET_VAL, clientSetMetadata.getMetadata(FILENAME_MET_KEY));
      assertEquals(NOMINAL_DATE_MET_VAL, clientSetMetadata.getMetadata(NOMINAL_DATE_MET_KEY));
   }

   private File createTmpDir() throws IOException {
      File bogusDir = File.createTempFile("bogus", "bogus");
      File tmpDir = bogusDir.getParentFile();
      bogusDir.delete();
      tmpDir = new File(tmpDir, "Metadata");
      tmpDir.mkdirs();
      return tmpDir;
   }

   private File createMetadataFile() throws IOException {
      File metadataFile = new File(tmpDir, "test.met");
      metadataFile.deleteOnExit();
      PrintStream ps = null;
      try {
         ps = new PrintStream(new FileOutputStream(metadataFile));
         ps.println("<cas:metadata xmlns:cas=\"http://oodt.jpl.nasa.gov/1.0/cas\">");
         ps.println("  <keyval type=\"scalar\">");
         ps.println("    <key>" + FILENAME_MET_KEY + "</key>");
         ps.println("    <val>" + FILENAME_MET_VAL + "</val>");
         ps.println("  </keyval>");
         ps.println("  <keyval type=\"scalar\">");
         ps.println("    <key>" +  NOMINAL_DATE_MET_KEY +"</key>");
         ps.println("    <val>" +  NOMINAL_DATE_MET_VAL + "</val>");
         ps.println("  </keyval>");
         ps.println("</cas:metadata>");
      } finally {
         ps.close();
      }
      return metadataFile;
   }

   private File createHierarchicalReference() throws IOException {
      File reference = new File(tmpDir, HIER_REF_NAME);
      reference.mkdirs();
      PrintStream ps = null;
      for (String subRef :  Lists.newArrayList(SUB_REF_1, SUB_REF_2)) {
         try {
            ps = new PrintStream(new FileOutputStream(new File(reference, subRef)));
            ps.println("This is a test sub-reference file");
         } finally {
            ps.close();
         }
      }
      return reference;
   }


   private File createFlatReference() throws IOException {
      File reference = new File(tmpDir, FLAT_REF_NAME);
      PrintStream ps = null;
      try {
         ps = new PrintStream(new FileOutputStream(reference));
         ps.println("This is a test sub-reference file");
      } finally {
         ps.close();
      }
      return reference;
   }

   public class MockIngestProductCliAction extends IngestProductCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String typeName) {
               ProductType pt = new ProductType();
               pt.setName(typeName);
               return pt;
            }
            @Override
            public void setDataTransfer(DataTransfer dataTransferer) {
               clientSetDataTransferer = dataTransferer;
            }
            @Override
            public String ingestProduct(Product product, Metadata metadata,
                  boolean useClientTransfer) {
               clientSetProduct = product;
               clientSetMetadata = metadata;
               return PRODUCT_ID;
            }
         };
      }
   }

   public class NullPTIngestProductCliAction extends MockIngestProductCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String typeName) {
               return null;
            }
         };
      }
   }
}

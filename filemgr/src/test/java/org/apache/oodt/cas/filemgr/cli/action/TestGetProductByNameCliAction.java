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
import java.net.MalformedURLException;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetProductByNameCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestGetProductByNameCliAction extends TestCase {

   private static final String PRODUCT_ID = "TestProductId";
   private static final String PRODUCT_NAME = "TestProductName";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";
   private static final String PRODUCT_STRUCTURE = Product.STRUCTURE_FLAT;
   private static final String PRODUCT_STATUS = Product.STATUS_RECEIVED;
   private static final String ROOT_REF = "file:/root/ref/path";
   private static final String ORIG_REF_1 = "file:/orig/ref/1/path";
   private static final String DS_REF_1 = "file:/ds/ref/1/path";
   private static final int FILE_SIZE_REF_1 = 2;
   private static final String ORIG_REF_2 = "file:/orig/ref/2/path";
   private static final String DS_REF_2 = "file:/ds/ref/2/path";
   private static final int FILE_SIZE_REF_2 = 2;

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      GetProductByNameCliAction cliAction = new MockGetProductByNameCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.execute(printer); // Should not throw exception.
      cliAction = new NullProductGetProductByNameCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetProductByNameCliAction cliAction = new MockGetProductByNameCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.execute(printer);
      assertEquals(20, printer.getPrintedMessages().size());
      assertEquals("Product:", printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(" - ID: " + PRODUCT_ID, printer.getPrintedMessages().get(2));
      assertEquals("\n", printer.getPrintedMessages().get(3));
      assertEquals(" - Name: " + PRODUCT_NAME, printer.getPrintedMessages()
            .get(4));
      assertEquals("\n", printer.getPrintedMessages().get(5));
      assertEquals(" - ProductType: " + PRODUCT_TYPE_NAME, printer
            .getPrintedMessages().get(6));
      assertEquals("\n", printer.getPrintedMessages().get(7));
      assertEquals(" - Structure: " + PRODUCT_STRUCTURE, printer
            .getPrintedMessages().get(8));
      assertEquals("\n", printer.getPrintedMessages().get(9));
      assertEquals(" - Status: " + PRODUCT_STATUS, printer.getPrintedMessages()
            .get(10));
      assertEquals("\n", printer.getPrintedMessages().get(11));
      assertEquals(" - RootRef: " + ROOT_REF,
            printer.getPrintedMessages().get(12));
      assertEquals("\n", printer.getPrintedMessages().get(13));
      assertEquals(" - References: ", printer.getPrintedMessages().get(14));
      assertEquals("\n", printer.getPrintedMessages().get(15));
      assertEquals("    - " + DS_REF_1 + " (" + FILE_SIZE_REF_1 + ")", printer
            .getPrintedMessages().get(16));
      assertEquals("\n", printer.getPrintedMessages().get(17));
      assertEquals("    - " + DS_REF_2 + " (" + FILE_SIZE_REF_2 + ")", printer
            .getPrintedMessages().get(18));
      assertEquals("\n", printer.getPrintedMessages().get(19));
   }

   public class MockGetProductByNameCliAction extends GetProductByNameCliAction {
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            public Product getProductByName(String name) {
               Product p = new Product();
               p.setProductId(PRODUCT_ID);
               p.setProductName(name);
               ProductType pt = new ProductType();
               pt.setName(PRODUCT_TYPE_NAME);
               p.setProductType(pt);
               p.setProductStructure(PRODUCT_STRUCTURE);
               p.setTransferStatus(PRODUCT_STATUS);
               p.setRootRef(new Reference("file:/dummy/path", ROOT_REF, 2));
               p.setProductReferences(Lists.newArrayList(new Reference(
                     ORIG_REF_1, DS_REF_1, FILE_SIZE_REF_1), new Reference(
                     ORIG_REF_2, DS_REF_2, FILE_SIZE_REF_2)));
               return p;
            }
         };
      }
   }

   public class NullProductGetProductByNameCliAction extends MockGetProductByNameCliAction {
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            public Product getProductByName(String name) {
               return null;
            }
         };
      }
   }
}

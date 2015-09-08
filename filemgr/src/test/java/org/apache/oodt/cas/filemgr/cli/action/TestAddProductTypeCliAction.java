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
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.versioning.BasicVersioner;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link AddProductTypeCliAction}.
 * 
 * @author bfoster (Brian Foster)
 * @author riverma (Rishi Verma)
 */
public class TestAddProductTypeCliAction extends TestCase {

   private static final String PRODUCT_TYPE_NAME = "TestProductTypeName";
   private static final String PRODUCT_TYPE_ID = "TestProductTypeId";
   private static final String PRODUCT_TYPE_DESC = "TestProductTypeDesc";
   private static final String PRODUCT_TYPE_REPO = "TestProductTypeRepo";
   private static final String PRODUCT_TYPE_VERSIONER = BasicVersioner.class
         .getCanonicalName();

   private static ProductType productTypePassedToClient;

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockAddProductTypeCliAction cliAction = new MockAddProductTypeCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductTypeDescription(PRODUCT_TYPE_DESC);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setFileRepositoryPath(PRODUCT_TYPE_REPO);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setVersioner(PRODUCT_TYPE_VERSIONER);
      try {
    	  cliAction.execute(printer);
    	  fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductTypeId(PRODUCT_TYPE_ID);
      cliAction.execute(printer); // Should not throw exception.
   }

   public void testDataFlow() throws CmdLineActionException {
      MockAddProductTypeCliAction cliAction = new MockAddProductTypeCliAction();
      cliAction.setProductTypeId(PRODUCT_TYPE_ID);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setProductTypeDescription(PRODUCT_TYPE_DESC);
      cliAction.setFileRepositoryPath(PRODUCT_TYPE_REPO);
      cliAction.setVersioner(PRODUCT_TYPE_VERSIONER);
      ActionMessagePrinter printer = new ActionMessagePrinter();
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("addProductType: Result: " + PRODUCT_TYPE_ID, printer
            .getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));

      assertEquals(PRODUCT_TYPE_ID, productTypePassedToClient.getProductTypeId());
      assertEquals(PRODUCT_TYPE_NAME, productTypePassedToClient.getName());
      assertEquals(PRODUCT_TYPE_DESC,
            productTypePassedToClient.getDescription());
      assertEquals(PRODUCT_TYPE_REPO,
            productTypePassedToClient.getProductRepositoryPath());
      assertEquals(PRODUCT_TYPE_VERSIONER,
            productTypePassedToClient.getVersioner());
   }

   public class MockAddProductTypeCliAction extends AddProductTypeCliAction {
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),false)
         {
            public String addProductType(ProductType type) {
               productTypePassedToClient = type;
               return PRODUCT_TYPE_ID;
            }
         };
      }
   }
}

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

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * Test class for {@link GetNumProductsCliAction}. 
 *
 * @author bfoster (Brian Foster)
 */
public class TestGetNumProductsCliAction extends TestCase {

   private static final int NUM_PRODUCTS = 10;
   private static final String PRODUCT_TYPE_NAME = "TestProductType";

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetNumProductsCliAction cliAction = new MockGetNumProductsCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("Type: [" + PRODUCT_TYPE_NAME+ "], Num Products: [" + NUM_PRODUCTS + "]", printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
   }

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      NullPTGetNumProductsCliAction cliAction = new NullPTGetNumProductsCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public class MockGetNumProductsCliAction extends GetNumProductsCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String name) {
               ProductType pt = new ProductType();
               pt.setName(name);
               return pt;
            }
            @Override
            public int getNumProducts(ProductType productType) {
               return NUM_PRODUCTS;
            }
         };
      }
   }

   public class NullPTGetNumProductsCliAction extends MockGetNumProductsCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String name) {
               return null;
            }
         };
      }
   }
}

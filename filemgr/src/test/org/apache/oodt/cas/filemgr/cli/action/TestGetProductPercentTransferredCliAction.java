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
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetProductPercentTransferredCliAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestGetProductPercentTransferredCliAction extends TestCase {

   private static final String PRODUCT_ID = "TestProductId";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";
   private static final double PERCENT_TRANSFERRED = 0.5;

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      GetProductPercentTransferredCliAction cliAction = new NullPTGetProductPercentTransferredCliAction();
      cliAction.setProductId(PRODUCT_ID);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetProductPercentTransferredCliAction cliAction = new MockGetProductPercentTransferredCliAction();
      cliAction.setProductId(PRODUCT_ID);
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("Product: [id=" + PRODUCT_ID + ", transferPct="
            + PERCENT_TRANSFERRED + "]", printer.getPrintedMessages()
            .get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
   }

   public class MockGetProductPercentTransferredCliAction extends GetProductPercentTransferredCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String name) {
               ProductType pt = new ProductType();
               pt.setName(name);
               return pt;
            }
            @Override
            public double getProductPctTransferred(Product product) {
               return PERCENT_TRANSFERRED;
            }
         };
      }
   }

   public class NullPTGetProductPercentTransferredCliAction extends MockGetProductPercentTransferredCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String name) {
               return null;
            }
         };
      }
   }
}

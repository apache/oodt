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
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link DeleteProductByNameCliAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestDeleteProductByNameCliAction extends TestCase {

   private static final String PRODUCT_ID = "TestProductId";
   private static final String PRODUCT_NAME = "TestProductName";
   private static final String REF_1 = "file:/ds/ref/1/path";
   private static final String REF_2 = "file:/ds/ref/2/path";

   private Product clientSetProduct;

   @Override
   public void setUp() throws Exception {
      super.setUp();
      clientSetProduct = null;
   }

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockDeleteProductByNameCliAction cliAction = new MockDeleteProductByNameCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.execute(printer); // Should not throw exception.
      cliAction = new NullProductDeleteProductByNameCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }      
      cliAction = new FalseDeleteProductByNameCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullRefsDeleteProductByNameCliAction();
      cliAction.setProductName(PRODUCT_ID);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockDeleteProductByNameCliAction cliAction = new MockDeleteProductByNameCliAction();
      cliAction.setProductName(PRODUCT_NAME);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("Successfully deleted product '" + PRODUCT_NAME + "'",
            printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals(PRODUCT_ID, clientSetProduct.getProductId());
      assertEquals(PRODUCT_NAME, clientSetProduct.getProductName());
      assertEquals(2, clientSetProduct.getProductReferences().size());
      assertEquals(REF_1, clientSetProduct.getProductReferences().get(0)
            .getDataStoreReference());
      assertEquals(REF_2, clientSetProduct.getProductReferences().get(1)
            .getDataStoreReference());
   }
   
   public class MockDeleteProductByNameCliAction extends DeleteProductByNameCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public Product getProductByName(String productName) {
               Product p = new Product();
               p.setProductId(PRODUCT_ID);
               p.setProductName(productName);
               return p;
            }
            @Override
            public List<Reference> getProductReferences(Product product) {
               product.setProductReferences(Lists.newArrayList(new Reference(
                     "file:/file/path", REF_1, 2), new Reference(
                     "file:/file/path", REF_2, 2)));
               return product.getProductReferences();
            }
            @Override
            public boolean removeProduct(Product p) {
               clientSetProduct = p;
               return true;
            }
            @Override
            public boolean removeFile(String file) {
               return true;
            }
         };
      }
   }

   public class NullProductDeleteProductByNameCliAction extends MockDeleteProductByNameCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public Product getProductByName(String productName) {
               return null;
            }
         };
      }
   }

   public class NullRefsDeleteProductByNameCliAction extends
         MockDeleteProductByNameCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public List<Reference> getProductReferences(Product product) {
               return null;
            }
         };
      }
   }

   public class FalseDeleteProductByNameCliAction extends MockDeleteProductByNameCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public boolean removeProduct(Product p) {
               return false;
            }
         };
      }
   }
}

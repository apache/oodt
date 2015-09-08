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
import org.apache.oodt.cas.filemgr.structs.ProductPage;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetLastPageCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestGetLastPageCliAction extends TestCase {

   private static final int PAGE_NUM = 1;
   private static final int TOTAL_PAGES = 1;
   private static final int PAGE_SIZE = 10;
   private static final String PRODUCT_ID = "TestProductId";
   private static final String PRODUCT_NAME = "TestProductName";
   private static final String PRODUCT_STRUCTURE = "Flat";
   private static final String PRODUCT_STATUS = "DONE";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetLastPageCliAction cliAction = new MockGetLastPageCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullPTGetLastPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullPPGetLastPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetLastPageCliAction cliAction = new MockGetLastPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.execute(printer);
      assertEquals(6, printer.getPrintedMessages().size());
      assertEquals("Page: [num=" + PAGE_NUM + ", totalPages=" + TOTAL_PAGES
            + ", pageSize=" + PAGE_SIZE + "]", printer.getPrintedMessages()
            .get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals("Products:", printer.getPrintedMessages().get(2));
      assertEquals("\n", printer.getPrintedMessages().get(3));
      assertEquals("Product: [id=" + PRODUCT_ID + ",name=" + PRODUCT_NAME
            + ",type=" + PRODUCT_TYPE_NAME + ",structure=" + PRODUCT_STRUCTURE
            + ", transferStatus=" + PRODUCT_STATUS + "]", printer
            .getPrintedMessages().get(4));
      assertEquals("\n", printer.getPrintedMessages().get(5));
   }

   public class MockGetLastPageCliAction extends GetLastPageCliAction {
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
            public ProductPage getLastPage(ProductType pt) {
               ProductPage pp = new ProductPage();
               pp.setPageNum(PAGE_NUM);
               pp.setTotalPages(TOTAL_PAGES);
               pp.setPageSize(PAGE_SIZE);
               Product p = new Product();
               p.setProductId(PRODUCT_ID);
               p.setProductName(PRODUCT_NAME);
               p.setProductType(pt);
               p.setProductStructure(PRODUCT_STRUCTURE);
               p.setTransferStatus(PRODUCT_STATUS);
               pp.setPageProducts(Lists.newArrayList(p));
               return pp;
            }
         };
      }
   }

   public class NullPTGetLastPageCliAction extends MockGetLastPageCliAction {
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

   public class NullPPGetLastPageCliAction extends MockGetLastPageCliAction {
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
            public ProductPage getLastPage(ProductType pt) {
               return null;
            }
         };
      }
   }
}

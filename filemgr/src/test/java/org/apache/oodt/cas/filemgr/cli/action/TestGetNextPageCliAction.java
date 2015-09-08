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

//JUnit imports
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
 * Test class for {@link GetNextPageCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestGetNextPageCliAction extends TestCase {

   private static final int PAGE_NUM = 1;
   private static final int TOTAL_PAGES = 2;
   private static final int PAGE_SIZE = 3;
   private static final String PRODUCT_ID_1 = "TestProductId1";
   private static final String PRODUCT_NAME_1 = "TestProductName1";
   private static final String PRODUCT_ID_2 = "TestProductId2";
   private static final String PRODUCT_NAME_2 = "TestProductName2";
   private static final String PRODUCT_ID_3 = "TestProductId3";
   private static final String PRODUCT_NAME_3 = "TestProductName3";
   private static final String PRODUCT_ID_4 = "TestProductId4";
   private static final String PRODUCT_NAME_4 = "TestProductName4";
   private static final String PRODUCT_STRUCTURE = "Flat";
   private static final String PRODUCT_STATUS = "DONE";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetNextPageCliAction cliAction = new MockGetNextPageCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullPTGetNextPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setCurrentPageNum(1);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullPPGetNextPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setCurrentPageNum(1);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
      cliAction = new NullNPGetNextPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setCurrentPageNum(1);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetNextPageCliAction cliAction = new MockGetNextPageCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.setCurrentPageNum(PAGE_NUM);
      cliAction.execute(printer);
      assertEquals(6, printer.getPrintedMessages().size());
      assertEquals("Page: [num=" + (PAGE_NUM + 1) + ", totalPages="
            + TOTAL_PAGES + ", pageSize=" + PAGE_SIZE + "]", printer
            .getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
      assertEquals("Products:", printer.getPrintedMessages().get(2));
      assertEquals("\n", printer.getPrintedMessages().get(3));
      assertEquals("Product: [id=" + PRODUCT_ID_4 + ",name=" + PRODUCT_NAME_4
            + ",type=" + PRODUCT_TYPE_NAME + ",structure=" + PRODUCT_STRUCTURE
            + ", transferStatus=" + PRODUCT_STATUS + "]", printer
            .getPrintedMessages().get(4));
      assertEquals("\n", printer.getPrintedMessages().get(5));
   }

   public class MockGetNextPageCliAction extends GetNextPageCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new MockXmlRpcFileManagerClient();
      }
   }

   public class NullPTGetNextPageCliAction extends MockGetNextPageCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new MockXmlRpcFileManagerClient() {
            @Override
            public ProductType getProductTypeByName(String name) {
               return null;
            }
         };
      }
   }

   public class NullPPGetNextPageCliAction extends MockGetNextPageCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new MockXmlRpcFileManagerClient() {
            @Override
            public ProductPage getFirstPage(ProductType pt) {
               return null;
            }
         };
      }
   }

   public class NullNPGetNextPageCliAction extends MockGetNextPageCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new MockXmlRpcFileManagerClient() {
            @Override
            public ProductPage getNextPage(ProductType pt,
                  ProductPage currentPage) {
               return null;
            }
         };
      }
   }

   public static class MockXmlRpcFileManagerClient extends
           DummyFileManagerClient {
      public MockXmlRpcFileManagerClient() throws MalformedURLException,
            ConnectionException {
         super(new URL("http://localhost:9000"), false);
      }

      @Override
      public ProductType getProductTypeByName(String name) {
         ProductType pt = new ProductType();
         pt.setName(name);
         return pt;
      }

      @Override
      public ProductPage getFirstPage(ProductType pt) {
         ProductPage pp = new ProductPage();
         pp.setPageNum(PAGE_NUM);
         pp.setTotalPages(TOTAL_PAGES);
         pp.setPageSize(PAGE_SIZE);
         Product p1 = new Product();
         p1.setProductId(PRODUCT_ID_1);
         p1.setProductName(PRODUCT_NAME_1);
         p1.setProductType(pt);
         p1.setProductStructure(PRODUCT_STRUCTURE);
         p1.setTransferStatus(PRODUCT_STATUS);
         Product p2 = new Product();
         p2.setProductId(PRODUCT_ID_2);
         p2.setProductName(PRODUCT_NAME_2);
         p2.setProductType(pt);
         p2.setProductStructure(PRODUCT_STRUCTURE);
         p2.setTransferStatus(PRODUCT_STATUS);
         Product p3 = new Product();
         p3.setProductId(PRODUCT_ID_3);
         p3.setProductName(PRODUCT_NAME_3);
         p3.setProductType(pt);
         p3.setProductStructure(PRODUCT_STRUCTURE);
         p3.setTransferStatus(PRODUCT_STATUS);
         pp.setPageProducts(Lists.newArrayList(p1, p2, p3));
         return pp;
      }

      @Override
      public ProductPage getNextPage(ProductType pt, ProductPage currentPage) {
         ProductPage pp = new ProductPage();
         pp.setPageNum(currentPage.getPageNum() + 1);
         pp.setTotalPages(currentPage.getTotalPages());
         pp.setPageSize(currentPage.getPageSize());
         Product p4 = new Product();
         p4.setProductId(PRODUCT_ID_4);
         p4.setProductName(PRODUCT_NAME_4);
         p4.setProductType(pt);
         p4.setProductStructure(PRODUCT_STRUCTURE);
         p4.setTransferStatus(PRODUCT_STATUS);
         pp.setPageProducts(Lists.newArrayList(p4));
         return pp;
      }
   }
}

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
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.versioning.BasicVersioner;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetProductTypeByNameCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestGetProductTypeByNameCliAction extends TestCase {

   private static final String PRODUCT_TYPE_ID = "urn:oodt:TestProductType";
   private static final String PRODUCT_TYPE_NAME = "TestProductType";
   private static final String PRODUCT_TYPE_DESC = "ProductTypeDesc";
   private static final String PRODUCT_TYPE_VERSIONER = BasicVersioner.class.getCanonicalName();
   private static final String PRODUCT_TYPE_REPO = "/path/to/repo";

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      GetProductTypeByNameCliAction cliAction = new NullPTGetProductTypeByNameCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetProductTypeByNameCliAction cliAction = new MockGetProductTypeByNameCliAction();
      cliAction.setProductTypeName(PRODUCT_TYPE_NAME);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("getProductTypeByName: Result: [name="
               + PRODUCT_TYPE_NAME + ", description=" + PRODUCT_TYPE_DESC
               + ", id=" + PRODUCT_TYPE_ID + ", versionerClass="
               + PRODUCT_TYPE_VERSIONER + ", repositoryPath="
               + PRODUCT_TYPE_REPO + "]", printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
   }

   public class MockGetProductTypeByNameCliAction extends GetProductTypeByNameCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public ProductType getProductTypeByName(String name) {
               ProductType pt = new ProductType();
               pt.setProductTypeId(PRODUCT_TYPE_ID);
               pt.setName(name);
               pt.setDescription(PRODUCT_TYPE_DESC);
               pt.setVersioner(PRODUCT_TYPE_VERSIONER);
               pt.setProductRepositoryPath(PRODUCT_TYPE_REPO);
               return pt;
            }
         };
      }
   }

   public class NullPTGetProductTypeByNameCliAction extends MockGetProductTypeByNameCliAction {
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

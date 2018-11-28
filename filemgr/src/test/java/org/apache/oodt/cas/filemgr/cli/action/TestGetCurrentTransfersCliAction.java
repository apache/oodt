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
import org.apache.oodt.cas.filemgr.structs.FileTransferStatus;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetCurrentTransfersCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestGetCurrentTransfersCliAction extends TestCase {

   private static final String ORIG_REF = "file:/orig/path";
   private static final String DS_REF = "file:/ds/path";
   private static final long FILE_SIZE = 20;
   private static final long BYTE_TRANS = 10;
   private static final String PRODUCT_NAME = "ProductName";

   private static FileTransferStatus status;

   @Override
   public void setUp() {
      status = null;
   }

   public void testValidateErrors() {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      NullStatusGetCurrentTransfersCliAction cliAction = new NullStatusGetCurrentTransfersCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {
      }
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetCurrentTransfersCliAction cliAction = new MockGetCurrentTransfersCliAction();
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("File Transfer: [ref={orig=" + ORIG_REF + ",ds=" + DS_REF
            + "},product=" + PRODUCT_NAME + ",fileSize=" + FILE_SIZE
            + ",amtTransferred=" + BYTE_TRANS + ",pct=0.5]", printer
            .getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));
   }

   public class MockGetCurrentTransfersCliAction extends GetCurrentTransfersCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public List<FileTransferStatus> getCurrentFileTransfers() {
               status = new FileTransferStatus();
               status.setFileRef(new Reference(ORIG_REF, DS_REF, FILE_SIZE));
               status.setBytesTransferred(BYTE_TRANS);
               Product parentProduct = new Product();
               parentProduct.setProductName(PRODUCT_NAME);
               status.setParentProduct(parentProduct);
               return Lists.newArrayList(status);
            }
         };
      }
   }

   public class NullStatusGetCurrentTransfersCliAction extends
         GetCurrentTransfersCliAction {
      @Override
      public XmlRpcFileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new XmlRpcFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public List<FileTransferStatus> getCurrentFileTransfers() {
               return null;
            }
         };
      }
   }
}

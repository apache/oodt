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
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link GetFilePercentTransferredCliAction}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestGetFilePercentTransferredCliAction extends TestCase {

   private static final String ORIG_REF = "file:/orig/path";
   private static final double PERCENT_TRANS = 0.6;

   private static Reference referencePassedToClient;

   @Override
   public void setUp() {
      referencePassedToClient = null;
   }

   public void testValidateErrors() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetFilePercentTransferredCliAction cliAction = new MockGetFilePercentTransferredCliAction();
      try {
         cliAction.execute(printer);
         fail("Expected throw CmdLineActionException");
      } catch (CmdLineActionException ignore) {}
      cliAction.setOrigRef(ORIG_REF);
      cliAction.execute(printer); // Should not throw exception.
   }

   public void testDataFlow() throws CmdLineActionException {
      ActionMessagePrinter printer = new ActionMessagePrinter();
      MockGetFilePercentTransferredCliAction cliAction = new MockGetFilePercentTransferredCliAction();
      cliAction.setOrigRef(ORIG_REF);
      cliAction.execute(printer);
      assertEquals(2, printer.getPrintedMessages().size());
      assertEquals("Reference: [origRef=" + ORIG_REF + ",transferPct="
            + PERCENT_TRANS + "]", printer.getPrintedMessages().get(0));
      assertEquals("\n", printer.getPrintedMessages().get(1));

      assertEquals(ORIG_REF, referencePassedToClient.getOrigReference());
   }

   public class MockGetFilePercentTransferredCliAction extends GetFilePercentTransferredCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public double getRefPctTransferred(Reference reference) {
               referencePassedToClient = reference;
               return PERCENT_TRANS;
            }
         };
      }
   }
}

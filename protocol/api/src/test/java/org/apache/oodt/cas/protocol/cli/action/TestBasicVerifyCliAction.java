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
package org.apache.oodt.cas.protocol.cli.action;

//JDK imports
import java.net.URI;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.config.MockSpringProtocolConfig;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;

/**
 * Test class for {@link BasicVerifyCliAction}
 * 
 * @author bfoster (Brian Foster)
 */
public class TestBasicVerifyCliAction extends TestCase {

   public void testVerification() throws Exception {
      BasicVerifyCliAction bva = new BasicVerifyCliAction();
      bva.setSite("http://localhost");
      bva.setVerifier(new ProtocolVerifier() {
         public boolean verify(Protocol protocol, URI site,
               Authentication auth) {
            return auth != null && site.toString().equals("http://localhost");
         }
      });
      bva.setProtocolManager(new ProtocolManager(new MockSpringProtocolConfig()));
      bva.execute(new ActionMessagePrinter());
      assertTrue(bva.getLastVerificationResults());
   }
}

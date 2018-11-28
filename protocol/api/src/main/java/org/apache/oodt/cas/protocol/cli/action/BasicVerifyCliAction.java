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

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;

/**
 * Action for determining whether a given {@link Protocol} or auto-determined
 * {@link Protocol} via {@link ProtocolManager} can connect and pass
 * verification of the given {@link Verifier} for given site.
 * 
 * @author bfoster (Brian Foster)
 */
public class BasicVerifyCliAction extends ProtocolCliAction {

   private ProtocolVerifier verifier;
   private ProtocolFactory factory;

   private boolean lastVerificationResult;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         if (factory != null) {
            Protocol protocol = factory.newInstance();
            if (lastVerificationResult = verifier.verify(protocol, getSite(),
                  getAuthentication())) {
               printer.println("Protocol '"
                     + protocol.getClass().getCanonicalName()
                     + "' PASSED verification!");
            } else {
               printer.println("Protocol '"
                     + protocol.getClass().getCanonicalName()
                     + "' FAILED verification!");
            }
         } else {
            Protocol protocol = getProtocolManager().getProtocolBySite(
                  getSite(), getAuthentication(), verifier);
            if (lastVerificationResult = protocol != null) {
               printer.println("Protocol '"
                     + protocol.getClass().getCanonicalName()
                     + "' PASSED verification!");
            } else {
               printer.println("No Protocol determined, FAILED verification!");
            }
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to verify factory '"
               + factory + "' : " + e.getMessage(), e);
      }
   }

   public void setVerifier(ProtocolVerifier verifier) {
      this.verifier = verifier;
   }

   public void setProtocolFactory(ProtocolFactory factory) {
      this.factory = factory;
   }

   protected boolean getLastVerificationResults() {
      return lastVerificationResult;
   }
}

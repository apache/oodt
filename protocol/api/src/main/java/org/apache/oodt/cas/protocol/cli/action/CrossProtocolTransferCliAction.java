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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.auth.NoAuthentication;
import org.apache.oodt.cas.protocol.system.ProtocolManager;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifier;
import org.apache.oodt.cas.protocol.verify.ProtocolVerifierFactory;

/**
 * {@link ProtocolAction} for transferring a file from one host to another
 * 
 * @author bfoster (Brian Foster)
 */
public class CrossProtocolTransferCliAction extends ProtocolCliAction {

   private URI fromUri;
   private URI toUri;
   private ProtocolVerifierFactory verifierFactory;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         ProtocolVerifier verifier = null;
         if (verifierFactory != null) {
            verifier = verifierFactory.newInstance();
         }

         File localFile = createTempDownloadFile();
         if (localFile == null) {
            throw new Exception("Failed to create tempory local file");
         }

         ProtocolManager protocolManager = getProtocolManager();
         Protocol fromProtocol = protocolManager.getProtocolBySite(fromUri,
               getAuthentication(fromUri), verifier);
         if (fromProtocol == null) {
            throw new Exception("Failed to get protocol for 'from' URI '"
                  + fromUri + "'");
         }

         Protocol toProtocol = protocolManager.getProtocolBySite(toUri,
               getAuthentication(toUri), verifier);
         if (toProtocol == null) {
            throw new Exception("Failed to get protocol for 'to' URI '" + toUri
                  + "'");
         }

         fromProtocol
               .get(new ProtocolFile(fromUri.getPath(), false), localFile);
         toProtocol.put(localFile, new ProtocolFile(toUri.getPath(), false));
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to transfer between 2 protocols : " + e.getMessage(), e);
      }
   }

   public void setFromUri(String fromUri) throws URISyntaxException {
      this.fromUri = new URI(fromUri);
   }

   public void setToUri(String toUri) throws URISyntaxException {
      this.toUri = new URI(toUri);
   }

   public void setVerifierFactory(ProtocolVerifierFactory verifierFactory) {
      this.verifierFactory = verifierFactory;
   }

   private File createTempDownloadFile() {
      File bogusFile = null;
      try {
         bogusFile = File.createTempFile("bogus", "bogus");
         File tmpDir = new File(bogusFile.getParentFile(), "ProtocolTransfer/"
               + UUID.randomUUID().toString());
         tmpDir.mkdirs();
         return new File(tmpDir, "temp_file");
      } catch (Exception e) {
         return null;
      } finally {
         try {
            bogusFile.delete();
         } catch (Exception e) {
         }
      }
   }

   private Authentication getAuthentication(URI uri) {
      if (uri.getUserInfo() != null) {
         String[] userInfo = uri.getUserInfo().split("\\:");
         return new BasicAuthentication(userInfo[0], userInfo[1]);
      } else {
         return new NoAuthentication();
      }
   }
}

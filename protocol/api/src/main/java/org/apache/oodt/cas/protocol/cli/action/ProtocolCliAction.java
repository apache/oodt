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
import java.net.URISyntaxException;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.auth.NoAuthentication;
import org.apache.oodt.cas.protocol.config.SpringProtocolConfig;
import org.apache.oodt.cas.protocol.system.ProtocolManager;

/**
 * Action used to perform some task via a {@link ProtocolManager}
 * 
 * @author bfoster (Brian Foster)
 */
public abstract class ProtocolCliAction extends CmdLineAction {

   private String user;
   private String pass;
   private String site;
   private ProtocolManager protocolManager;

   public void setUser(String user) {
      this.user = user;
   }

   public void setPass(String pass) {
      this.pass = pass;
   }

   public void setSite(String site) {
      this.site = site;
   }

   public URI getSite() throws URISyntaxException {
      return site != null ? new URI(site) : null;
   }

   public Authentication getAuthentication() {
      if (user == null || pass == null) {
         return new NoAuthentication();
      } else {
         return new BasicAuthentication(user, pass);
      }
   }

   public void setProtocolManager(ProtocolManager protocolManager) {
      this.protocolManager = protocolManager;
   }

   public ProtocolManager getProtocolManager() throws Exception {
      if (protocolManager != null) {
         return protocolManager;
      }
      String protocolConfig = PathUtils
            .doDynamicReplacement(System
                  .getProperty(
                        "org.apache.oodt.cas.protocol.manager.config.file",
                        "classpath:/org/apache/oodt/cas/protocol/protocol-config.xml"));
      return protocolManager = new ProtocolManager(new SpringProtocolConfig(
            protocolConfig));
   }
}

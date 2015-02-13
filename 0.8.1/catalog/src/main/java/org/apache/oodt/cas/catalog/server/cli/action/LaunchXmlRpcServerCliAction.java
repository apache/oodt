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
package org.apache.oodt.cas.catalog.server.cli.action;

//OODT imports
import org.apache.oodt.cas.catalog.server.channel.xmlrpc.XmlRpcCommunicationChannelServerFactory;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceLocalFactory;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * 
 * @author bfoster (Brian Foster)
 */
public class LaunchXmlRpcServerCliAction extends CatalogServiceServerCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         printer.println("Starting XML-RPC server on port " + getPort());
         getXmlRpcFactory().createCommunicationChannelServer().startup();
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to startup server : "
               + e.getMessage(), e);
      }
   }

   protected XmlRpcCommunicationChannelServerFactory getXmlRpcFactory()
         throws InstantiationException, IllegalAccessException,
         ClassNotFoundException {
      XmlRpcCommunicationChannelServerFactory factory = new XmlRpcCommunicationChannelServerFactory();
      factory.setPort(getPort());
      factory.setCatalogServiceFactory(getCatalogServiceFactory());
      return factory;
   }

   protected CatalogServiceLocalFactory getCatalogServiceFactory()
         throws InstantiationException, IllegalAccessException,
         ClassNotFoundException {
      CatalogServiceLocalFactory factory = new CatalogServiceLocalFactory();
      factory.setTransactionIdFactory(getTransactionId());
      factory.setCatalogRepositoryFactory(getRepository());
      factory.setIngestMapperFactory(getIngestMapper());
      factory.setRestrictQueryPermissions(getRestrictQueryPermissions());
      factory.setRestrictIngestPermissions(getRestrictIngestPermissions());
      factory.setOneCatalogFailsAllFail(getOneCatalogFailsAllFail());
      factory.setSimplifyQueries(getSimplifyQueries());
      factory.setDisableIntersectingCrossCatalogQueries(
            getDisableIntersectingCrossCatalogQueries());
      factory.setCrossCatalogResultSortingThreshold(
            getCrossCatalogResultSortingThreshold());
      return factory;
   }
}

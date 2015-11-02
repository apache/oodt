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
package org.apache.oodt.cas.catalog.cli.action;

//OODT imports
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelClientFactory;
import org.apache.oodt.cas.catalog.server.channel.xmlrpc.XmlRpcCommunicationChannelClientFactory;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;
import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClientFactory;
import org.apache.oodt.cas.cli.action.CmdLineAction;

/**
 * Base Catalog Service {@link CmdLineAction}.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class CatalogServiceCliAction extends CmdLineAction {

   public static final int VAL = 1024;
   public static final int VAL1 = 20;
   public static final int VAL2 = 60;
   private CatalogServiceClient client;

   private String getUrl() {
      return System.getProperty("org.apache.oodt.cas.catalog.url");
   }

   private int getChunkSize() {
      return Integer.getInteger("org.apache.oodt.cas.catalog.chunkSize", VAL);
   }

   private int getRequestTimeout() {
      return Integer.getInteger("org.apache.oodt.cas.catalog.requestTimeout", VAL1);
   }

   private int getConnectionTimeout() {
      return Integer.getInteger("org.apache.oodt.cas.catalog.connectionTimeout", VAL2);
   }

   private int getAutoPagerSize() {
      return Integer.getInteger("org.apache.oodt.cas.catalog.autoPagerSize", 1000);
   }

   public CatalogServiceClient getClient() {
      if (client == null) {
         CatalogServiceClientFactory factory = new CatalogServiceClientFactory();
         factory.setCommunicationChannelClientFactory(getCommunicationChannelFactory());
         factory.setAutoPagerSize(getAutoPagerSize());
         return client = factory.createCatalogService();
      } else {
         return client;
      }
   }

   private CommunicationChannelClientFactory getCommunicationChannelFactory() {
      XmlRpcCommunicationChannelClientFactory factory = new XmlRpcCommunicationChannelClientFactory();
      factory.setServerUrl(getUrl());
      factory.setChunkSize(getChunkSize());
      factory.setRequestTimeout(getRequestTimeout());
      factory.setConnectionTimeout(getConnectionTimeout());
      return factory;
   }
}

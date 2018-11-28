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
import org.apache.oodt.cas.catalog.mapping.IngestMapperFactory;
import org.apache.oodt.cas.catalog.repository.CatalogRepositoryFactory;
import org.apache.oodt.cas.cli.action.CmdLineAction;

/**
 * Base Catalog Service Server {@link CmdLineAction}.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class CatalogServiceServerCliAction extends CmdLineAction {

   public int getPort() {
      return Integer.getInteger("org.apache.oodt.cas.catalog.port");
   }

   public String getTransactionId() {
      return System.getProperty("org.apache.oodt.cas.catalog.transactionid");
   }

   public CatalogRepositoryFactory getRepository()
         throws InstantiationException, IllegalAccessException,
         ClassNotFoundException {
      return (CatalogRepositoryFactory) Class.forName(
            System.getProperty("org.apache.oodt.cas.catalog.repository"))
            .newInstance();
   }

   public IngestMapperFactory getIngestMapper() throws InstantiationException,
         IllegalAccessException, ClassNotFoundException {
      return (IngestMapperFactory) Class.forName(
            System.getProperty("org.apache.oodt.cas.catalog.ingestmapper"))
            .newInstance();
   }

   public boolean getRestrictQueryPermissions() {
      return Boolean
            .getBoolean("org.apache.oodt.cas.catalog.restrict.query.permissions");
   }

   public boolean getRestrictIngestPermissions() {
      return Boolean
            .getBoolean("org.apache.oodt.cas.catalog.restrict.ingest.permissions");
   }

   public boolean getOneCatalogFailsAllFail() {
      return Boolean
            .getBoolean("org.apache.oodt.cas.catalog.oneCatalogFailsAllFail");
   }

   public boolean getSimplifyQueries() {
      return Boolean.getBoolean("org.apache.oodt.cas.catalog.simplifyQueries");
   }

   public boolean getDisableIntersectingCrossCatalogQueries() {
      return Boolean
            .getBoolean("org.apache.oodt.cas.catalog.disableIntersectingCrossCatalogQueries");
   }

   public int getCrossCatalogResultSortingThreshold() {
      return Integer
            .getInteger("org.apache.oodt.cas.catalog.crossCatalogResultSortingThreshold");
   }
}

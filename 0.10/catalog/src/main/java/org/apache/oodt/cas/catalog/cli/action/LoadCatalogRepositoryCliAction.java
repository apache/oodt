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

//JDK imports
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.catalog.repository.CatalogRepository;
import org.apache.oodt.cas.catalog.repository.CatalogRepositoryFactory;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.Serializer;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * A {@link CmdLineAction} which allows {@link Catalog}s to be loaded from
 * an existing {@link CatalogRepositoryFactory} and to be add to a running
 * Catalog Service from an existing Spring bean XML file.
 *
 * @author bfoster (Brian Foster)
 */
public class LoadCatalogRepositoryCliAction extends CatalogServiceCliAction {

   protected String catalogRepositoryId;
   protected String beanRepo;

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         Validate.notNull(catalogRepositoryId, "Must specify catalogRepositoryId");
         Validate.notNull(beanRepo, "Must specify beanRepo");

         FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(
               new String[] { this.beanRepo }, false);
         appContext.setClassLoader(new Serializer().getClassLoader());
         appContext.refresh();
         CatalogRepositoryFactory factory = (CatalogRepositoryFactory) appContext
               .getBean(this.catalogRepositoryId, CatalogRepositoryFactory.class);
         CatalogRepository catalogRepository = factory.createRepository();
         Set<Catalog> catalogs = catalogRepository.deserializeAllCatalogs();
         printer.println("Deserialized Catalogs: " + catalogs.toString());
         for (Catalog catalog : catalogs) {
            printer.println("Adding Catalog: " + catalog);
            getClient().addCatalog(catalog);
         }
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to load catalogs from bean repo : " + e.getMessage(), e);
      }
   }

   public void setCatalogRepositoryId(String catalogRepositoryId) {
      this.catalogRepositoryId = catalogRepositoryId;
   }

   public void setBeanRepo(String beanRepo) {
      this.beanRepo = beanRepo;
   }
}

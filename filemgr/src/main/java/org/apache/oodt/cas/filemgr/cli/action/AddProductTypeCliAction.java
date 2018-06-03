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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * A {@link CmdLineAction} which adds a {@link ProductType} to the file manager.
 * 
 * @author bfoster (Brian Foster)
 * @author riverma (Rishi Verma)
 */
public class AddProductTypeCliAction extends FileManagerCliAction {

   private String productTypeId;
   private String productTypeName;
   private String productTypeDescription;
   private String fileRepositoryPath;
   private String versioner;

   @Override
   public void execute(ActionMessagePrinter printer) throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         Validate.notNull(productTypeId, "Must specify productTypeId");
         Validate.notNull(productTypeName, "Must specify productTypeName");
         Validate.notNull(productTypeDescription,
               "Must specify productTypeDescription");
         Validate
               .notNull(fileRepositoryPath, "Must specify fileRepositoryPath");
         Validate.notNull(versioner, "Must specify versioner");

         ProductType type = new ProductType();
         type.setProductTypeId(productTypeId);
         type.setName(productTypeName);
         type.setDescription(productTypeDescription);
         type.setProductRepositoryPath(fileRepositoryPath);
         type.setVersioner(versioner);

         printer.println("addProductType: Result: " + client.addProductType(type));
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to add product type with "
               + "id '" + productTypeId + "', name '" + productTypeName + "', description '"
               + productTypeDescription + "', repository '"
               + fileRepositoryPath + ", and versioner '" + versioner + "' : "
               + e.getMessage(), e);
      }
   }

   public void setProductTypeId(String productTypeId) {
      this.productTypeId = productTypeId;
   }
	
   public void setProductTypeName(String productTypeName) {
      this.productTypeName = productTypeName;
   }

   public void setProductTypeDescription(String productTypeDescription) {
      this.productTypeDescription = productTypeDescription;
   }

   public void setFileRepositoryPath(String fileRepositoryPath) {
      this.fileRepositoryPath = fileRepositoryPath;
   }

   public void setVersioner(String versioner) {
      this.versioner = versioner;
   }
}

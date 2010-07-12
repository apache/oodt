/**
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

package org.apache.oodt.cas.catalog.repository;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogRepositoryException;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * 
 * A Factory for creating SerializedMemoryBasedCatalogRepository
 * 
 */
public class SerializedCatalogRepositoryFactory implements
    CatalogRepositoryFactory {

  protected String storageDir;

  public SerializedCatalogRepositoryFactory() {
  }

  @Required
  public void setStorageDir(String storageDir) {
    this.storageDir = storageDir;
  }

  public SerializedCatalogRepository createRepository()
      throws CatalogRepositoryException {
    try {
      return new SerializedCatalogRepository(PathUtils
          .doDynamicReplacement(this.storageDir));
    } catch (Exception e) {
      throw new CatalogRepositoryException(
          "Failed to create Serialized Catalog Repository : " + e.getMessage(),
          e);
    }
  }

}

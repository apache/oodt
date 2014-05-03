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
package org.apache.oodt.cas.filemgr.versioning;

// JDK imports
import java.util.Properties;

// OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;

// Google imports
import com.google.common.base.Strings;

/**
 * A {@link MetadataBasedFileVersioner} which is configurable via java properties.  Set the
 * following java property to configure file_spec per product type:
 *    org.apache.oodt.cas.filemgr.versioning.configuration.<product_type_name>
 * Or set the following java property to configure all product types:
 *    org.apache.oodt.cas.filemgr.versioning.configuration.all_product_types
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
public class ConfigurableMetadataBasedFileVersioner extends MetadataBasedFileVersioner {

  private static final String BASE_PROPERTY =
      "org.apache.oodt.cas.filemgr.versioning.configuration.";
  private static final String ALL = "all_product_types";

  private final Properties properties;

  public ConfigurableMetadataBasedFileVersioner() {
    this(System.getProperties());
  }

  public ConfigurableMetadataBasedFileVersioner(Properties properties) {
    this.properties = properties;
  }

  @Override
  public void createDataStoreReferences(Product product, Metadata metadata)
      throws VersioningException {
    setFilePathSpec(getFilePathSpec(product.getProductType()));
    super.createDataStoreReferences(product, metadata);
  }

  private String getFilePathSpec(ProductType productType) throws VersioningException {
    String fileSpec = properties.getProperty(BASE_PROPERTY + productType.getName().toLowerCase());
    if (Strings.isNullOrEmpty(fileSpec)) {
      fileSpec = properties.getProperty(BASE_PROPERTY + ALL);
      if (Strings.isNullOrEmpty(fileSpec)) {
        throw new VersioningException("Not defined for product type " + productType.getName());
      }
    }
    return fileSpec;
  }
}

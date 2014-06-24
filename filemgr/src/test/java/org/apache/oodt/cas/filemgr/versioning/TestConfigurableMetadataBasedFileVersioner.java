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

// JUnit static imports
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

// JDK imports
import java.util.Properties;

// OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;

// JUnit imports
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

// Google imports
import com.google.common.collect.Lists;

/**
 * Test class for {@link ConfigurableMetadataBasedFileVersioner}.
 *
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class TestConfigurableMetadataBasedFileVersioner {

  @Rule public ExpectedException expectedException = ExpectedException.none();

  private Properties properties;
  private ConfigurableMetadataBasedFileVersioner versioner;
  private Metadata metadata;
  private Product product;

  @Before
  public void setUp() {
    properties = new Properties();
    properties.setProperty("org.apache.oodt.cas.filemgr.versioning.configuration.test_type",
        "/[Year]/[Month]/[Day]/[Filename]");
    versioner = new ConfigurableMetadataBasedFileVersioner(properties);
    metadata = new Metadata();
    metadata.addMetadata("Year", "2013");
    metadata.addMetadata("Month", "03");
    metadata.addMetadata("Day", "23");
    metadata.addMetadata("Filename", "test.dat");
    product = new Product();
    product.setProductStructure(Product.STRUCTURE_FLAT);
    ProductType pt = new ProductType();
    pt.setProductRepositoryPath("file:/base/path");
    pt.setName("TEST_TYPE");
    product.setProductType(pt);
    Reference ref = new Reference();
    ref.setOrigReference("/path/to/file");
    product.setProductReferences(Lists.newArrayList(ref));
  }

  @Test
  public void testVersioningForProductType() throws VersioningException {
    versioner.createDataStoreReferences(product, metadata);

    assertThat(product.getProductReferences().size(), is(1));
    assertThat(product.getProductReferences().get(0).getDataStoreReference(),
        is("file:/base/path/2013/03/23/test.dat"));
  }

  @Test
  public void testVersioningForProductTypeNotDefined() throws VersioningException {
    ProductType pt = new ProductType();
    pt.setProductRepositoryPath("file:/base/path");
    pt.setName("TEST_TYPE2");
    product.setProductType(pt);

    expectedException.expect(VersioningException.class);
    versioner.createDataStoreReferences(product, metadata);
  }

  @Test
  public void testVersioningForAllProductTypesDoesntOverrideSpecific() throws VersioningException {
    properties.setProperty("org.apache.oodt.cas.filemgr.versioning.configuration.all_product_types",
        "/[Year]/[Month]/[Filename]");

    versioner.createDataStoreReferences(product, metadata);
    
    assertThat(product.getProductReferences().size(), is(1));
    assertThat(product.getProductReferences().get(0).getDataStoreReference(),
        is("file:/base/path/2013/03/23/test.dat"));
  }

  @Test
  public void testVersioningForAllProductTypes() throws VersioningException {
    properties.setProperty("org.apache.oodt.cas.filemgr.versioning.configuration.all_product_types",
        "/[Year]/[Month]/[Filename]");

    ProductType pt = new ProductType();
    pt.setProductRepositoryPath("file:/base/path");
    pt.setName("TEST_TYPE2");
    product.setProductType(pt);

    versioner.createDataStoreReferences(product, metadata);

    assertThat(product.getProductReferences().size(), is(1));
    assertThat(product.getProductReferences().get(0).getDataStoreReference(),
        is("file:/base/path/2013/03/test.dat"));
  }
}

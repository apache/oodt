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

package org.apache.oodt.cas.filemgr.catalog;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 * 
 *          <p>
 *          Describe your class here
 *          </p>
 *          .
 */
public class TestOrderedDataSourceCatalog extends TestDataSourceCatalog {

  /**
     * 
     */
  public TestOrderedDataSourceCatalog() {
    super();
    System.setProperty(
        "org.apache.oodt.cas.filemgr.catalog.datasource.orderedValues", "true");
    setCatalog(getCatalog());

  }

  protected Catalog getCatalog() {
    try {
      return new DataSourceCatalogFactory().createCatalog();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.filemgr.catalog.TestDataSourceCatalog#getSchemaPath()
   */
  @Override
  protected String getSchemaPath() {
    return "./src/testdata/testcat.ordered.sql";
  }

  public void testOrdering() {
    Product testProduct = getTestProduct();
    Metadata testMet = getTestMetadata("test");

    try {
      myCat.addProduct(testProduct);
      myCat.addMetadata(testMet, testProduct);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    Product retProduct;
    try {
      Metadata retMet = myCat.getMetadata(testProduct);
      assertNotNull(retMet);
      assertNotNull(retMet.getAllMetadata("CAS.ProductName"));
      assertEquals(3, retMet.getAllMetadata("CAS.ProductName").size());
      assertEquals("test", retMet.getAllMetadata("CAS.ProductName").get(0));
      assertEquals("test2", retMet.getAllMetadata("CAS.ProductName").get(1));
      assertEquals("test3", retMet.getAllMetadata("CAS.ProductName").get(2));
    } catch (Exception e) {
      fail(e.getMessage());
    }

  }

  private static Product getTestProduct() {
    Product testProduct = Product.getDefaultFlatProduct("test",
        "urn:oodt:GenericFile");
    testProduct.getProductType().setName("GenericFile");
    return testProduct;
  }

  private static Metadata getTestMetadata(String prodName) {
    Metadata met = new Metadata();
    met.addMetadata("CAS.ProductName", prodName);
    met.addMetadata("CAS.ProductName", prodName + "2");
    met.addMetadata("CAS.ProductName", prodName + "3");
    return met;
  }

}

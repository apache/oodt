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

package org.apache.oodt.cas.filemgr.versioning;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for ProductTypeMetVersioner.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TestProductTypeMetVersioner extends TestCase {

  private static final String expected = "file:/data/archive/130622/somefile.txt";

  public void testVersion() {
    Metadata met = new Metadata();
    met.addMetadata("Filename", "somefile.txt");
    met.addMetadata("AcquisitionDate", "130622");
    Product prod = Product.getDefaultFlatProduct("foo", "urn:sometype:foo");
    prod.getProductType().setProductRepositoryPath("file:///data/archive");
    prod.getProductType().getTypeMetadata()
        .addMetadata("filePathSpec", "/[AcquisitionDate]/[Filename]");
    prod.getProductReferences().add(
        new Reference("file:///data/staging/somefile.txt", null, 4L));
    ProductTypeMetVersioner versioner = new ProductTypeMetVersioner();
    try {
      versioner.createDataStoreReferences(prod, met);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    assertNotNull(prod);
    assertNotNull(prod.getProductReferences());
    assertEquals(1, prod.getProductReferences().size());
    assertEquals(expected, prod.getProductReferences().get(0)
        .getDataStoreReference());

  }

}

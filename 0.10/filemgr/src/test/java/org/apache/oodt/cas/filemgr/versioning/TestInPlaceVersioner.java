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

//Junit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * Test suite for the {@link InPlaceVersioner}.
 * 
 * @author davoodi
 * @author mattmann
 * 
 */
public class TestInPlaceVersioner extends TestCase {

  /**
   * @since OODT-108
   */
  public void testVersionerFlat() {
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    Reference r = new Reference("file:///tmp/test.txt", null, 0L);
    p.getProductReferences().add(r);
    InPlaceVersioner versioner = new InPlaceVersioner();
    Metadata met = new Metadata();
    try {
      versioner.createDataStoreReferences(p, met);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertTrue(r.getDataStoreReference().equals(r.getOrigReference()));
  }

  /**
   * @since OODT-108
   */
  public void testVersionerHierarchical() {
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    p.setProductStructure(Product.STRUCTURE_HIERARCHICAL);
    Reference r = new Reference("file:///tmp", null, 0L);
    Reference r2 = new Reference("file:///tmp/file1.txt", null, 4096L);
    Reference r3 = new Reference("file:///tmp/file2.txt", null, 4096L);
    p.getProductReferences().add(r);
    p.getProductReferences().add(r2);
    p.getProductReferences().add(r3);
    InPlaceVersioner versioner = new InPlaceVersioner();
    Metadata met = new Metadata();
    try {
      versioner.createDataStoreReferences(p, met);
    } catch (Exception e) {
      fail(e.getMessage());
    }

    for (Reference ref : p.getProductReferences()) {
      assertEquals(ref.getDataStoreReference(), ref.getOrigReference());
    }
  }
}

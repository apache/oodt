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


package org.apache.oodt.cas.filemgr.structs;

//OODT imports

import org.apache.oodt.commons.xml.XMLUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

//JDK imports
//Junit imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test cases for serializing and deserializing XML representations of
 * {@link Product}s.
 * </p>.
 */
public class TestProduct extends TestCase {

  private static Logger LOG = Logger.getLogger(TestProduct.class.getName());
  private Properties initialProperties = new Properties(System.getProperties());

  public void setUp() throws Exception {
    Properties properties = new Properties(System.getProperties());

    URL url = this.getClass().getResource("/mime-types.xml");
    properties.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
        new File(url.getFile()).getAbsolutePath());

    System.setProperties(properties);
  }

  public void tearDown() throws Exception {
    System.setProperties(initialProperties);
  }

  /**
   * @since OODT-41
   */
  public void testSetTransferStatus() {
    Product p = new Product("foo", new ProductType(), Product.STRUCTURE_FLAT,
        Product.STATUS_TRANSFER, Collections.EMPTY_LIST);
    assertNotNull(p);
    assertEquals(p.getTransferStatus(), Product.STATUS_TRANSFER);
  }

  public void testReadWriteProductWithProductType() {
    Product p = new Product();
    p.setProductId("testId");
    p.setProductName("TestProduct");
    p.setProductStructure("Flat");
    p.setTransferStatus(Product.STATUS_RECEIVED);
    ProductType type = new ProductType();
    type.setName("BogusType");
    p.setProductType(type);

    Reference r = new Reference();
    r.setDataStoreReference("file:///data.store");
    r.setOrigReference("file:///data.orig");
    r.setFileSize(99999L);
    p.getProductReferences().add(r);
    readWrite(p);
  }

  public void testReadWriteProductNoProductType() {
    Product p = new Product();
    p.setProductId("testId");
    p.setProductName("TestProduct");
    p.setProductStructure("Flat");
    p.setTransferStatus(Product.STATUS_RECEIVED);

    Reference r = new Reference();
    r.setDataStoreReference("file:///data.store");
    r.setOrigReference("file:///data.orig");
    r.setFileSize(99999L);
    p.getProductReferences().add(r);
    readWrite(p);

  }

  private void readWrite(Product product) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      XMLUtils.writeXmlToStream(product.toXML(), os);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      fail(e.getMessage());
    }

    Product readProduct = null;

    try {
      readProduct = new Product(new ByteArrayInputStream(os.toByteArray()));
    } catch (InstantiationException e) {
      fail(e.getMessage());
    }

    assertNotNull(readProduct);
    assertEquals("testId", readProduct.getProductId());
    assertEquals("TestProduct", readProduct.getProductName());
    assertEquals(Product.STRUCTURE_FLAT, readProduct.getProductStructure());
    assertEquals(Product.STATUS_RECEIVED, readProduct.getTransferStatus());
    assertNotNull(readProduct.getProductType());
    assertNotNull(readProduct.getProductType().getName(), "BogusType");
    assertNotNull(readProduct.getProductReferences());
    assertEquals(1, readProduct.getProductReferences().size());
    Reference readRef = (Reference) readProduct.getProductReferences().get(0);
    assertNotNull(readRef);
    assertEquals(readRef.getOrigReference(), "file:///data.orig");
    assertEquals(readRef.getDataStoreReference(), "file:///data.store");
    assertEquals(readRef.getFileSize(), 99999L);
  }

}

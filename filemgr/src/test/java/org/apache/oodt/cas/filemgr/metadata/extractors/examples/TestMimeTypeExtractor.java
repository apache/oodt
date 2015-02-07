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

package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

//Junit imports
import java.io.File;
import java.net.URL;
import java.util.Properties;
//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the {@link MimeTypeExtractor}.
 * 
 * @since OODT-58
 * 
 */
public class TestMimeTypeExtractor extends TestCase implements CoreMetKeys {

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
   * @since OODT-58
   */
  public void testExtract() {
    MimeTypeExtractor extractor = new MimeTypeExtractor();
    Product p = Product.getDefaultFlatProduct("test", "urn:oodt:GenericFile");
    Reference r = new Reference("file:///tmp/test.he5",
        "file:///archive/test.he5/test.he5", 0L);
    p.getProductReferences().add(r);
    Metadata met = new Metadata();
    try {
      met = extractor.doExtract(p, met);
    } catch (MetExtractionException e) {
      fail(e.getMessage());
    }

    assertNotNull(met);
    assertNotNull(met.getAllMetadata(MIME_TYPE));
    assertEquals(3, met.getAllMetadata(MIME_TYPE).size());
    System.out.println(met.getAllMetadata(MIME_TYPE));
    assertEquals("application/x-hdf", met.getAllMetadata(MIME_TYPE).get(0));
    assertEquals("application", met.getAllMetadata(MIME_TYPE).get(1));
    assertEquals("x-hdf", met.getAllMetadata(MIME_TYPE).get(2));
  }

}

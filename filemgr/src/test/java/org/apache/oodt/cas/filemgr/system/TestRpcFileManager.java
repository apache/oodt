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

package org.apache.oodt.cas.filemgr.system;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;
//OODT imports
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.metadata.ProductMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * Test harness for the XmlRpcFileManagerServer.
 * 
 * @since OODT-72
 * 
 */
@Deprecated
public class TestRpcFileManager extends TestCase {

  private static final int FM_PORT = 50002;

  private FileManagerServer fm;

  private String luceneCatLoc;

  private static final String transferServiceFacClass = "org.apache.oodt.cas."
      + "filemgr.datatransfer.LocalDataTransferFactory";

  private Properties initialProperties = new Properties(
      System.getProperties());

  /**
   * @since OODT-72
   */
  public void testExpandProductMet() {
    FileManagerClient fmc = null;
    try {
      fmc = RpcCommunicationFactory.createClient(new URL("http://localhost:" + FM_PORT));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    Metadata met = null;
    try {
      met = fmc.getMetadata(fmc.getProductByName("test.txt"));
    } catch (CatalogException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    assertNotNull(met);
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ID));
    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_ID));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_NAME));
    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_NAME));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_STRUCTURE));
    assertEquals("Flat", met.getMetadata(ProductMetKeys.PRODUCT_STRUCTURE));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_TRANSFER_STATUS));
    assertEquals(Product.STATUS_RECEIVED, met
        .getMetadata(ProductMetKeys.PRODUCT_TRANSFER_STATUS));

    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ORIG_REFS));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_DATASTORE_REFS));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_FILE_SIZES));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_MIME_TYPES));

    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_ORIG_REFS).size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS)
        .size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_FILE_SIZES)
        .size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_MIME_TYPES)
        .size());

    URL refUrl = this.getClass().getResource("/ingest/test.txt");

    String origPath = null;
    try {
      origPath = new File(refUrl.getFile()).getCanonicalPath();
    } catch (IOException e) {
      fail(e.getMessage());
    }
    assertEquals(origPath, met.getMetadata(ProductMetKeys.PRODUCT_ORIG_REFS));
    assertEquals("/tmp/test.txt/test.txt", met
        .getMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS));

    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_FILE_SIZES));
    assertEquals("text/plain", met
        .getMetadata(ProductMetKeys.PRODUCT_MIME_TYPES));

    try {
      met = fmc.getReducedMetadata(fmc.getProductByName("test.txt"),
          Collections.EMPTY_LIST);
    } catch (CatalogException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    assertNotNull(met);
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ID));
    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_ID));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_NAME));
    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_NAME));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_STRUCTURE));
    assertEquals("Flat", met.getMetadata(ProductMetKeys.PRODUCT_STRUCTURE));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_TRANSFER_STATUS));
    assertEquals(Product.STATUS_RECEIVED, met
        .getMetadata(ProductMetKeys.PRODUCT_TRANSFER_STATUS));

    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_ORIG_REFS));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_DATASTORE_REFS));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_FILE_SIZES));
    assertTrue(met.containsKey(ProductMetKeys.PRODUCT_MIME_TYPES));

    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_ORIG_REFS).size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS)
        .size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_FILE_SIZES)
        .size());
    assertEquals(1, met.getAllMetadata(ProductMetKeys.PRODUCT_MIME_TYPES)
        .size());

    origPath = null;
    try {
      origPath = new File(refUrl.getFile()).getCanonicalPath();
    } catch (IOException e) {
      fail(e.getMessage());
    }
    assertEquals(origPath, met.getMetadata(ProductMetKeys.PRODUCT_ORIG_REFS));
    assertEquals("/tmp/test.txt/test.txt", met
        .getMetadata(ProductMetKeys.PRODUCT_DATASTORE_REFS));

    assertNotNull(met.getMetadata(ProductMetKeys.PRODUCT_FILE_SIZES));
    assertEquals("text/plain", met
        .getMetadata(ProductMetKeys.PRODUCT_MIME_TYPES));

  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    startXmlRpcFileManager();
    ingestTestFile();
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    fm.shutdown();
    fm = null;

    // blow away lucene cat
    deleteAllFiles(luceneCatLoc);

    // blow away test file
    deleteAllFiles("/tmp/test.txt");

    // Reset the System properties to initial values.
    System.setProperties(initialProperties);
  }

  private void deleteAllFiles(String startDir) {
    File startDirFile = new File(startDir);
    File[] delFiles = startDirFile.listFiles();

    if (delFiles != null && delFiles.length > 0) {
      for (int i = 0; i < delFiles.length; i++) {
        delFiles[i].delete();
      }
    }

    startDirFile.delete();

  }

  private void ingestTestFile() {
    Metadata prodMet = null;
    StdIngester ingester = new StdIngester(transferServiceFacClass);

    try {
      URL ingestUrl = this.getClass().getResource("/ingest");
      URL refUrl = this.getClass().getResource("/ingest/test.txt");
      URL metUrl = this.getClass().getResource("/ingest/test.txt.met");

      prodMet = new SerializableMetadata(new FileInputStream(
        new File(metUrl.getFile())));
      // now add the right file location
      prodMet.addMetadata(CoreMetKeys.FILE_LOCATION, new File(ingestUrl.getFile()).getCanonicalPath());
      prodMet.addMetadata(CoreMetKeys.FILENAME, "test.txt");
      prodMet.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
      ingester.ingest(new URL("http://localhost:" + FM_PORT), new File(refUrl.getFile()), prodMet);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  private void startXmlRpcFileManager() {

    Properties properties = new Properties(System.getProperties());

    // first make sure to load properties for the file manager
    // and make sure to load logging properties as well

    // set the log levels
    URL loggingPropertiesUrl = this.getClass().getResource(
      "/test.logging.properties");
    properties.setProperty("java.util.logging.config.file", new File(
      loggingPropertiesUrl.getFile()).getAbsolutePath());

    // first load the example configuration
    try {
      URL filemgrPropertiesUrl = this.getClass().getResource(
        "/filemgr.properties");
      properties.load(
        new FileInputStream(new File(filemgrPropertiesUrl.getFile())));
    } catch (Exception e) {
      fail(e.getMessage());
    }

    // override the catalog to use: we'll use lucene
    try {
      URL ingestUrl = this.getClass().getResource("/ingest");
      luceneCatLoc = new File(ingestUrl.getFile()).getCanonicalPath() + "/cat";
    } catch (Exception e) {
      fail(e.getMessage());
    }

    properties.setProperty("filemgr.catalog.factory",
        "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
    properties.setProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
        luceneCatLoc);

    // now override the repo mgr policy
    URL fmpolicyUrl = this.getClass().getResource("/ingest/fmpolicy");
    try {
      properties.setProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs",
        "file://" + new File(fmpolicyUrl.getFile()).getCanonicalPath());
    } catch (Exception e) {
      fail(e.getMessage());
    }

    // now override the val layer ones
    properties.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
      "file://" + new File(fmpolicyUrl.getFile()).getAbsolutePath());

    // set up mime repo path
    URL mimeTypesUrl = this.getClass().getResource("/mime-types.xml");
    properties.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
      new File(mimeTypesUrl.getFile()).getAbsolutePath());

    // override expand product met
    properties.setProperty("org.apache.oodt.cas.filemgr.metadata.expandProduct",
      "true");

    System.setProperties(properties);

    try {
      fm = RpcCommunicationFactory.createServer(FM_PORT);
      fm.startUp();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}

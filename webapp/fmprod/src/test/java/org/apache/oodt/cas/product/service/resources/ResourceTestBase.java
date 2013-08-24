/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.oodt.cas.product.service.resources;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base setup for JUnit test classes for JAX-RS resources.  On startup, it sets
 * up a local transport server with mock context and starts a file manager and
 * ingests some test products.  On teardown it stops the file manager and
 * deletes the ingested products.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ResourceTestBase
{
  // The URL of the file manager.
  private static final int FM_PORT = 50001;
  private static final String FM_URL = "http://localhost:" + FM_PORT;

  // The URL of the web server.
  protected static final String SERVER_URL = "local://service";

  // The file manager and client.
  private static XmlRpcFileManager fileManager;
  private static XmlRpcFileManagerClient client;

  // The web server.
  private static Server server;

  // The file manager's catalog directory.
  private static File catalogDir;

  // The file manager's repository directory.
  private static File repositoryDir;

  // The file manager's logs directory.
  private static File logsDir;

  // The file manager's working directory.
  private static File workingDir;

  // The type of data transfer factory used by the ingester.
  private static final String TRANSFER_FACTORY =
    "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  // Strings used to store IDs of ingested products.
  private static String genericFileFlatProductId;
  private static String genericFileHierarchicalProductId;
  private static String locationAwareFlatProductId;



  /**
   * Sets up a file manager, ingests test data into the file manager's catalog
   * and initializes a file manager client.
   * @throws Exception exceptions such as MalformedURLException, IOException
   * and IngestException could occur when setting up the file manager and client
   * and ingesting data.  Any such exception is considered an overall failure
   * and is therefore propagated upwards for JUnit to handle
   */
  @BeforeClass
  public static void startUpFileManager() throws Exception
  {
    initializeFileManager();
    ingestTestData();
    client = new XmlRpcFileManagerClient(new URL(FM_URL));
  }



  /**
   * Stops the file manager and destroys the ingested test data.
   * @throws Exception (IOException) if the file manager catalog
   * cannot be deleted.  This is considered an overall failure and so it is
   * propagated upwards for JUnit to handle
   */
  @AfterClass
  public static void shutDownFileManager() throws Exception
  {
    // Shut down the file manager.
    fileManager.shutdown();

    // Destroy the ingested test data.
    FileUtils.deleteDirectory(catalogDir);

    // Clean up the repository, logs and workingDir directories.
    FileUtils.cleanDirectory(repositoryDir);
    FileUtils.cleanDirectory(logsDir);
    FileUtils.cleanDirectory(workingDir);
  }



  /**
   * Starts a web server using the local transport protocol and uses a mock
   * servlet context to inject context parameters into the JAX-RS resource
   * under test.
   * @param resource the resource to associate with the server and servlet
   * context
   */
  public static void startWebServer(Resource resource)
  {
    // Create a web server for testing using local transport.
    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
    sf.setTransportId(LocalTransportFactory.TRANSPORT_ID);
    sf.setServiceBean(resource);
    sf.setAddress(SERVER_URL);
    server = sf.create();

    // Use a mock servlet context for the resource.
    // This is done after creating the server to avoid being overwritten by
    // the server's default context.
    ServletContext mockContext = EasyMock.createNiceMock(ServletContext.class);
    EasyMock.expect(mockContext.getAttribute("client"))
      .andReturn(getClient()).anyTimes();
    EasyMock.expect(mockContext.getAttribute("workingDir"))
      .andReturn(getWorkingDir()).anyTimes();
    EasyMock.replay(mockContext);
    resource.setServletContext(mockContext);
  }



  /**
   * Shuts down the web server.
   */
  public static void stopWebServer()
  {
    // Stop the server.
    server.stop();
    server.destroy();
  }



  /**
   * Sets up and starts a file manager.
   * @throws Exception
   */
  private static void initializeFileManager() throws Exception
  {
    String fileMgrDirLocation = "./src/test/resources/filemgr";

    // Initialize file manager directories.
    catalogDir = new File(new File(fileMgrDirLocation + "/catalog")
      .getCanonicalPath());
    repositoryDir = new File(new File(fileMgrDirLocation + "/repository")
      .getCanonicalPath());
    logsDir = new File(new File(fileMgrDirLocation + "/logs")
      .getCanonicalPath());
    workingDir = new File(new File(fileMgrDirLocation + "/workingDir")
      .getCanonicalPath());

    // Set properties for the file manager.
    FileInputStream fis = new FileInputStream(fileMgrDirLocation +
      "/etc/filemgr.properties");
    System.getProperties().load(fis);
    fis.close();

    System.setProperty("java.util.logging.config.file",
      new File(fileMgrDirLocation + "/etc/logging.properties")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs",
      "file://" + new File(fileMgrDirLocation + "/policy/core")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
      "file://" + new File(fileMgrDirLocation + "/policy/core")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
      new File(fileMgrDirLocation + "/etc/mime-types.xml")
        .getCanonicalPath());
    System.setProperty("filemgr.catalog.factory",
      "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
    System.setProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
      catalogDir.getAbsolutePath());

    // Start the file manager.
    fileManager = new XmlRpcFileManager(FM_PORT);

    // Overwrite the repository path for the GenericFile product type defined in
    // product-types.xml with a dynamically constructed repository location.
    Hashtable<String, Object> genericTypeHash = fileManager
      .getProductTypeByName("GenericFile");
    genericTypeHash.put("repositoryPath", "file://"
      + repositoryDir.getAbsolutePath());
    fileManager.addProductType(genericTypeHash);

    // Overwrite the repository path for the LocationAwareProduct product type
    // defined in product-types.xml with a dynamically constructed repository
    // location.
    Hashtable<String, Object> locationAwareTypeHash = fileManager
      .getProductTypeByName("LocationAwareProduct");
    locationAwareTypeHash.put("repositoryPath", "file://"
      + repositoryDir.getAbsolutePath());
    fileManager.addProductType(locationAwareTypeHash);
  }



  /**
   * Ingests test data into the file manager repository.
   * @throws Exception
   */
  private static void ingestTestData() throws Exception
  {
    // Ingest a flat product of type GenericFile.
    genericFileFlatProductId = ingestProduct(
      "./src/test/resources/filemgr/ingest/flat", "test.txt",
      "GenericFile", Product.STRUCTURE_FLAT);

    // Ingest a hierarchical product of type GenericFile.
    genericFileHierarchicalProductId = ingestProduct(
      "./src/test/resources/filemgr/ingest/hierarchical", "test",
      "GenericFile", Product.STRUCTURE_HIERARCHICAL);

    // Ingest a flat product of type LocationAwareProduct.
    locationAwareFlatProductId = ingestProduct(
      "./src/test/resources/filemgr/ingest/flat", "location.txt",
      "LocationAwareProduct", Product.STRUCTURE_FLAT);
  }



  /**
   * Creates a product with associated metadata and ingests it into the file
   * manager's repository.
   * @param productBaseDir the directory containing the product's file(s)
   * @param productName the product's name
   * @param productType the product's type, e.g. GenericFile
   * @param productStructure the product's structure, i.e. flat or hierarchical
   * @return the ID of the ingested product
   * @throws Exception
   */
  private static String ingestProduct(String productBaseDir, String productName,
    String productType, String productStructure) throws Exception
  {
    FileInputStream fis = new FileInputStream(productBaseDir + "/" + productName
      + ".met");
    Metadata meta = new SerializableMetadata(fis);
    fis.close();
    meta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File(productBaseDir).getCanonicalPath());
    meta.addMetadata(CoreMetKeys.FILENAME, productName);
    meta.addMetadata(CoreMetKeys.PRODUCT_TYPE, productType);
    meta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, productStructure);

    return new StdIngester(TRANSFER_FACTORY).ingest(new URL(FM_URL),
      new File(productBaseDir + "/" + productName), meta);
  }



  /**
   * Gets the product ID for the flat product of type GenericFile.
   * @return the genericFileFlatProductId
   */
  public static String getGenericFileFlatProductId()
  {
    return genericFileFlatProductId;
  }



  /**
   * Gets the product ID for the hierarchical product of type GenericFile.
   * @return the genericFileHierarchicalProductId
   */
  public static String getGenericFileHierarchicalProductId()
  {
    return genericFileHierarchicalProductId;
  }



  /**
   * Gets the product ID for the flat product of type LocationAwareProduct.
   * @return the locationAwareFlatProductId
   */
  public static String getLocationAwareFlatProductId()
  {
    return locationAwareFlatProductId;
  }



  /**
   * Gets the file manager client.
   * @return the file manager client
   */
  public static XmlRpcFileManagerClient getClient()
  {
    return client;
  }



  /**
   * Gets the file manager's working directory.
   * @return the file manager's working directory
   */
  public static File getWorkingDir()
  {
    return workingDir;
  }
}

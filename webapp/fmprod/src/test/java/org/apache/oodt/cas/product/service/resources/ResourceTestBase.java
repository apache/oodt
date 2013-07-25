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
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManager;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
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

  // The file manager.
  private static XmlRpcFileManager fileManager;

  // The location of the file manager's catalog.
  private static String catalogLocation;
  private static String repositoryLocation;
  private static String workingDirLocation;
  private static String logsLocation;

  // The type of data transfer factory used by the ingester.
  private static final String TRANSFER_FACTORY =
    "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  // Strings used to store IDs of ingested products.
  private static String genericFileFlatProductId;
  private static String genericFileHierarchicalProductId;
  private static String locationAwareFlatProductId;



  /**
   * Sets up a file manager and ingests test data into the file manager catalog.
   * @throws Exception exceptions such as MalformedURLException, IOException
   * and IngestException could occur when setting up the file manager and
   * ingesting data.  Any such exception is considered an overall failure and
   * is therefore propagated upwards for JUnit to handle
   */
  @BeforeClass
  public static void startUpFileManager() throws Exception
  {
    startFileManager();
    ingestTestData();
  }



  /**
   * Shuts down the web server, stops the file manager and destroys the ingested
   * test data.
   * @throws Exception An IOException is thrown if the file manager catalog
   * cannot be deleted.  This is considered an overall failure and so it is
   * propagated upwards for JUnit to handle
   */
  @AfterClass
  public static void shutDownFileManager() throws Exception
  {
    // Shut down the file manager.
    fileManager.shutdown();

    // Destroy the ingested test data.
    FileUtils.deleteDirectory(new File(catalogLocation));

    // Clean up the repository, workingDir and logs directories.
    FileUtils.cleanDirectory(new File(repositoryLocation));
    FileUtils.cleanDirectory(new File(workingDirLocation));
    FileUtils.cleanDirectory(new File(logsLocation));
  }



  /**
   * Sets up and starts a file manager.
   * @throws FileNotFoundException, IOException, Exception
   */
  private static void startFileManager() throws Exception
  {
    catalogLocation = new File("./src/test/resources/filemgr/catalog")
      .getCanonicalPath();
    repositoryLocation = new File("./src/test/resources/filemgr/repository")
      .getCanonicalPath();
    workingDirLocation = new File("./src/test/resources/filemgr/workingDir")
      .getCanonicalPath();
    logsLocation = new File("./src/test/resources/filemgr/logs")
      .getCanonicalPath();

    // Set properties for the file manager.
    FileInputStream fis = new FileInputStream(
      "./src/test/resources/filemgr/etc/filemgr.properties");
    System.getProperties().load(fis);
    fis.close();

    System.setProperty("java.util.logging.config.file",
      new File("./src/test/resources/filemgr/etc/logging.properties")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs",
      "file://" + new File("./src/test/resources/filemgr/policy/core")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.validation.dirs",
      "file://" + new File("./src/test/resources/filemgr/policy/core")
        .getCanonicalPath());
    System.setProperty("org.apache.oodt.cas.filemgr.mime.type.repository",
      new File("./src/test/resources/filemgr/etc/mime-types.xml")
        .getCanonicalPath());
    System.setProperty("filemgr.catalog.factory",
      "org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory");
    System.setProperty("org.apache.oodt.cas.filemgr.catalog.lucene.idxPath",
      catalogLocation);

    // Start the file manager.
    fileManager = new XmlRpcFileManager(FM_PORT);

    // Overwrite the repository path for the GenericFile product type defined in
    // product-types.xml with a dynamically constructed repository location.
    Hashtable<String, Object> genericTypeHash = fileManager
      .getProductTypeByName("GenericFile");
    genericTypeHash.put("repositoryPath", "file://" + repositoryLocation);
    fileManager.addProductType(genericTypeHash);


    // Overwrite the repository path for the LocationAwareProduct product type
    // defined in product-types.xml with a dynamically constructed repository location.
    Hashtable<String, Object> locationAwareTypeHash = fileManager
      .getProductTypeByName("LocationAwareProduct");
    locationAwareTypeHash.put("repositoryPath", "file://" + repositoryLocation);
    fileManager.addProductType(locationAwareTypeHash);
  }



  /**
   * Ingests test data into the file manager repository.
   * @throws FileNotFoundException, IOException, MalformedURLException,
   * IngestException
   */
  private static void ingestTestData() throws Exception
  {
    StdIngester ingester = new StdIngester(TRANSFER_FACTORY);

    // Ingest a flat product of type GenericFile.
    FileInputStream ffis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/flat/test.txt.met");
    Metadata fMeta = new SerializableMetadata(ffis);
    ffis.close();
    fMeta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/flat").getCanonicalPath());
    fMeta.addMetadata(CoreMetKeys.FILENAME, "test.txt");
    fMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    fMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, Product.STRUCTURE_FLAT);

    genericFileFlatProductId = ingester.ingest(new URL(FM_URL),
      new File("./src/test/resources/filemgr/ingest/flat/test.txt"), fMeta);


    // Ingest a hierarchical product of type GenericFile.
    FileInputStream hfis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/hierarchical/test.met");
    Metadata hMeta = new SerializableMetadata(hfis);
    hfis.close();
    hMeta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/hierarchical")
        .getCanonicalPath());
    hMeta.addMetadata(CoreMetKeys.FILENAME, "test");
    hMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
    hMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE,
      Product.STRUCTURE_HIERARCHICAL);

    genericFileHierarchicalProductId = ingester.ingest(new URL(FM_URL),
      new File("./src/test/resources/filemgr/ingest/hierarchical/test"),
        hMeta);


    // Ingest a flat product of type LocationAwareProduct.
    FileInputStream lfis = new FileInputStream(
      "./src/test/resources/filemgr/ingest/flat/location.txt.met");
    Metadata lMeta = new SerializableMetadata(lfis);
    lfis.close();
    lMeta.addMetadata(CoreMetKeys.FILE_LOCATION,
      new File("./src/test/resources/filemgr/ingest/flat").getCanonicalPath());
    lMeta.addMetadata(CoreMetKeys.FILENAME, "location.txt");
    lMeta.addMetadata(CoreMetKeys.PRODUCT_TYPE, "LocationAwareProduct");
    lMeta.addMetadata(CoreMetKeys.PRODUCT_STRUCTURE, Product.STRUCTURE_FLAT);

    locationAwareFlatProductId = ingester.ingest(new URL(FM_URL),
      new File("./src/test/resources/filemgr/ingest/flat/location.txt"), lMeta);
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
   * Gets the product ID for the flat product of type Location Aware Product.
   * @return the locationAwareFlatProductId
   */
  public static String getLocationAwareFlatProductId()
  {
    return locationAwareFlatProductId;
  }



  /**
   * Gets the file manager's URL.
   * @return the file manager's URL.
   */
  public static String getFileManagerUrl()
  {
    return FM_URL;
  }



  /**
   * Gets the file manager's working directory location.
   * @return the file manager's working directory location
   */
  public static String getWorkingDirLocation()
  {
    return workingDirLocation;
  }
}

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
package org.apache.oodt.cas.filemgr.datatransfer;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

//Junit imports
import junit.framework.TestCase;

/**
 * Test class for {@link LocalDataTransferer}.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class TestLocalDataTransferer extends TestCase {

   private LocalDataTransferer transfer;

   private File origFile;
   private File testDir;
   private File repoDir;
   private File repoFile;
   private File destDir;
   private File destFile;

   public void setUp() throws Exception {
      transfer = (LocalDataTransferer) new LocalDataTransferFactory()
         .createDataTransfer();
      URL url = this.getClass().getResource("/test.txt");
      origFile = new File(url.getFile());
      File testFile = File.createTempFile("test", ".txt");
      testDir = new File(testFile.getParentFile(), UUID.randomUUID().toString());
      repoDir = new File(testDir, "repo");
      if (!repoDir.mkdirs()) {
         throw new Exception("Failed to create repo directory!");
      }
      repoFile = new File(repoDir, "test.txt"); 
      destDir = new File(testDir, "dest");
      if (!destDir.mkdirs()) {
         throw new Exception("Failed to create destination directory!");
      }
      destFile = new File(destDir, "test.txt"); 
   }

   public void tearDown() throws Exception {
      FileUtils.forceDelete(testDir);
   }

   public void testTransferAndRetrieve() throws DataTransferException, IOException {
      Product testProduct = createDummyProduct();

      // Test transfer.
      transfer.transferProduct(testProduct);

      // Check that file was successfully transfered.
      assertTrue("Repo file does not exist", repoFile.exists());
      assertTrue("Repo file does not have the same contents as orig file",
            FileUtils.contentEquals(origFile, repoFile));

      // Test retrieve
      transfer.retrieveProduct(testProduct, destDir);

      // Check that file was successfully transfered.
      assertTrue("Destination file does not exist", destFile.exists());
      assertTrue("Destination file does not have the same contents as orig file",
            FileUtils.contentEquals(origFile, destFile));
   }

   private Product createDummyProduct() {
      Product testProduct = Product.getDefaultFlatProduct("test",
            "urn:oodt:GenericFile");
      testProduct.getProductReferences().add(
            new Reference(origFile.toURI().toString(), new File(repoDir,
                  "test.txt").toURI().toString(), origFile.length()));
      return testProduct;
   }
}

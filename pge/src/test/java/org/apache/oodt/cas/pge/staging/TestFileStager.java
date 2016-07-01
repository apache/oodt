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
package org.apache.oodt.cas.pge.staging;

//EasyMock static imports
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reportMatcher;
import static org.easymock.EasyMock.verify;

//JDK imports
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys;

//EasyMock imports
import org.easymock.IArgumentMatcher;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link FileStager}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestFileStager extends TestCase {

   public void testCreateFileManagerClient() throws Exception {
      // Test throws case.
      PgeMetadata pgeMetadata = createStrictMock(PgeMetadata.class);
      expect(pgeMetadata.getMetadata(PgeTaskMetKeys.QUERY_FILE_MANAGER_URL))
            .andReturn(null);
      replay(pgeMetadata);

      try {
         FileStager.createFileManagerClient(pgeMetadata);
         fail("Should have thrown Exception");
      } catch (Exception e) { /* expect throw */
      }
      verify(pgeMetadata);

      // Test success case.
      System.setProperty("filemgr.catalog.factory", "");
      System.setProperty("filemgr.repository.factory", "");
      int port = 9876;
      FileManagerServer filemgr = RpcCommunicationFactory.createServer(port);
      filemgr.startUp();
      String filemgrUrl = "http://localhost:" + port;
      pgeMetadata = createStrictMock(PgeMetadata.class);
      expect(pgeMetadata.getMetadata(PgeTaskMetKeys.QUERY_FILE_MANAGER_URL))
            .andReturn(filemgrUrl);
      replay(pgeMetadata);

      assertEquals(filemgrUrl, FileStager.createFileManagerClient(pgeMetadata)
            .getFileManagerUrl().toString());
      verify(pgeMetadata);
      filemgr.shutdown();
   }

   public void testGetProductReferences() throws CatalogException,
         URISyntaxException {
      String productId = "12345";

      String uri1 = "file:///path/to/file1";
      String uri2 = "file:///path/to/file2";
      Reference ref1 = new Reference();
      ref1.setDataStoreReference(uri1);
      Reference ref2 = new Reference();
      ref2.setDataStoreReference(uri2);

      FileManagerClient fmClient = createStrictMock(FileManagerClient.class);
      expect(fmClient.getProductReferences(ProductIdMatcher.eqProductId(productId))).andReturn(
            Lists.newArrayList(ref1, ref2));
      replay(fmClient);

      List<URI> uris = FileStager.getProductReferences(productId, fmClient);
      assertEquals(2, uris.size());
      assertTrue(uris.contains(new URI("file:///path/to/file1")));
      assertTrue(uris.contains(new URI("file:///path/to/file2")));

      verify(fmClient);
   }

   public void testAsURI() throws URISyntaxException {
      String absoluteHttpUri = "http://somewhere.com/path/to/data.dat";
      String absoluteFileUri = "file:///path/to/data.dat";
      String relativePath = "path/to/data.dat";
      String absolutePath = "/path/to/data.dat";

      assertEquals("http://somewhere.com/path/to/data.dat",
            FileStager.asURI(absoluteHttpUri).toString());
      assertEquals("file:///path/to/data.dat", FileStager
            .asURI(absoluteFileUri).toString());
      assertEquals("file://" + new File("").getAbsolutePath()
            + "/path/to/data.dat", FileStager.asURI(relativePath).toString());
      assertEquals("file:///path/to/data.dat", FileStager.asURI(absolutePath)
            .toString());
   }

   public static class ProductIdMatcher implements IArgumentMatcher {

      private String productId;

      public ProductIdMatcher(String productId) {
         this.productId = productId;
      }

      @Override
      public void appendTo(StringBuffer buffer) {
         buffer.append("eqProduct(");
         buffer.append(Product.class.getName());
         buffer.append(" with product id [");
         buffer.append(productId).append("])");
      }

      @Override
      public boolean matches(Object obj) {
         if (obj instanceof Product) {
            return productId.equals(((Product) obj).getProductId());
         }
         return false;
      }

      public static Product eqProductId(String productId) {
         reportMatcher(new ProductIdMatcher(productId));
         return null;
      }
   }
   
}

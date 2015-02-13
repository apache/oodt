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
package org.apache.oodt.cas.crawl;

//EasyMock static imports
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

//OODT imports
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.action.CrawlerActionRepo;
import org.apache.oodt.cas.crawl.status.IngestStatus;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.Metadata;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ProductCrawler}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestProductCrawler extends TestCase {

   private static final String CRAWLER_CONFIG =
      "src/main/resources/crawler-config.xml";

   // Case1:
   //  - Preconditions: fail
   public void testHandleFileCase1() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailPreconditions();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.PRECONDS_FAILED, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertFalse(pc.ranExtraction());
      assertFalse(pc.ranRenaming());
      assertFalse(pc.ranRequiredMetadata());
      assertFalse(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertFalse(pc.ranPostIngestFailActions());
   }

   // Case2:
   //  - Preconditions: pass
   //  - FailExtraction: fail
   public void testHandleFileCase2() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailExtraction();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.FAILURE, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertFalse(pc.ranRenaming());
      assertFalse(pc.ranRequiredMetadata());
      assertFalse(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertTrue(pc.ranPostIngestFailActions());
   }

   // Case3:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: fail
   public void testHandleFileCase3() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailRenaming();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.FAILURE, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertFalse(pc.ranRequiredMetadata());
      assertFalse(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertTrue(pc.ranPostIngestFailActions());
   }

   // Case4:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: pass
   //  - RequiredMetadata: fail
   public void testHandleFileCase4() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailRequiredMetadata();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.FAILURE, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertTrue(pc.ranRequiredMetadata());
      assertFalse(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertTrue(pc.ranPostIngestFailActions());
   }

   // Case5:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: pass
   //  - RequiredMetadata: pass
   //  - PreIngestActions: fail
   public void testHandleFileCase5() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailPreIngestActions();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.FAILURE, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertTrue(pc.ranRequiredMetadata());
      assertTrue(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertTrue(pc.ranPostIngestFailActions());
   }

   // Case6:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: pass
   //  - RequiredMetadata: pass
   //  - PreIngestActions: pass
   //  - SkipIngest: true
   public void testHandleFileCase6() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markSkipIngest();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.SKIPPED, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertTrue(pc.ranRequiredMetadata());
      assertTrue(pc.ranPreIngestActions());
      assertFalse(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertFalse(pc.ranPostIngestFailActions());
   }

   // Case7:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: pass
   //  - RequiredMetadata: pass
   //  - PreIngestActions: pass
   //  - SkipIngest: false
   //  - Ingest: fail
   public void testHandleFileCase7() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();
      pc.markFailIngest();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.FAILURE, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertTrue(pc.ranRequiredMetadata());
      assertTrue(pc.ranPreIngestActions());
      assertTrue(pc.ranIngest());
      assertFalse(pc.ranPostIngestSuccessActions());
      assertTrue(pc.ranPostIngestFailActions());
   }

   // Case8:
   //  - Preconditions: pass
   //  - FailExtraction: pass
   //  - RenameProduct: pass
   //  - RequiredMetadata: pass
   //  - PreIngestActions: pass
   //  - SkipIngest: false
   //  - Ingest: pass
   public void testHandleFileCase8() {
      File p = new File("/tmp/data.dat");

      // Setup Crawler.
      StateAwareProductCrawler pc = new StateAwareProductCrawler();

      // Run Crawler.
      IngestStatus status = pc.handleFile(p);

      // Verify IngestStatus.
      assertEquals(IngestStatus.Result.SUCCESS, status.getResult());
      assertEquals(p, status.getProduct());

      // Verify correct methods were run. 
      assertTrue(pc.ranPreconditions());
      assertTrue(pc.ranExtraction());
      assertTrue(pc.ranRenaming());
      assertTrue(pc.ranRequiredMetadata());
      assertTrue(pc.ranPreIngestActions());
      assertTrue(pc.ranIngest());
      assertTrue(pc.ranPostIngestSuccessActions());
      assertFalse(pc.ranPostIngestFailActions());
   }

   public void testSetupIngester() {
      ProductCrawler pc = createDummyCrawler();
      pc.setClientTransferer(LocalDataTransferFactory.class.getCanonicalName());
      pc.setupIngester();
      assertNotNull(pc.ingester);
   }

   public void testLoadAndValidateActions() {
      ProductCrawler pc = createDummyCrawler();
      pc.setApplicationContext(new FileSystemXmlApplicationContext(
            CRAWLER_CONFIG));
      pc.loadAndValidateActions();
      assertEquals(0, pc.actionRepo.getActions().size());

      pc = createDummyCrawler();
      pc.setApplicationContext(new FileSystemXmlApplicationContext(
            CRAWLER_CONFIG));
      pc.setActionIds(Lists.newArrayList("Unique", "DeleteDataFile"));
      pc.loadAndValidateActions();
      assertEquals(Sets.newHashSet(
            pc.getApplicationContext().getBean("Unique"),
            pc.getApplicationContext().getBean("DeleteDataFile")),
            pc.actionRepo.getActions());
   }

   public void testValidateActions() throws CrawlerActionException {
      // Test case invalid action.
      ProductCrawler pc = createDummyCrawler();
      pc.actionRepo = createMock(CrawlerActionRepo.class);

      CrawlerAction action = createMock(CrawlerAction.class);
      action.validate();
      expectLastCall().andThrow(new CrawlerActionException());
      expect(action.getId()).andReturn("ActionId");
      replay(action);

      expect(pc.actionRepo.getActions()).andReturn(
            Sets.newHashSet(action));
      replay(pc.actionRepo);
      try {
         pc.validateActions();
         fail("Should have thrown RuntimeException");
      } catch (RuntimeException e) { /* expect throw */ }
      verify(pc.actionRepo);
      verify(action);

      // Test case valid action.
      pc = createDummyCrawler();
      pc.actionRepo = createMock(CrawlerActionRepo.class);
      action = createMock(CrawlerAction.class);
      expect(pc.actionRepo.getActions()).andReturn(
            Sets.newHashSet(action));
      action.validate();
      replay(pc.actionRepo);
      replay(action);
      pc.validateActions();
      verify(pc.actionRepo);
      verify(action);
   }

   public void testContainsRequiredMetadata() {
      ProductCrawler pc = createDummyCrawler();
      Metadata m = new Metadata();
      m.replaceMetadata(CoreMetKeys.PRODUCT_TYPE, "GenericFile");
      m.replaceMetadata(CoreMetKeys.FILENAME, "TestFile.txt");
      m.replaceMetadata(CoreMetKeys.FILE_LOCATION, "/tmp/dir");
      m.replaceMetadata(CoreMetKeys.FILE_SIZE, "0");
      assertTrue(pc.containsRequiredMetadata(m));
      assertFalse(pc.containsRequiredMetadata(new Metadata()));
   }

   public void testAddKnowMetadata() {
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();
      ProductCrawler pc = createDummyCrawler();
      pc.addKnownMetadata(p, m);
      assertEquals(4, m.getAllKeys().size());
      assertEquals(p.getName(), m.getMetadata(CoreMetKeys.PRODUCT_NAME));
      assertEquals(p.getName(), m.getMetadata(CoreMetKeys.FILENAME));
      assertEquals(p.getParentFile().getAbsolutePath(),
            m.getMetadata(CoreMetKeys.FILE_LOCATION));
      assertEquals(String.valueOf(p.length()), m.getMetadata(CoreMetKeys.FILE_SIZE));
   }

   public void testCreateIngestStatus() {
      File p = new File("/tmp/data.dat");
      IngestStatus.Result result = IngestStatus.Result.SUCCESS;
      String message = "Ingest OK";
      ProductCrawler pc = createDummyCrawler();
      IngestStatus status = pc.createIngestStatus(p, result, message);
      assertEquals(p, status.getProduct());
      assertEquals(result, status.getResult());
      assertEquals(message, status.getMessage());
   }

   public void testIngest() throws MalformedURLException, IngestException {
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();

      // Test successful ingest.
      ProductCrawler pc = createDummyCrawler();
      pc.setFilemgrUrl("http://localhost:9000");
      pc.ingester = createMock(Ingester.class);
      expect(pc.ingester.ingest(new URL("http://localhost:9000"), p, m))
         .andReturn("TestProductId");
      replay(pc.ingester);
      assertTrue(pc.ingest(p, m));
      verify(pc.ingester);

      // Test failed ingest.
      pc = createDummyCrawler();
      pc.setFilemgrUrl("http://localhost:9000");
      pc.ingester = createMock(Ingester.class);
      expect(pc.ingester.ingest(new URL("http://localhost:9000"), p, m))
         .andThrow(new IngestException());
      replay(pc.ingester);
      assertFalse(pc.ingest(p, m));
      verify(pc.ingester);
   }

   public void testPerformPreIngestActions() throws CrawlerActionException {
      ProductCrawler pc = createDummyCrawler();
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();

      // Test actionRepo == null.
      assertTrue(pc.performPreIngestActions(p, m));

      // Test actionRepo != null and performAction return true.
      CrawlerAction action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(true);
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPreIngestActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertTrue(pc.performPreIngestActions(p, m));
      verify(action);
      verify(pc.actionRepo);

      // Test actionRepo != null and performAction return false.
      action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(false);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPreIngestActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertFalse(pc.performPreIngestActions(p, m));
      verify(action);
      verify(pc.actionRepo);
   }

   public void testPerformPostIngestOnSuccessActions() throws CrawlerActionException {
      ProductCrawler pc = createDummyCrawler();
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();

      // Test actionRepo == null.
      assertTrue(pc.performPostIngestOnSuccessActions(p, m));

      // Test actionRepo != null and performAction return true.
      CrawlerAction action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(true);
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPostIngestOnSuccessActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertTrue(pc.performPostIngestOnSuccessActions(p, m));
      verify(action);
      verify(pc.actionRepo);

      // Test actionRepo != null and performAction return false.
      action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(false);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPostIngestOnSuccessActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertFalse(pc.performPostIngestOnSuccessActions(p, m));
      verify(action);
      verify(pc.actionRepo);
   }

   public void testPerformPostIngestOnFailActions() throws CrawlerActionException {
      ProductCrawler pc = createDummyCrawler();
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();

      // Test actionRepo == null.
      assertTrue(pc.performPostIngestOnFailActions(p, m));

      // Test actionRepo != null and performAction return true.
      CrawlerAction action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(true);
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPostIngestOnFailActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertTrue(pc.performPostIngestOnFailActions(p, m));
      verify(action);
      verify(pc.actionRepo);

      // Test actionRepo != null and performAction return false.
      action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(false);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      replay(action);

      pc.actionRepo = createMock(CrawlerActionRepo.class);
      expect(pc.actionRepo.getPostIngestOnFailActions())
         .andReturn(Lists.newArrayList(action));
      replay(pc.actionRepo);

      assertFalse(pc.performPostIngestOnFailActions(p, m));
      verify(action);
      verify(pc.actionRepo);
   }

   public void testPerformProductCrawlerActions() throws CrawlerActionException {
      ProductCrawler pc = createDummyCrawler();
      File p = new File("/tmp/data.dat");
      Metadata m = new Metadata();

      // Test no actions.
      assertTrue(pc.performProductCrawlerActions(
            Collections.<CrawlerAction>emptyList(), p, m));

      // Test 1 action pass.
      CrawlerAction action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(true);
      replay(action);
      assertTrue(pc.performProductCrawlerActions(
            Lists.newArrayList(action), p, m));
      verify(action);

      // Test 1 action fail.
      action = createMock(CrawlerAction.class);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      expect(action.performAction(p, m)).andReturn(false);
      expect(action.getId()).andReturn("ActionId");
      expect(action.getDescription()).andReturn("Action Description");
      replay(action);
      assertFalse(pc.performProductCrawlerActions(
            Lists.newArrayList(action), p, m));
      verify(action);

      // Test 1 action pass and 1 action fail.
      CrawlerAction passAction = createMock(CrawlerAction.class);
      expect(passAction.getId()).andReturn("ActionId");
      expect(passAction.getDescription()).andReturn("Action Description");
      expect(passAction.performAction(p, m)).andReturn(true);
      replay(passAction);
      CrawlerAction failAction = createMock(CrawlerAction.class);
      expect(failAction.getId()).andReturn("ActionId");
      expect(failAction.getDescription()).andReturn("Action Description");
      expect(failAction.performAction(p, m)).andReturn(false);
      expect(failAction.getId()).andReturn("ActionId");
      expect(failAction.getDescription()).andReturn("Action Description");
      replay(failAction);
      assertFalse(pc.performProductCrawlerActions(
            Lists.newArrayList(passAction, failAction), p, m));
      verify(passAction);
      verify(failAction);
   }

   private static ProductCrawler createDummyCrawler() {
      return createDummyCrawler(true, new Metadata(), null); 
   }

   private static ProductCrawler createDummyCrawler(
         final boolean passesPreconditions, final Metadata productMetadata,
         final File renamedFile) {
      return new ProductCrawler() {
         @Override
         protected boolean passesPreconditions(File product) {
            return passesPreconditions;
         }
         @Override
         protected Metadata getMetadataForProduct(File product) {
            return productMetadata;
         }
         @Override
         protected File renameProduct(File product, Metadata productMetadata)
               throws Exception {
            return renamedFile == null ? product : renamedFile;
         }
      };
   }    
}

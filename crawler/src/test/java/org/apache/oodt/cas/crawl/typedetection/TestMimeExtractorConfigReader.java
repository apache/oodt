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
package org.apache.oodt.cas.crawl.typedetection;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.UUID;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.metadata.extractors.CopyAndRewriteExtractor;
import org.apache.oodt.cas.metadata.extractors.MetReaderExtractor;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link MimeExtractorConfigReader}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestMimeExtractorConfigReader extends TestCase {

   private File mimeTypesFile;
   private File defaultExtractorConfig;
   private File tmpDir;

   @Override
   public void setUp() throws Exception {
      File tmpFile = File.createTempFile("bogus", "bogus");
      tmpDir = new File(tmpFile.getParentFile(), UUID.randomUUID().toString());
      tmpFile.delete();
      if (!tmpDir.mkdirs()) {
         throw new Exception("Failed to create temp directory");
      }
      mimeTypesFile = new File(tmpDir, "mime-types.xml");
      FileUtils.touch(mimeTypesFile);
      defaultExtractorConfig = new File(tmpDir, "default-extractor.properties");
      FileUtils.touch(defaultExtractorConfig);
   }

   @Override
   public void tearDown() throws Exception {
      FileUtils.forceDelete(tmpDir);
   }

   public void testReadWithDefaults() throws Exception {
      String namingConvId = "PathUtilsNC";
      String defaultPreconditionId = "TestPrecondition";
      String preconditionId1 = "Precondition1";
      String preconditionId2 = "Precondition2";
      String xmlFileContents =
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<cas:mimetypemap xmlns:cas=\"http://oodt.jpl.nassa.gov/1.0/cas\""
               + " magic=\"false\" mimeRepo=\""
               + mimeTypesFile.getAbsolutePath() + "\">\n"
         + "<default>\n"
         + "   <namingConvention id=\"" + namingConvId + "\" />\n"
         + "   <extractor class=\""
               + CopyAndRewriteExtractor.class.getCanonicalName() + "\">\n"
         + "      <config file=\"" + defaultExtractorConfig.getAbsolutePath()
                     + "\"/>\n"
         + "      <preCondComparators>\n"
         + "         <preCondComparator id=\"" + defaultPreconditionId + "\"/>\n"
         + "      </preCondComparators>\n"
         + "   </extractor>\n"
         + "</default>\n"
         + "<mime type=\"some/mime-type\">\n"
         + "   <extractor class=\""
               + MetReaderExtractor.class.getCanonicalName() + "\">\n"
         + "      <config file=\"" + defaultExtractorConfig.getAbsolutePath()
                     + "\"/>\n"
         + "      <preCondComparators>\n"
         + "         <preCondComparator id=\"" + preconditionId1 + "\"/>\n"
         + "         <preCondComparator id=\"" + preconditionId2 + "\"/>\n"
         + "      </preCondComparators>\n"
         + "   </extractor>\n"
         + "</mime>\n"
         + "</cas:mimetypemap>\n";
      File xmlMimeRepo = new File(tmpDir, "mime-repo.xml");
      FileUtils.writeStringToFile(xmlMimeRepo, xmlFileContents, "UTF-8");
      assertTrue(xmlMimeRepo.exists());
      MimeExtractorRepo mimeRepo = MimeExtractorConfigReader.read(
            xmlMimeRepo.getAbsolutePath());
      assertEquals(namingConvId, mimeRepo.getNamingConventionId("some/mime-type"));
      List<MetExtractorSpec> specs = mimeRepo.getExtractorSpecsForMimeType("some/mime-type");
      assertEquals(1, specs.size());
      assertEquals(MetReaderExtractor.class,
            specs.get(0).getMetExtractor().getClass());
      assertEquals(Lists.newArrayList(preconditionId1, preconditionId2),
            specs.get(0).getPreCondComparatorIds());
      specs = mimeRepo.getExtractorSpecsForMimeType("someother/mime-type");
      assertEquals(1, specs.size());
      assertEquals(CopyAndRewriteExtractor.class,
            specs.get(0).getMetExtractor().getClass());
      assertEquals(Lists.newArrayList(defaultPreconditionId),
            specs.get(0).getPreCondComparatorIds());
   }

   public void testReadWithoutDefaults() throws Exception {
      String namingConvId = "PathUtilsNC";
      String preconditionId1 = "Precondition1";
      String preconditionId2 = "Precondition2";
      String xmlFileContents =
           "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         + "<cas:mimetypemap xmlns:cas=\"http://oodt.jpl.nassa.gov/1.0/cas\""
               + " magic=\"false\" mimeRepo=\""
               + mimeTypesFile.getAbsolutePath() + "\">\n"
         + "<mime type=\"some/mime-type\">\n"
         + "   <namingConvention id=\"" + namingConvId + "\" />\n"
         + "   <extractor class=\""
               + MetReaderExtractor.class.getCanonicalName() + "\">\n"
         + "      <config file=\"" + defaultExtractorConfig.getAbsolutePath()
                     + "\"/>\n"
         + "      <preCondComparators>\n"
         + "         <preCondComparator id=\"" + preconditionId1 + "\"/>\n"
         + "         <preCondComparator id=\"" + preconditionId2 + "\"/>\n"
         + "      </preCondComparators>\n"
         + "   </extractor>\n"
         + "</mime>\n"
         + "</cas:mimetypemap>\n";
      File xmlMimeRepo = new File(tmpDir, "mime-repo.xml");
      FileUtils.writeStringToFile(xmlMimeRepo, xmlFileContents, "UTF-8");
      assertTrue(xmlMimeRepo.exists());
      MimeExtractorRepo mimeRepo = MimeExtractorConfigReader.read(
            xmlMimeRepo.getAbsolutePath());
      assertEquals(namingConvId, mimeRepo.getNamingConventionId("some/mime-type"));
      List<MetExtractorSpec> specs = mimeRepo.getExtractorSpecsForMimeType("some/mime-type");
      assertEquals(1, specs.size());
      assertEquals(MetReaderExtractor.class,
            specs.get(0).getMetExtractor().getClass());
      assertEquals(Lists.newArrayList(preconditionId1, preconditionId2),
            specs.get(0).getPreCondComparatorIds());
      specs = mimeRepo.getExtractorSpecsForMimeType("someother/mime-type");
      assertEquals(0, specs.size());
   }
}

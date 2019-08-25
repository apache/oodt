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
package org.apache.oodt.cas.metadata.filenaming;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.UUID;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.NamingConventionException;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link PathUtilsNamingConvention}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestPathUtilsNamingConvention extends TestCase {

   public void testRename() throws IOException, NamingConventionException {
      File tmpFile = File.createTempFile("bogus", "bogus");
      File tmpDir = new File(tmpFile.getParentFile(),
            UUID.randomUUID().toString());
      if (!tmpDir.mkdirs()) {
         throw new IOException("Failed to create temp directory");
      }
      tmpFile.delete();
      File testFile = new File(tmpDir, "TestProduct.txt");
      Metadata m = new Metadata();
      m.replaceMetadata("NewName", "NewProduct.txt");

      // Test failure.
      PathUtilsNamingConvention nc = new PathUtilsNamingConvention();
      nc.setNamingConv("[NewName]");
      try {
         nc.rename(testFile, m);
         fail("Should have thrown IOException");
      } catch (NamingConventionException e) { /* expect throw */ }

      // Test success.
      FileUtils.touch(testFile);
      File newFile = nc.rename(testFile, m);
      assertTrue(newFile.exists());
      assertEquals("NewProduct.txt", newFile.getName());
      assertFalse(testFile.exists());

      FileUtils.forceDelete(tmpDir);
   }
}

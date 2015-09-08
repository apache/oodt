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
package org.apache.oodt.cas.filemgr.cli.action;

//OODT static imports
import static org.apache.oodt.cas.filemgr.metadata.CoreMetKeys.FILENAME;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link DumpMetadataCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestDumpMetadataCliAction extends TestCase {

   private static final String PRODUCT_ID = "TestProductId";
   private static final String FILE_NAME = "data.dat";

   private File tmpFile;

   @Override
   public void setUp() throws Exception {
      File bogusFile = File.createTempFile("bogus", "bogus");
      tmpFile = new File(bogusFile.getParentFile(), "MetadataDump");
      tmpFile.mkdirs();
      bogusFile.delete();
   }

   @Override
   public void tearDown() throws Exception {
      FileUtils.forceDelete(tmpFile);
   }

   public void testDataFlow() throws CmdLineActionException, IOException {
      MockDumpMetadataCliAction cliAction = new MockDumpMetadataCliAction();
      cliAction.setProductId(PRODUCT_ID);
      ActionMessagePrinter printer = new ActionMessagePrinter();
      cliAction.execute(printer);
      assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
          + "<cas:metadata xmlns:cas=\"http://oodt.jpl.nasa.gov/1.0/cas\">\n"
          + "<keyval type=\"vector\">\n"
          + "<key>Filename</key>\n"
          + "<val>data.dat</val>\n"
          + "</keyval>\n"
          + "</cas:metadata>\n",
            printer.getPrintedMessages().get(0));

      cliAction.setOutputDir(tmpFile);
      cliAction.execute(printer);
      assertTrue(new File(tmpFile, FILE_NAME + ".met").exists());
      assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
            + "<cas:metadata xmlns:cas=\"http://oodt.jpl.nasa.gov/1.0/cas\">\n"
            + "<keyval type=\"vector\">\n"
            + "<key>Filename</key>\n"
            + "<val>data.dat</val>\n"
            + "</keyval>\n"
            + "</cas:metadata>\n",
            FileUtils.readFileToString(new File(tmpFile, FILE_NAME + ".met")));
   }

   public class MockDumpMetadataCliAction extends DumpMetadataCliAction {
      @Override
      public FileManagerClient getClient() throws MalformedURLException,
            ConnectionException {
         return new DummyFileManagerClient(new URL("http://localhost:9000"),
               false) {
            @Override
            public Product getProductById(String productId) {
               Product p = new Product();
               p.setProductId(productId);
               return p;
            }
            @Override
            public Metadata getMetadata(Product product) {
               Metadata m = new Metadata();
               m.addMetadata(FILENAME, FILE_NAME);
               return m;
            }
         };
      }
   }
}

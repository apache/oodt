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
package org.apache.oodt.cas.pge;

//OODT static imports
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.NAME;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PROPERTY_ADDERS;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pge.PGETaskInstance;
import org.apache.oodt.cas.pge.PGETaskInstance.LoggerOuputStream;
import org.apache.oodt.cas.pge.config.PgeConfig;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;

//Google imports
import com.google.common.collect.Lists;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link PGETaskInstance}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestPGETaskInstance extends TestCase {

   public void testLoadPropertyAdders() throws Exception {
      PGETaskInstance pgeTask = new PGETaskInstance();
      ConfigFilePropertyAdder propAdder = pgeTask
            .loadPropertyAdder(MockConfigFilePropertyAdder.class
                  .getCanonicalName());
      assertNotNull(propAdder);
      assertTrue(propAdder instanceof MockConfigFilePropertyAdder);
   }

   public void testRunPropertyAdders() throws Exception {
      PGETaskInstance pgeTask = new PGETaskInstance();
      Metadata staticMet = new Metadata();
      staticMet.addMetadata(PROPERTY_ADDERS.getName(),
            MockConfigFilePropertyAdder.class.getCanonicalName());
      Metadata dynMet = new Metadata();
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key",
            "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));

      staticMet = new Metadata();
      dynMet = new Metadata();
      dynMet.addMetadata(PROPERTY_ADDERS.getName(), Lists.newArrayList(
            MockConfigFilePropertyAdder.class.getCanonicalName(),
            MockConfigFilePropertyAdder.class.getCanonicalName()));
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key",
            "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));
      assertEquals("2",
            pgeTask.pgeMetadata
                  .getMetadata(MockConfigFilePropertyAdder.RUN_COUNTER));

      System.setProperty(PgeTaskMetKeys.USE_LEGACY_PROPERTY, "true");
      staticMet = new Metadata();
      dynMet = new Metadata();
      dynMet.addMetadata(PROPERTY_ADDERS.getName(),
            MockConfigFilePropertyAdder.class.getCanonicalName());
      pgeTask.pgeMetadata = new PgeMetadata(staticMet, dynMet);
      pgeTask.pgeConfig = new PgeConfig();
      pgeTask.pgeConfig.setPropertyAdderCustomArgs(new Object[] { "key",
            "value" });
      pgeTask.runPropertyAdders();
      assertEquals("value", pgeTask.pgeMetadata.getMetadata("key"));
      assertEquals("1",
            pgeTask.pgeMetadata
                  .getMetadata(MockConfigFilePropertyAdder.RUN_COUNTER));
   }

   public void testCreatePgeMetadata() throws Exception {
      final String PGE_NAME = "PGE_Test";
      final String PGE_REQUIRED_METADATA = "Filename, FileLocation ";
      final String PROP_ADDERS = "some.prop.adder.classpath,some.other.classpath";
      PGETaskInstance pgeTask = new PGETaskInstance();
      Metadata dynMet = new Metadata();
      WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
      config.addConfigProperty(PgeTaskMetKeys.NAME.getName(), PGE_NAME);
      config.addConfigProperty(PgeTaskMetKeys.REQUIRED_METADATA.getName(),
            PGE_REQUIRED_METADATA);
      config.addConfigProperty(PgeTaskMetKeys.PROPERTY_ADDERS.getName(),
            PROP_ADDERS);

      PgeMetadata pgeMet = pgeTask.createPgeMetadata(dynMet, config);
      assertEquals(1, pgeMet.getAllMetadata(PgeTaskMetKeys.NAME.getName())
            .size());
      assertEquals(PGE_NAME,
            pgeMet.getAllMetadata(PgeTaskMetKeys.NAME.getName()).get(0));
      assertEquals(2,
            pgeMet.getAllMetadata(PgeTaskMetKeys.REQUIRED_METADATA.getName())
                  .size());
      assertTrue(pgeMet.getAllMetadata(
            PgeTaskMetKeys.REQUIRED_METADATA.getName()).contains("Filename"));
      assertTrue(pgeMet.getAllMetadata(
            PgeTaskMetKeys.REQUIRED_METADATA.getName())
            .contains("FileLocation"));
      assertEquals(2,
            pgeMet.getAllMetadata(PgeTaskMetKeys.PROPERTY_ADDERS.getName())
                  .size());
      assertTrue(pgeMet
            .getAllMetadata(PgeTaskMetKeys.PROPERTY_ADDERS.getName()).contains(
                  "some.prop.adder.classpath"));
      assertTrue(pgeMet
            .getAllMetadata(PgeTaskMetKeys.PROPERTY_ADDERS.getName()).contains(
                  "some.other.classpath"));

      // Verify still works when only one property adder is specified.
      pgeTask = new PGETaskInstance();
      config = new WorkflowTaskConfiguration();
      config.addConfigProperty(PgeTaskMetKeys.PROPERTY_ADDERS.getName(),
            "one.prop.adder.only");

      pgeMet = pgeTask.createPgeMetadata(dynMet, config);
      assertEquals(1,
            pgeMet.getAllMetadata(PgeTaskMetKeys.PROPERTY_ADDERS.getName())
                  .size());
      assertEquals("one.prop.adder.only",
            pgeMet.getAllMetadata(PgeTaskMetKeys.PROPERTY_ADDERS.getName())
                  .get(0));
   }

   public void testLogger() throws Exception {
      File tmpFile = File.createTempFile("bogus", "bogus");
      File tmpDir = tmpFile.getParentFile();
      tmpFile.delete();
      File tmpDir1 = new File(tmpDir, UUID.randomUUID().toString());
      assertTrue(tmpDir1.mkdirs());
      File tmpDir2 = new File(tmpDir, UUID.randomUUID().toString());
      assertTrue(tmpDir2.mkdirs());
      File tmpDir3 = new File(tmpDir, UUID.randomUUID().toString());
      assertTrue(tmpDir3.mkdirs());

      final String PGE_1_NAME = "PGE1";
      PGETaskInstance pgeTask1 = new PGETaskInstance();
      pgeTask1.pgeMetadata = new PgeMetadata();
      pgeTask1.pgeMetadata.replaceMetadata(NAME, PGE_1_NAME);
      pgeTask1.pgeConfig = new PgeConfig();
      pgeTask1.pgeConfig.setExeDir(tmpDir1.getAbsolutePath());
      Handler handler1 = pgeTask1.initializePgeLogger();
      pgeTask1.log(Level.INFO, "pge1 message1");
      pgeTask1.log(Level.INFO, "pge1 message2");
      pgeTask1.log(Level.INFO, "pge1 message3");
      pgeTask1.closePgeLogger(handler1);
      List<String> messages = FileUtils.readLines(
            new File(tmpDir1, "logs").listFiles()[0], "UTF-8");
      assertEquals("INFO: pge1 message1", messages.get(1));
      assertEquals("INFO: pge1 message2", messages.get(3));
      assertEquals("INFO: pge1 message3", messages.get(5));

      final String PGE_2_NAME = "PGE2";
      PGETaskInstance pgeTask2 = new PGETaskInstance();
      pgeTask2.pgeMetadata = new PgeMetadata();
      pgeTask2.pgeMetadata.replaceMetadata(NAME, PGE_2_NAME);
      pgeTask2.pgeConfig = new PgeConfig();
      pgeTask2.pgeConfig.setExeDir(tmpDir2.getAbsolutePath());
      Handler handler2 = pgeTask2.initializePgeLogger();
      pgeTask2.log(Level.SEVERE, "pge2 message1");
      pgeTask2.closePgeLogger(handler2);
      messages = FileUtils.readLines(new File(tmpDir2, "logs").listFiles()[0],
            "UTF-8");
      assertEquals("SEVERE: pge2 message1", messages.get(1));

      PGETaskInstance pgeTask3 = new PGETaskInstance() {
         @Override
         protected LoggerOuputStream createStdOutLogger() {
            return new LoggerOuputStream(Level.INFO, 10);
         }
      };
      pgeTask3.pgeMetadata = new PgeMetadata();
      pgeTask3.pgeMetadata.replaceMetadata(NAME, "TestPGE");
      pgeTask3.pgeConfig = new PgeConfig();
      pgeTask3.pgeConfig.setExeDir(tmpDir3.getAbsolutePath());
      Handler handler3 = pgeTask3.initializePgeLogger();
      LoggerOuputStream los = pgeTask3.createStdOutLogger();
      los.write("This is a test write to a log file".getBytes());
      los.close();
      pgeTask3.closePgeLogger(handler3);
      messages = FileUtils.readLines(new File(tmpDir3, "logs").listFiles()[0],
            "UTF-8");
      assertEquals(8, messages.size());
      assertEquals("INFO: This is a ", messages.get(1));
      assertEquals("INFO: test write", messages.get(3));
      assertEquals("INFO:  to a log ", messages.get(5));
      assertEquals("INFO: file", messages.get(7));

      FileUtils.forceDelete(tmpDir1);
      FileUtils.forceDelete(tmpDir2);
      FileUtils.forceDelete(tmpDir3);
   }
}

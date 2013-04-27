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
package org.apache.oodt.cas.pge.config;

//OODT static imports
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CONFIG_FILE_PATH;

//JDK imports
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

//Google imports
import com.google.common.collect.Lists;

//OODT imports
import org.apache.oodt.cas.pge.metadata.PgeMetadata;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link XmlFilePgeConfigBuilder}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestXmlFilePgeConfigBuilder extends TestCase {

   public static final String CONFIG_FILE = "src/test/resources/pge-config.xml";

   public void testBuild() throws IOException {
      XmlFilePgeConfigBuilder builder = new XmlFilePgeConfigBuilder();
      PgeMetadata pgeMetadata = new PgeMetadata();
      pgeMetadata.replaceMetadata(CONFIG_FILE_PATH, CONFIG_FILE);
      pgeMetadata.replaceMetadata("INPUT_FILE_1", "src/test/resources/data-file-1.txt");
      pgeMetadata.replaceMetadata("INPUT_FILE_2", "src/test/resources/data-file-2.txt");
      pgeMetadata.replaceMetadata("WORKING_DIR", "/tmp");
      pgeMetadata.markAsDynamicMetadataKey();
      pgeMetadata.commitMarkedDynamicMetadataKeys();
      PgeConfig pgeConfig = builder.build(pgeMetadata);

      // Verify metadata checks out.
      assertEquals(40, pgeMetadata.asMetadata().getAllKeys().size());
      assertEquals(CONFIG_FILE, pgeMetadata.getMetadata(CONFIG_FILE_PATH));
      assertEquals("src/test/resources/data-file-1.txt", pgeMetadata.getMetadata("INPUT_FILE_1"));
      assertEquals("src/test/resources/data-file-2.txt", pgeMetadata.getMetadata("INPUT_FILE_2"));
      assertEquals("/tmp", pgeMetadata.getMetadata("WORKING_DIR"));
      assertEquals(">", pgeMetadata.getMetadata("commons/GreaterThan"));
      assertEquals("<", pgeMetadata.getMetadata("commons/LessThan"));
      assertEquals("&", pgeMetadata.getMetadata("commons/Ampersand"));
      assertEquals("'", pgeMetadata.getMetadata("commons/Apostrophe"));
      assertEquals("\"", pgeMetadata.getMetadata("commons/QuotationMark"));
      assertEquals("org.apache.oodt.cas.pge.writers.TextConfigFileWriter", pgeMetadata.getMetadata("writers/TextWriter"));
      assertEquals("org.apache.oodt.cas.pge.writers.CsvConfigFileWriter", pgeMetadata.getMetadata("writers/CsvWriter"));
      assertEquals("Simple", pgeMetadata.getMetadata("GREETING_ENUM"));
      assertEquals("true", pgeMetadata.getMetadata("FORCE_STAGING"));
      assertEquals("Custom", pgeMetadata.getMetadata("CUSTOM_GREETING_ENUM"));
      assertEquals("<Custom Greeting Here>", pgeMetadata.getMetadata("CUSTOM_GREETING_ENUM_VALUE"));
      assertEquals("/tmp", pgeMetadata.getMetadata("WorkingDir"));
      assertEquals("Simple", pgeMetadata.getMetadata("GreetingEnum"));
      assertEquals("Custom", pgeMetadata.getMetadata("CustomGreetingEnum"));
      assertEquals("<Custom Greeting Here>", pgeMetadata.getMetadata("CustomGreetingEnumValue"));
      assertEquals("src/test/resources/data-file-1.txt", pgeMetadata.getMetadata("InputFile1"));
      assertEquals("src/test/resources/data-file-2.txt", pgeMetadata.getMetadata("InputFile2"));
      assertEquals("true", pgeMetadata.getMetadata("ForceStaging"));
      assertEquals(Lists.newArrayList("/tmp/staging/data-file-1.txt", "/tmp/staging/data-file-2.txt"), pgeMetadata.getAllMetadata("InputFiles"));
      assertEquals("/tmp/config", pgeMetadata.getMetadata("ConfigDir"));
      assertEquals("/tmp/output", pgeMetadata.getMetadata("OutputDir"));
      assertEquals("/tmp/staging", pgeMetadata.getMetadata("StagingDir"));
      assertEquals("dyn-input.txt", pgeMetadata.getMetadata("DynInput/Text/Name"));
      assertEquals("dyn-input.csv", pgeMetadata.getMetadata("DynInput/CSV/Name"));
      assertEquals("/tmp/config/dyn-input.txt", pgeMetadata.getMetadata("TextInputFile"));
      assertEquals("/tmp/config/dyn-input.csv", pgeMetadata.getMetadata("CsvInputFile"));
      assertEquals("PgeOutput.txt", pgeMetadata.getMetadata("OutputFileName"));
      assertEquals("\n      [Greeting],\n      This is a template for text file [TextInputFile].\n      This template was written at: [DATE.UTC].\n      [Signature]\n    ", pgeMetadata.getMetadata("TextFileTemplate"));
      assertEquals("Hello", pgeMetadata.getMetadata("SimpleGreeting"));
      assertEquals("Hi", pgeMetadata.getMetadata("CasualGreeting"));
      assertEquals("<Custom Greeting Here>", pgeMetadata.getMetadata("CustomGreeting"));
      assertEquals("Hello", pgeMetadata.getMetadata("Greating"));
      assertEquals("-bfost", pgeMetadata.getMetadata("Signature"));
      assertEquals(Lists.newArrayList("File1.txt","File2.dat","File3.xml"), pgeMetadata.getAllMetadata("AuxInputFiles"));
      assertEquals(Lists.newArrayList("true","false","true"), pgeMetadata.getAllMetadata("IsText"));
      assertEquals(Lists.newArrayList("AuxInputFiles","IsText"), pgeMetadata.getAllMetadata("CsvHeader"));

      // Verify pgeconfig checks out.
      List<DynamicConfigFile> dynConfigFiles = pgeConfig.getDynamicConfigFiles();
      assertEquals(2, dynConfigFiles.size());
      assertEquals("org.apache.oodt.cas.pge.writers.TextConfigFileWriter", dynConfigFiles.get(0).getWriterClass());
      assertEquals("/tmp/config/dyn-input.txt", dynConfigFiles.get(0).getFilePath());
      assertEquals(Lists.newArrayList("\n      [Greeting],\n      This is a template for text file [TextInputFile].\n      This template was written at: [DATE.UTC].\n      [Signature]\n    "), Arrays.asList(dynConfigFiles.get(0).getArgs()));
      assertEquals("org.apache.oodt.cas.pge.writers.CsvConfigFileWriter", dynConfigFiles.get(1).getWriterClass());
      assertEquals("/tmp/config/dyn-input.csv", dynConfigFiles.get(1).getFilePath());
      assertEquals(Lists.newArrayList("AuxInputFiles,IsText"), Arrays.asList(dynConfigFiles.get(1).getArgs()));

      assertEquals(5, pgeConfig.getExeCmds().size());
      assertEquals("echo /tmp/config/dyn-input.txt > PgeOutput.txt", pgeConfig.getExeCmds().get(0));
      assertEquals("echo /tmp/config/dyn-input.csv >> PgeOutput.txt", pgeConfig.getExeCmds().get(1));
      assertEquals("if ( ! -e src/test/resources/data-file-1.txt || ! -e src/test/resources/data-file-2.txt ) then", pgeConfig.getExeCmds().get(2));
      assertEquals("  exit 1", pgeConfig.getExeCmds().get(3));
      assertEquals("endif", pgeConfig.getExeCmds().get(4));
      assertEquals("/tmp", pgeConfig.getExeDir());
      assertEquals("csh", pgeConfig.getShellType());

      FileStagingInfo fileStagingInfo = pgeConfig.getFileStagingInfo();
      assertEquals(Lists.newArrayList("src/test/resources/data-file-2.txt", "src/test/resources/data-file-1.txt"), fileStagingInfo.getFilePaths());
      assertEquals("/tmp/staging", fileStagingInfo.getStagingDir());
      assertEquals(true, fileStagingInfo.isForceStaging());

      List<OutputDir> outputDirs = pgeConfig.getOuputDirs();
      assertEquals(1, outputDirs.size());
      assertEquals(true, outputDirs.get(0).isCreateBeforeExe());
      assertEquals("/tmp/output", outputDirs.get(0).getPath());
   }
}

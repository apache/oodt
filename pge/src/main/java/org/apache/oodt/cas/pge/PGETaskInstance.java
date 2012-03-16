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
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.ACTION_IDS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.ACTION_REPO_FILE;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.ATTEMPT_INGEST_ALL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CRAWLER_CRAWL_FOR_DIRS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CRAWLER_RECUR;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.INGEST_CLIENT_TRANSFER_SERVICE_FACTORY;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.INGEST_FILE_MANAGER_URL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.MET_FILE_EXT;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.NAME;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PGE_RUNTIME;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PROPERTY_ADDERS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.REQUIRED_METADATA;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.WORKFLOW_MANAGER_URL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.CONF_FILE_BUILD;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.CRAWLING;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.RUNNING_PGE;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawl.StdProductCrawler;
import org.apache.oodt.cas.crawl.status.IngestStatus;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.config.DynamicConfigFile;
import org.apache.oodt.cas.pge.config.OutputDir;
import org.apache.oodt.cas.pge.config.PgeConfig;
import org.apache.oodt.cas.pge.config.RegExprOutputFiles;
import org.apache.oodt.cas.pge.config.RenamingConv;
import org.apache.oodt.cas.pge.config.XmlFilePgeConfigBuilder;
import org.apache.oodt.cas.pge.logging.PgeLogHandler;
import org.apache.oodt.cas.pge.logging.PgeLogRecord;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys;
import org.apache.oodt.cas.pge.writers.PcsMetFileWriter;
import org.apache.oodt.cas.pge.writers.SciPgeConfigFileWriter;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;
import org.apache.oodt.cas.workflow.util.ScriptFile;
import org.apache.oodt.commons.exec.ExecUtils;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Google imports
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Runs a CAS-style Product Generation Executive based on the PCS Wrapper
 * Architecture from mattmann et al. on OCO.
 * 
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class PGETaskInstance implements WorkflowTaskInstance {

   protected XmlRpcWorkflowManagerClient wm;
   protected String workflowInstId;
   protected PgeMetadata pgeMetadata;
   protected PgeConfig pgeConfig;

   protected PGETaskInstance() {}

   protected void updateStatus(String status) throws Exception {
      log(Level.INFO, "Updating status to workflow as [" + status + "]");
      wm.updateWorkflowInstanceStatus(workflowInstId, status);
   }

   protected Handler initializePgeLogger() throws Exception {
      File logDir = new File(pgeConfig.getExeDir(), "logs");
      if (!logDir.mkdirs()) {
         throw new Exception("mkdirs for logs directory return false");
      }
      Handler handler = new PgeLogHandler(pgeMetadata.getMetadata(NAME),
            new FileOutputStream(new File(logDir, createLogFileName())));
      Logger.getLogger(PGETaskInstance.class.getName()).addHandler(handler);
      return handler;
   }

   protected String createLogFileName() {
      return pgeMetadata.getMetadata(NAME) + "." + System.currentTimeMillis()
            + ".log";
   }

   protected void closePgeLogger(Handler handler) {
      handler.close();
      Logger.getLogger(PGETaskInstance.class.getName()).removeHandler(handler);
   }

   protected void log(Level level, String message) {
      Logger.getLogger(PGETaskInstance.class.getName()).log(
            new PgeLogRecord(pgeMetadata.getMetadata(NAME), level, message));
   }

   protected void log(Level level, String message, Throwable t) {
      Logger.getLogger(PGETaskInstance.class.getName()).log(
            new PgeLogRecord(pgeMetadata.getMetadata(NAME), level, message, t));
   }

   protected PgeMetadata createPgeMetadata(Metadata dynMetadata,
         WorkflowTaskConfiguration config) throws Exception {
      Metadata staticMetadata = new Metadata();
      for (Object objKey : config.getProperties().keySet()) {
         String key = (String) objKey;
         PgeTaskMetKeys metKey = PgeTaskMetKeys.getByName(key);
         if (metKey != null && metKey.isVector()) {
            staticMetadata.addMetadata(key,
                  Lists.newArrayList(Splitter.on(",").trimResults()
                        .omitEmptyStrings()
                        .split(config.getProperty(key))));
         } else {
            staticMetadata.addMetadata(key, config.getProperty(key));
         }
      }
      return new PgeMetadata(staticMetadata, dynMetadata);
   }

   protected PgeConfig createPgeConfig() throws Exception {
      return new XmlFilePgeConfigBuilder().build(pgeMetadata);
   }

   protected void runPropertyAdders() throws Exception {
      try {
         List<String> propertyAdders = pgeMetadata.getAllMetadata(PROPERTY_ADDERS);
         if (propertyAdders != null) {
            for (String propertyAdder : propertyAdders) {
               runPropertyAdder(loadPropertyAdder(propertyAdder));
            }
         }
      } catch (Exception e) {
         throw new Exception("Failed to instanciate/run Property Adders : "
               + e.getMessage(), e);
      }
   }

   protected ConfigFilePropertyAdder loadPropertyAdder(
         String propertyAdderClasspath) throws Exception {
      return (ConfigFilePropertyAdder) Class.forName(propertyAdderClasspath)
            .newInstance();
   }

   protected void runPropertyAdder(ConfigFilePropertyAdder propAdder)
         throws Exception {
      propAdder.addConfigProperties(pgeMetadata,
            pgeConfig.getPropertyAdderCustomArgs());
   }

   protected XmlRpcWorkflowManagerClient createWorkflowManagerClient()
         throws Exception {
      String urlString = pgeMetadata.getMetadata(WORKFLOW_MANAGER_URL);
      Validate.notNull(urlString, "Must specify " + WORKFLOW_MANAGER_URL);
      return new XmlRpcWorkflowManagerClient(new URL(urlString));
   }

   protected String getWorkflowInstanceId() throws Exception {
      String instanceId = pgeMetadata.getMetadata(CoreMetKeys.WORKFLOW_INST_ID);
      Validate.notNull(instanceId, "Must specify "
            + CoreMetKeys.WORKFLOW_INST_ID);
      return instanceId;
   }

   protected void createExeDir() throws Exception {
      log(Level.INFO, "Creating PGE execution working directory: ["
            + pgeConfig.getExeDir() + "]");
      if (!new File(pgeConfig.getExeDir()).mkdirs()) {
         throw new Exception("mkdirs returned false for creating ["
               + pgeConfig.getExeDir() + "]");
      }
   }

   protected void createOuputDirsIfRequested() throws Exception {
      for (OutputDir outputDir : pgeConfig.getOuputDirs()) {
         if (outputDir.isCreateBeforeExe()) {
            log(Level.INFO, "Creating PGE file ouput directory: ["
                  + outputDir.getPath() + "]");
            if (!new File(outputDir.getPath()).mkdirs()) {
               throw new Exception("mkdir returned false for creating ["
                     + outputDir.getPath() + "]");
            }
         }
      }
   }

   protected void createSciPgeConfigFiles() throws Exception {
      log(Level.INFO, "Starting creation of science PGE files...");
      for (DynamicConfigFile dynamicConfigFile : pgeConfig
            .getDynamicConfigFiles()) {
         createSciPgeConfigFile(dynamicConfigFile);
      }
      log(Level.INFO, "Successfully wrote all science PGE files!");
   }

   protected void createSciPgeConfigFile(DynamicConfigFile dynamicConfigFile)
         throws Exception {
      Validate.notNull(dynamicConfigFile, "dynamicConfigFile cannot be null");
      log(Level.INFO, "Starting creation of science PGE file [" + dynamicConfigFile.getFilePath() + "]...");

      // Create parent directory if it doesn't exist.
      File parentDir = new File(dynamicConfigFile.getFilePath())
            .getParentFile();
      if (!parentDir.exists()) {
         parentDir.mkdirs();
      }

      // Load writer and write file.
      log(Level.INFO, "Loading writer class for science PGE file [" + dynamicConfigFile.getFilePath() + "]...");
      SciPgeConfigFileWriter writer = (SciPgeConfigFileWriter) Class.forName(
            dynamicConfigFile.getWriterClass()).newInstance();
      log(Level.INFO, "Loaded writer [" + writer.getClass().getCanonicalName()
            + "] for science PGE file [" + dynamicConfigFile.getFilePath()
            + "]...");
      log(Level.INFO, "Writing science PGE file [" + dynamicConfigFile.getFilePath() + "]...");
      writer.createConfigFile(dynamicConfigFile.getFilePath(),
            pgeMetadata.asMetadata(), dynamicConfigFile.getArgs());
   }

   protected void processOutput() throws FileNotFoundException, IOException {
      for (final OutputDir outputDir : this.pgeConfig.getOuputDirs()) {
         File[] createdFiles = new File(outputDir.getPath()).listFiles();
         for (File createdFile : createdFiles) {
            Metadata outputMetadata = new Metadata();
            for (RegExprOutputFiles regExprFiles : outputDir
                  .getRegExprOutputFiles()) {
               if (Pattern.matches(regExprFiles.getRegExp(),
                     createdFile.getName())) {
                  try {
                     PcsMetFileWriter writer = (PcsMetFileWriter) Class
                           .forName(regExprFiles.getConverterClass())
                           .newInstance();
                     outputMetadata
                           .replaceMetadata(this.getMetadataForFile(
                                 (regExprFiles.getRenamingConv() != null) ? createdFile = this
                                       .renameFile(createdFile,
                                             regExprFiles.getRenamingConv())
                                       : createdFile, writer, regExprFiles
                                       .getArgs()));
                  } catch (Exception e) {
                     log(Level.SEVERE,
                           "Failed to create metadata file for '" + createdFile
                                 + "' : " + e.getMessage(), e);
                  }
               }
            }
            if (outputMetadata.getAllKeys().size() > 0)
               this.writeFromMetadata(
                     outputMetadata,
                     createdFile.getAbsolutePath()
                           + "."
                           + pgeMetadata.getMetadata(MET_FILE_EXT));
         }
      }
   }

   protected File renameFile(File file, RenamingConv renamingConv)
         throws Exception {
      Metadata curMetadata = this.pgeMetadata.asMetadata();
      curMetadata.replaceMetadata(renamingConv.getTmpReplaceMet());
      String newFileName = PathUtils.doDynamicReplacement(
            renamingConv.getRenamingString(), curMetadata);
      File newFile = new File(file.getParentFile(), newFileName);
      log(Level.INFO, "Renaming file '" + file.getAbsolutePath() + "' to '"
            + newFile.getAbsolutePath() + "'");
      if (!file.renameTo(newFile))
         throw new IOException("Renaming returned false");
      return newFile;
   }

   protected Metadata getMetadataForFile(File sciPgeCreatedDataFile,
         PcsMetFileWriter writer, Object[] args) throws Exception {
      return writer.getMetadataForFile(sciPgeCreatedDataFile, this.pgeMetadata,
            args);
   }

   protected void writeFromMetadata(Metadata metadata, String toMetFilePath)
         throws FileNotFoundException, IOException {
      new SerializableMetadata(metadata, "UTF-8", false)
            .writeMetadataToXmlStream(new FileOutputStream(toMetFilePath));
   }

   protected ScriptFile buildPgeRunScript() {
      ScriptFile sf = new ScriptFile(this.pgeConfig.getShellType());
      sf.setCommands(this.pgeConfig.getExeCmds());
      return sf;
   }

   protected String getScriptPath() {
      return new File(this.pgeConfig.getExeDir()).getAbsolutePath() + "/"
            + this.getPgeScriptName();
   }

   protected String getPgeScriptName() {
      return "sciPgeExeScript_" + this.pgeMetadata.getMetadata(NAME);
   }

   protected void runPge() throws Exception {
      ScriptFile sf = null;
      OutputStream stdOS = createStdOutLogger();
      OutputStream errOS = createStdErrLogger();
      try {
         long startTime = System.currentTimeMillis();

         // create script to run
         sf = this.buildPgeRunScript();
         sf.writeScriptFile(this.getScriptPath());

         // run script and evaluate whether success or failure
         this.updateStatus(RUNNING_PGE.getWorkflowStatusName());
         if (!this.wasPgeSuccessful(ExecUtils.callProgram(
               this.pgeConfig.getShellType() + " " + this.getScriptPath(),
               stdOS, errOS,
               new File(this.pgeConfig.getExeDir()).getAbsoluteFile())))
            throw new RuntimeException("Pge didn't finish successfully");
         else
            log(Level.INFO,
                  "Successfully completed running: '" + sf.getCommands() + "'");

         long endTime = System.currentTimeMillis();
         pgeMetadata.replaceMetadata(PGE_RUNTIME, (endTime - startTime) + "");

      } catch (Exception e) {
         throw new Exception("Exception when executing PGE commands '"
               + (sf != null ? sf.getCommands() : "NULL") + "' : "
               + e.getMessage(), e);
      } finally {
         try { stdOS.close(); } catch (Exception e) {}
         try { errOS.close(); } catch (Exception e) {}
      }
   }

   protected LoggerOuputStream createStdOutLogger() {
      return new LoggerOuputStream(Level.INFO);
   }

   protected LoggerOuputStream createStdErrLogger() {
      return new LoggerOuputStream(Level.SEVERE);
   }

   protected boolean wasPgeSuccessful(int returnCode) {
      return returnCode == 0;
   }

   protected void ingestProducts() throws Exception {
      StdProductCrawler crawler = new StdProductCrawler();
      this.setCrawlerConfigurations(crawler);
      this.runIngestCrawler(crawler, this.getOutputDirs());
   }

   protected List<File> getOutputDirs() {
      List<File> outputDirs = new LinkedList<File>();
      for (OutputDir outputDir : pgeConfig.getOuputDirs())
         outputDirs.add(new File(outputDir.getPath()));
      return outputDirs;
   }

   protected void setCrawlerConfigurations(StdProductCrawler crawler)
         throws Exception {
      crawler.setMetFileExtension(pgeMetadata.getMetadata(MET_FILE_EXT));
      crawler.setClientTransferer(pgeMetadata
            .getMetadata(INGEST_CLIENT_TRANSFER_SERVICE_FACTORY));
      crawler.setFilemgrUrl(pgeMetadata.getMetadata(INGEST_FILE_MANAGER_URL));
      String actionRepoFile = pgeMetadata.getMetadata(ACTION_REPO_FILE);
      if (actionRepoFile != null && !actionRepoFile.equals("")) {
         crawler.setApplicationContext(new FileSystemXmlApplicationContext(
               actionRepoFile));
         List<String> actionIds = pgeMetadata.getAllMetadata(ACTION_IDS);
         if (actionIds != null) {
            crawler.setActionIds(actionIds);
         }
      }
      crawler.setRequiredMetadata(
            pgeMetadata.getAllMetadata(REQUIRED_METADATA));
      String crawlForDirsString = pgeMetadata
            .getMetadata(CRAWLER_CRAWL_FOR_DIRS);
      boolean crawlForDirs = (crawlForDirsString != null) ? crawlForDirsString
            .toLowerCase().equals("true") : false;
      String recurString = pgeMetadata.getMetadata(CRAWLER_RECUR);
      boolean recur = (recurString != null) ? recurString.toLowerCase().equals(
            "true") : true;
      crawler.setCrawlForDirs(crawlForDirs);
      crawler.setNoRecur(!recur);
      log(Level.INFO,
            "Passing Workflow Metadata to CAS-Crawler as global metadata . . .");
      crawler.setGlobalMetadata(this.pgeMetadata
            .asMetadata(PgeMetadata.Type.DYNAMIC));
   }

   protected void runIngestCrawler(StdProductCrawler crawler,
         List<File> crawlDirs) {
      File currentDir = null;
      try {
         this.updateStatus(CRAWLING.getWorkflowStatusName());
         boolean attemptIngestAll = Boolean.parseBoolean(pgeMetadata
               .getMetadata(ATTEMPT_INGEST_ALL));
         for (File crawlDir : crawlDirs) {
            currentDir = crawlDir;
            log(Level.INFO, "Executing StdProductCrawler in productPath: ["
                  + crawlDir + "]");
            crawler.crawl(crawlDir);
            if (!attemptIngestAll)
               this.verifyIngests(crawler);
         }
         if (attemptIngestAll)
            this.verifyIngests(crawler);
      } catch (Exception e) {
         log(Level.WARNING,
             "Failed while attempting to ingest products while crawling directory '"
                     + currentDir
                     + "' (all products may not have been ingested) : "
                     + e.getMessage(), e);
      }
   }

   protected void verifyIngests(ProductCrawler crawler) throws Exception {
      boolean ingestsSuccess = true;
      String exceptionMsg = "";
      for (IngestStatus status : crawler.getIngestStatus()) {
         if (status.getResult().equals(IngestStatus.Result.FAILURE)) {
            exceptionMsg += (exceptionMsg.equals("") ? "" : " : ")
                  + "Failed to ingest product [file='"
                  + status.getProduct().getAbsolutePath() + "',result='"
                  + status.getResult() + "',msg='" + status.getMessage() + "']";
            ingestsSuccess = false;
         } else if (!status.getResult().equals(IngestStatus.Result.SUCCESS)) {
            log(Level.WARNING, "Product was not ingested [file='"
                  + status.getProduct().getAbsolutePath() + "',result='"
                  + status.getResult() + "',msg='" + status.getMessage() + "']");
         }
      }
      if (!ingestsSuccess)
         throw new Exception(exceptionMsg);
   }

   protected void updateDynamicMetadata() {
      pgeMetadata.commitMarkedDynamicMetadataKeys();
   }

   public void run(Metadata metadata, WorkflowTaskConfiguration config)
         throws WorkflowTaskInstanceException {
      Handler handler = null;
      try {
         // Initialize CAS-PGE.
         pgeMetadata = createPgeMetadata(metadata, config);
         pgeConfig = createPgeConfig();
         runPropertyAdders();
         wm = createWorkflowManagerClient();
         workflowInstId = getWorkflowInstanceId();

         // Initialize Logger.
         handler = initializePgeLogger();

         // Setup the PGE.
         createExeDir();
         createOuputDirsIfRequested();
         updateStatus(CONF_FILE_BUILD.getWorkflowStatusName());
         createSciPgeConfigFiles();

         // Run the PGE and proccess it data.
         runPge();

         // Update metadata.
         processOutput();
         updateDynamicMetadata();

         // Inject products.
         ingestProducts();
      } catch (Exception e) {
         throw new WorkflowTaskInstanceException("PGETask failed : "
               + e.getMessage(), e);
      } finally {
         closePgeLogger(handler);
      }
   }

   /**
    * OutputStream which wraps {@link PGETaskInstance}'s
    * {@link PGETaskInstance#log(Level, String)} method.
    *
    * @author bfoster (Brian Foster)
    */
   protected class LoggerOuputStream extends OutputStream {

      private CharBuffer buffer;
      private Level level;

      public LoggerOuputStream(Level level) {
         this(level, 512);
      }

      public LoggerOuputStream(Level level, int bufferSize) {
         this.level = level;
         buffer = CharBuffer.wrap(new char[bufferSize]);
      }

      @Override
      public void write(int b) throws IOException {
         if (!buffer.hasRemaining()) {
            flush();
         }
         buffer.put((char) b);
      }

      @Override
      public void flush() {
         System.out.println("HELLO");
         if (buffer.position() > 0) {
            char[] flushContext = new char[buffer.position()];
            System.arraycopy(buffer.array(), 0, flushContext, 0,
                  buffer.position());
            log(level, new String(flushContext));
            buffer.clear();
         }
      }

      @Override
      public void close() throws IOException {
         flush();
         super.close();
      }
   }
}

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
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.LOG_FILE_PATTERN;
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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

//OODT imports
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

   protected static Logger LOG = Logger.getLogger(PGETaskInstance.class
         .getName());

   protected XmlRpcWorkflowManagerClient wm;

   protected String workflowInstId;

   protected PgeMetadata pgeMetadata;

   protected PgeConfig pgeConfig;

   protected PGETaskInstance() {
   }

   protected void initialize(Metadata metadata, WorkflowTaskConfiguration config)
         throws InstantiationException {
      try {
         // merge metadata
         this.pgeMetadata = createPgeMetadata(metadata, config);

         // create PgeConfig
         this.pgeConfig = this.createPgeConfig();

         // run Property Adders.
         runPropertyAdders();

         // configure workflow manager
         wm = new XmlRpcWorkflowManagerClient(new URL(
               this.pgeMetadata
                     .getMetadata(WORKFLOW_MANAGER_URL.getName())));
         workflowInstId = this.pgeMetadata
               .getMetadata(CoreMetKeys.WORKFLOW_INST_ID);

      } catch (Exception e) {
         e.printStackTrace();
         throw new InstantiationException(
               "Failed to instanciate PGETaskInstance : " + e.getMessage());
      }
   }

   protected PgeMetadata createPgeMetadata(Metadata dynMetadata,
         WorkflowTaskConfiguration config) {
      Metadata staticMetadata = new Metadata();
      for (Object key : config.getProperties().keySet()) {
         PgeTaskMetKeys metKey = PgeTaskMetKeys.getByName((String) key);
         if (metKey != null && metKey.isVector()) {
            staticMetadata.addMetadata(
                  (String) key,
                  Lists.newArrayList(Splitter.on(",").trimResults()
                        .omitEmptyStrings()
                        .split(config.getProperty((String) key))));
         } else {
            staticMetadata.addMetadata((String) key,
                  config.getProperty((String) key));
         }
      }
      return new PgeMetadata(staticMetadata, dynMetadata);
   }

   protected void runPropertyAdders() throws Exception {
      try {
         List<String> propertyAdders = pgeMetadata
               .getAllMetadata(PROPERTY_ADDERS.getName());
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

   protected void runPropertyAdder(ConfigFilePropertyAdder propAdder) {
      propAdder.addConfigProperties(this.pgeMetadata,
            this.pgeConfig.getPropertyAdderCustomArgs());
   }

   protected PgeConfig createPgeConfig() throws Exception {
      return new XmlFilePgeConfigBuilder().build(this.pgeMetadata);
   }

   protected void updateStatus(String status) {
      try {
         LOG.log(Level.INFO, "Updating status to workflow as " + status);
         this.wm.updateWorkflowInstanceStatus(this.workflowInstId, status);
      } catch (Exception e) {
         LOG.log(Level.WARNING, "Failed to update to workflow as " + status
               + " : " + e.getMessage());
      }
   }

   protected void prePgeSetup() throws Exception {
      this.createExeDir();
      this.createOuputDirsIfRequested();
      this.createSciPgeConfigFiles();
   }

   protected void createExeDir() {
      LOG.log(Level.INFO, "Creating PGE execution working directory: ["
            + this.pgeConfig.getExeDir() + "]");
      new File(this.pgeConfig.getExeDir()).mkdirs();
   }

   protected void createOuputDirsIfRequested() {
      for (OutputDir outputDir : this.pgeConfig.getOuputDirs()) {
         if (outputDir.isCreateBeforeExe()) {
            LOG.log(Level.INFO, "Creating PGE file ouput directory: ["
                  + outputDir.getPath() + "]");
            new File(outputDir.getPath()).mkdirs();
         }
      }
   }

   protected void createSciPgeConfigFiles() throws IOException {
      this.updateStatus(CONF_FILE_BUILD.getWorkflowStatusName());
      for (DynamicConfigFile dynamicConfigFile : pgeConfig
            .getDynamicConfigFiles()) {
         try {
            this.createSciPgeConfigFile(dynamicConfigFile);
         } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Failed to created pge input config file ' "
                  + dynamicConfigFile.getFilePath() + "' : " + e.getMessage());
         }
      }
   }

   protected void createSciPgeConfigFile(DynamicConfigFile dynamicConfigFile)
         throws Exception {
      File parentDir = new File(dynamicConfigFile.getFilePath())
            .getParentFile();
      if (!parentDir.exists())
         parentDir.mkdirs();
      SciPgeConfigFileWriter writer = (SciPgeConfigFileWriter) Class.forName(
            dynamicConfigFile.getWriterClass()).newInstance();
      writer.createConfigFile(dynamicConfigFile.getFilePath(),
            this.pgeMetadata.asMetadata(), dynamicConfigFile.getArgs());
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
                     LOG.log(Level.SEVERE,
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
                           + this.pgeMetadata
                                 .getMetadata(MET_FILE_EXT.getName()));
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
      LOG.log(Level.INFO, "Renaming file '" + file.getAbsolutePath() + "' to '"
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
      return "sciPgeExeScript_"
            + this.pgeMetadata.getMetadata(NAME.getName());
   }

   protected Handler initializePgeLogHandler() throws SecurityException,
         IOException {
      FileHandler handler = null;
      String logFilePattern = this.pgeMetadata
            .getMetadata(LOG_FILE_PATTERN.getName());
      if (logFilePattern != null) {
         LOG.log(Level.INFO,
               "Creating Log Handler to capture pge output to file '"
                     + logFilePattern + "'");
         new File(logFilePattern).getParentFile().mkdirs();
         handler = new FileHandler(logFilePattern);
         handler.setFormatter(new SimpleFormatter());

      }
      return handler;
   }

   protected Logger initializePgeLogger(Handler handler) {
      if (handler != null) {
         Logger pgeLogger = Logger.getLogger(this.pgeMetadata
               .getMetadata(NAME.getName())
               + System.currentTimeMillis());
         pgeLogger.addHandler(handler);
         return pgeLogger;
      } else {
         return LOG;
      }
   }

   protected void closePgeLogHandler(Logger logger, Handler handler) {
      if (logger != null && handler != null) {
         logger.removeHandler(handler);
         handler.close();
      }
   }

   protected void runPge() throws Exception {
      ScriptFile sf = null;
      Handler handler = null;
      Logger pgeLogger = null;
      try {
         long startTime = System.currentTimeMillis();

         // create script to run
         sf = this.buildPgeRunScript();
         sf.writeScriptFile(this.getScriptPath());

         // run script and evaluate whether success or failure
         handler = this.initializePgeLogHandler();
         pgeLogger = this.initializePgeLogger(handler);
         this.updateStatus(RUNNING_PGE.getWorkflowStatusName());
         if (!this.wasPgeSuccessful(ExecUtils.callProgram(
               this.pgeConfig.getShellType() + " " + this.getScriptPath(),
               pgeLogger,
               new File(this.pgeConfig.getExeDir()).getAbsoluteFile())))
            throw new RuntimeException("Pge didn't finish successfully");
         else
            LOG.log(Level.INFO,
                  "Successfully completed running: '" + sf.getCommands() + "'");

         long endTime = System.currentTimeMillis();
         this.pgeMetadata.replaceMetadata(PGE_RUNTIME.getName(),
               (endTime - startTime) + "");

      } catch (Exception e) {
         e.printStackTrace();
         throw new Exception("Exception when executing PGE commands '"
               + (sf != null ? sf.getCommands() : "NULL") + "' : "
               + e.getMessage());
      } finally {
         this.closePgeLogHandler(pgeLogger, handler);
      }
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
      crawler.setMetFileExtension(this.pgeMetadata
            .getMetadata(MET_FILE_EXT.getName()));
      crawler.setClientTransferer(this.pgeMetadata
            .getMetadata(INGEST_CLIENT_TRANSFER_SERVICE_FACTORY.getName()));
      crawler.setFilemgrUrl(this.pgeMetadata
            .getMetadata(INGEST_FILE_MANAGER_URL.getName()));
      String actionRepoFile = this.pgeMetadata
            .getMetadata(ACTION_REPO_FILE.getName());
      if (actionRepoFile != null && !actionRepoFile.equals("")) {
         crawler.setApplicationContext(new FileSystemXmlApplicationContext(
               actionRepoFile));
         List<String> actionIds = pgeMetadata
            .getAllMetadata(ACTION_IDS.getName());
         if (actionIds != null) {
            crawler.setActionIds(actionIds);
         }
      }
      crawler.setRequiredMetadata(this.pgeMetadata
            .getAllMetadata(REQUIRED_METADATA.getName()));
      String crawlForDirsString = this.pgeMetadata
            .getMetadata(CRAWLER_CRAWL_FOR_DIRS.getName());
      boolean crawlForDirs = (crawlForDirsString != null) ? crawlForDirsString
            .toLowerCase().equals("true") : false;
      String recurString = this.pgeMetadata
            .getMetadata(CRAWLER_RECUR.getName());
      boolean recur = (recurString != null) ? recurString.toLowerCase().equals(
            "true") : true;
      crawler.setCrawlForDirs(crawlForDirs);
      crawler.setNoRecur(!recur);
      LOG.log(Level.INFO,
            "Passing Workflow Metadata to CAS-Crawler as global metadata . . .");
      crawler.setGlobalMetadata(this.pgeMetadata
            .asMetadata(PgeMetadata.Type.DYNAMIC));
   }

   protected void runIngestCrawler(StdProductCrawler crawler,
         List<File> crawlDirs) {
      File currentDir = null;
      try {
         this.updateStatus(CRAWLING.getWorkflowStatusName());
         boolean attemptIngestAll = Boolean.parseBoolean(this.pgeMetadata
               .getMetadata(ATTEMPT_INGEST_ALL.getName()));
         for (File crawlDir : crawlDirs) {
            currentDir = crawlDir;
            LOG.log(Level.INFO, "Executing StdProductCrawler in productPath: ["
                  + crawlDir + "]");
            crawler.crawl(crawlDir);
            if (!attemptIngestAll)
               this.verifyIngests(crawler);
         }
         if (attemptIngestAll)
            this.verifyIngests(crawler);
      } catch (Exception e) {
         LOG.log(
               Level.WARNING,
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
            LOG.log(Level.WARNING, "Product was not ingested [file='"
                  + status.getProduct().getAbsolutePath() + "',result='"
                  + status.getResult() + "',msg='" + status.getMessage() + "']");
         }
      }
      if (!ingestsSuccess)
         throw new Exception(exceptionMsg);
   }

   protected void updateDynamicMetadata() {
      this.pgeMetadata.commitMarkedDynamicMetadataKeys();
   }

   public void run(Metadata metadata, WorkflowTaskConfiguration config)
         throws WorkflowTaskInstanceException {
      try {
         this.initialize(metadata, config);
         this.prePgeSetup();
         this.runPge();
         this.processOutput();
         this.updateDynamicMetadata();
         this.ingestProducts();
      } catch (Exception e) {
         throw new WorkflowTaskInstanceException("PGETask failed : "
               + e.getMessage(), e);
      }
   }
}

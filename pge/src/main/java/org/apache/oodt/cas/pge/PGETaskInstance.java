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
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.ATTEMPT_INGEST_ALL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CRAWLER_CONFIG_FILE;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CRAWLER_CRAWL_FOR_DIRS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.CRAWLER_RECUR;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.DUMP_METADATA;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.FILE_STAGER;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.INGEST_CLIENT_TRANSFER_SERVICE_FACTORY;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.INGEST_FILE_MANAGER_URL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.LOG_FILENAME_PATTERN;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.MET_FILE_EXT;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.NAME;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.MIME_EXTRACTOR_REPO;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PGE_CONFIG_BUILDER;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PGE_RUNTIME;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.PROPERTY_ADDERS;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.REQUIRED_METADATA;
import static org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys.WORKFLOW_MANAGER_URL;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.CONF_FILE_BUILD;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.CRAWLING;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.RUNNING_PGE;
import static org.apache.oodt.cas.pge.metadata.PgeTaskStatus.STAGING_INPUT;
import static org.apache.oodt.cas.pge.util.GenericPgeObjectFactory.createConfigFilePropertyAdder;
import static org.apache.oodt.cas.pge.util.GenericPgeObjectFactory.createFileStager;
import static org.apache.oodt.cas.pge.util.GenericPgeObjectFactory.createPgeConfigBuilder;
import static org.apache.oodt.cas.pge.util.GenericPgeObjectFactory.createSciPgeConfigFileWriter;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.crawl.AutoDetectProductCrawler;
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawl.StdProductCrawler;
import org.apache.oodt.cas.crawl.status.IngestStatus;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.filenaming.PathUtilsNamingConvention;
import org.apache.oodt.cas.pge.config.DynamicConfigFile;
import org.apache.oodt.cas.pge.config.OutputDir;
import org.apache.oodt.cas.pge.config.PgeConfig;
import org.apache.oodt.cas.pge.config.RegExprOutputFiles;
import org.apache.oodt.cas.pge.config.XmlFilePgeConfigBuilder;
import org.apache.oodt.cas.pge.metadata.PgeMetadata;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetKeys;
import org.apache.oodt.cas.pge.staging.FileManagerFileStager;
import org.apache.oodt.cas.pge.staging.FileStager;
import org.apache.oodt.cas.pge.writers.PcsMetFileWriter;
import org.apache.oodt.cas.pge.writers.SciPgeConfigFileWriter;
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;
import org.apache.oodt.cas.workflow.util.ScriptFile;
import org.apache.oodt.commons.exec.ExecUtils;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Google imports
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Runs a CAS-style Product Generation Executive based on the PCS Wrapper
 * Architecture from mattmann et al. on OCO.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class PGETaskInstance implements WorkflowTaskInstance {

   protected Logger logger = Logger.getLogger(PGETaskInstance.class.getName());
   protected WorkflowManagerClient wm;
   protected String workflowInstId;
   protected PgeMetadata pgeMetadata;
   protected PgeConfig pgeConfig;

   protected PGETaskInstance() {}

   @Override
   public void run(Metadata metadata, WorkflowTaskConfiguration config)
         throws WorkflowTaskInstanceException {
      try {
         // Initialize CAS-PGE.
         pgeMetadata = createPgeMetadata(metadata, config);
         pgeConfig = createPgeConfig();
         runPropertyAdders();
         wm = createWorkflowManagerClient();
         workflowInstId = getWorkflowInstanceId();
         logger = createLogger(); // use workflow ID specific logger from now on 

         // Write out PgeMetadata.
         dumpMetadataIfRequested();

         // Setup the PGE.
         createExeDir();
         createOuputDirsIfRequested();
         updateStatus(CONF_FILE_BUILD.getWorkflowStatusName());
         createDynamicConfigFiles();
         updateStatus(STAGING_INPUT.getWorkflowStatusName());
         stageFiles();

         // Run the PGE.
         runPge();

         // Ingest products.
         runIngestCrawler(createProductCrawler());

         // Commit dynamic metadata.
         updateDynamicMetadata();
      } catch (Exception e) {
         logger.log(Level.SEVERE, "PGETask FAILED!!! : " + e.getMessage(), e);
         throw new WorkflowTaskInstanceException("PGETask FAILED!!! : "
               + e.getMessage(), e);
      }
   }

   protected void updateStatus(String status) throws Exception {
      logger.info("Updating status to workflow as [" + status + "]");
      if (!wm.updateWorkflowInstanceStatus(workflowInstId, status)) {
         throw new Exception(
               "Failed to update workflow status : client returned false");
      }
   }

   protected Logger createLogger() throws Exception {
      File logDir = new File(pgeConfig.getExeDir(), "logs");
      if (!(logDir.exists() || logDir.mkdirs())) {
         throw new Exception("mkdirs for logs directory return false");
      }

      Logger logger = Logger.getLogger(PGETaskInstance.class.getName()
            + "." + workflowInstId);
      FileHandler handler = new FileHandler(
            new File(logDir, createLogFileName()).getAbsolutePath());
      handler.setEncoding("UTF-8");
      handler.setFormatter(new SimpleFormatter());
      logger.addHandler(handler);
      return logger;
   }

   protected String createLogFileName() throws Exception {
      String filenamePattern = pgeMetadata.getMetadata(LOG_FILENAME_PATTERN);
      if (filenamePattern != null) {
         return filenamePattern;
      } else {
         return pgeMetadata.getMetadata(NAME) + "." + System.currentTimeMillis()
            + ".log";
      }
   }

   protected PgeMetadata createPgeMetadata(Metadata dynMetadata,
         WorkflowTaskConfiguration config) throws Exception {
      logger.info("Converting workflow configuration to static metadata...");
      Metadata staticMetadata = new Metadata();
      for (Object objKey : config.getProperties().keySet()) {
         String key = (String) objKey;
         PgeTaskMetKeys metKey = PgeTaskMetKeys.getByName(key);
         if (metKey != null && metKey.isVector()) {
            List<String> values = Lists.newArrayList(
                  Splitter.on(",").trimResults()
                  .omitEmptyStrings()
                  .split(config.getProperty(key)));
            logger.finest("Adding static metadata: key = [" + key
                  + "] value = " + values);
            staticMetadata.addMetadata(key, values);
         } else {
            String value = config.getProperty(key);
            logger.finest("Adding static metadata: key = [" + key
                  + "] value = [" + value + "]");
            staticMetadata.addMetadata(key, value);
         }
      }
      logger.info("Loading workflow context metadata...");
      for (String key : dynMetadata.getAllKeys()) {
         logger.finest(
               "Adding dynamic metadata: key = [" + key + "] value = "
                     + dynMetadata.getAllMetadata(key));
      }
      return new PgeMetadata(staticMetadata, dynMetadata);
   }

   protected PgeConfig createPgeConfig() throws Exception {
      logger.info("Create PgeConfig...");
      String pgeConfigBuilderClass = pgeMetadata
            .getMetadata(PGE_CONFIG_BUILDER);
      if (pgeConfigBuilderClass != null) {
         logger.info("Using PgeConfigBuilder: " + pgeConfigBuilderClass);
         return createPgeConfigBuilder(pgeConfigBuilderClass, logger)
               .build(pgeMetadata);
      } else {
         logger.info("Using default PgeConfigBuilder: "
               + XmlFilePgeConfigBuilder.class.getCanonicalName());
         return new XmlFilePgeConfigBuilder().build(pgeMetadata);
      }
   }

   protected void runPropertyAdders() throws Exception {
      try {
         logger.info("Loading/Running property adders...");
         List<String> propertyAdders = pgeMetadata
               .getAllMetadata(PROPERTY_ADDERS);
         if (propertyAdders != null) {
            for (String propertyAdder : propertyAdders) {
               runPropertyAdder(loadPropertyAdder(propertyAdder));
            }
         } else {
            logger.info("No property adders specified");
         }
      } catch (Exception e) {
         throw new Exception("Failed to instanciate/run Property Adders : "
               + e.getMessage(), e);
      }
   }

   protected ConfigFilePropertyAdder loadPropertyAdder(
         String propertyAdderClasspath) throws Exception {
      logger.fine("Loading property adder: " + propertyAdderClasspath);
      return createConfigFilePropertyAdder(propertyAdderClasspath, logger);
   }

   protected void runPropertyAdder(ConfigFilePropertyAdder propAdder)
         throws Exception {
      logger.info("Running property adder: "
            + propAdder.getClass().getCanonicalName());
      propAdder.addConfigProperties(pgeMetadata,
            pgeConfig.getPropertyAdderCustomArgs());
   }

   protected WorkflowManagerClient createWorkflowManagerClient()
         throws Exception {
      String url = pgeMetadata.getMetadata(WORKFLOW_MANAGER_URL);
      logger.info("Creating WorkflowManager client for url [" + url + "]");
      Validate.notNull(url, "Must specify " + WORKFLOW_MANAGER_URL);
      return RpcCommunicationFactory.createClient(new URL(url));
   }

   protected String getWorkflowInstanceId() throws Exception {
      String instanceId = pgeMetadata.getMetadata(CoreMetKeys.WORKFLOW_INST_ID);
      logger.info("Workflow instanceId is [" + instanceId + "]");
      Validate.notNull(instanceId, "Must specify "
            + CoreMetKeys.WORKFLOW_INST_ID);
      return instanceId;
   }

   protected void dumpMetadataIfRequested() throws Exception {
      if (Boolean.parseBoolean(pgeMetadata
            .getMetadata(DUMP_METADATA))) {
         new SerializableMetadata(pgeMetadata.asMetadata())
               .writeMetadataToXmlStream(new FileOutputStream(
                     getDumpMetadataPath()));
      }
   }

   protected String getDumpMetadataPath() throws Exception {
      return new File(pgeConfig.getExeDir()).getAbsolutePath() + "/"
            + getDumpMetadataName();
   }

   protected String getDumpMetadataName() throws Exception {
      return "pgetask-metadata.xml";
   }

   protected void createExeDir() throws Exception {
      logger.info("Creating PGE execution working directory: ["
            + pgeConfig.getExeDir() + "]");
      File executionDir = new File(pgeConfig.getExeDir());
      if (!(executionDir.exists() || executionDir.mkdirs())) {
         throw new Exception("mkdirs returned false for creating ["
               + pgeConfig.getExeDir() + "]");
      }
   }

   protected void createOuputDirsIfRequested() throws Exception {
      for (OutputDir outputDir : pgeConfig.getOuputDirs()) {
         if (outputDir.isCreateBeforeExe()) {
            logger.info("Creating PGE file ouput directory: ["
                  + outputDir.getPath() + "]");
            File dir = new File(outputDir.getPath());
            if (!(dir.exists() || dir.mkdirs())) {
               throw new Exception("mkdir returned false for creating ["
                     + outputDir.getPath() + "]");
            }
         }
      }
   }

   protected void stageFiles() throws Exception {
      if (pgeConfig.getFileStagingInfo() != null) {
         FileStager fileStager = getFileStager();
         logger.info("Starting file staging...");
         fileStager.stageFiles(
               pgeConfig.getFileStagingInfo(), pgeMetadata, logger);
      } else {
         logger.info("No files to stage.");
      }
   }

   protected FileStager getFileStager() throws Exception {
      String fileStagerClass = pgeMetadata.getMetadata(FILE_STAGER);
      if (fileStagerClass != null) {
         logger.info("Loading FileStager [" + fileStagerClass + "]");
         return createFileStager(fileStagerClass, logger);
      } else {
         logger.info("Using default FileStager ["
               + FileManagerFileStager.class.getCanonicalName() + "]");
         return new FileManagerFileStager();
      }
   }

   protected void createDynamicConfigFiles() throws Exception {
      logger.info("Starting creation of sci pge config files...");
      for (DynamicConfigFile dynamicConfigFile : pgeConfig
            .getDynamicConfigFiles()) {
         createDynamicConfigFile(dynamicConfigFile);
      }
      logger.info("Successfully wrote all sci pge config files!");
   }

   protected void createDynamicConfigFile(DynamicConfigFile dynamicConfigFile)
         throws Exception {
      Validate.notNull(dynamicConfigFile, "dynamicConfigFile cannot be null");
      logger.fine("Starting creation of sci pge config file ["
            + dynamicConfigFile.getFilePath() + "]...");

      // Create parent directory if it doesn't exist.
      File parentDir = new File(dynamicConfigFile.getFilePath())
            .getParentFile();
      if (!(parentDir.exists() || parentDir.mkdirs())) {
         throw new Exception("Failed to create directory where sci pge config file ["
               + dynamicConfigFile.getFilePath() + "] was to be written");
      }

      // Load writer and write file.
      logger.fine("Loading writer class for sci pge config file ["
            + dynamicConfigFile.getFilePath() + "]...");
      SciPgeConfigFileWriter writer = createSciPgeConfigFileWriter(
            dynamicConfigFile.getWriterClass(), logger);
      logger.fine("Loaded writer [" + writer.getClass().getCanonicalName()
            + "] for sci pge config file [" + dynamicConfigFile.getFilePath()
            + "]...");
      logger.info("Writing sci pge config file [" + dynamicConfigFile.getFilePath()
                  + "]...");
      File configFile = writer.createConfigFile(dynamicConfigFile.getFilePath(),
            pgeMetadata.asMetadata(), dynamicConfigFile.getArgs());
      if (!configFile.exists()) {
         throw new Exception("Writer failed to create config file ["
               + configFile + "], exists returned false");
      }
   }

   protected ScriptFile buildPgeRunScript() {
      logger.fine("Creating PGE run script for shell [" + pgeConfig.getShellType()
                  + "] with contents " + pgeConfig.getExeCmds());
      ScriptFile sf = new ScriptFile(pgeConfig.getShellType());
      sf.setCommands(pgeConfig.getExeCmds());
      return sf;
   }

   protected File getScriptPath() {
      File script = new File(pgeConfig.getExeDir(), getPgeScriptName());
      logger.fine("Script file with be written to [" + script + "]");
      return script;
   }

   protected String getPgeScriptName() {
      String pgeScriptName = "sciPgeExeScript_" + pgeMetadata.getMetadata(NAME);
      logger.fine("Generated script file name [" + pgeScriptName + "]");
      return pgeScriptName;
   }

   protected void runPge() throws Exception {
      ScriptFile sf = null;
      try {
         long startTime = System.currentTimeMillis();
         logger.info("PGE start time [" + new Date(startTime) + "]");

         // create script to run
         sf = buildPgeRunScript();
         sf.writeScriptFile(getScriptPath().getAbsolutePath());

         // run script and evaluate whether success or failure
         updateStatus(RUNNING_PGE.getWorkflowStatusName());
         logger.info("Starting execution of PGE...");
         if (!wasPgeSuccessful(ExecUtils.callProgram(
               pgeConfig.getShellType() + " " + getScriptPath(), logger,
               new File(pgeConfig.getExeDir()).getAbsoluteFile()))) {
            throw new RuntimeException("Pge didn't finish successfully");
         } else {
            logger.info(
                  "Successfully completed running: '" + sf.getCommands() + "'");
         }

         long endTime = System.currentTimeMillis();
         logger.info("PGE end time [" + new Date(startTime) + "]");

         long runTime = endTime - startTime;
         logger.info("PGE runtime in millis [" + runTime + "]");

         pgeMetadata.replaceMetadata(PGE_RUNTIME, Long.toString(runTime));

      } catch (Exception e) {
         throw new Exception("Exception when executing PGE commands '"
               + (sf != null ? sf.getCommands() : "NULL") + "' : "
               + e.getMessage(), e);
      }
   }

   protected boolean wasPgeSuccessful(int returnCode) {
      return returnCode == 0;
   }
   
   protected void processOutput() throws FileNotFoundException, IOException {
     for (final OutputDir outputDir : this.pgeConfig.getOuputDirs()) {
         File[] createdFiles = new File(outputDir.getPath()).listFiles();
         for (File createdFile : createdFiles) {
             Metadata outputMetadata = new Metadata();
             for (RegExprOutputFiles regExprFiles : outputDir
                     .getRegExprOutputFiles()) {
                 if (Pattern.matches(regExprFiles.getRegExp(), createdFile
                         .getName())) {
                     try {
                         PcsMetFileWriter writer = (PcsMetFileWriter) Class
                                 .forName(regExprFiles.getConverterClass())
                                 .newInstance();
                         outputMetadata.replaceMetadata(this.getMetadataForFile(
                 (regExprFiles.getRenamingConv() != null) 
               ? createdFile = this.renameFile(createdFile, regExprFiles.getRenamingConv())
               : createdFile, writer, regExprFiles.getArgs()));
                     } catch (Exception e) {
                         logger.severe(
                                 "Failed to create metadata file for '"
                                         + createdFile + "' : "
                                         + e.getMessage());
                     }
                 }
             }
             if (outputMetadata.getAllKeys().size() > 0)
               this.writeFromMetadata(outputMetadata, createdFile.getAbsolutePath() 
                   + "." + this.pgeMetadata.getMetadata(MET_FILE_EXT));
         }
     }
 }

	protected File renameFile(File file, PathUtilsNamingConvention renamingConv)
			throws Exception {
		Metadata curMetadata = this.pgeMetadata.asMetadata();
		curMetadata.replaceMetadata(renamingConv.getTmpReplaceMet());
		return renamingConv.rename(file, curMetadata);
	}

	protected Metadata getMetadataForFile(File sciPgeCreatedDataFile,
			PcsMetFileWriter writer, Object[] args) throws Exception {
		return writer.getMetadataForFile(sciPgeCreatedDataFile,
				this.pgeMetadata, args);
	}

	protected void writeFromMetadata(Metadata metadata, String toMetFilePath)
			throws FileNotFoundException, IOException {
		new SerializableMetadata(metadata, "UTF-8", false)
				.writeMetadataToXmlStream(new FileOutputStream(toMetFilePath));
	}

	protected ProductCrawler createProductCrawler() throws Exception {
     /* create a ProductCrawler based on whether or not the output dir specifies a MIME_EXTRACTOR_REPO */
      logger.info("Configuring ProductCrawler...");
      ProductCrawler crawler = null;
      if (pgeMetadata.getMetadata(MIME_EXTRACTOR_REPO) != null && 
    		  !pgeMetadata.getMetadata(MIME_EXTRACTOR_REPO).equals("")){
          crawler = new AutoDetectProductCrawler();
          ((AutoDetectProductCrawler)crawler).
            setMimeExtractorRepo(pgeMetadata.getMetadata(MIME_EXTRACTOR_REPO));    	  
      }
      else{
    	  crawler = new StdProductCrawler();
      }

      crawler.setClientTransferer(pgeMetadata
            .getMetadata(INGEST_CLIENT_TRANSFER_SERVICE_FACTORY));
      crawler.setFilemgrUrl(pgeMetadata.getMetadata(INGEST_FILE_MANAGER_URL));
      String crawlerConfigFile = pgeMetadata.getMetadata(CRAWLER_CONFIG_FILE);
      if (!Strings.isNullOrEmpty(crawlerConfigFile)) {
         crawler.setApplicationContext(
               new FileSystemXmlApplicationContext(crawlerConfigFile));
         List<String> actionIds = pgeMetadata.getAllMetadata(ACTION_IDS);
         if (actionIds != null) {
            crawler.setActionIds(actionIds);
         }
      }
      crawler.setRequiredMetadata(pgeMetadata.getAllMetadata(REQUIRED_METADATA));
      crawler.setCrawlForDirs(Boolean.parseBoolean(pgeMetadata
            .getMetadata(CRAWLER_CRAWL_FOR_DIRS)));
      crawler.setNoRecur(!Boolean.parseBoolean(
            pgeMetadata.getMetadata(CRAWLER_RECUR)));
      logger.fine(
            "Passing Workflow Metadata to CAS-Crawler as global metadata . . .");
      crawler.setGlobalMetadata(pgeMetadata.asMetadata(PgeMetadata.Type.DYNAMIC));
      logger.fine("Created ProductCrawler ["
            + crawler.getClass().getCanonicalName() + "]");
      return crawler;
   }

   protected void runIngestCrawler(ProductCrawler crawler) throws Exception {
      // Determine if we need to create Metadata files
	   if (crawler instanceof StdProductCrawler){
		   this.processOutput();
	   }
	   
	   // Determine directories to crawl.
      List<File> crawlDirs = new LinkedList<File>();
      for (OutputDir outputDir : pgeConfig.getOuputDirs()) {
         crawlDirs.add(new File(outputDir.getPath()));
      }

      // Start crawlin...
      updateStatus(CRAWLING.getWorkflowStatusName());
      boolean attemptIngestAll = Boolean.parseBoolean(pgeMetadata
            .getMetadata(ATTEMPT_INGEST_ALL));
      for (File crawlDir : crawlDirs) {
         logger.info("Crawling for products in [" + crawlDir + "]");
         crawler.crawl(crawlDir);
         if (!attemptIngestAll) {
            verifyIngests(crawler);
         }
      }
      if (attemptIngestAll) {
         verifyIngests(crawler);
      }
   }

   protected void verifyIngests(ProductCrawler crawler) throws Exception {
      logger.info("Verifying ingests successful...");
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
            logger.warning("Product was not ingested [file='"
                  + status.getProduct().getAbsolutePath() + "',result='"
                  + status.getResult() + "',msg='" + status.getMessage() + "']");
         }
      }
      if (!ingestsSuccess) {
         throw new Exception(exceptionMsg);
      } else {
         logger.info("Ingests were successful");
      }
   }

   protected void updateDynamicMetadata() throws Exception {
      pgeMetadata.commitMarkedDynamicMetadataKeys();
      wm.updateMetadataForWorkflow(workflowInstId,
            pgeMetadata.asMetadata(PgeMetadata.Type.DYNAMIC));
   }
}

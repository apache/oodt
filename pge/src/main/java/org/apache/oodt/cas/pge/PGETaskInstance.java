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

//JDK imports
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.crawl.ProductCrawler;
import org.apache.oodt.cas.crawl.StdProductCrawler;
import org.apache.oodt.cas.crawl.status.IngestStatus;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pge.config.DynamicConfigFile;
import org.apache.oodt.cas.pge.config.FileStagingInfo;
import org.apache.oodt.cas.pge.config.OutputDir;
import org.apache.oodt.cas.pge.config.PgeConfig;
import org.apache.oodt.cas.pge.config.PgeConfigBuilder;
import org.apache.oodt.cas.pge.config.RegExprOutputFiles;
import org.apache.oodt.cas.pge.config.RenamingConv;
import org.apache.oodt.cas.pge.config.XmlFilePgeConfigBuilder;
import org.apache.oodt.cas.pge.metadata.PGETaskMetKeys;
import org.apache.oodt.cas.pge.staging.FileManagerFileStager;
import org.apache.oodt.cas.pge.staging.FileStager;
import org.apache.oodt.cas.pge.writers.PcsMetFileWriter;
import org.apache.oodt.cas.pge.writers.SciPgeConfigFileWriter;
import org.apache.oodt.commons.exec.ExecUtils;
import org.apache.oodt.commons.xml.XMLUtils;

//JPL OODT imports
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;
import org.apache.oodt.cas.workflow.state.results.ResultsFailureState;
import org.apache.oodt.cas.workflow.state.results.ResultsState;
import org.apache.oodt.cas.workflow.state.results.ResultsSuccessState;
import org.apache.oodt.cas.workflow.util.ScriptFile;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;


/**
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Runs a CAS-style Product Generation Executive based on the PCS Wrapper
 * Architecture from mattmann et al. on OCO
 * </p>.
 */
public class PGETaskInstance extends TaskInstance {
    
    protected static final Logger LOG = Logger.getLogger(PGETaskInstance.class.getName());
        
    protected PGETaskInstance() {
    	super();
    }
    
    protected PgeConfig createPgeConfig(ControlMetadata ctrlMetadata) throws Exception {
    	String pgeConfigBuilderClass = ctrlMetadata.getMetadata(PGETaskMetKeys.PGE_CONFIG_BUILDER);
    	if (pgeConfigBuilderClass != null)
    		return ((PgeConfigBuilder) Class.forName(pgeConfigBuilderClass).newInstance()).build(ctrlMetadata);
    	else
    		return new XmlFilePgeConfigBuilder().build(ctrlMetadata);
    }
    
    protected void runPropertyAdders(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
        try {
            // load/run property adders
            String propertyAdders = ctrlMetadata.getMetadata(PGETaskMetKeys.PROPERTY_ADDERS);
            if (propertyAdders != null)
            	for (String propertyAdder : propertyAdders.split(","))
            		this.runPropertyAdder(this.loadPropertyAdder(propertyAdder, pgeConfig, ctrlMetadata), pgeConfig, ctrlMetadata);
        } catch (Exception e) {
            throw new Exception("Failed to instanciate/run Property Adders : " + e.getMessage(), e);
        }
    }

    protected ConfigFilePropertyAdder loadPropertyAdder(
            String propertyAdderClasspath, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
        return (ConfigFilePropertyAdder) Class.forName(propertyAdderClasspath)
                .newInstance();
    }

    protected void runPropertyAdder(ConfigFilePropertyAdder propAdder, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        propAdder.addConfigProperties(ctrlMetadata, pgeConfig
                .getPropertyAdderCustomArgs());
    }

    protected void createExeDir(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        LOG.log(Level.INFO, "Creating PGE execution working directory: ["
                + pgeConfig.getExeDir() + "]");
        new File(pgeConfig.getExeDir()).mkdirs();
    }

    protected void createOuputDirsIfRequested(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        for (OutputDir outputDir : pgeConfig.getOuputDirs()) {
            if (outputDir.isCreateBeforeExe()) {
                LOG.log(Level.INFO, "Creating PGE file ouput directory: ["
                        + outputDir.getPath() + "]");
                new File(outputDir.getPath()).mkdirs();
            }
        }
    }

    protected void ensureFilesStaged(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
    	if (pgeConfig.getFileStagingInfo() != null) {
    		FileStager fileStager = this.getFileStager(pgeConfig, ctrlMetadata);
    		if (fileStager != null)
    			fileStager.stageFiles(pgeConfig.getFileStagingInfo(), ctrlMetadata);
    	}
    }
    
    protected FileStager getFileStager(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
    	return new FileManagerFileStager();
    }
    
    protected void createSciPgeConfigFiles(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
//        this.update(new ExecutingState("Building Config Files"), ctrlMetadata);
        for (DynamicConfigFile dynamicConfigFile : pgeConfig
                .getDynamicConfigFiles()) {
            try {
                this.createSciPgeConfigFile(dynamicConfigFile, pgeConfig, ctrlMetadata);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException(
                        "Failed to created pge input config file ' "
                                + dynamicConfigFile.getFilePath() + "' : "
                                + e.getMessage());
            }
        }
    }

    protected void createSciPgeConfigFile(DynamicConfigFile dynamicConfigFile, PgeConfig pgeConfig, ControlMetadata ctrlMetadata)
            throws Exception {
        File parentDir = new File(dynamicConfigFile.getFilePath())
                .getParentFile();
        if (!parentDir.exists())
            parentDir.mkdirs();
        SciPgeConfigFileWriter writer = (SciPgeConfigFileWriter) Class.forName(
                dynamicConfigFile.getWriterClass()).newInstance();
        writer.createConfigFile(dynamicConfigFile.getFilePath(),
        		ctrlMetadata.asMetadata(), dynamicConfigFile.getArgs());
    }

    protected void processOutput(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws FileNotFoundException, IOException {
        for (final OutputDir outputDir : pgeConfig.getOuputDirs()) {
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
							    ? createdFile = this.renameFile(createdFile, regExprFiles.getRenamingConv(), pgeConfig, ctrlMetadata)
							    : createdFile, writer, regExprFiles.getArgs(), pgeConfig, ctrlMetadata));
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE,
                                    "Failed to create metadata file for '"
                                            + createdFile + "' : "
                                            + e.getMessage(), e);
                        }
                    }
                }
                if (outputMetadata.getAllKeys().size() > 0)
                	this.writeMetadataToFile(outputMetadata, createdFile.getAbsolutePath() 
                			+ "." + ctrlMetadata.getMetadata(PGETaskMetKeys.MET_FILE_EXT));
            }
        }
    }
    
    protected File renameFile(File file, RenamingConv renamingConv, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
    	Metadata curMetadata = ctrlMetadata.asMetadata();
    	curMetadata.replaceMetadata(renamingConv.getTmpReplaceMet());
    	String newFileName = PathUtils.doDynamicReplacement(renamingConv.getRenamingString(), curMetadata);
    	File newFile = new File(file.getParentFile(), newFileName);
        LOG.log(Level.INFO, "Renaming file '" + file.getAbsolutePath() + "' to '" + newFile.getAbsolutePath() + "'");
    	if (!file.renameTo(newFile))
    		throw new IOException("Renaming returned false");
    	return newFile;
    }

    protected Metadata getMetadataForFile(File sciPgeCreatedDataFile,
            PcsMetFileWriter writer, Object[] args, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
        return writer.getMetadataForFile(sciPgeCreatedDataFile, ctrlMetadata, args);
    }
    
    protected void writeMetadataToFile(Metadata metadata, String toMetFilePath) 
    		throws FileNotFoundException, IOException {
		new SerializableMetadata(metadata, "UTF-8", false)
				.writeMetadataToXmlStream(new FileOutputStream(toMetFilePath));
	}
    
    protected ScriptFile buildPgeRunScript(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        ScriptFile sf = new ScriptFile(pgeConfig.getShellType());
        sf.setCommands(pgeConfig.getExeCmds());
        return sf;
    }

    protected String getScriptPath(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        return new File(pgeConfig.getExeDir()).getAbsolutePath() + "/"
                + this.getPgeScriptName(pgeConfig, ctrlMetadata);
    }

    protected String getPgeScriptName(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        return "sciPgeExeScript_" + ctrlMetadata.getMetadata(PGETaskMetKeys.NAME);
    }

    protected String getDumpMetadataPath(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        return new File(pgeConfig.getExeDir()).getAbsolutePath() + "/"
                + this.getDumpMetadataName(pgeConfig, ctrlMetadata);
    }
    
    protected String getDumpMetadataName(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        return "pgetask-metadata.xml";
    }

    protected Handler initializePgeLogHandler(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws SecurityException,
			IOException {
    	FileHandler handler = null;
		String logFilePattern = ctrlMetadata.getMetadata(PGETaskMetKeys.LOG_FILE_PATTERN);
		if (logFilePattern != null) {
			LOG.log(Level.INFO, "Creating Log Handler to capture pge output to file '"
							+ logFilePattern + "'");
			new File(logFilePattern).getParentFile().mkdirs();
			handler = new FileHandler(logFilePattern);
			handler.setFormatter(new SimpleFormatter());

		}
		return handler;
	}
    
    protected Logger initializePgeLogger(Handler handler, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
    	if (handler != null) {
	    	Logger pgeLogger = Logger.getLogger(ctrlMetadata
					.getMetadata(PGETaskMetKeys.NAME)
					+ System.currentTimeMillis());
			pgeLogger.addHandler(handler);
			return pgeLogger;
    	}else {
    		return LOG;
    	}
    }
    
    protected void closePgeLogHandler(Logger logger, Handler handler, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
    	if (logger != null && handler != null) {
    		logger.removeHandler(handler);
    		handler.close();
    	}
    }
    
    protected void runPge(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
        ScriptFile sf = null;
        Handler handler = null;
        Logger pgeLogger = null;
        try {
            long startTime = System.currentTimeMillis();

            // create script to run
            sf = this.buildPgeRunScript(pgeConfig, ctrlMetadata);
            sf.writeScriptFile(this.getScriptPath(pgeConfig, ctrlMetadata));

            // run script and evaluate whether success or failure
            handler = this.initializePgeLogHandler(pgeConfig, ctrlMetadata);
            pgeLogger = this.initializePgeLogger(handler, pgeConfig, ctrlMetadata);
//            this.update(new ExecutingState("Running PGE"), ctrlMetadata);
            if (!this.wasPgeSuccessful(ExecUtils.callProgram(pgeConfig
                    .getShellType()
                    + " " + this.getScriptPath(pgeConfig, ctrlMetadata), pgeLogger, new File(pgeConfig
                    .getExeDir()).getAbsoluteFile()), pgeConfig, ctrlMetadata))
                throw new RuntimeException("Pge didn't finish successfully");
            else
                LOG.log(Level.INFO, "Successfully completed running: '"
                        + sf.getCommands() + "'");
           
            
            long endTime = System.currentTimeMillis();
            ctrlMetadata.replaceLocalMetadata(PGETaskMetKeys.PGE_RUNTIME,
                    (endTime - startTime) + "");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Exception when executing PGE commands '"
                    + (sf != null ? sf.getCommands() : "NULL") + "' : "
                    + e.getMessage());
        }finally {
        	this.closePgeLogHandler(pgeLogger, handler, pgeConfig, ctrlMetadata);
        }
    }

    protected boolean wasPgeSuccessful(int returnCode, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        return returnCode == 0;
    }

    protected void ingestProducts(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
        ProductCrawler crawler = this.createProductCrawler(pgeConfig, ctrlMetadata); 
        this.configureCrawler(crawler, pgeConfig, ctrlMetadata);
        this.runIngestCrawler(crawler, this.getOutputDirs(pgeConfig, ctrlMetadata), pgeConfig, ctrlMetadata);
    }
    
    protected ProductCrawler createProductCrawler(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
    	StdProductCrawler crawler = new StdProductCrawler();
        crawler.setMetFileExtension(ctrlMetadata
                .getMetadata(PGETaskMetKeys.MET_FILE_EXT));
    	return crawler;
    }

    protected List<File> getOutputDirs(PgeConfig pgeConfig, ControlMetadata ctrlMetadata) {
        List<File> outputDirs = new LinkedList<File>();
        for (OutputDir outputDir : pgeConfig.getOuputDirs())
            outputDirs.add(new File(outputDir.getPath()));
        return outputDirs;
    }

    protected void configureCrawler(ProductCrawler crawler, PgeConfig pgeConfig, ControlMetadata ctrlMetadata)
            throws Exception {
    	crawler.setClientTransferer(ctrlMetadata
                .getMetadata(PGETaskMetKeys.INGEST_CLIENT_TRANSFER_SERVICE_FACTORY));
        crawler.setFilemgrUrl(ctrlMetadata
                .getMetadata(PGETaskMetKeys.INGEST_FILE_MANAGER_URL));
        String actionRepoFile = ctrlMetadata
                .getMetadata(PGETaskMetKeys.ACTION_REPO_FILE);
        if (actionRepoFile != null && !actionRepoFile.equals("")) {
            crawler.setApplicationContext(new FileSystemXmlApplicationContext(
                    actionRepoFile));
            crawler.setActionIds(ctrlMetadata
                    .getAllMetadata(PGETaskMetKeys.ACTION_IDS));
        }
        crawler.setRequiredMetadata(ctrlMetadata
                .getAllMetadata(PGETaskMetKeys.REQUIRED_METADATA));
        String crawlForDirsString = ctrlMetadata
                .getMetadata(PGETaskMetKeys.CRAWLER_CRAWL_FOR_DIRS);
        boolean crawlForDirs = (crawlForDirsString != null) ? crawlForDirsString
                .toLowerCase().equals("true")
                : false;
        String recurString = ctrlMetadata
                .getMetadata(PGETaskMetKeys.CRAWLER_RECUR);
        boolean recur = (recurString != null) ? recurString.toLowerCase()
                .equals("true") : true;
        crawler.setCrawlForDirs(crawlForDirs);
        crawler.setNoRecur(!recur);
    	LOG.log(Level.INFO, "Passing Workflow Metadata to CAS-Crawler as global metadata . . .");
    	crawler.setGlobalMetadata(ctrlMetadata.asMetadata());
    }

    protected void runIngestCrawler(ProductCrawler crawler,
            List<File> crawlDirs, PgeConfig pgeConfig, ControlMetadata ctrlMetadata) throws Exception {
    	File currentDir = null;
        try {
//            this.update(new ExecutingState("Crawling/Ingesting Products"), ctrlMetadata);
        	boolean attemptIngestAll = Boolean.parseBoolean(ctrlMetadata.getMetadata(PGETaskMetKeys.ATTEMPT_INGEST_ALL));
            for (File crawlDir : crawlDirs) {
            	currentDir = crawlDir;
                LOG.log(Level.INFO,
                        "Executing StdProductCrawler in productPath: ["
                                + crawlDir + "]");
                crawler.crawl(crawlDir);
                if (!attemptIngestAll)
                	this.verifyIngests(crawler);
            }
            if (attemptIngestAll)
            	this.verifyIngests(crawler);
        } catch (Exception e) {
            throw new Exception(
                    "Failed while attempting to ingest products while crawling directory '" + currentDir + "' (all products may not have been ingested) : "
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
						+ status.getResult() + "',msg='" + status.getMessage()
						+ "']";
    			ingestsSuccess = false;
    		}else if (!status.getResult().equals(IngestStatus.Result.SUCCESS)) {
                LOG.log(Level.WARNING, "Product was not ingested [file='"
						+ status.getProduct().getAbsolutePath() + "',result='"
						+ status.getResult() + "',msg='" + status.getMessage()
						+ "']");
    		}
    	}
    	if (!ingestsSuccess)
    		throw new Exception(exceptionMsg);
    }

	@Override
	protected ResultsState performExecution(ControlMetadata ctrlMetadata) {
        try {
        	//Initialization
        	PgeConfig pgeConfig = this.createPgeConfig(ctrlMetadata);
        	this.runPropertyAdders(pgeConfig, ctrlMetadata);
        	
        	//PGE Setup
            this.createExeDir(pgeConfig, ctrlMetadata);
            this.createOuputDirsIfRequested(pgeConfig, ctrlMetadata);
            if (Boolean.parseBoolean(ctrlMetadata.getMetadata(PGETaskMetKeys.DUMP_METADATA))) 
	        	new SerializableMetadata(ctrlMetadata.asMetadata())
						.writeMetadataToXmlStream(new FileOutputStream(this
								.getDumpMetadataPath(pgeConfig, ctrlMetadata)));
            this.ensureFilesStaged(pgeConfig, ctrlMetadata);
            this.createSciPgeConfigFiles(pgeConfig, ctrlMetadata);
            
            //Run/Process PGE
            this.runPge(pgeConfig, ctrlMetadata);
            this.processOutput(pgeConfig, ctrlMetadata);
            this.ingestProducts(pgeConfig, ctrlMetadata);
        	return new ResultsSuccessState("Successfully executed PGE");
        } catch (Exception e) {
        	LOG.log(Level.SEVERE, "Failed to execute PGE : " + e.getMessage(), e);	
        	return new ResultsFailureState("Failed to execute PGE : " + e.getMessage());
        }
	}

}

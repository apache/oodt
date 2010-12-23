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

//OODT imports
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory;
import org.apache.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.pge.metadata.PGETaskMetKeys;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.Collections;

//Apache imports
import org.apache.commons.io.FileUtils;

/**
 * 
 * @author bfoster
 *
 */
public abstract class FileBasedPgeConfigBuilder implements PgeConfigBuilder {
	
	public static final String FILE_BASED_CONFIG_GROUP = PGETaskMetKeys.PGE_TASK_GROUP + "/FileBasedConfig";
    public static final String STAGE_CONFIG_FILE = FILE_BASED_CONFIG_GROUP + "/StageConfigFile";
    public static final String DATA_TRANSERER = FILE_BASED_CONFIG_GROUP + "/DataTransferer";
	public static final String CONFIG_FILE_PATH = FILE_BASED_CONFIG_GROUP + "/ConfigFilePath";
	
	public PgeConfig build(ControlMetadata ctrlMetadata) throws Exception {
		File stagedConfigFile = stagePgeConfig(ctrlMetadata);
		if (stagedConfigFile != null) {
			//update config metadata path
			ctrlMetadata.replaceLocalMetadata(CONFIG_FILE_PATH, stagedConfigFile.getAbsolutePath());
			if (ctrlMetadata.getMetadata(CONFIG_FILE_PATH, ControlMetadata.DYN) != null) 
				ctrlMetadata.commitWorkflowMetadataKeys(CONFIG_FILE_PATH);
		}
		
		PgeConfig pgeConfig = this._build(ctrlMetadata);
		
		if (stagedConfigFile != null) {
			//move config file to exe dir
			new LocalDataTransferFactory().createDataTransfer().copyProduct(createDummyProduct(stagedConfigFile.getAbsolutePath()), new File(pgeConfig.getExeDir()));
			
			//update config metadata path
			ctrlMetadata.replaceLocalMetadata(CONFIG_FILE_PATH, pgeConfig.getExeDir() + "/" + stagedConfigFile.getName());
			if (ctrlMetadata.getMetadata(CONFIG_FILE_PATH, ControlMetadata.DYN) != null) 
				ctrlMetadata.commitWorkflowMetadataKeys(CONFIG_FILE_PATH);
			
			FileUtils.deleteDirectory(stagedConfigFile.getParentFile());
		}
		
		return pgeConfig;
	}
	
	protected abstract PgeConfig _build(ControlMetadata ctrlMetadata) throws Exception;
	
    private static File stagePgeConfig(ControlMetadata ctrlMetadata) throws Exception {
    	if (Boolean.parseBoolean(ctrlMetadata.getMetadata(STAGE_CONFIG_FILE))) {
    		String origPath = ctrlMetadata.getMetadata(CONFIG_FILE_PATH);
    		File origFile = new File(origPath);
    		if (!origFile.exists()) {
	    		String configFileName = origFile.getName();
	    		
	    		//ensure tmp dir exists
	    		File tempFile = File.createTempFile("cas-pge", "bogus");
	    		tempFile.delete();
	    		File tmpDir = tempFile.getParentFile();
	    		File casPgeTmpDir = new File(tmpDir, ".cas-pge/" + System.currentTimeMillis());
	    		casPgeTmpDir.mkdirs();
	    				    		
	    		//stage config file
				DataTransfer dataTransferer = null;
				if (ctrlMetadata.getMetadata(DATA_TRANSERER) != null) 
					dataTransferer = GenericFileManagerObjectFactory.getDataTransferServiceFromFactory(ctrlMetadata.getMetadata(DATA_TRANSERER));
				else
					dataTransferer = new RemoteDataTransferFactory().createDataTransfer();
		   		dataTransferer.setFileManagerUrl(new URL(ctrlMetadata.getMetadata(PGETaskMetKeys.QUERY_FILE_MANAGER_URL)));
				dataTransferer.copyProduct(createDummyProduct(origPath), casPgeTmpDir);
				
				return new File(casPgeTmpDir, configFileName);
    		}
    	}
    	return null;
    }

	private static Product createDummyProduct(String path) {
		Product dummy = new Product();
		Reference reference = new Reference();
		reference.setDataStoreReference("file:" + new File(path).getAbsolutePath());
		dummy.setProductReferences(Collections.singletonList(reference));
		return dummy;
	}
	
}

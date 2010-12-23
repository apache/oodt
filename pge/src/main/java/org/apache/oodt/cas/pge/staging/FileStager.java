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
package org.apache.oodt.cas.pge.staging;

//JDK imports
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.pge.config.FileStagingInfo;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;

/**
 * 
 * @author bfoster
 *
 */
public abstract class FileStager {

	protected static final Logger LOG = Logger.getLogger(FileStager.class.getName());
	
	public void stageFiles(FileStagingInfo fileStagingInfo, ControlMetadata ctrlMetadata) throws Exception {
    	new File(fileStagingInfo.getStagingDir()).mkdirs();
    	for (String file : fileStagingInfo.getFilePaths()) {
			File fileHandle = new File(file);
			if (fileStagingInfo.isForceStaging() || !fileHandle.exists()) {
				LOG.log(Level.INFO, "Staging file '" + file + "' to directory '" + fileStagingInfo.getStagingDir() + "'");
				this.stageFile(file, fileStagingInfo.getStagingDir(), ctrlMetadata);
			}
    	}
	}	
	
	protected abstract void stageFile(String origPath, String destPath, ControlMetadata ctrlMetadata) throws Exception;
	
}

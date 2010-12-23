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
package org.apache.oodt.cas.workflow.processor.repo;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

//APACHE imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;

//XStream imports
import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * WorkflowProcessor repository which uses XStream
 * </p>.
 */
public class XStreamWorkflowProcessorRepository extends
		WorkflowProcessorRepository {
	
	private static final String WORKFLOW_PROCESSOR_FILENAME = "WorkflowProcessor.xstream";
	private static final String INPUT_METADATA_FILENAME = "InputMetadata.xstream";
	private static final String RUNNABLES_FILENAME = "Runnables.xstream";
	private static final String RUNNING_FILENAME = "Running.xstream";

	private File directory;
	
	public XStreamWorkflowProcessorRepository(File directory) {
		if (!directory.exists())
			directory.mkdirs();
		this.directory = directory;
	}
	
	@Override
	public synchronized WorkflowProcessor load(String instanceId) throws Exception {
		XStream xstream = new XStream();
		FileInputStream wpFileStream = null;
		try {
			File loadFile = new File(new File(this.directory, instanceId), WORKFLOW_PROCESSOR_FILENAME);
			File workerFile = new File(new File(this.directory, instanceId), WORKFLOW_PROCESSOR_FILENAME + ".worker");
			File bkupFile = new File(new File(this.directory, instanceId), WORKFLOW_PROCESSOR_FILENAME + ".bkup");
			if (workerFile.exists()) 
				loadFile = bkupFile;
			wpFileStream = new FileInputStream(loadFile);
			return (WorkflowProcessor) xstream.fromXML(wpFileStream);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			try {
				wpFileStream.close();
			}catch (Exception e) {}
		}		
	}
	
	@Override
	public synchronized void store(WorkflowProcessor workflowProcessor) throws Exception {
		File instanceDir = new File(this.directory, workflowProcessor.getInstanceId());
		if (!instanceDir.exists())
			instanceDir.mkdirs();
		
		XStream xstream = new XStream();
		FileOutputStream wpFileStream = null;
		try {
			File storeFile = new File(new File(this.directory, workflowProcessor.getInstanceId()), WORKFLOW_PROCESSOR_FILENAME);
			File workerFile = new File(new File(this.directory, workflowProcessor.getInstanceId()), WORKFLOW_PROCESSOR_FILENAME + ".worker");
			File bkupFile = new File(new File(this.directory, workflowProcessor.getInstanceId()), WORKFLOW_PROCESSOR_FILENAME + ".bkup");
			if (storeFile.exists())
				FileUtils.copyFile(storeFile, bkupFile);
			wpFileStream = new FileOutputStream(workerFile);
			xstream.toXML(workflowProcessor, wpFileStream);
			FileUtils.copyFile(workerFile, storeFile);
			workerFile.delete();
			bkupFile.delete();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				wpFileStream.close();
			}catch (Exception e) {}
		}
	}
	
	public synchronized void delete(String instanceId) throws Exception {
		 FileUtils.deleteDirectory(new File(this.directory, instanceId));
	}

	@Override
	public synchronized List<String> getStoredInstanceIds() throws Exception {
		return Arrays.asList(this.directory.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return !(name.equals(RUNNABLES_FILENAME) || name.equals(RUNNING_FILENAME) || name.equals(INPUT_METADATA_FILENAME));
			}
			
		}));
	}

}

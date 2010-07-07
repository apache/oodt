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


package org.apache.oodt.cas.workflow.repository;

//JDK imports
import org.apache.oodt.cas.metadata.util.PathUtils;

import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>A Factory class for creating {@link XMLWorkflowRepository}s.</p>
 */
public class XMLWorkflowRepositoryFactory implements WorkflowRepositoryFactory {

	/* list of dir uris specifying file paths to workflow directories */
	private List workflowDirList = null;
	
	/* our log stream */
	private static Logger LOG = Logger.getLogger(XMLWorkflowRepositoryFactory.class.getName());
	
	/**
	 * <p>Default Constructor</p>.
	 */
	public XMLWorkflowRepositoryFactory() {
		String workflowDirUris = System.getProperty("org.apache.oodt.cas.workflow.repo.dirs");
		
		if(workflowDirUris != null){
			/* do env var replacement */
			workflowDirUris = PathUtils.replaceEnvVariables(workflowDirUris);
			String [] dirUris = workflowDirUris.split(",");
			workflowDirList = Arrays.asList(dirUris);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.workflow.repository.WorkflowRepositoryFactory#createRepository()
	 */
	public WorkflowRepository createRepository() {
		if(workflowDirList != null){
			return new XMLWorkflowRepository(workflowDirList);
		}
		else{
			LOG.log(Level.WARNING, "Cannot create XML Workflow Repository: no workflow dir uris specified: value: "+System.getProperty("org.apache.oodt.cas.workflow.repo.dirs"));
			return null;
		}
	}

}

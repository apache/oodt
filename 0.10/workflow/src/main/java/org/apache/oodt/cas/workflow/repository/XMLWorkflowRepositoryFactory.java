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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.io.DirectorySelector;


/**
 * @author mattmann
 * @author luca
 * @version $Revision$
 * 
 * <p>A Factory class for creating {@link XMLWorkflowRepository}s.</p>
 */
public class XMLWorkflowRepositoryFactory implements WorkflowRepositoryFactory {

	/* list of dir uris specifying file paths to workflow directories */
	private List<String> workflowDirList = null;
	
	/* our log stream */
	private static Logger LOG = Logger.getLogger(XMLWorkflowRepositoryFactory.class.getName());
	
	/**
	 * <p>Default Constructor</p>.
	 */
	public XMLWorkflowRepositoryFactory() {
		
		String workflowDirUris = System.getProperty("org.apache.oodt.cas.workflow.repo.dirs");
		
        // only returns true if org.apache.oodt.cas.workflow.repo.dirs.recursive=true
        boolean recursive = Boolean.parseBoolean( 
        		System.getProperty("org.apache.oodt.cas.workflow.repo.dirs.recursive") );
		
		if(workflowDirUris != null){
			/* do env var replacement */
			workflowDirUris = PathUtils.replaceEnvVariables(workflowDirUris);
			String [] dirUris = workflowDirUris.split(",");
			
			// recursive directory listing
			if (recursive) {
				
            	// empty list
				workflowDirList = new ArrayList<String>();
            	
            	// loop over specified root directories,
            	// add directories and sub-directories that contain all workflow related XML files
            	for (String rootDir : dirUris) {
            		try {
            			
            			DirectorySelector dirsel = new DirectorySelector(
            					Arrays.asList( 
            							new String[] {"events.xml", "tasks.xml", "conditions.xml"} ));
            			workflowDirList.addAll( dirsel.traverseDir(new File(new URI(rootDir))) );
            			
            		} catch (URISyntaxException e) {
            			LOG.log(Level.WARNING, "URISyntaxException when traversing directory: "+rootDir);
            		}
            	}        
			
			// non-recursive directory listing
			} else {
				workflowDirList = Arrays.asList(dirUris);
			}
			
            LOG.log(Level.FINE,"Collecting XML workflows from the following directories:");
            for (String pdir : workflowDirList) {
            	LOG.log(Level.FINE, pdir);
            }
			
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

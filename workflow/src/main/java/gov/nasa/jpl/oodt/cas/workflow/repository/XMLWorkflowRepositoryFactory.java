//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.repository;

//JDK imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

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
		String workflowDirUris = System.getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.dirs");
		
		if(workflowDirUris != null){
			/* do env var replacement */
			workflowDirUris = PathUtils.replaceEnvVariables(workflowDirUris);
			String [] dirUris = workflowDirUris.split(",");
			workflowDirList = Arrays.asList(dirUris);
		}
		
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.repository.WorkflowRepositoryFactory#createRepository()
	 */
	public WorkflowRepository createRepository() {
		if(workflowDirList != null){
			return new XMLWorkflowRepository(workflowDirList);
		}
		else{
			LOG.log(Level.WARNING, "Cannot create XML Workflow Repository: no workflow dir uris specified: value: "+System.getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.dirs"));
			return null;
		}
	}

}

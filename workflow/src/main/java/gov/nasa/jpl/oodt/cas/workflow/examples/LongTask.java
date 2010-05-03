//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>An example task to simulate actual work that should last
 * a specified number of seconds.</p>
 *
 */
public class LongTask implements WorkflowTaskInstance {

	/**
	 * 
	 */
	public LongTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance#run(gov.nasa.jpl.oodt.cas.metadata.Metadata, 
	 * gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
	 */
	public void run(Metadata metadata, WorkflowTaskConfiguration config) {
		//simulate that this job takes a passed in amount of seconds to execute for
		
		long waitSeconds = (String)metadata.getMetadata("numSeconds") != null ? Long.parseLong((String)metadata.getMetadata("numSeconds")):10L;
		System.out.println("Task: Num seconds: "+waitSeconds);
		
		  try{
			  Thread.currentThread().sleep(waitSeconds*1000);
		  }
		  catch(InterruptedException ignore){}

	}

}

//Copyright (c) 2005, California Institute of Technology.
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
 * @version $Revsion$
 * 
 * <p>A simple task that takes in a static configuration parameter, "Person", 
 * and echos the string "Goodbye ${person}".</p>
 */
public class GoodbyeWorld implements WorkflowTaskInstance {

	/**
	 * 
	 */
	public GoodbyeWorld() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTasInstancek#run(gov.nasa.jpl.oodt.cas.metadata.Metadata, 
	 * gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
	 */
	public void run(Metadata metadata, WorkflowTaskConfiguration config) {
		System.out.println("Goodbye World: "+config.getProperties().get("Person"));

	}

}

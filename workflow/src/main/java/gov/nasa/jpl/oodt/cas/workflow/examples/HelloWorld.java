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
 * <p>A simple task that takes in the static configuration
 * parameter, "Person", and echos the String "Hello ${person}."</p>
 */
public class HelloWorld implements WorkflowTaskInstance {

	/**
	 * 
	 */
	public HelloWorld() {
		
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance#run(java.util.Map, 
	 * gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
	 */
	public void run(Metadata metadata, WorkflowTaskConfiguration config) {
		System.out.println("Hello World: "+config.getProperties().get("Person"));
	}

}

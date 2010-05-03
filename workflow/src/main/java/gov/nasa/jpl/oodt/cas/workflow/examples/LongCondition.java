//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>A Simple condition that evaluates to false as many times as specified
 * by the dynamic metadata parameter "numFalse". After that, the condition returns
 * true.</p>
 *
 */
public class LongCondition implements WorkflowConditionInstance {

	private int timesFalse = 0;
	
	/**
	 * 
	 */
	public LongCondition() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(gov.nasa.jpl.oodt.cas.metadata.Metadata)
	 */
    public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		//simulate that this condition takes a passed in amount of seconds to wait for
		
		int numFalse = (String)metadata.getMetadata("numFalse") != null ? Integer.parseInt((String)metadata.getMetadata("numFalse")):5;
		System.out.println("Condition: Num false: "+numFalse);
		
		if(timesFalse < numFalse){
			timesFalse++;
			return false;			
		}
		else return true;
	}

}

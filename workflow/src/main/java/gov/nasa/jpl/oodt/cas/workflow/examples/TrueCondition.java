//Copyright (c) 2005, California Institute of Technology.
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
 * @version $Revsion$
 * 
 * <p>A simple condition that always returns true when invoked.</p>
 */
public class TrueCondition implements WorkflowConditionInstance {

	/**
	 * 
	 */
	public TrueCondition() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(gov.nasa.jpl.oodt.cas.metadata.Metadata)
	 */
    public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		return true;
	}

}

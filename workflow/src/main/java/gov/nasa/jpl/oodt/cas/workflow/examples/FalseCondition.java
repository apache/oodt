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
 * @version $Revision$
 *
 * <p>A {@link WorkflowConditionInstance} that always returns false.</p>
 *
 */
public class FalseCondition implements WorkflowConditionInstance {

	/**
	 * 
	 */
	public FalseCondition() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance#evaluate(gov.nasa.jpl.oodt.cas.metadata.Metadata)
	 */
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		// TODO Auto-generated method stub
		return false;
	}

}

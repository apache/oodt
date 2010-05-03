//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * The actual evaluation method for the condition should be defined in any class
 * that implements this interface.
 * </p>
 */
public interface WorkflowConditionInstance {

	/**
	 * <p>
	 * The actual conditional: this method should return <code>true</code> if
	 * the condition is satisfied, otherwise, false.
	 * </p>
	 * 
	 * @param metadata
	 *            Any metadata needed by the conditional to determine
	 *            satisfaction.
	 * @return true if the condition is satisfied, otherwise, false.
	 */
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config);

}

//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.util;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Test Suite for the GenericWorkflowObjectFactory class.
 * </p>.
 */
public class TestGenericWorkflowObjectFactory extends TestCase {

	public void testCreateTask() {
		String taskClass = "gov.nasa.jpl.oodt.cas.workflow.examples.LongTask";

		WorkflowTaskInstance taskInst = GenericWorkflowObjectFactory
				.getTaskObjectFromClassName(taskClass);
		assertNotNull(taskInst);
		assertEquals("The class: [" + taskInst.getClass().getName()
				+ "] is not " + "equal to the expected class name: ["
				+ taskClass + "]", taskClass, taskInst.getClass().getName());
	}

	public void testCreateCondition() {
		String condClass = "gov.nasa.jpl.oodt.cas.workflow.examples.LongCondition";

		WorkflowConditionInstance condInst = GenericWorkflowObjectFactory
				.getConditionObjectFromClassName(condClass);
		assertNotNull(condInst);
		assertEquals(condClass, condInst.getClass().getName());
	}

}

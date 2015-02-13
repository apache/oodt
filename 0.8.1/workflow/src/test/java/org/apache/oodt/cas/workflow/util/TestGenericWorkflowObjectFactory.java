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


package org.apache.oodt.cas.workflow.util;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;

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
		String taskClass = "org.apache.oodt.cas.workflow.examples.LongTask";

		WorkflowTaskInstance taskInst = GenericWorkflowObjectFactory
				.getTaskObjectFromClassName(taskClass);
		assertNotNull(taskInst);
		assertEquals("The class: [" + taskInst.getClass().getName()
				+ "] is not " + "equal to the expected class name: ["
				+ taskClass + "]", taskClass, taskInst.getClass().getName());
	}

	public void testCreateCondition() {
		String condClass = "org.apache.oodt.cas.workflow.examples.LongCondition";

		WorkflowConditionInstance condInst = GenericWorkflowObjectFactory
				.getConditionObjectFromClassName(condClass);
		assertNotNull(condInst);
		assertEquals(condClass, condInst.getClass().getName());
	}

}

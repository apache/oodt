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
package org.apache.oodt.cas.cl.option.require;

//OODT imports
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ActionDependency}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestActionDependency extends TestCase {

	public void testInitialCase() {
		ActionDependency actionDependency = new ActionDependency();
		assertNull(actionDependency.getActionName());
		assertNull(actionDependency.getRelation());
	}

	public void testVariableSetting() {
		String actionName = "operation";
		Relation relation = Relation.OPTIONAL;
		ActionDependency actionDependency = new ActionDependency(actionName,
				relation);
		assertEquals(actionName, actionDependency.getActionName());
		assertEquals(relation, actionDependency.getRelation());

		actionName = "action";
		relation = Relation.REQUIRED;
		actionDependency.setActionName(actionName);
		actionDependency.setRelation(relation);
		assertEquals(actionName, actionDependency.getActionName());
		assertEquals(relation, actionDependency.getRelation());
	}
}

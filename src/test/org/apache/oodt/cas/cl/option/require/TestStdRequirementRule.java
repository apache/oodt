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

//OODT static imports
import static org.apache.oodt.cas.cl.test.util.TestUtils.createAction;

//OODT imports
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link StdRequirementRule}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestStdRequirementRule extends TestCase {

	public void testIntialCase() {
		StdRequirementRule requirementRule = new StdRequirementRule();
		assertEquals(0, requirementRule.getActionDependency().size());
		assertEquals(Relation.NONE,
				requirementRule.getRelation(createAction("operation")));
	}

	public void testVariableSetting() {
		String actionName = "operation";
		Relation relation = Relation.REQUIRED;
		ActionDependency actionDependency = new ActionDependency(actionName,
				relation);
		StdRequirementRule requirementRule = new StdRequirementRule();
		requirementRule.addActionDependency(actionDependency);
		assertEquals(Relation.NONE,
				requirementRule.getRelation(createAction("action")));
		assertEquals(relation,
				requirementRule.getRelation(createAction(actionName)));
	}
}

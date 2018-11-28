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
package org.apache.oodt.cas.cli.option.require;

//OODT imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAction;

//OODT imports
import org.apache.oodt.cas.cli.option.require.ActionDependencyRule;
import org.apache.oodt.cas.cli.option.require.RequirementRule.Relation;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ActionDependencyRule}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestActionDependencyRule extends TestCase {

   public void testInitialCase() {
      ActionDependencyRule actionDependency = new ActionDependencyRule();
      assertNull(actionDependency.getActionName());
      assertNull(actionDependency.getRelation());
   }

   public void testVariableSetting() {
      String actionName = "operation";
      Relation relation = Relation.OPTIONAL;
      ActionDependencyRule actionDependency = new ActionDependencyRule(
            actionName, relation);
      assertEquals(actionName, actionDependency.getActionName());
      assertEquals(relation, actionDependency.getRelation());

      actionName = "action";
      relation = Relation.REQUIRED;
      actionDependency.setActionName(actionName);
      actionDependency.setRelation(relation);
      assertEquals(actionName, actionDependency.getActionName());
      assertEquals(relation, actionDependency.getRelation());
   }

   public void testGetRelation() {
      String actionName = "operation";
      Relation relation = Relation.OPTIONAL;
      ActionDependencyRule actionDependency = new ActionDependencyRule(
            actionName, relation);
      assertEquals(relation,
            actionDependency.getRelation(createAction(actionName)));
   }
}

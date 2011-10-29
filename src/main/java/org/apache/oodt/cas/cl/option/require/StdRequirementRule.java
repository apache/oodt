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

//JDK imports
import java.util.HashSet;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;

/**
 * Standard {@link RequirementRule} which defines requirement
 * relationships between {@link CmdLineOption}s and
 * {@link CmdLineAction}s. 
 *
 * @author bfoster (Brian Foster)
 */
public class StdRequirementRule implements RequirementRule {

	private Set<ActionDependency> dependencies;

	public StdRequirementRule() {
		dependencies = new HashSet<ActionDependency>();
	}

	public void setActionDependency(Set<ActionDependency> dependencies) {
		this.dependencies = new HashSet<ActionDependency>(dependencies);
	}

	public void addActionDependency(ActionDependency dependency) {
		dependencies.add(dependency);
	}

	public Set<ActionDependency> getActionDependency() {
		return dependencies;
	}

	public Relation getRelation(CmdLineAction action) {
		for (ActionDependency dependency : dependencies) {
			if (dependency.getActionName().equals(action.getName())) {
				return dependency.getRelation();
			}
		}
		return Relation.NONE;
	}
}

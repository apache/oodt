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

/**
 * An {@link CmdLineAction} dependency for a {@link CmdLineOption}.
 * Links the {@link CmdLineOption} to the {@link CmdLineAction} via
 * the {@link CmdLineAction}'s name and also specifies its
 * relationship to the {@link CmdLineAction} (i.e. Required, Optional, etc...).
 *
 * @author bfoster (Brian Foster)
 */
public class ActionDependency {
	String actionName;
	Relation relation;

	public ActionDependency() {}

	public ActionDependency(String actionName, Relation relation) {
		this.actionName = actionName;
		this.relation = relation;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionName == null) ? 0 : actionName.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionDependency other = (ActionDependency) obj;
		if (actionName == null) {
			if (other.actionName != null)
				return false;
		} else if (!actionName.equals(other.actionName))
			return false;
		if (relation != other.relation)
			return false;
		return true;
	}
}

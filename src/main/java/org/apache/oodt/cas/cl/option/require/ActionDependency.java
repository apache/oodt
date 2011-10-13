package org.apache.oodt.cas.cl.option.require;

import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;

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

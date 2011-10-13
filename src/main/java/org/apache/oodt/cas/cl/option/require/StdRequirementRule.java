package org.apache.oodt.cas.cl.option.require;

import java.util.HashSet;
import java.util.Set;

import org.apache.oodt.cas.cl.action.CmdLineAction;

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

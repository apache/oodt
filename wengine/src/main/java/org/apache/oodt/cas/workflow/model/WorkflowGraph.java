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
package org.apache.oodt.cas.workflow.model;

//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.util.WorkflowUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Workflow Graph of Models which make up the mapping of workflows to one another
 * </p>.
 */
public class WorkflowGraph {

	private WorkflowGraph parent;
	private WorkflowModel model;
	private WorkflowGraph preConditions;
	private Vector<WorkflowGraph> children;
	private WorkflowGraph postConditions;
	private boolean isCondition;
	
	public WorkflowGraph(WorkflowModel model) {
		this.model = model;
		this.children = new Vector<WorkflowGraph>();
		this.isCondition = false;
	}

	public void setParent(WorkflowGraph parent) {
		this.parent = parent;
	}
	
	public void setModel(WorkflowModel model) {
		this.model = model;
	}
	
	public void setPreConditions(WorkflowGraph preConditions) {
		this.preConditions = preConditions;
		WorkflowUtils.markAsCondition(this.preConditions);
	}
	
	public void setPostConditions(WorkflowGraph postConditions) {
		this.postConditions = postConditions;
		WorkflowUtils.markAsCondition(this.postConditions);
	}
	
	public void setIsCondition(boolean isCondition) {
		this.isCondition = isCondition;
	}
	
	public void addChild(WorkflowGraph child) {
		this.children.add(child);
	}
	
	public void addChildren(List<WorkflowGraph> children) {
		this.children.addAll(children);
	}
	
	public WorkflowModel getModel() {
		return this.model;
	}
	
	public WorkflowGraph getParent() {
		return this.parent;
	}
	
	public List<WorkflowGraph> getChildren() {
		return this.children;
	}
	
	public WorkflowGraph getPreConditions() {
		return preConditions;
	}

	public WorkflowGraph getPostConditions() {
		return postConditions;
	}

	public boolean isCondition() {
		return this.isCondition;
	}
	
	public String getId() {
		return this.model.getId();
	}

}

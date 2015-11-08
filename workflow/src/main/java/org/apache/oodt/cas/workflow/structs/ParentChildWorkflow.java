/**
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

package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.List;

/**
 * 
 * A {@link Workflow} container object that maps a Parent and a Child
 * {@link Workflow} model by using the {@link Graph} construct.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ParentChildWorkflow extends Workflow {

  private Graph graph;

  public ParentChildWorkflow(Workflow workflow) {
    super(workflow.getName(), workflow.getId(), workflow.getTasks(), workflow
        .getConditions());
    this.graph = new Graph();
  }

  public ParentChildWorkflow(Graph graph) {
    super();
    this.graph = graph;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("[workflow id=");
    buf.append(this.getId());
    buf.append(",name=");
    buf.append(this.getName());
    buf.append(",parent=");
    buf.append(this.graph.getParent() != null ? this.graph.getParent()
        .getModelId() : null);
    buf.append(",children=");
    buf.append(this.graph.getChildren());
    buf.append(",executionType=");
    buf.append(this.graph.getExecutionType());
    buf.append(",tasks=");
    for (WorkflowTask task : (List<WorkflowTask>) this.getTasks()) {
      buf.append("[task name=");
      buf.append(task.getTaskName());
      buf.append(",id=");
      buf.append(task.getTaskId());
      buf.append(",instanceClass=");
      buf.append(task.getTaskInstanceClassName());
      buf.append(",requiredMet=");
      buf.append(task.getRequiredMetFields());

      buf.append(",conditions=");
      for (WorkflowCondition cond : (List<WorkflowCondition>) task
          .getConditions()) {
        buf.append("[condition name=");
        buf.append(cond.getConditionName());
        buf.append(",id=");
        buf.append(cond.getConditionId());
        buf.append(",instanceClass=");
        buf.append(cond.getConditionInstanceClassName());
        buf.append(",timeout=");
        buf.append(cond.getTimeoutSeconds());
        buf.append(",optiona=");
        buf.append(cond.isOptional());
        buf.append(",config=");
        buf.append(cond.getCondConfig().getProperties());
        buf.append("]");
      }

      buf.append("]");
    }

    return buf.toString();
  }

  /**
   * @return the graph
   */
  public Graph getGraph() {
    return graph;
  }

  /**
   * @param graph
   *          the graph to set
   */
  public void setGraph(Graph graph) {
    this.graph = graph;
  }

}

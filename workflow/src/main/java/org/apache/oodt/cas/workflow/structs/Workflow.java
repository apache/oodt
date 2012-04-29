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
import java.util.Vector;

/**
 * 
 * 
 * A Workflow is an abstract representation of a set of interconnected
 * processes. Processes, or jobs, may have dependencies upon one another, may
 * provide each other input, and/or output, or may be completely independent of
 * one another.
 * 
 * <br>
 * See <a href="http://www.gridbus.org/reports/GridWorkflowTaxonomy.pdf">Buyya
 * et al.</a> for a great description in detail of what exactly a Workflow is.
 * 
 * <br>
 * Important note: As of Apache OODT 0.4, Workflows now support both pre- and 
 * post- conditions (as opposed to just pre-conditions, as was the behavior)
 * before. The methods {@link #getConditions()} and {@link #setConditions(List)} are
 * now deprecated in favor of their {@link #getPreConditions()} and {@link #setPreConditions(List)}
 * and {@link #getPostConditions()} and {@link #setPostConditions(List)} counterparts.
 * The existing condition only methods have been preserved for back compat, but will
 * go away in later versions of the class and API. Also over the next few releases,
 * we intend to change the inner APIs to use pre and post conditions.
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class Workflow {

  private String name;

  private String id;

  private List<WorkflowTask> tasks;

  private List<WorkflowCondition> preConditions;

  private List<WorkflowCondition> postConditions;

  /**
   * Default Constructor
   * 
   */
  public Workflow() {
    this(null, null, new Vector<WorkflowTask>(), new Vector<WorkflowCondition>(), 
        new Vector<WorkflowCondition>());
  }

  /**
   * Constructs a new Workflow with the given parameters. Deprecated. Use
   * {@link #Workflow(String, String, List, List, List)} instead.
   * 
   * @param name
   *          The name of this workflow.
   * @param id
   *          The identifier for this workflow.
   * @param tasks
   *          The {@link List} of {@link WorkflowTask}s associated with this
   *          workflow.
   * 
   * @param conditions
   *          The {@link List} of {@link WorkflowCondition}s associated with
   *          this workflow.
   */
  @Deprecated
  public Workflow(String name, String id, List<WorkflowTask> tasks,
      List<WorkflowCondition> conditions) {
    this(name, id, tasks, conditions,
        new Vector<WorkflowCondition>());
  }

  /**
   * 
   * @param name
   *          The name of the Workflow.
   * @param id
   *          The identifier of the Workflow.
   * @param tasks
   *          The associated {@link List} of {@link WorkflowTask}s.
   * @param preConditions
   *          The associated {@link List} of pre-{@link WorkflowCondition}s.
   * @param postConditions
   *          The associated {@link List} of post{@link WorkflowCondition}s.
   */
  public Workflow(String name, String id, List<WorkflowTask> tasks,
      List<WorkflowCondition> preConditions,
      List<WorkflowCondition> postConditions) {
    this.name = name;
    this.id = id;
    this.tasks = tasks;
    this.preConditions = preConditions;
    this.postConditions = postConditions;

  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Deprecated. Currently, this will return a list
   * of all pre- {@link WorkflowCondition}s. The list
   * is mutable, and can change the inner portion of the
   * class. 
   * 
   * @return All pre- conditions.
   */
  @Deprecated
  public List<WorkflowCondition> getConditions() {
    return this.preConditions;
  }

  /**
   * Deprecated. Use {@link #setPreConditions(List)} or 
   * {@link #setPostConditions(List)} instead.
   * 
   * @param conditions
   *          the conditions to set
   */
  @Deprecated
  public void setConditions(List<WorkflowCondition> conditions) {
    this.preConditions = conditions;
  }

  /**
   * @param tasks
   *          the tasks to set
   */
  public void setTasks(List<WorkflowTask> tasks) {
    this.tasks = tasks;
  }

  /**
   * @return the tasks
   */
  public List<WorkflowTask> getTasks() {
    return tasks;
  }

  /**
   * @return the preConditions
   */
  public List<WorkflowCondition> getPreConditions() {
    return preConditions;
  }

  /**
   * @param preConditions the preConditions to set
   */
  public void setPreConditions(List<WorkflowCondition> preConditions) {
    this.preConditions = preConditions;
  }

  /**
   * @return the postConditions
   */
  public List<WorkflowCondition> getPostConditions() {
    return postConditions;
  }

  /**
   * @param postConditions the postConditions to set
   */
  public void setPostConditions(List<WorkflowCondition> postConditions) {
    this.postConditions = postConditions;
  }

}

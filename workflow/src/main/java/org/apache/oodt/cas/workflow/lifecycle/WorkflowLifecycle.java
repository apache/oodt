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

package org.apache.oodt.cas.workflow.lifecycle;


//JDK imports
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * Defines the lifecycle of a {@link org.apache.oodt.cas.workflow.structs.Workflow}, identifying what
 * {@link org.apache.oodt.cas.workflow.structs.WorkflowStatus}es belong to a particular phase.
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 */
public class WorkflowLifecycle {

  public static final String DEFAULT_LIFECYCLE = "__default__";

  public static final String NO_WORKFLOW_ID = "__no__workflow__id";

  private SortedSet stages;

  private String name;

  private String workflowId;

  /**
   * Default Constructor.
   * 
   */
  public WorkflowLifecycle() {
    this(null, null);
  }

  /**
   * Constructs a new WorkflowLifecycle with the given parameters.
   * 
   * @param name
   *          The name of the WorkflowLifecycle.
   * @param workflowId
   *          The associated identifier for the {@link org.apache.oodt.cas.workflow.structs.Workflow}s that this
   *          WorkflowLifecycle is appropriate for.
   */
  public WorkflowLifecycle(String name, String workflowId) {
    this.name = name;
    this.workflowId = workflowId;
    this.stages = new TreeSet(new Comparator() {

      public int compare(Object o1, Object o2) {
        WorkflowLifecycleStage stage1 = (WorkflowLifecycleStage) o1;
        WorkflowLifecycleStage stage2 = (WorkflowLifecycleStage) o2;

        if (stage1.getOrder() < stage2.getOrder()) {
          return -1;
        } else if (stage1.getOrder() == stage2.getOrder()) {
          return 0;
        } else {
          return 1;
        }
      }

    });

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
   * @return the stages
   */
  public SortedSet getStages() {
    return stages;
  }

  /**
   * Adds a {@link WorkflowStage} to this WorkflowLifecycle.
   * 
   * @param stage
   *          The {@link WorkflowStage} to add to this WorkflowLifecycle.
   */
  public void addStage(WorkflowLifecycleStage stage) {
    if (!stages.contains(stage)) {
      stages.add(stage);
    }
  }

  /**
   * Removes the given {@link WorkflowStage} from this WorkflowLifecycle.
   * 
   * @param stage
   *          The {@link WorkflowStage} to remove.
   * @return True on success, false on failure.
   */
  public boolean removeStage(WorkflowLifecycleStage stage) {
    return stages.remove(stage);
  }

  /**
   * Clears the {@link WorkflowStage}s in this WorkflowLifecycle.
   * 
   */
  public void clearStages() {
    stages.clear();
  }

  /**
   * @return the workflowId
   */
  public String getWorkflowId() {
    return workflowId;
  }

  /**
   * @param workflowId
   *          the workflowId to set
   */
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  /**
   * Gets the associated {@link WorkflowLifecycleStage} for a
   * {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance} with a given status.
   * 
   * @param status
   *          The status of the {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance} to get the
   *          {@link WorkflowLifecycleStage} for.
   * @return The corresponding {@link WorkflowLifecycleStage} for the
   *         {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance} with the given status, or null if that
   *         status does not exist in any defined {@link WorkflowLifecycleStage}
   *         .
   */
  @Deprecated
  public WorkflowLifecycleStage getStageForWorkflow(String status) {
    if (this.stages != null && this.stages.size() > 0) {
      for (Object stage1 : this.stages) {
        WorkflowLifecycleStage stage = (WorkflowLifecycleStage) stage1;
        for (WorkflowState state : stage.getStates()) {
          if (state.getName().equals(status)) {
            return stage;
          }
        }
      }

      return null;
    } else {
      return null;
    }
  }

  /**
   * Looks up an associated {@link WorkflowState} by scanning the
   * {@link WorkflowLifecycleStage}s present in this lifecycle (aka, by scanning
   * its "Categories", in Workflow2 terminology). Since no Category is provided,
   * the first instance of a {@link WorkflowState} found in the given
   * {@link WorkflowLifecycleStage} being scanned is returned, even if there are
   * multiple instances of that state (e.g., others present in another
   * category).
   * 
   * If found, the {@link WorkflowState} is returned, otherwise null is
   * returned.
   * 
   * @param stateName
   *          The name of the {@link WorkflowState} to locate and return.
   * @return The {@link WorkflowState} if found, otherwise null.
   */
  public WorkflowState getStateByName(String stateName) {
    return this.getStateByNameAndCategory(stateName, null);
  }

  /**
   * Gets a {@link WorkflowState} by its name and {@link WorkflowLifecycleStage}
   * (or Category in Workflow2 terminology). If the state is found within that
   * category, then it is returned, otherwise null is returned.
   * 
   * @param stateName
   *          The name of the {@link WorkflowState} to locate and return.
   * @param category
   *          The provided {@link WorkflowLifecycleStage} name to categorize the
   *          state by.
   * @return The {@link WorkflowState} if found, otherwise null.
   */
  public WorkflowState getStateByNameAndCategory(String stateName,
      String category) {
    if (this.getStages() != null) {
      for (WorkflowLifecycleStage stage : (SortedSet<WorkflowLifecycleStage>) this
          .getStages()) {
        if (category != null && !stage.getName().equals(category)) {
          continue;
        }
        if (stage.getStates() != null) {
          for (WorkflowState state : (List<WorkflowState>) stage.getStates()) {
            if (state.getName().equals(stateName)) {
              return makeCopy(state);
            }
          }
        }
      }
    }

    return null;
  }
  
  public WorkflowLifecycleStage getCategoryByName(String category){
    if(this.getStages() != null){
      for(WorkflowLifecycleStage stage: (SortedSet<WorkflowLifecycleStage>)this.getStages()){
        if(stage.getName().equals(category)){
          return stage;
        }
      }
    }
    
    return null;
  }
  
  public WorkflowState createState(String name, String category, String message){
    WorkflowState state = new WorkflowState();
    state.setName(name);
    state.setCategory(getCategoryByName(category));
    state.setMessage(message);
    return state;
  }

  private WorkflowState makeCopy(WorkflowState state){
    WorkflowState newState = new WorkflowState();
    newState.setCategory(state.getCategory());
    newState.setDescription(state.getDescription());
    newState.setMessage(state.getMessage());
    newState.setName(state.getName());
    newState.setPrevState(state.getPrevState());
    newState.setStartTime(state.getStartTime());
    return newState;
  }
  

}

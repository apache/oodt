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
package org.apache.oodt.cas.workflow.repository;

//JDK imports
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A mock {@link WorkflowRepository}.
 *
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class MockWorkflowRepository implements WorkflowRepository {

   private Map<String, List<Workflow>> eventToWorkflowsMap;
   private Map<String, Workflow> workflows;
   private Map<String, WorkflowTask> tasks;
   private Map<String, WorkflowCondition> conditions;

   public final static String EVENT1_NAME = "Event1_Name";
   public final static String EVENT2_NAME = "Event2_Name";

   public final static String WORKFLOW1_ID = "Workflow1_Id";
   public final static String WORKFLOW2_ID = "Workflow2_Id";
   public final static String WORKFLOW3_ID = "Workflow3_Id";

   public final static String WORKFLOW1_NAME = "Workflow1_Name";
   public final static String WORKFLOW2_NAME = "Workflow2_Name";
   public final static String WORKFLOW3_NAME = "Workflow3_Name";

   public final static String TASK1_ID = "Task1_Id";
   public final static String TASK2_ID = "Task2_Id";
   public final static String TASK3_ID = "Task3_Id";
   public final static String TASK4_ID = "Task4_Id";

   public final static String TASK1_NAME = "Task1_Name";
   public final static String TASK2_NAME = "Task2_Name";
   public final static String TASK3_NAME = "Task3_Name";
   public final static String TASK4_NAME = "Task4_Name";   

   public final static String CONDITION1_ID = "Condition1_Id";
   public final static String CONDITION2_ID = "Condition2_Id";
   public final static String CONDITION3_ID = "Condition3_Id";
   public final static String CONDITION4_ID = "Condition4_Id"; 

   public final static String CONDITION1_NAME = "Condition1_Name";
   public final static String CONDITION2_NAME = "Condition2_Name";
   public final static String CONDITION3_NAME = "Condition3_Name";
   public final static String CONDITION4_NAME = "Condition4_Name"; 

   public final static WorkflowTaskConfiguration tConf = new WorkflowTaskConfiguration();
   public final static WorkflowConditionConfiguration cConf = new WorkflowConditionConfiguration();

   public MockWorkflowRepository() {
      eventToWorkflowsMap = Maps.newHashMap();
      workflows = Maps.newHashMap();
      tasks = Maps.newHashMap();
      conditions = Maps.newHashMap();

      WorkflowCondition condition1 = new WorkflowCondition();
      condition1.setConditionId(CONDITION1_ID);
      condition1.setConditionName(CONDITION1_NAME);
      condition1.setConditionInstanceClassName("some.class.path");
      condition1.setCondConfig(cConf);
      conditions.put(condition1.getConditionId(), condition1);

      WorkflowCondition condition2 = new WorkflowCondition();
      condition2.setConditionId(CONDITION2_ID);
      condition2.setConditionName(CONDITION2_NAME);
      condition2.setCondConfig(cConf);
      condition2.setConditionInstanceClassName("some.class.path");
      conditions.put(condition2.getConditionId(), condition2);

      WorkflowCondition condition3 = new WorkflowCondition();
      condition3.setConditionId(CONDITION3_ID);
      condition3.setConditionName(CONDITION3_NAME);
      condition3.setCondConfig(cConf);
      condition3.setConditionInstanceClassName("some.class.path");
      conditions.put(condition3.getConditionId(), condition3);

      WorkflowCondition condition4 = new WorkflowCondition();
      condition4.setConditionId(CONDITION4_ID);
      condition4.setConditionName(CONDITION4_NAME);
      condition4.setCondConfig(cConf);
      condition4.setConditionInstanceClassName("some.class.path");
      conditions.put(condition4.getConditionId(), condition4);

      WorkflowTask task1 = new WorkflowTask();
      task1.setTaskId(TASK1_ID);
      task1.setTaskName(TASK1_NAME);
      task1.setTaskConfig(tConf);
      task1.setConditions(Lists.newArrayList(condition1, condition2));
      tasks.put(task1.getTaskId(), task1);

      WorkflowTask task2 = new WorkflowTask();
      task2.setTaskId(TASK2_ID);
      task2.setTaskName(TASK2_NAME);
      task2.setTaskConfig(tConf);
      task2.setConditions(Lists.newArrayList());
      tasks.put(task2.getTaskId(), task2);

      WorkflowTask task3 = new WorkflowTask();
      task3.setTaskId(TASK3_ID);
      task3.setTaskName(TASK3_NAME);
      task3.setTaskConfig(tConf);
      task3.setConditions(Lists.newArrayList());
      tasks.put(task3.getTaskId(), task3);

      WorkflowTask task4 = new WorkflowTask();
      task4.setTaskId(TASK4_ID);
      task4.setTaskName(TASK4_NAME);
      task4.setTaskConfig(tConf);
      task4.setConditions(Lists.newArrayList(condition4));
      tasks.put(task4.getTaskId(), task4);

      Workflow workflow1 = new Workflow();
      workflow1.setId(WORKFLOW1_ID);
      workflow1.setName(WORKFLOW1_NAME);
      workflow1.setTasks(Lists.newArrayList(task1, task2));
      workflow1.setConditions(Lists.newArrayList(condition1));
      workflows.put(workflow1.getId(), workflow1);

      Workflow workflow2 = new Workflow();
      workflow2.setId(WORKFLOW2_ID);
      workflow2.setName(WORKFLOW2_NAME);
      workflow2.setTasks(Lists.newArrayList(task1, task2, task4));
      workflow2.setConditions(Lists.newArrayList(condition1, condition4));
      workflows.put(workflow2.getId(), workflow2);

      Workflow workflow3 = new Workflow();
      workflow3.setId(WORKFLOW3_ID);
      workflow3.setName(WORKFLOW3_NAME);
      workflow3.setTasks(Lists.newArrayList(task3));
      workflow3.setConditions(new ArrayList<WorkflowCondition>());
      workflows.put(workflow3.getId(), workflow3);

      eventToWorkflowsMap.put(EVENT1_NAME, Lists.newArrayList(workflow1));
      eventToWorkflowsMap.put(EVENT2_NAME, Lists.newArrayList(workflow1,
            workflow2));
   }

   @Override
   public Workflow getWorkflowByName(String workflowName)
         throws RepositoryException {
      Validate.notNull(workflowName);

      for (Workflow workflow : workflows.values()) {
         if (workflow.getName().equals(workflowName)) {
            return workflow;
         }
      }
      return null;
   }

   @Override
   public Workflow getWorkflowById(String workflowId)
         throws RepositoryException {
      Validate.notNull(workflowId);

      return workflows.get(workflowId);
   }

   @Override
   public List<Workflow> getWorkflows() throws RepositoryException {
      return Lists.newArrayList(workflows.values());
   }

   @Override
   public List<WorkflowTask> getTasksByWorkflowId(String workflowId)
         throws RepositoryException {
      Workflow workflow = getWorkflowById(workflowId);
      List<WorkflowTask> tasks = Lists.newArrayList();
      if (workflow != null) {
         tasks.addAll(workflow.getTasks());
      }
      return tasks;
   }

   @Override
   public List<WorkflowTask> getTasksByWorkflowName(String workflowName)
         throws RepositoryException {
      List<WorkflowTask> tasks = Lists.newArrayList();
      Workflow workflow = getWorkflowByName(workflowName);
      if (workflow != null) {
         tasks.addAll(workflow.getTasks());
      }
      return tasks;
   }

   @Override
   public List<Workflow> getWorkflowsForEvent(String eventName)
         throws RepositoryException {
      Validate.notNull(eventName);

      List<Workflow> workflows = eventToWorkflowsMap.get(eventName);
      if (workflows == null) {
         return Lists.newArrayList();
      }
      return workflows;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<WorkflowCondition> getConditionsByTaskName(String taskName)
         throws RepositoryException {
      List<WorkflowCondition> conditions = Lists.newArrayList();
      WorkflowTask task = getWorkflowTaskByName(taskName);
      if (task != null) {
         conditions.addAll(task.getConditions()); 
      }
      return conditions;
   }

   @SuppressWarnings("unchecked")
   @Override
   public List<WorkflowCondition> getConditionsByTaskId(String taskId)
         throws RepositoryException {
      List<WorkflowCondition> conditions = Lists.newArrayList();
      WorkflowTask task = getWorkflowTaskById(taskId);
      if (task != null) {
         conditions.addAll(Lists.newArrayList(task.getConditions()));
      }
      return conditions;
   }

   @Override
   public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
         throws RepositoryException {
      WorkflowTask task = getWorkflowTaskById(taskId);
      if (task != null) {
         return task.getTaskConfig();
      }
      return null;
   }

   @Override
   public WorkflowTask getWorkflowTaskById(String taskId)
         throws RepositoryException {
      Validate.notNull(taskId);

      return tasks.get(taskId);
   }

   public WorkflowTask getWorkflowTaskByName(String taskName) {
      Validate.notNull(taskName);

      for (WorkflowTask task : tasks.values()) {
         if (task.getTaskName().equals(taskName)) {
            return task;
         }
      }
      return null;
   }

   @Override
   public WorkflowCondition getWorkflowConditionById(String conditionId)
         throws RepositoryException {
      Validate.notNull(conditionId);

      return conditions.get(conditionId);
   }

   @Override
   public List<String> getRegisteredEvents() throws RepositoryException {
      return Lists.newArrayList(eventToWorkflowsMap.keySet());
   }

   @Override
   public String addWorkflow(Workflow workflow) throws RepositoryException {
      workflows.put(workflow.getId(), workflow);
      return workflow.getId();
   }

   @Override
   public List<WorkflowCondition> getConditionsByWorkflowId(String workflowId)
         throws RepositoryException {
      List<WorkflowCondition> conditions = Lists.newArrayList();
      Workflow workflow = getWorkflowById(workflowId);
      if (workflow != null) {
         conditions.addAll(workflow.getConditions());
      }
      return conditions;
   }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#addTask(org.apache.oodt.cas.workflow.structs.WorkflowTask)
   */
  @Override
  public String addTask(WorkflowTask task) throws RepositoryException {
    // check its conditions
    if(task.getPreConditions() != null && task.getPreConditions().size() > 0){
      for(WorkflowCondition cond: task.getPreConditions()){
        if(!this.conditions.containsKey(cond.getConditionId())){
          throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined pre condition ith id: ["+cond.getConditionId()+"]");            
        }          
      }
      
      for(WorkflowCondition cond: task.getPostConditions()){
        if(!this.conditions.containsKey(cond.getConditionId())){
          throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined post condition ith id: ["+cond.getConditionId()+"]");            
        }              
      }
    }
    
    String taskId = task.getTaskId() != null ? 
        task.getTaskId():UUID.randomUUID().toString();
   this.tasks.put(taskId, task);
   return taskId;    
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTaskById(java.lang.String)
   */
  @Override
  public WorkflowTask getTaskById(String taskId) throws RepositoryException {
    return tasks.get(taskId);
  }
}

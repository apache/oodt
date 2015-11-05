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

package org.apache.oodt.cas.workflow.util;

//JDK imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * 
 * A Struct Factory for creating and unpacking Workflow Objects to be sent and
 * received across the XML-RPC wire.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class XmlRpcStructFactory {

  private XmlRpcStructFactory() throws InstantiationException {
    throw new InstantiationException("Don't instantiate XmlRpcStructFactories!");
  }

  /**
   * Gets a {@link HashMap} representation of a {@link WorkflowInstancePage}
   * that is serializable over the XML-RPC wire.
   * 
   * @param page
   *          The {@link WorkflowInstancePage} to turn into a {@link Hashtable}.
   * @return A {@link Hashtable} representation of a
   *         {@link WorkflowInstancePage}.
   */
  public static Map getXmlRpcWorkflowInstancePage(
      WorkflowInstancePage page) {
    Map pageHash = new Hashtable();
    pageHash.put("totalPages", String.valueOf(page.getTotalPages()));
    pageHash.put("pageNum", String.valueOf(page.getPageNum()));
    pageHash.put("pageSize", String.valueOf(page.getPageSize()));
    pageHash.put("pageWorkflows",
        getXmlRpcWorkflowInstances(page.getPageWorkflows()));

    return pageHash;

  }

  /**
   * Gets a {@link WorkflowInstancePage} off of the XML-RPC wire by converting
   * the XML-RPC {@link HashMap} representation of the page into a
   * {@link WorkflowInstancePage}.
   * 
   * @param pageHash
   *          The XML-RPC {@link HashMap} representation of this
   *          {@link WorkflowInstancePage}.
   * @return The {@link WorkflowInstancePage} that this XML-RPC
   *         {@link HashMap} provided represents.
   */
  public static WorkflowInstancePage getWorkflowInstancePageFromXmlRpc(
      Map pageHash) {
    WorkflowInstancePage page = new WorkflowInstancePage();
    page.setPageNum(Integer.parseInt((String) pageHash.get("pageNum")));
    page.setPageSize(Integer.parseInt((String) pageHash.get("pageSize")));
    page.setTotalPages(Integer.parseInt((String) pageHash.get("totalPages")));
    page.setPageWorkflows(getWorkflowInstancesFromXmlRpc((Vector) pageHash
        .get("pageWorkflows")));

    return page;

  }

  /**
   * <p>
   * Gets a {@link HashMap} representation of a {@link WorkflowInstance} to be
   * sent across the XML-RPC wire.
   * </p>
   * 
   * @param wInst
   *          The WorkflowInstance to turned into a java.util.HashMap.
   * @return A {@link HashMap} representation of a {@link WorkflowInstance}.
   */
  public static Map getXmlRpcWorkflowInstance(WorkflowInstance wInst) {
    Map workflowInstance = new Hashtable();
    workflowInstance.put("current_task_id", wInst.getCurrentTaskId());
    workflowInstance.put("status", wInst.getStatus());
    workflowInstance.put("id", wInst.getId());
    workflowInstance.put("workflow", getXmlRpcWorkflow(wInst.getWorkflow()));
    workflowInstance.put("start_date_time",
        wInst.getStartDateTimeIsoStr() != null ? wInst.getStartDateTimeIsoStr()
            : "");
    workflowInstance.put("end_date_time",
        wInst.getEndDateTimeIsoStr() != null ? wInst.getEndDateTimeIsoStr()
            : "");
    workflowInstance.put(
        "current_task_start_date_time",
        wInst.getCurrentTaskStartDateTimeIsoStr() != null ? wInst
            .getCurrentTaskStartDateTimeIsoStr() : "");
    workflowInstance.put(
        "current_task_end_date_time",
        wInst.getCurrentTaskEndDateTimeIsoStr() != null ? wInst
            .getCurrentTaskEndDateTimeIsoStr() : "");
    workflowInstance.put("sharedContext",
        wInst.getSharedContext() != null ? wInst.getSharedContext()
            .getHashTable() : new Hashtable());
    workflowInstance.put(
        "priority",
        wInst.getPriority() != null ? String.valueOf(wInst.getPriority()
            .getValue()) : String.valueOf(Priority.getDefault().getValue()));
    return workflowInstance;
  }

  public static WorkflowInstance getWorkflowInstanceFromXmlRpc(
      Map workflowInstance) {
    WorkflowInstance wInst = new WorkflowInstance();
    wInst.setCurrentTaskId((String) workflowInstance.get("current_task_id"));
    wInst.setStatus((String) workflowInstance.get("status"));
    wInst.setId((String) workflowInstance.get("id"));
    wInst.setWorkflow(getWorkflowFromXmlRpc((Map) workflowInstance
        .get("workflow")));
    wInst.setStartDateTimeIsoStr((String) workflowInstance
        .get("start_date_time"));
    wInst.setEndDateTimeIsoStr((String) workflowInstance.get("end_date_time"));
    wInst.setCurrentTaskStartDateTimeIsoStr((String) workflowInstance
        .get("current_task_start_date_time"));
    wInst.setCurrentTaskEndDateTimeIsoStr((String) workflowInstance
        .get("current_task_end_date_time"));
    if (workflowInstance.get("sharedContext") != null) {
      Metadata met = new Metadata();
      met.addMetadata((Map) workflowInstance.get("sharedContext"));
      wInst.setSharedContext(met);
    } else {
      wInst.setSharedContext(new Metadata());
    }


    return wInst;
  }

  /**
   * Gets a {@link List} of {@link WorkflowInstance}s from their representations
   * as {@link HashMap}s in XML-RPC.
   * 
   * @param instsVector
   *          The {@link Vector} of {@link HashMap} representations of
   *          {@link WorkflowInstance}s.
   * @return A {@link List} of {@link WorkflowInstance}s from their
   *         representations as {@link HashMap}s in XML-RPC.
   */
  public static List getWorkflowInstancesFromXmlRpc(Vector instsVector) {
    List wInsts = new Vector();

    if (instsVector != null && instsVector.size() > 0) {
      for (Object anInstsVector : instsVector) {
        Map wInstHash = (Map) anInstsVector;
        WorkflowInstance inst = getWorkflowInstanceFromXmlRpc(wInstHash);
        wInsts.add(inst);
      }
    }

    return wInsts;
  }

  /**
   * Gets an XML-RPC serializable {@link Vector} of {@link HashMap}
   * representations of {@link WorkflowInstance}s.
   * 
   * @param wInsts
   *          The {@link List} of {@link WorkflowInstance}s to serialize.
   * @return A XML-RPC serializable {@link Vector} of {@link HashMap}
   *         representations of {@link WorkflowInstance}s.
   */
  public static Vector getXmlRpcWorkflowInstances(List<WorkflowInstance> wInsts) {
    Vector instsVector = new Vector();

    if (wInsts != null && wInsts.size() > 0) {
      for (WorkflowInstance wInst : wInsts) {
        WorkflowInstance inst = wInst;
        instsVector.add(getXmlRpcWorkflowInstance(inst));
      }
    }

    return instsVector;
  }

  /**
   * <p>
   * Gets a {@link HashMap} representation of a {@link Workflow} to be sent
   * across the XML-RPC wire.
   * </p>
   * 
   * @param w
   *          The Workflow to be turned into a java.util.HashMap
   * @return A {@link HashMap} representation of a {@link Workflow}.
   */
  public static Map getXmlRpcWorkflow(Workflow w) {
    Map workflow = new Hashtable();
    workflow.put("id", w.getId());
    workflow.put("name", w.getName() != null ? w.getName() : "");
    workflow.put("tasks", getXmlRpcWorkflowTasks(w.getTasks()));
    workflow.put("conditions", getXmlRpcWorkflowConditions(w.getConditions()));

    return workflow;
  }

  /**
   * <p>
   * Gets a {@link Workflow} from the XML-RPC {@link HashMap} version.
   * </p>
   * 
   * @param w
   *          The HashMap to obtain a Workflow from.
   * @return a {@link Workflow} from the XML-RPC {@link HashMap} version.
   */
  public static Workflow getWorkflowFromXmlRpc(Map w) {
    Workflow workflow = new Workflow();
    workflow.setName((String) w.get("name"));
    workflow.setId((String) w.get("id"));
    workflow.setTasks(getWorkflowTasksFromXmlRpc((Vector) w.get("tasks")));
    workflow.setConditions(getWorkflowConditionsFromXmlRpc((Vector) w
        .get("conditions")));

    return workflow;
  }

  /**
   * <p>
   * Gets an XML-RPC version of the {@link WorkflowTask} to send over the wire.
   * </p>
   * 
   * @param t
   *          The WorkflowTask to obtain an XML-RPC HashMap from.
   * @return an XML-RPC version of the {@link WorkflowTask} to send over the
   *         wire.
   */
  public static Map getXmlRpcWorkflowTask(WorkflowTask t) {
    Map task = new Hashtable();
    task.put("class", t.getTaskInstanceClassName());
    task.put("id", t.getTaskId());
    task.put("name", t.getTaskName());
    task.put("order", String.valueOf(t.getOrder()));
    task.put("conditions", getXmlRpcWorkflowConditions(t.getConditions()));
    task.put("configuration",
        getXmlRpcWorkflowTaskConfiguration(t.getTaskConfig()));
    task.put("requiredMetFields",
        getXmlRpcWorkflowTaskReqMetFields(t.getRequiredMetFields()));
    return task;
  }

  /**
   * <p>
   * Gets a {@link Vector} representation of a {@link List} of
   * {@link WorkflowTask}s to be sent across the XML-RPC wire.
   * </p>
   * 
   * @param tasks
   *          The {@link List} of {@link WorkflowTask}s.
   * 
   * @return A {@link Vector} representation of a {@link List} of
   *         {@link WorkflowTask}s.
   */
  public static Vector getXmlRpcWorkflowTasks(List tasks) {
    Vector wTasks = new Vector();

    if (tasks == null) {
      return wTasks;
    }

    for (Object task1 : tasks) {
      WorkflowTask t = (WorkflowTask) task1;
      Map task = getXmlRpcWorkflowTask(t);
      wTasks.add(task);
    }

    return wTasks;
  }

  /**
   * <p>
   * Gets a {@link WorkflowTask} from an XML-RPC {@link HashMap} sent over the
   * wire.
   * </p>
   * 
   * @param task
   *          The XML-RPC HashMap version of the WorkflowTask.
   * @return a {@link WorkflowTask} from an XML-RPC {@link HashMap} sent over
   *         the wire.
   */
  public static WorkflowTask getWorkflowTaskFromXmlRpc(Map task) {
    WorkflowTask t = new WorkflowTask();
    t.setTaskInstanceClassName((String) task.get("class"));
    t.setTaskId((String) task.get("id"));
    t.setTaskName((String) task.get("name"));
    t.setOrder(Integer.valueOf((String) task.get("order")));
    t.setTaskConfig(getWorkflowTaskConfigurationFromXmlRpc((Map) task
        .get("configuration")));
    t.setConditions(getWorkflowConditionsFromXmlRpc((Vector) task
        .get("conditions")));
    t.setRequiredMetFields(getWorkflowTaskReqMetFieldsFromXmlRpc((Vector) task
        .get("requiredMetFields")));

    return t;
  }

  /**
   * <p>
   * Gets a {@link List} of {@link WorkflowTask}s from an XML-RPC {@link Vector}
   * .
   * </p>
   * 
   * @param tsks
   *          The {@link Vector} of {@link WorkflowTask}s.
   * @return A {@link List} of {@link WorkflowTask}s from an XML-RPC
   *         {@link Vector}.
   */
  public static List getWorkflowTasksFromXmlRpc(Vector tsks) {
    List tasks = new Vector();

    for (Object tsk : tsks) {
      Map taskHashMap = (Hashtable) tsk;
      WorkflowTask task = getWorkflowTaskFromXmlRpc(taskHashMap);
      tasks.add(task);

    }
    return tasks;
  }

  /**
   * <p>
   * Gets an XML-RPC {@link HashMap} representation of the
   * {@link WorkflowCondition} to send over the wire.
   * </p>
   * 
   * @param c
   *          The WorkflowCondition to turn into an XML-RPC HashMap.
   * @return an XML-RPC {@link HashMap} representation of the
   *         {@link WorkflowCondition} to send over the wire.
   */
  public static Map getXmlRpcWorkflowCondition(WorkflowCondition c) {
    Map condition = new Hashtable();
    condition.put("class", c.getConditionInstanceClassName());
    condition.put("id", c.getConditionId());
    condition.put("name", c.getConditionName());
    condition.put("order", String.valueOf(c.getOrder()));
    condition.put("timeout", String.valueOf(c.getTimeoutSeconds()));
    condition.put("optional", String.valueOf(c.isOptional()));
    condition.put("configuration",
        getXmlRpcWorkflowConditionConfig(c.getCondConfig()));
    return condition;

  }

  /**
   * Bulids an XML-RPC friendly version of a
   * {@link WorkflowConditionConfiguration}.
   * 
   * @param conf
   *          The {@link WorkflowConditionConfiguration} to transform into an
   *          XML-RPC {@link HashMap}.
   * @return an XML-RPC friendly version of a
   *         {@link WorkflowConditionConfiguration}.
   */
  public static Map getXmlRpcWorkflowConditionConfig(
      WorkflowConditionConfiguration conf) {
    Map confHash = new Hashtable();
    for (String propName : (Set<String>) (Set<?>) conf.getProperties().keySet()) {
      confHash.put(propName, conf.getProperty(propName));
    }
    return confHash;
  }

  /**
   * <p>
   * Gets a {@link Vector} representation of the {@link List} of
   * {@link WorkflowCondition}s to be sent across the XML-RPC wire.
   * </p>
   * 
   * @param conditions
   *          The List of WorkflowConditions to turn into a Vector.
   * @return A {@link Vector} representation of a {@link List} of
   *         {@link WorkflowCondition}s.
   */
  public static Vector getXmlRpcWorkflowConditions(List conditions) {
    Vector wConditions = new Vector();

    /*
     * because conditions are optional, so if they're null, just return an empty
     * Vector: XML-RPC doesn't support null
     */
    if (conditions == null) {
      return wConditions;
    }

    for (Object condition1 : conditions) {
      WorkflowCondition c = (WorkflowCondition) condition1;
      Map condition = getXmlRpcWorkflowCondition(c);
      wConditions.add(condition);
    }

    return wConditions;
  }

  /**
   * Gets the required {@link List} of {@link String} met fields for this
   * {@link WorkflowTask}.
   * 
   * @param fields
   *          The fields required for this task.
   * @return The {@link List} of {@link String} met fields for this
   *         {@link WorkflowTask}.
   */
  public static List getWorkflowTaskReqMetFieldsFromXmlRpc(Vector fields) {
    List reqFields = new Vector();

    if (fields == null) {
      return reqFields;
    }

    for (Object field : fields) {
      String reqField = (String) field;
      reqFields.add(reqField);
    }

    return reqFields;
  }

  /**
   * Gets a {@link List} of {@link String}s that are required {@link Metadata}
   * fields for this {@link WorkflowTask}.
   * 
   * @param metFields
   *          The required {@link Metadata} fields.
   * @return A {@link List} of {@link String}s that are required for this
   *         {@link WorkflowTask}.
   */
  public static Vector getXmlRpcWorkflowTaskReqMetFields(List metFields) {
    Vector fields = new Vector();
    if (metFields == null) {
      return fields;
    }

    for (Object metField : metFields) {
      String reqFieldName = (String) metField;
      fields.add(reqFieldName);
    }

    return fields;
  }

  /**
   * <p>
   * Gets a {@link WorkflowCondition} from an XML-RPC {@link HashMap}.
   * </p>
   * 
   * @param cond
   *          The HashMap to turn into a real WorkflowCondition.
   * @return a {@link WorkflowCondition} from an XML-RPC {@link HashMap}.
   */
  public static WorkflowCondition getWorkflowConditionFromXmlRpc(Map cond) {
    WorkflowCondition condition = new WorkflowCondition();
    condition.setConditionInstanceClassName((String) cond.get("class"));
    condition.setConditionId((String) cond.get("id"));
    condition.setConditionName((String) cond.get("name"));
    condition.setOrder(Integer.valueOf((String) cond.get("order")));
    condition
        .setTimeoutSeconds(Long.valueOf(cond.get("timeout") != null ? (String) cond
            .get("timeout") : "-1"));
    condition.setOptional(Boolean.valueOf((String) cond.get("optional")));
    condition
        .setCondConfig(getWorkflowConditionConfigurationFromXmlRpc((Map) cond
            .get("configuration")));
    return condition;
  }

  /**
   * Unravels a {@link WorkflowConditionConfiguration} from XML-RPC.
   * 
   * @param conf
   *          XML-RPC friendly HashMap representing a
   *          {@link WorkflowConditionConfiguration}.
   * @return A {@link WorkflowConditionConfiguration} from XML-RPC.
   */
  public static WorkflowConditionConfiguration getWorkflowConditionConfigurationFromXmlRpc(
      Map conf) {
    WorkflowConditionConfiguration config = new WorkflowConditionConfiguration();
    for (String key : (Set<String>) (Set<?>) conf.keySet()) {
      config.addConfigProperty(key, (String) conf.get(key));
    }

    return config;
  }

  /**
   * <p>
   * Gets a {@link List} of {@link WorkflowCondition}s from an XML-RPC
   * {@link Vector}.
   * </p>
   * 
   * @param conds
   *          The {@link Vector} of {@link WorkflowCondition}s.
   * @return A {@link List} of {@link WorkflowCondition}s from an XML-RPC
   *         {@link Vector}.
   */
  public static List getWorkflowConditionsFromXmlRpc(Vector conds) {
    List conditions = new Vector();

    if (conds != null && conds.size() > 0) {
      for (Object cond1 : conds) {
        Map cond = (Map) cond1;
        WorkflowCondition condition = getWorkflowConditionFromXmlRpc(cond);
        conditions.add(condition);
      }
    }

    return conditions;
  }

  /**
   * <p>
   * Gets a {@link HashMap} representation of the passed in
   * {@link WorkflowTaskConfiguration}'s {@link Properties} to be sent across
   * the XML-RPC wire.
   * </p>t
   * 
   * @param config
   *          The WorkflowTaskConfiguration to convert to a HashMap.
   * @return A {@link HashMap} representation of the passed in
   *         {@link WorkflowTaskConfiguration}'s {@link Properties}.
   */
  public static Map getXmlRpcWorkflowTaskConfiguration(
      WorkflowTaskConfiguration config) {
    Map configuration = new Hashtable();

    for (Object o : config.getProperties().keySet()) {
      String name = (String) o;
      String value = (String) config.getProperties().get(name);
      configuration.put(name, value);
    }

    return configuration;
  }

  /**
   * <p>
   * Gets a {@link WorkflowTaskConfiguration} from an XML-RPC {@link HashMap}.
   * 
   * @param config
   *          The original HashMap version of the WorkflowTaskConfiguration.
   * @return A {@link WorkflowTaskConfiguration} from an XML-RPC
   *         {@link HashMap}.
   */
  public static WorkflowTaskConfiguration getWorkflowTaskConfigurationFromXmlRpc(
      Map config) {
    WorkflowTaskConfiguration configuration = new WorkflowTaskConfiguration();

    for (Object o : config.keySet()) {
      String name = (String) o;
      String value = (String) config.get(name);

      configuration.getProperties().put(name, value);
    }

    return configuration;
  }

}

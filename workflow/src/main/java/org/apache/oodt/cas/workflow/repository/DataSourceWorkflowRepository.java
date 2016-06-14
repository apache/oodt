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

package org.apache.oodt.cas.workflow.repository;

import org.apache.oodt.cas.workflow.examples.NoOpTask;
import org.apache.oodt.cas.workflow.structs.*;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.DbStructFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 
 * A {@link DataSource}-based implementation of a workflow repository.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class DataSourceWorkflowRepository implements WorkflowRepository {

  /* our data source */
  private DataSource dataSource = null;

  /* our log stream */
  private Logger LOG = Logger.getLogger(DataSourceWorkflowRepository.class
      .getName());

  /**
   * <p>
   * Default Constructor
   * </p>
   * .
   */
  public DataSourceWorkflowRepository(DataSource ds) {
    dataSource = ds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowByName
   * (java.lang.String)
   */
  public Workflow getWorkflowByName(String workflowName)
      throws RepositoryException {
    return getWorkflowByName(workflowName, true, true);
  }

  public Workflow getWorkflowByName(String workflowName, boolean getTasks,
      boolean getConditions) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    Workflow workflow = null;

    try {
      if(dataSource!=null) {
        conn = dataSource.getConnection();
      }
      else{
        throw new RepositoryException("Null datasource");
      }
      statement = conn.createStatement();

      String getWorkflowSql = "SELECT * from workflows WHERE workflow_name = '"
          + workflowName + "'";

      LOG.log(Level.FINE, "getWorkflowByName: Executing: " + getWorkflowSql);
      rs = statement.executeQuery(getWorkflowSql);

      while (rs.next()) {
        workflow = DbStructFactory.getWorkflow(rs);

        if (getTasks) {
          workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
        }

        if (getConditions) {
          workflow.setConditions(getConditionsByWorkflowId(workflow.getId()));
          handleGlobalWorkflowConditions(workflow);
        }
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting workflow. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback getWorkflowByName transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return workflow;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowById
   * (java.lang.String)
   */
  public Workflow getWorkflowById(String workflowId) throws RepositoryException {
    return getWorkflowById(workflowId, true, true);
  }

  public Workflow getWorkflowById(String workflowId, boolean getTasks,
      boolean getConditions) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    Workflow workflow = null;

    try {

      if(dataSource!=null) {
        conn = dataSource.getConnection();
      }
      else{
        throw new RepositoryException("Null datasource");
      }      statement = conn.createStatement();

      String getWorkflowSql = "SELECT * from workflows WHERE workflow_id = '"
          + workflowId + "'";

      LOG.log(Level.FINE, "getWorkflowById: Executing: " + getWorkflowSql);
      rs = statement.executeQuery(getWorkflowSql);

      while (rs.next()) {
        workflow = DbStructFactory.getWorkflow(rs);

        if (getTasks) {
          workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
        }

        if (getConditions) {
          workflow.setConditions(getConditionsByWorkflowId(workflow.getId()));
          handleGlobalWorkflowConditions(workflow);
        }
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting workflow. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback getWorkflowById transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return workflow;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflows()
   */
  public List getWorkflows() throws RepositoryException {
    return getWorkflows(true, true);
  }

  public List getWorkflows(boolean getTasks, boolean getConditions)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List workflows = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getWorkflowSql = "SELECT * from workflows";

      LOG.log(Level.FINE, "getWorkflows: Executing: " + getWorkflowSql);
      rs = statement.executeQuery(getWorkflowSql);
      workflows = new Vector();

      while (rs.next()) {
        Workflow workflow = DbStructFactory.getWorkflow(rs);

        if (getTasks) {
          workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
        }

        if (getConditions) {
          workflow.setConditions(getConditionsByWorkflowId(workflow.getId()));
          handleGlobalWorkflowConditions(workflow);
        }

        workflows.add(workflow);
      }

      if (workflows.size() == 0) {
        workflows = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting workflows. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback getWorkflows transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return workflows;
  }

  public List getTasksByWorkflowId(String workflowId)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List tasks = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getTasksSql = "SELECT workflow_tasks.*, workflow_task_map.task_order "
          + "FROM workflow_tasks, workflow_task_map "
          + "WHERE workflow_task_map.workflow_task_id = workflow_tasks.workflow_task_id "
          + "AND workflow_task_map.workflow_id = "
          + workflowId
          + " "
          + "ORDER BY workflow_task_map.task_order";

      LOG.log(Level.FINE, "getTasksByWorkflowId: Executing: " + getTasksSql);
      rs = statement.executeQuery(getTasksSql);
      tasks = new Vector();

      while (rs.next()) {
        // get an instance of the class name

        WorkflowTask task = DbStructFactory.getWorkflowTask(rs, true);

        if (task != null) {
          task.setConditions(getConditionsByTaskId(task.getTaskId()));
          task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
          tasks.add(task);
        }
      }

      if (tasks.size() == 0) {
        tasks = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting tasks for workflow. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getTasksByWorkflowId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return tasks;
  }

  public List<WorkflowTask> getTasksByWorkflowName(String workflowName)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List<WorkflowTask> tasks = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getTasksSql = "SELECT workflow_tasks.*, workflow_task_map.task_order "
          + "FROM workflow_tasks, workflow_task_map, workflows "
          + "WHERE workflow_task_map.workflow_task_id = workflow_tasks.workflow_task_id "
          + "AND workflow_task_map.workflow_id = workflows.workflow_id "
          + "AND workflows.workflow_name = '"
          + workflowName
          + "' "
          + "ORDER BY workflow_task_map.task_order";

      LOG.log(Level.FINE, "getTasksByWorkflowName: Executing: " + getTasksSql);
      rs = statement.executeQuery(getTasksSql);
      tasks = new Vector();

      while (rs.next()) {
        // get an instance of the class name
        WorkflowTask task = DbStructFactory.getWorkflowTask(rs, true);

        if (task != null) {
          task.setConditions(getConditionsByTaskId(task.getTaskId()));
          task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
          tasks.add(task);
        }
      }

      if (tasks.size() == 0) {
        tasks = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting tasks for workflow. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getTasksByWorkflowName transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return tasks;
  }

  public List<Workflow> getWorkflowsForEvent(String eventName) throws RepositoryException {
    return getWorkflowsForEvent(eventName, true, true);
  }

  public List<Workflow> getWorkflowsForEvent(String eventName, boolean getTasks,
      boolean getConditions) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List<Workflow> workflows = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getWorkflowSql = "SELECT * from workflows, event_workflow_map WHERE event_workflow_map.workflow_id = workflows.workflow_id  "
          + "AND event_workflow_map.event_name = '" + eventName + "'";

      LOG.log(Level.FINE, "getWorkflowsForEvent: Executing: " + getWorkflowSql);
      rs = statement.executeQuery(getWorkflowSql);
      workflows = new Vector();

      while (rs.next()) {
        Workflow workflow = DbStructFactory.getWorkflow(rs);

        if (getTasks) {
          workflow.setTasks(getTasksByWorkflowId(workflow.getId()));
        }

        if (getConditions) {
          workflow.setConditions(getConditionsByWorkflowId(workflow.getId()));
          handleGlobalWorkflowConditions(workflow);
        }

        workflows.add(workflow);
      }

      if (workflows.size() == 0) {
        workflows = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting workflows for event. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getWorkflowsForEvent transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return workflows;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConditionsByTaskName(java.lang.String)
   */
  public List<WorkflowCondition> getConditionsByTaskName(String taskName)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List conditions = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConditionsSql = "SELECT workflow_conditions.*, task_condition_map.condition_order "
          + "FROM workflow_conditions, task_condition_map, workflow_tasks "
          + "WHERE task_condition_map.workflow_condition_id = workflow_conditions.workflow_condition_id "
          + "AND task_condition_map.workflow_task_id = workflow_tasks.workflow_task_id "
          + "AND workflow_tasks.workflow_task_name = '"
          + taskName
          + "' "
          + "ORDER BY task_condition_map.condition_order";

      LOG.log(Level.FINE, "getConditionsByTaskName: Executing: "
          + getConditionsSql);
      rs = statement.executeQuery(getConditionsSql);
      conditions = new Vector();

      while (rs.next()) {
        // get an instance of the class name
        WorkflowCondition condition = DbStructFactory.getWorkflowCondition(rs,
            true);
        conditions.add(condition);
      }

      if (conditions.size() == 0) {
        conditions = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting conditions for task. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConditionsByTaskName transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return conditions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConditionsByTaskId(java.lang.String)
   */
  public List getConditionsByTaskId(String taskId) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List conditions = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConditionsSql = "SELECT workflow_conditions.*, task_condition_map.condition_order "
          + "FROM workflow_conditions, task_condition_map "
          + "WHERE task_condition_map.workflow_condition_id = workflow_conditions.workflow_condition_id "
          + "AND task_condition_map.workflow_task_id = "
          + taskId
          + " "
          + "ORDER BY task_condition_map.condition_order";

      LOG.log(Level.FINE, "getConditionsByTaskId: Executing: "
          + getConditionsSql);
      rs = statement.executeQuery(getConditionsSql);
      conditions = new Vector();

      while (rs.next()) {
        // get an instance of the class name
        WorkflowCondition condition = DbStructFactory.getWorkflowCondition(rs,
            true);
        conditions.add(condition);
      }

      if (conditions.size() == 0) {
        conditions = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting conditions for task. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConditionsByTaskId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return conditions;
  }

  public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    WorkflowTaskConfiguration config = null;

    try {
      if(dataSource!=null) {
        conn = dataSource.getConnection();
      }
      else{
        throw new RepositoryException("Null data source");
      }
      statement = conn.createStatement();

      String getConfigurationSql = "SELECT * from workflow_task_configuration WHERE workflow_task_id = "
          + taskId;

      LOG.log(Level.FINE, "getConfigurationByTaskId: Executing: "
          + getConfigurationSql);
      rs = statement.executeQuery(getConfigurationSql);

      config = new WorkflowTaskConfiguration();
      while (rs.next()) {
        config.getProperties().put(rs.getString("property_name"),
            rs.getString("property_value"));
      }

      if (config.getProperties().keySet().size() == 0) {
        config = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting task configuration for taskId: " + taskId
              + " Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConfigurationBytaskId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return config;
  }

  public WorkflowConditionConfiguration getConfigurationByConditionId(
      String condId) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    WorkflowConditionConfiguration config = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConfigurationSql = "SELECT * from workflow_condition_configuration WHERE workflow_condition_id = "
          + condId;

      LOG.log(Level.FINE, "getConfigurationByConfigurationId: Executing: "
          + getConfigurationSql);
      rs = statement.executeQuery(getConfigurationSql);

      config = new WorkflowConditionConfiguration();
      while (rs.next()) {
        config.getProperties().put(rs.getString("property_name"),
            rs.getString("property_value"));
      }

      if (config.getProperties().keySet().size() == 0) {
        config = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting task configuration for condId: " + condId
              + " Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConfigurationByConfigurationId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowTaskById
   * (java.lang.String)
   */
  public WorkflowTask getWorkflowTaskById(String taskId)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    WorkflowTask task = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getTaskSql = "SELECT * FROM workflow_tasks WHERE workflow_task_id = "
          + taskId;

      LOG.log(Level.FINE, "getWorkflowTaskById: Executing: " + getTaskSql);
      rs = statement.executeQuery(getTaskSql);

      while (rs.next()) {
        // get an instance of the class name
        task = DbStructFactory.getWorkflowTask(rs, false);
        task.setConditions(getConditionsByTaskId(task.getTaskId()));
        task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting task by id. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getWorkflowTaskById transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return task;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getWorkflowConditionById(java.lang.String)
   */
  public WorkflowCondition getWorkflowConditionById(String conditionId)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    WorkflowCondition condition = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConditionsSql = "SELECT * FROM workflow_conditions WHERE workflow_condition_id = "
          + conditionId;

      LOG.log(Level.FINE, "getWorkflowConditionById: Executing: "
          + getConditionsSql);
      rs = statement.executeQuery(getConditionsSql);

      while (rs.next()) {
        // get an instance of the class name
        condition = DbStructFactory.getWorkflowCondition(rs, false);
        if (condition != null) {
          condition.setCondConfig(getConfigurationByConditionId(conditionId));
        }
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting condition by id. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getWorkflowConditionById transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return condition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getRegisteredEvents
   * ()
   */
  public List<String> getRegisteredEvents() throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List events = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getEventSql = "SELECT DISTINCT event_name FROM event_workflow_map ORDER BY event_name ASC";

      LOG.log(Level.FINE, "getRegisteredEvents: Executing: " + getEventSql);
      rs = statement.executeQuery(getEventSql);
      events = new Vector();

      while (rs.next()) {
        // get an instance of the class name
        String event = DbStructFactory.getEvent(rs);
        events.add(event);
      }

      if (events.size() == 0) {
        events = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting registered events. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getRegisteredEvents transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return events;
  }
  

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#addTask(org.apache.oodt.cas.workflow.structs.WorkflowTask)
   */
  @Override
  public String addTask(WorkflowTask task) throws RepositoryException {
    List<WorkflowCondition> allConditions = getConditions();
    // check its conditions
    if(task.getPreConditions() != null && task.getPreConditions().size() > 0){
      for(WorkflowCondition cond: task.getPreConditions()){
        if(!this.hasConditionId(allConditions, cond.getConditionId())){
          throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined pre condition ith id: ["+cond.getConditionId()+"]");            
        }          
      }
      
      for(WorkflowCondition cond: task.getPostConditions()){
        if(!this.hasConditionId(allConditions, cond.getConditionId())){
          throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined post condition ith id: ["+cond.getConditionId()+"]");            
        }              
      }
    }

    return this.commitTask(null, task);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#addWorkflow(
   * org.apache.oodt.cas.workflow.structs.Workflow)
   */
  @Override
  public String addWorkflow(Workflow workflow) throws RepositoryException {
    // first check to see that its tasks are all present
    if (workflow.getTasks() == null || (workflow.getTasks().size() == 0)) {
      throw new RepositoryException("Attempt to define a new worklfow: ["
          + workflow.getName() + "] with no tasks.");
    }

    List<WorkflowTask> allTasks = this.getTasks();

    for (WorkflowTask task : workflow.getTasks()) {
      if (!this.hasTaskId(allTasks, task.getTaskId())) {
        throw new RepositoryException("Reference in new workflow: ["
            + workflow.getName() + "] to undefined task with id: ["
            + task.getTaskId() + "]");
      }

      // check its conditions
      if (task.getConditions() != null && task.getConditions().size() > 0) {
        List<WorkflowCondition> conditions = this.getConditionsByTaskId(task
            .getTaskId());
        for (WorkflowCondition cond : (List<WorkflowCondition>) task
            .getConditions()) {
          if (!this.hasConditionId(conditions, cond.getConditionId())) {
            throw new RepositoryException("Reference in new workflow: ["
                + workflow.getName() + "] to undefined condition ith id: ["
                + cond.getConditionId() + "]");
          }
        }
      }
    }

    return this.commitWorkflow(workflow);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConditionsByWorkflowId(java.lang.String)
   */
  @Override
  public List<WorkflowCondition> getConditionsByWorkflowId(String workflowId)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List<WorkflowCondition> conditions = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConditionsSql = "SELECT workflow_conditions.*, workflow_condition_map.condition_order "
          + "FROM workflow_conditions, workflow_condition_map "
          + "WHERE workflow_condition_map.workflow_condition_id = workflow_conditions.workflow_condition_id "
          + "AND workflow_condition_map.workflow_id = "
          + workflowId
          + " "
          + "ORDER BY workflow_condition_map.condition_order";

      LOG.log(Level.FINE, "getConditionsByWorkflowId: Executing: "
          + getConditionsSql);
      rs = statement.executeQuery(getConditionsSql);
      conditions = new Vector<WorkflowCondition>();

      while (rs.next()) {
        // get an instance of the class name
        WorkflowCondition condition = DbStructFactory.getWorkflowCondition(rs,
            true);
        conditions.add(condition);
      }

      if (conditions.size() == 0) {
        conditions = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(
          Level.WARNING,
          "Exception getting conditions for workflow. Message: "
              + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConditionsByWorkflowId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return conditions;
  }
  

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTaskById(java.lang.String)
   */
  @Override
  public WorkflowTask getTaskById(String taskId) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    WorkflowTask task = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getTasksSql = "SELECT * "
          + "FROM workflow_tasks "
          + "WHERE workflow_task_id = " + taskId;

      LOG.log(Level.FINE, "getTaskById: Executing: " + getTasksSql);
      rs = statement.executeQuery(getTasksSql);
     

      while (rs.next()) {
        // get an instance of the class name

        task = DbStructFactory.getWorkflowTask(rs, true);

        if (task != null) {
          task.setConditions(getConditionsByTaskId(task.getTaskId()));
          task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
        }
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting tasks for workflow. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getTasksByWorkflowId transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return task;
  }    

  private String commitWorkflow(Workflow workflow) throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    String workflowId = null;

    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);
      statement = conn.createStatement();

      String sql = "INSERT INTO workflows (workflow_id, workflow_name) VALUES ('"+workflow.getId()+"','"
          + workflow.getName() + "')";

      LOG.log(Level.FINE, "commitWorkflowToDB: Executing: " + sql);
      statement.execute(sql);

      sql = "SELECT MAX(workflow_id) AS max_id FROM workflows";
      rs = statement.executeQuery(sql);

      while (rs.next()) {
        workflowId = String.valueOf(rs.getInt("max_id"));
      }

      workflow.setId(workflowId);

      // event to workflow map
      sql = "INSERT INTO event_workflow_map (workflow_id, event_name) VALUES ("
          + workflowId + ",'workflow-" + workflowId + "')";
      LOG.log(Level.FINE, "commitWorkflowToDB: Executing: " + sql);
      statement.execute(sql);
      conn.commit();

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception adding workflow. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback workflow transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return workflowId;
  }

  private String commitTask(Workflow workflow, WorkflowTask task)
      throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;
    String taskId = null;

    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);
      statement = conn.createStatement();

      String sql = "SELECT MAX(workflow_task_id) AS max_id FROM workflow_tasks";
      LOG.log(Level.FINE, "commitTaskToDB: Executing: " + sql);
      rs = statement.executeQuery(sql);

      while (rs.next()) {
        taskId = String.valueOf(rs.getInt("max_id"));
      }

      synchronized (taskId) {
        taskId = String.valueOf(Integer.valueOf(taskId) + 1);
      }

      task.setTaskId(taskId);
      sql = "INSERT INTO workflow_tasks (workflow_task_id, workflow_task_name, workflow_task_class) VALUES ("
          + taskId
          + ", '"
          + task.getTaskName()
          + "', '"
          + task.getTaskInstanceClassName() + "')";

      LOG.log(Level.FINE, "commitTaskToDB: Executing: " + sql);
      statement.execute(sql);

      if (workflow != null) {
        // task to workflow map
        sql = "INSERT INTO workflow_task_map (workflow_id, workflow_task_id, task_order) VALUES ("
            + workflow.getId() + "," + taskId + ",1)";
        LOG.log(Level.FINE, "commitTaskToDB: Executing: " + sql);
        statement.execute(sql);
      }

      conn.commit();

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception adding task. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE, "Unable to rollback task transaction. Message: "
            + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return taskId;
  }
  
  public List<WorkflowCondition> getConditions() throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List<WorkflowCondition> conditions = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getConditionsSql = "SELECT workflow_conditions.* "
          + "FROM workflow_conditions "
          + "ORDER BY workflow_condition_id";

      LOG.log(Level.FINE, "getConditions: Executing: "
          + getConditionsSql);
      rs = statement.executeQuery(getConditionsSql);
      conditions = new Vector<WorkflowCondition>();

      while (rs.next()) {
        // get an instance of the class name
        WorkflowCondition condition = DbStructFactory.getWorkflowCondition(rs,
            true);
        conditions.add(condition);
      }

      if (conditions.size() == 0) {
        conditions = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Exception getting conditions. Message: "
          + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(Level.SEVERE,
            "Unable to rollback getConditions transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return conditions;
  }  
  
  private List<WorkflowTask> getTasks() throws RepositoryException {
    Connection conn = null;
    Statement statement = null;
    ResultSet rs = null;

    List<WorkflowTask> tasks = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();

      String getTasksSql = "SELECT workflow_tasks.*, workflow_task_map.task_order "
          + "FROM workflow_tasks, workflow_task_map WHERE workflow_tasks.workflow_task_id = workflow_task_map.workflow_task_id "
                           + "ORDER BY workflow_task_map.task_order";

      LOG.log(Level.FINE, "getTasks: Executing: " + getTasksSql);
      rs = statement.executeQuery(getTasksSql);
      tasks = new Vector<WorkflowTask>();

      while (rs.next()) {
        // get an instance of the class name

        WorkflowTask task = DbStructFactory.getWorkflowTask(rs, true);

        if (task != null) {
          task.setConditions(getConditionsByTaskId(task.getTaskId()));
          task.setTaskConfig(getConfigurationByTaskId(task.getTaskId()));
          tasks.add(task);
        }
      }

      if (tasks.size() == 0) {
        tasks = null;
      }

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting tasks. Message: " + e.getMessage());
      try {
        if (conn != null) {
          conn.rollback();
        }
      } catch (SQLException e2) {
        LOG.log(
            Level.SEVERE,
            "Unable to rollback getTasks transaction. Message: "
                + e2.getMessage());
      }
      throw new RepositoryException(e.getMessage());
    } finally {

      if (rs != null) {
        try {
          rs.close();
        } catch (SQLException ignore) {
        }

      }

      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }

      }

      if (conn != null) {
        try {
          conn.close();

        } catch (SQLException ignore) {
        }

      }
    }

    return tasks;
  }

  private boolean hasTaskId(List<WorkflowTask> tasks, String id) {
    if (tasks == null || (tasks.size() == 0)) {
      return false;
    }

    for (WorkflowTask task : tasks) {
      if (task.getTaskId().equals(id)) {
        return true;
      }
    }

    return false;
  }

  private boolean hasConditionId(List<WorkflowCondition> conds, String id) {
    if (conds == null || (conds.size() == 0)) {
      return false;
    }

    for (WorkflowCondition cond : conds) {
      if (cond.getConditionId().equals(id)) {
        return true;
      }
    }

    return false;
  }

  private void handleGlobalWorkflowConditions(Workflow workflow) throws RepositoryException {
    if (workflow.getConditions() != null && workflow.getConditions().size() > 0) {
      if (workflow.getTasks() == null || (workflow.getTasks().size() == 0)) {
        workflow.setTasks(new Vector<WorkflowTask>());
      }

      workflow.getTasks().add(
          0,
          getGlobalWorkflowConditionsTask(workflow,
              workflow.getConditions()));
    }
  }

  private WorkflowTask getGlobalWorkflowConditionsTask(Workflow workflow,
      List<WorkflowCondition> conditions) throws RepositoryException {
    WorkflowTask task = new WorkflowTask();
    task.setConditions(conditions);
    task.setTaskConfig(new WorkflowTaskConfiguration());
    task.setTaskId(workflow.getId() + "-global-conditions-eval");
    task.setTaskName(workflow.getName() + "-global-conditions-eval");
    task.setTaskInstanceClassName(NoOpTask.class.getName());
    task.setTaskId(this.commitTask(workflow, task));
    return task;
  }


}

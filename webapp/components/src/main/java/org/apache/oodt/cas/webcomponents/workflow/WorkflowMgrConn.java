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

package org.apache.oodt.cas.webcomponents.workflow;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowMgrConn implements Serializable {

  private static final long serialVersionUID = -9081117871702614402L;

  private static final Logger LOG = Logger.getLogger(WorkflowMgrConn.class
      .getName());

  private WorkflowManagerClient wm;

  public WorkflowMgrConn(String wmUrlStr) {
    try {
      this.wm = RpcCommunicationFactory.createClient(new URL(wmUrlStr));
    } catch (MalformedURLException e) {
      LOG.log(Level.SEVERE, "Unable to contact Workflow Manager at URL: ["
          + wmUrlStr + "]: Message: " + e.getMessage());
      this.wm = null;
    }
  }

  public WorkflowTask safeGetTaskById(String taskId) {
    try {
      return new SerializableWorkflowTask(this.wm.getTaskById(taskId));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow task with ID: ["
          + taskId + "]: Message: " + e.getMessage());
      return null;
    }
  }

  public WorkflowCondition safeGetConditionById(String conditionId) {
    try {
      return new SerializableWorkflowCondition(this.wm
          .getConditionById(conditionId));
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow condition with ID: ["
          + conditionId + "]: Message: " + e.getMessage());
      return null;
    }
  }

  public Workflow safeGetWorkflowById(String workflowId) {
    try {
      return this.wm.getWorkflowById(workflowId);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow with ID: ["
          + workflowId + "]: Message: " + e.getMessage());
      return null;
    }
  }

  public List<Workflow> safeGetWorkflowsByEvent(String eventName) {
    try {
      return this.wm.getWorkflowsByEvent(eventName);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflows by event: ["
          + eventName + "]: Message: " + e.getMessage());
      return new Vector<Workflow>();
    }
  }

  public List<String> safeGetRegisteredEvents() {
    try {
      return this.wm.getRegisteredEvents();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain registered events: Message: "
          + e.getMessage());
      return new Vector<String>();
    }
  }

  public List<Workflow> safeGetWorkflows() {
    try {
      return this.wm.getWorkflows();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflows: Message: "
          + e.getMessage());
      return new Vector<Workflow>();
    }
  }

  public List<WorkflowInstance> safeGetWorkflowInsts() {
    try {
      return this.wm.getWorkflowInstances();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow instances: Message: "
          + e.getMessage());
      return new Vector<WorkflowInstance>();
    }
  }

  public List<WorkflowInstance> safeGetWorkflowInstsByStatus(String status) {
    try {
      return this.wm.getWorkflowInstancesByStatus(status);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow instances by status: ["
          + status + "]: Message: " + e.getMessage());
      return new Vector<WorkflowInstance>();
    }
  }

  public WorkflowInstancePage safeGetWorkflowInstPageByStatus(int pageNum,
      String status) {
    try {
      return this.wm.paginateWorkflowInstances(pageNum, status);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow instance page: ["
          + pageNum + "] by status: [" + status + "]: Message: "
          + e.getMessage());
      return null;
    }
  }

  public WorkflowInstancePage safeGetWorkflowInstPageByStatus(int pageNum) {
    try {
      return this.wm.paginateWorkflowInstances(pageNum);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Unable to obtain workflow instance page: ["
          + pageNum + "]: Message: " + e.getMessage());
      return null;
    }
  }

  public double safeGetWorkflowWallClockMinutes(WorkflowInstance inst) {
    try {
      return this.wm.getWorkflowWallClockMinutes(inst.getId());
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Unable to obtain workflow wall clock mins: inst id: ["
              + inst.getId() + "]: Message: " + e.getMessage());
      return -999.0;
    }
  }

  public double safeGetWorkflowCurrentTaskWallClockMinutes(WorkflowInstance inst) {
    try {
      return this.wm.getWorkflowCurrentTaskWallClockMinutes(inst.getId());
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "Unable to obtain workflow current task wall clock mins: inst id: ["
              + inst.getId() + "]: Message: " + e.getMessage());
      return -999.0;
    }
  }

  public WorkflowManagerClient getWM() {
    return this.wm;
  }

  class SerializableWorkflowTask extends WorkflowTask implements Serializable {

    private static final long serialVersionUID = 6859678680008137795L;

    public SerializableWorkflowTask(WorkflowTask task) {
      this.taskName = task.getTaskName();
      this.taskId = task.getTaskId();
      this.taskInstanceClassName = task.getTaskInstanceClassName();
      this.preConditions = task.getPreConditions();
      this.postConditions = task.getPostConditions();
      this.order = task.getOrder();
      this.requiredMetFields = task.getRequiredMetFields();
      this.taskConfig = task.getTaskConfig();
    }
  }

  class SerializableWorkflowCondition extends WorkflowCondition implements
      Serializable {

    private static final long serialVersionUID = 909843381568743621L;

    public SerializableWorkflowCondition(WorkflowCondition cond) {
      this.setConditionName(cond.getConditionName());
      this.conditionInstanceClassName = cond.getConditionInstanceClassName();
      this.order = cond.getOrder();
      this.condConfig = cond.getTaskConfig();
    }

  }

}

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

package org.apache.oodt.cas.wmservices.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.workflow.repository.PackagedWorkflowRepository;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.commons.xml.XMLUtils;

/**
 * Helper class to handle PackagedWorkflowRepository workflows
 * 
 * @author vratnakar
 */
public class PackagedWorkflowManager {
  private PackagedWorkflowRepository repo;

  /**
   * Constructor
   * 
   * @param workflowDir
   *          directory where packaged workflows exist
   * @throws InstantiationException
   */
  public PackagedWorkflowManager()
      throws InstantiationException {
    this.repo = new PackagedWorkflowRepository(Collections.EMPTY_LIST);
  }

  /**
   * Add a workflow to the repository
   * 
   * @param workflow
   *          a {@Link Workflow} to add into the repository
   * @throws RepositoryException
   */
  public void addWorkflow(Workflow workflow, String workflowDir) throws RepositoryException {
    this.loadTasksToRepo(workflow);
    String workflowId = this.repo.addWorkflow(workflow);
    String filePath = workflowDir + File.separator + workflowId + ".xml";
    this.saveWorkflow(workflowId, filePath);
  }

  /**
   * Serialize a workflow
   * 
   * @param workflow
   * @return XML representation of the workflow
   * @throws RepositoryException
   */
  public String serializeWorkflow(Workflow workflow) throws RepositoryException {
    try {
      this.loadTasksToRepo(workflow);
      String workflowId = this.repo.addWorkflow(workflow);
      File f = File.createTempFile("tempworkflow-", "-packaged");
      this.saveWorkflow(workflowId, f.getAbsolutePath());
      String workflowXML = FileUtils.readFileToString(f);
      f.delete();
      return workflowXML;
    } catch (Exception e) {
      throw new RepositoryException("Failed to serialize workflow: "
          + e.getMessage());
    }
  }

  /**
   * Parse a workflow
   * 
   * @param workflowID
   *          workflow id
   * @param workflowXML
   *          xml representation of the workflow including all tasks
   * @return a workflow
   * @throws RepositoryException
   */
  public Workflow parsePackagedWorkflow(String workflowID, String workflowXML)
      throws RepositoryException {
    try {
      File tmpfile = File.createTempFile("tempworkflow-", "-packaged");
      FileUtils.writeStringToFile(tmpfile, workflowXML);
      PackagedWorkflowRepository tmprepo = new PackagedWorkflowRepository(
          Collections.singletonList(tmpfile));
      tmpfile.delete();
      return tmprepo.getWorkflowById(workflowID);
    } catch (Exception e) {
      throw new RepositoryException("Failed to parse workflow xml: "
          + e.getMessage());
    }
  }

  // Private helper functions

  private void loadTasksToRepo(Workflow workflow) throws RepositoryException {
    for (WorkflowTask task : workflow.getTasks()) {
      if (this.repo.getTaskById(task.getTaskId()) == null)
        this.repo.addTask(task);
    }
  }

  private void saveWorkflow(String workflowId, String filePath)
      throws RepositoryException {
    List<ParentChildWorkflow> pcwlist = new ArrayList<ParentChildWorkflow>();
    // Check if the workflow exists
    ParentChildWorkflow pcw = (ParentChildWorkflow) repo
        .getWorkflowById(workflowId);
    if (pcw == null) {
      // Else check if this workflow Id is found in the event map
      // - It would be here if the top task is a parallel task
      pcwlist = repo.getWorkflowsForEvent(workflowId);
    }

    if (pcw != null)
      pcwlist.add(pcw);

    if (pcwlist.isEmpty())
      throw new RepositoryException("Cannot find " + workflowId
          + " in the repository");

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      String casns = "http://oodt.jpl.nasa.gov/2.0/cas";
      Element rootElem = document.createElement("cas:workflows");
      rootElem.setAttribute("xmlns:cas", casns);
      rootElem.setAttribute("xmlns", casns);
      document.appendChild(rootElem);

      // Create the workflow itself
      if (pcwlist.size() == 1) {
        Element elem = this.addGraphToDocument(document, pcwlist.get(0)
            .getGraph());
        if (elem != null)
          rootElem.appendChild(elem);
      } else {
        Element parallelElem = document.createElement("parallel");
        parallelElem.setAttribute("id", workflowId);
        for (ParentChildWorkflow cpcw : pcwlist) {
          Element elem = this.addGraphToDocument(document, cpcw.getGraph());
          if (elem != null)
            parallelElem.appendChild(elem);
        }
        rootElem.appendChild(parallelElem);
      }

      for (Object obj : repo.getTasksByWorkflowId(workflowId)) {
        WorkflowTask task = (WorkflowTask) obj;
        Element elem = this.createTaskElement(document, task);
        if (elem != null)
          rootElem.appendChild(elem);
      }
      XMLUtils.writeXmlToStream(document, new FileOutputStream(new File(
          filePath)));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryException("Could not save workflow. "
          + e.getMessage());
    }
  }

  private Element addGraphToDocument(Document document, Graph g) {
    String exetype = g.getExecutionType();
    Element elem = null;
    if ("parallel".equals(exetype) || "sequential".equals(exetype)) {
      elem = document.createElement(exetype);
      if (g.getModelId() != null && !g.getModelId().equals(""))
        elem.setAttribute("id", g.getModelId());
      if (g.getModelName() != null && !g.getModelName().equals(""))
        elem.setAttribute("name", g.getModelName());
    } else if ("task".equals(exetype) || "condition".equals(exetype)) {
      elem = document.createElement(exetype);
      if (g.getModelIdRef() != null && !g.getModelIdRef().equals("")) {
        elem.setAttribute("id-ref", g.getModelIdRef());
      }
    }

    if (elem != null) {
      Element condElem = null;
      for (Graph cg : g.getChildren()) {
        Element celem = addGraphToDocument(document, cg);
        if (celem != null) {
          // Wrap condition child elements inside "conditions" tag
          if ("condition".equals(cg.getExecutionType())) {
            if (condElem == null) {
              condElem = document.createElement("conditions");
              elem.appendChild(condElem);
            }
            condElem.appendChild(celem);
          } else {
            elem.appendChild(celem);
          }
        }
      }
    }
    return elem;
  }

  private Element createTaskElement(Document document, WorkflowTask task) {
    Element taskElem = document.createElement("task");
    taskElem.setAttribute("id", task.getTaskId());
    taskElem.setAttribute("name", task.getTaskName());
    taskElem.setAttribute("class", task.getTaskInstanceClassName());

    if (task.getConditions() != null && task.getConditions().size() > 0) {
      Element conditionsElem = document.createElement("conditions");
      for (Object obj : task.getConditions()) {
        WorkflowCondition cond = (WorkflowCondition) obj;
        Element condElem = document.createElement("condition");
        condElem.setAttribute("id", cond.getConditionId());
        conditionsElem.appendChild(condElem);
      }

      taskElem.appendChild(conditionsElem);
    }

    if (task.getRequiredMetFields() != null
        && task.getRequiredMetFields().size() > 0) {
      Element reqMetFieldsElem = document.createElement("requiredMetFields");
      for (Object obj : task.getRequiredMetFields()) {
        String metField = (String) obj;
        Element reqMetFieldElem = document.createElement("metfield");
        reqMetFieldElem.setAttribute("name", metField);
        reqMetFieldsElem.appendChild(reqMetFieldElem);
      }
      taskElem.appendChild(reqMetFieldsElem);
    }

    WorkflowTaskConfiguration config = task.getTaskConfig();
    if (config != null && config.getProperties().keySet().size() > 0) {
      Element taskConfigElem = document.createElement("configuration");
      for (Object obj : config.getProperties().keySet()) {
        String propName = (String) obj;
        String propVal = config.getProperty(propName);
        Element configPropElem = document.createElement("property");
        configPropElem.setAttribute("name", propName);
        configPropElem.setAttribute("value", propVal);
        taskConfigElem.appendChild(configPropElem);
      }
      taskElem.appendChild(taskConfigElem);
    }
    return taskElem;
  }
}

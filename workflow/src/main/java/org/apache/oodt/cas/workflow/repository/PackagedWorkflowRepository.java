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

//OODT imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.CasMetadataException;
import org.apache.oodt.cas.workflow.examples.BranchRedirector;
import org.apache.oodt.cas.workflow.examples.NoOpTask;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.apache.oodt.cas.workflow.structs.Graph;
import org.apache.oodt.cas.workflow.structs.ParentChildWorkflow;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.XmlStructFactory;
import org.apache.oodt.commons.exceptions.CommonsException;
import org.apache.oodt.commons.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//JDK imports

/**
 * 
 * 
 * Loads Workflow2 (WEngine) Style Workflow XML files.
 * 
 * @author mattmann
 * @author bfoster
 */
public class PackagedWorkflowRepository implements WorkflowRepository {

  private List<File> files;

  private Map<String, ParentChildWorkflow> workflows;

  private Map<String, WorkflowCondition> conditions;

  private Map<String, WorkflowTask> tasks;

  private Map<String, Metadata> globalConfGroups;

  private Map<String, List<ParentChildWorkflow>> eventWorkflowMap;

  private static final Logger LOG = Logger
      .getLogger(PackagedWorkflowRepository.class.getName());

  public PackagedWorkflowRepository(List<File> files)
      throws InstantiationException {
    this.files = files;
    try {
      this.init();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new InstantiationException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowByName
   * (java.lang.String)
   */
  @Override
  public Workflow getWorkflowByName(String workflowName)
      throws RepositoryException {

    for (Workflow w : this.workflows.values()) {
      if (w.getName().equals(workflowName)) {
        return w;
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowById
   * (java.lang.String)
   */
  @Override
  public Workflow getWorkflowById(String workflowId) throws RepositoryException {
    return workflows.get(workflowId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflows()
   */
  @Override
  public List getWorkflows() throws RepositoryException {
    List<Workflow> workflows = new Vector<Workflow>();
    workflows.addAll(this.workflows.values());
    return workflows;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTasksByWorkflowId
   * (java.lang.String)
   */
  @Override
  public List getTasksByWorkflowId(String workflowId)
      throws RepositoryException {
    Workflow w = this.getWorkflowById(workflowId);
    return w.getTasks();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getTasksByWorkflowName(java.lang.String)
   */
  @Override
  public List getTasksByWorkflowName(String workflowName)
      throws RepositoryException {
    Workflow w = this.getWorkflowByName(workflowName);
    if (w != null) {
      return w.getTasks();
    } else {
      return Collections.emptyList();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowsForEvent
   * (java.lang.String)
   */
  @Override
  public List getWorkflowsForEvent(String eventName) throws RepositoryException {
    List<ParentChildWorkflow> workflows = this.eventWorkflowMap.get(eventName);
    if (workflows != null && workflows.size() > 0) {
      return workflows;
    } else {
      return Collections.emptyList();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConditionsByTaskName(java.lang.String)
   */
  @Override
  public List getConditionsByTaskName(String taskName)
      throws RepositoryException {

    for (WorkflowTask task : this.tasks.values()) {
      if (task.getTaskName().equals(taskName)) {
        return task.getConditions();
      }
    }

    return Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConditionsByTaskId(java.lang.String)
   */
  @Override
  public List getConditionsByTaskId(String taskId) throws RepositoryException {
    if (this.tasks.get(taskId) != null) {
      return this.tasks.get(taskId).getConditions();
    } else {
      return Collections.emptyList();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getConfigurationByTaskId(java.lang.String)
   */
  @Override
  public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
      throws RepositoryException {
    return convertToTaskConfiguration(this.globalConfGroups.get(taskId));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowTaskById
   * (java.lang.String)
   */
  @Override
  public WorkflowTask getWorkflowTaskById(String taskId)
      throws RepositoryException {
    return this.tasks.get(taskId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#
   * getWorkflowConditionById(java.lang.String)
   */
  @Override
  public WorkflowCondition getWorkflowConditionById(String conditionId)
      throws RepositoryException {
    return this.conditions.get(conditionId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.repository.WorkflowRepository#getRegisteredEvents
   * ()
   */
  @Override
  public List getRegisteredEvents() throws RepositoryException {
    return Arrays.asList(this.eventWorkflowMap.keySet().toArray());
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

    for (WorkflowTask task : (List<WorkflowTask>) workflow.getTasks()) {
      if (!this.tasks.containsKey(task.getTaskId())) {
        throw new RepositoryException("Reference in new workflow: ["
            + workflow.getName() + "] to undefined task with id: ["
            + task.getTaskId() + "]");
      }

      // check its conditions
      if (task.getConditions() != null && task.getConditions().size() > 0) {
        for (WorkflowCondition cond : (List<WorkflowCondition>) task
            .getConditions()) {
          if (!this.conditions.containsKey(cond.getConditionId())) {
            throw new RepositoryException("Reference in new workflow: ["
                + workflow.getName() + "] to undefined condition ith id: ["
                + cond.getConditionId() + "]");
          }
        }
      }
    }

    // recast it as a parent/child workflow
    String workflowId = workflow.getId();
	if (workflowId == null || (workflowId.equals(""))) {
		// generate its ID
		workflowId = UUID.randomUUID().toString();
		workflow.setId(workflowId);
	}
      
    ParentChildWorkflow pcw;
    if(workflow instanceof ParentChildWorkflow) {
        pcw = (ParentChildWorkflow) workflow;
    }
    else {
        Graph graph = new Graph();
        graph.setExecutionType("sequential");
        pcw = new ParentChildWorkflow(graph);
        pcw.setName(workflow.getName());
        pcw.setTasks(workflow.getTasks());
        pcw.setId(workflow.getId());
    }
    this.workflows.put(pcw.getId(), pcw);
    this.eventWorkflowMap.put(workflowId, Collections.singletonList(pcw));

    // generate its ID
    return workflowId;
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
    if (!this.workflows.containsKey(workflowId)) {
      throw new RepositoryException(
          "Attempt to obtain conditions for a workflow: " + "[" + workflowId
          + "] that does not exist!");
    }

    return this.workflows.get(workflowId).getConditions();
  }
  

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTaskById(java.lang.String)
   */
  @Override
  public WorkflowTask getTaskById(String taskId) throws RepositoryException {
    return this.tasks.get(taskId);
  }  

  private void init() throws RepositoryException {
    this.workflows = new ConcurrentHashMap<String, ParentChildWorkflow>();
    this.tasks = new ConcurrentHashMap<String, WorkflowTask>();
    this.conditions = new ConcurrentHashMap<String, WorkflowCondition>();
    this.eventWorkflowMap = new ConcurrentHashMap<String, List<ParentChildWorkflow>>();
    this.globalConfGroups = new ConcurrentHashMap<String, Metadata>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser;

    try {
      parser = factory.newDocumentBuilder();
      List<Element> rootElements = new Vector<Element>();
      for (File file : files) {
        rootElements.add(parser.parse(file).getDocumentElement());
      }
      for (Element root : rootElements) {
        Metadata staticMetadata = new Metadata();
        loadConfiguration(rootElements, root, staticMetadata);
        loadTaskAndConditionDefinitions(rootElements, root, staticMetadata);
        loadGraphs(rootElements, root, new Graph(), staticMetadata);
        computeEvents();
        computeWorkflowConditions();
      }
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(e.getMessage());
    }
  }

  private void computeWorkflowConditions() {
    if (this.workflows != null && this.workflows.values().size() > 0) {
      for (ParentChildWorkflow w : this.workflows.values()) {
        if (w.getConditions() != null && w.getConditions().size() > 0) {
          w.getTasks().add(0,
              getGlobalWorkflowConditionsTask(w.getName(), w.getId(), w.getConditions()));
        }
      }
    }
  }

  private void computeEvents() throws WorkflowException {
    List<ParentChildWorkflow> workflows = new Vector<ParentChildWorkflow>();
    for (ParentChildWorkflow w : this.workflows.values()) {
      workflows.add(w);

    }
    for (ParentChildWorkflow workflow : workflows) {

      // event for each workflow id
      List<ParentChildWorkflow> wList = new Vector<ParentChildWorkflow>();
      wList.add(workflow);
      this.eventWorkflowMap.put(workflow.getId(), wList);

      // clear its tasks, we are going to re-add them back
      workflow.getTasks().clear();
      List<Graph> children = workflow.getGraph().getChildren();
      if (workflow.getGraph().getExecutionType().equals("sequential")) {
        for (Graph child : children) {
          if (child.getWorkflow() != null) {
            workflow.getTasks().add(
                generateRedirector(child.getWorkflow().getId()));
          } else if (child.getTask() != null) {
            workflow.getTasks().add(child.getTask());
          }
        }
      } else if (workflow.getGraph().getExecutionType().equals("parallel")) {
        // clear it as a workflow from the list
        // to begin with
        this.eventWorkflowMap.get(workflow.getId()).clear();
        this.workflows.remove(workflow.getId());
        for (Graph child : children) {
          if (child.getWorkflow() != null) {
            // add child workflow to the event kickoff for this id
            this.eventWorkflowMap.get(workflow.getId())
                .add(child.getWorkflow());
          } else if (child.getTask() != null) {
            // add a new dynamic workflow
            // with just this task
            ParentChildWorkflow w = getDynamicWorkflow(child.getTask());
            this.eventWorkflowMap.get(workflow.getId()).add(w);
          }
        }
      } else {
        throw new WorkflowException("Unsupported execution type: ["
                                    + workflow.getGraph().getExecutionType() + "]");
      }
    }
  }

  private void loadTaskAndConditionDefinitions(List<Element> rootElements,
      Element rootElem, Metadata staticMetadata)
      throws CommonsException, CasMetadataException, WorkflowException, ParseException {

    List<Element> conditionBlocks = this.getChildrenByTagName(rootElem,
        "condition");
    List<Element> taskBlocks = this.getChildrenByTagName(rootElem, "task");

    if (conditionBlocks != null && conditionBlocks.size() > 0) {
      LOG.log(Level.FINER, "Loading: [" + conditionBlocks.size()
          + "] conditions from: ["
          + rootElem.getOwnerDocument().getDocumentURI() + "]");

      for (Element condElem : conditionBlocks) {
        loadGraphs(rootElements, condElem, new Graph(), staticMetadata);
      }

    }

    if (taskBlocks != null && taskBlocks.size() > 0) {
      LOG.log(Level.FINER, "Loading: [" + taskBlocks.size() + "] tasks from: ["
          + rootElem.getOwnerDocument().getDocumentURI() + "]");
      for (Element taskElem : taskBlocks) {
        loadGraphs(rootElements, taskElem, new Graph(), staticMetadata);
      }
    }
  }

  private void loadGraphs(List<Element> rootElements, Element graphElem,
      Graph parent, Metadata staticMetadata)
      throws CommonsException, CasMetadataException, WorkflowException, ParseException {

    LOG.log(Level.FINEST, "Visiting node: [" + graphElem.getNodeName() + "]");
    loadConfiguration(rootElements, graphElem, staticMetadata);
    Graph graph = !graphElem.getNodeName().equals("cas:workflows") ? new Graph(
        graphElem, staticMetadata) : new Graph();
    parent.getChildren().add(graph);
    graph.setParent(parent);
    if (!graphElem.getNodeName().equals("cas:workflows")) {
      expandWorkflowTasksAndConditions(graph, staticMetadata);
    }

    for (String processorType : Graph.processorIds) {
      LOG.log(Level.FINE, "Scanning for: [" + processorType + "] nodes");
      List<Element> procTypeBlocks = this.getChildrenByTagName(graphElem,
          processorType);
      if (procTypeBlocks != null && procTypeBlocks.size() > 0) {
        LOG.log(Level.FINE, "Found: [" + procTypeBlocks.size() + "] ["
            + processorType + "] processor types");
        for (Element procTypeBlock : procTypeBlocks) {
          loadGraphs(rootElements, procTypeBlock, graph, staticMetadata);
        }
      } else {
        if (processorType.equals("condition")) {
          Element conditionsElem = XMLUtils.getFirstElement("conditions",
              graphElem);
          if (conditionsElem != null) {
            List<Element> procTypeBlockNodes = this.getChildrenByTagName(
                conditionsElem, "condition");
            if (procTypeBlockNodes != null && procTypeBlockNodes.size() > 0) {
              LOG.log(Level.FINE, "Found: [" + procTypeBlockNodes.size()
                  + "] linked condition definitions");
              for (Element procTypeBlockNode : procTypeBlockNodes) {
                loadGraphs(rootElements, procTypeBlockNode, graph,
                    staticMetadata);
              }
            }
          }
        }
      }
    }


  }

  private void loadConfiguration(List<Element> rootElements, Node workflowNode,
      Metadata staticMetadata) throws ParseException, CommonsException, CasMetadataException, WorkflowException {
    NodeList children = workflowNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curChild = children.item(i);

      if (curChild.getNodeName().equals("configuration")) {
        Metadata curMetadata = new Metadata();
        if (!((Element) curChild).getAttribute("extends").equals("")) {
          for (String extension : ((Element) curChild).getAttribute("extends")
                                                      .split(",")) {
            curMetadata
                .replaceMetadata(globalConfGroups.containsKey(extension) ? globalConfGroups
                    .get(extension) : this.loadConfGroup(rootElements,
                    extension, globalConfGroups));
          }
        }
        curMetadata.replaceMetadata(XmlStructFactory
            .getConfigurationAsMetadata(curChild));
        NamedNodeMap attrMap = curChild.getAttributes();
        String configName = null;
        for (int j = 0; j < attrMap.getLength(); j++) {
          Attr attr = (Attr) attrMap.item(j);
          if (attr.getName().equals("name")) {
            configName = attr.getValue();
          }
        }

        if (configName == null || (configName.equals(""))) {
          NamedNodeMap workflowNodeAttrs = workflowNode.getAttributes();
          for (int j = 0; j < workflowNodeAttrs.getLength(); j++) {
            Attr attr = (Attr) workflowNodeAttrs.item(j);
            if (attr.getName().equals("id")) {
              configName = attr.getValue();
            }
          }
        }

        this.globalConfGroups.put(configName, curMetadata);
        staticMetadata.replaceMetadata(curMetadata);
      }
    }
  }

  private Metadata loadConfGroup(List<Element> rootElements, String group,
      Map<String, Metadata> globalConfGroups)
      throws ParseException, CommonsException, CasMetadataException, WorkflowException {
    for (final Element rootElement : rootElements) {
      NodeList nodes = rootElement.getElementsByTagName("configuration");
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String name = ((Element) node).getAttribute("name");
        if (name.equals(group)) {
          return XmlStructFactory.getConfigurationAsMetadata(node);
        }
      }
    }
    throw new WorkflowException("Configuration group '" + group + "' not defined!");
  }

  private void expandWorkflowTasksAndConditions(Graph graph,
      Metadata staticMetadata) {
    if (graph.getExecutionType().equals("workflow")
        || graph.getExecutionType().equals("sequential")
        || graph.getExecutionType().equals("parallel")) {
      ParentChildWorkflow workflow = new ParentChildWorkflow(graph);
      workflow.setId(graph.getModelId());
      workflow.setName(graph.getModelName());
      graph.setWorkflow(workflow);
      if (graph.getParent() == null || (graph.getParent().getWorkflow() == null)) {
        LOG.log(Level.FINEST, "Workflow: [" + graph.getModelId()
            + "] has no parent: it's a top-level workflow");
      }

      if (workflow.getName() == null || (workflow.getName().equals(""))) {
        workflow.setName(graph.getExecutionType() + "-" + workflow.getId());
      }
      this.workflows.put(graph.getModelId(), workflow);
    } else if (graph.getExecutionType().equals("condition")) {
      WorkflowCondition cond;

      if (graph.getModelIdRef() != null && !graph.getModelIdRef().equals("")) {
        cond = this.conditions.get(graph.getModelIdRef());
      } else {
        cond = new WorkflowCondition();
        cond.setConditionId(graph.getModelId());
        cond.setConditionName(graph.getModelName());
        cond.setConditionInstanceClassName(graph.getClazz());
        cond.setTimeoutSeconds(graph.getTimeout());
        cond.setOptional(graph.isOptional());
        cond.setCondConfig(convertToConditionConfiguration(staticMetadata));

        if (cond.getConditionName() == null || (cond.getConditionName()
                                                    .equals(""))) {
          cond.setConditionName(cond.getConditionId());
        }
        this.conditions.put(graph.getModelId(), cond);

      }

      graph.setCond(cond);
      if (graph.getParent() != null) {
        if (graph.getParent().getWorkflow() != null) {
          LOG.log(Level.FINEST, "Adding condition: [" + cond.getConditionName()
              + "] to parent workflow: ["
              + graph.getParent().getWorkflow().getName() + "]");
          graph.getParent().getWorkflow().getConditions().add(cond);
        } else if (graph.getParent().getTask() != null) {
          graph.getParent().getTask().getConditions().add(cond);
        } else {
          LOG.log(Level.FINEST, "Condition: [" + graph.getModelId()
              + "] has not parent: it's a condition definition");
        }
      } else {
        LOG.log(Level.FINEST, "Condition: [" + graph.getModelId()
            + "]: parent is null");
      }
      // if parent doesn't have task or workflow set, then its parent
      // is null and it's a condition definition, just add it

    } else if (graph.getExecutionType().equals("task")) {
      WorkflowTask task;
      if (graph.getModelIdRef() != null && !graph.getModelIdRef().equals("")) {
        LOG.log(Level.FINER, "Model ID-Ref to: [" + graph.getModelIdRef() + "]");
        task = this.tasks.get(graph.getModelIdRef());
      } else {
        task = new WorkflowTask();
        task.setTaskId(graph.getModelId());
        task.setTaskName(graph.getModelName());
        task.setTaskConfig(convertToTaskConfiguration(staticMetadata));
        task.setTaskInstanceClassName(graph.getClazz());

        if (task.getTaskName() == null || (task.getTaskName().equals(""))) {
          task.setTaskName(task.getTaskId());
        }
        this.tasks.put(graph.getModelId(), task);
      }

      graph.setTask(task);
      if (graph.getParent() != null) {
        if (graph.getParent().getWorkflow() != null) {
          graph.getParent().getWorkflow().getTasks().add(task);
        } else {
          LOG.log(Level.FINEST, "Task: [" + graph.getModelId()
              + "] has no parent: it's a task definition");
        }
      } else {
        LOG.log(Level.FINEST, "Task: [" + graph.getModelId()
            + "]: parent is null");
      }
    }

  }

  private ParentChildWorkflow getDynamicWorkflow(WorkflowTask task) {
    Graph graph = new Graph();
    graph.setExecutionType("sequential");
    ParentChildWorkflow workflow = new ParentChildWorkflow(graph);
    workflow.setId("parallel-" + UUID.randomUUID().toString());
    workflow.setName("Parallel Single Task " + task.getTaskName());
    workflow.getTasks().add(task);
    this.workflows.put(workflow.getId(), workflow);
    return workflow;
  }

  private WorkflowTask generateRedirector(String eventName) {
    WorkflowTask task = new WorkflowTask();
    WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
    config.addConfigProperty("eventName", eventName);
    task.setTaskId("redirector-" + UUID.randomUUID().toString());
    task.setTaskName("Redirector Task");
    task.setTaskInstanceClassName(BranchRedirector.class.getName());
    this.tasks.put(task.getTaskId(), task);
    return task;
  }

  private WorkflowTaskConfiguration convertToTaskConfiguration(Metadata met) {
    WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
    for (String key : met.getAllKeys()) {
      config.addConfigProperty(key, met.getMetadata(key));
    }
    return config;
  }

  private WorkflowConditionConfiguration convertToConditionConfiguration(
      Metadata met) {
    WorkflowConditionConfiguration config = new WorkflowConditionConfiguration();
    for (String key : met.getAllKeys()) {
      config.addConfigProperty(key, met.getMetadata(key));
    }
    return config;
  }

  /**
   * Taken from: http://stackoverflow.com/questions/1241525/java-element-
   * getelementsbytagname-restrict-to-top-level
   */
  private List<Element> getChildrenByTagName(Element parent, String name) {
    List<Element> nodeList = new Vector<Element>();
    for (Node child = parent.getFirstChild(); child != null; child = child
        .getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE
          && name.equals(child.getNodeName())) {
        nodeList.add((Element) child);
      }
    }

    return nodeList;
  }

  private WorkflowTask getGlobalWorkflowConditionsTask(String workflowName, String workflowId,
      List<WorkflowCondition> conditions) {
    WorkflowTask task = new WorkflowTask();
    task.setConditions(conditions);
    task.setTaskConfig(new WorkflowTaskConfiguration());
    task.setTaskId(workflowId + "-global-conditions-eval");
    task.setTaskName(workflowName + "-global-conditions-eval");
    task.setTaskInstanceClassName(NoOpTask.class.getName());
    this.tasks.put(task.getTaskId(), task);
    return task;
  }
  
}

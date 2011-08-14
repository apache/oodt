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
import org.apache.oodt.cas.workflow.metadata.CoreMetKeys;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.cas.workflow.system.XmlRpcWorkflowManagerClient;
import org.apache.oodt.cas.workflow.util.XmlStructFactory;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

  private List<String> processorIds = Arrays.asList(new String[] {
      "sequential", "parallel", "condition", "task" });

  private Map<String, ParentChildWorkflow> workflows;

  private List<Graph> graphs;

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
      e.printStackTrace();
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
    } else
      return Collections.emptyList();
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
    } else
      return Collections.emptyList();
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
    } else
      return Collections.emptyList();
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
    if (workflow.getTasks() == null
        || (workflow.getTasks() != null && workflow.getTasks().size() == 0)) {
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
    String workflowId = UUID.randomUUID().toString();
    workflow.setId(workflowId);

    Graph graph = new Graph();
    graph.setExecutionType("sequential");
    ParentChildWorkflow pcw = new ParentChildWorkflow(graph);
    pcw.setName(workflow.getName());
    pcw.setTasks(workflow.getTasks());
    pcw.setId(workflow.getId());
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
    if (!this.workflows.containsKey(workflowId))
      throw new RepositoryException(
          "Attempt to obtain conditions for a workflow: " + "[" + workflowId
              + "] that does not exist!");

    return this.workflows.get(workflowId).getConditions();
  }

  private void init() throws RepositoryException {
    this.workflows = new HashMap<String, ParentChildWorkflow>();
    this.tasks = new HashMap<String, WorkflowTask>();
    this.conditions = new HashMap<String, WorkflowCondition>();
    this.eventWorkflowMap = new HashMap<String, List<ParentChildWorkflow>>();
    this.globalConfGroups = new HashMap<String, Metadata>();
    this.graphs = new Vector<Graph>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = null;

    try {
      parser = factory.newDocumentBuilder();
      List<Element> rootElements = new Vector<Element>();
      for (File file : files)
        rootElements.add(parser.parse(file).getDocumentElement());
      for (Element root : rootElements) {
        Metadata staticMetadata = new Metadata();
        loadConfiguration(rootElements, root, staticMetadata);
        loadTaskAndConditionDefinitions(rootElements, root, staticMetadata);
        loadGraphs(rootElements, root, new Graph(), staticMetadata);
        computeEvents();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RepositoryException(e.getMessage());
    }
  }

  private void computeEvents() throws Exception {
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
      } else
        throw new Exception("Unsupported execution type: ["
            + workflow.getGraph().getExecutionType() + "]");
    }
  }

  private void loadTaskAndConditionDefinitions(List<Element> rootElements,
      Element rootElem, Metadata staticMetadata) throws Exception {

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
      Graph parent, Metadata staticMetadata) throws Exception {

    LOG.log(Level.FINEST, "Visiting node: [" + graphElem.getNodeName() + "]");
    loadConfiguration(rootElements, graphElem, staticMetadata);
    Graph graph = !graphElem.getNodeName().equals("cas:workflows") ? new Graph(
        graphElem, staticMetadata) : new Graph();
    parent.getChildren().add(graph);
    graph.setParent(parent);
    if (!graphElem.getNodeName().equals("cas:workflows")) {
      expandWorkflowTasksAndConditions(graph, staticMetadata);
    }

    for (String processorType : this.processorIds) {
      LOG.log(Level.FINE, "Scanning for: [" + processorType + "] nodes");
      List<Element> procTypeBlocks = this.getChildrenByTagName(graphElem,
          processorType);
      if (procTypeBlocks != null && procTypeBlocks.size() > 0) {
        LOG.log(Level.FINE, "Found: [" + procTypeBlocks.size() + "] ["
            + processorType + "] processor types");
        for (int i = 0; i < procTypeBlocks.size(); i++) {
          loadGraphs(rootElements, procTypeBlocks.get(i), graph, staticMetadata);
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
              for (int i = 0; i < procTypeBlockNodes.size(); i++) {
                loadGraphs(rootElements, procTypeBlockNodes.get(i), graph,
                    staticMetadata);
              }
            }
          }
        }
      }
    }

    if (graphElem.getNodeName().equals("cas:workflows"))
      return;
  }

  private void loadConfiguration(List<Element> rootElements, Node workflowNode,
      Metadata staticMetadata) throws Exception {
    NodeList children = workflowNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curChild = children.item(i);

      if (curChild.getNodeName().equals("configuration")) {
        Metadata curMetadata = new Metadata();
        if (!((Element) curChild).getAttribute("extends").equals(""))
          for (String extension : ((Element) curChild).getAttribute("extends")
              .split(","))
            curMetadata
                .replaceMetadata(globalConfGroups.containsKey(extension) ? globalConfGroups
                    .get(extension) : this.loadConfGroup(rootElements,
                    extension, globalConfGroups));
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

        if (configName == null || (configName != null && configName.equals(""))) {
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
      Map<String, Metadata> globalConfGroups) throws Exception {
    for (final Element rootElement : rootElements) {
      NodeList nodes = rootElement.getElementsByTagName("configuration");
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String name = ((Element) node).getAttribute("name");
        if (name.equals(group))
          return XmlStructFactory.getConfigurationAsMetadata(node);
      }
    }
    throw new Exception("Configuration group '" + group + "' not defined!");
  }

  private void expandWorkflowTasksAndConditions(Graph graph,
      Metadata staticMetadata) throws Exception {
    if (graph.getExecutionType().equals("workflow")
        || graph.getExecutionType().equals("sequential")
        || graph.getExecutionType().equals("parallel")) {
      ParentChildWorkflow workflow = new ParentChildWorkflow(graph);
      workflow.setId(graph.getModelId());
      workflow.setName(graph.getModelName());
      graph.setWorkflow(workflow);
      if (graph.getParent() == null
          || (graph.getParent() != null && graph.getParent().getWorkflow() == null)) {
        LOG.log(Level.FINEST, "Workflow: [" + graph.getModelId()
            + "] has no parent: it's a top-level workflow");
      }

      if (workflow.getName() == null
          || (workflow.getName() != null && workflow.getName().equals(""))) {
        workflow.setName(graph.getExecutionType() + "-" + workflow.getId());
      }
      this.workflows.put(graph.getModelId(), workflow);
    } else if (graph.getExecutionType().equals("condition")) {
      WorkflowCondition cond = null;

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

        if (cond.getConditionName() == null
            || (cond.getConditionName() != null && cond.getConditionName()
                .equals(""))) {
          cond.setConditionName(cond.getConditionId());
        }
        this.conditions.put(graph.getModelId(), cond);

      }

      graph.setCond(cond);
      if (graph.getParent() != null) {
        if (graph.getParent().getWorkflow() != null) {
          System.out.println("Adding condition: [" + cond.getConditionName()
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
      WorkflowTask task = null;
      if (graph.getModelIdRef() != null && !graph.getModelIdRef().equals("")) {
        LOG.log(Level.FINER, "Model ID-Ref to: [" + graph.getModelIdRef() + "]");
        task = this.tasks.get(graph.getModelIdRef());
      } else {
        task = new WorkflowTask();
        task.setTaskId(graph.getModelId());
        task.setTaskName(graph.getModelName());
        task.setTaskConfig(convertToTaskConfiguration(staticMetadata));
        task.setTaskInstanceClassName(graph.getClazz());

        if (task.getTaskName() == null
            || (task.getTaskName() != null && task.getTaskName().equals(""))) {
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

  private boolean checkValue(String value) {
    return value != null && !value.equals("");
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

  private class Graph {

    private WorkflowTask task;

    private WorkflowCondition cond;

    private ParentChildWorkflow workflow;

    private String modelIdRef;

    private String modelId;

    private String modelName;

    private String alias;

    private String executionType;

    private String minReqSuccessfulSubProcessors;

    private List<String> excused;

    private String clazz;

    private long timeout;

    private boolean optional;

    private Graph parent;

    private List<Graph> children;

    public Graph(Element graphElem, Metadata staticMetadata) throws Exception {
      this();
      this.modelId = graphElem.getAttribute("id");
      this.modelName = graphElem.getAttribute("name");
      this.clazz = graphElem.getAttribute("class");
      this.modelIdRef = graphElem.getAttribute("id-ref");
      this.excused.addAll(Arrays.asList(graphElem.getAttribute("excused")
          .split(",")));
      this.alias = graphElem.getAttribute("alias");
      this.minReqSuccessfulSubProcessors = graphElem.getAttribute("min");
      this.executionType = graphElem.getAttribute("execution");
      this.timeout = Long.valueOf(graphElem.getAttribute("timeout") != null
          && !graphElem.getAttribute("timeout").equals("") ? graphElem
          .getAttribute("timeout") : "-1");
      this.optional = Boolean.valueOf(graphElem.getAttribute("optional"));

      NamedNodeMap attrMap = graphElem.getAttributes();
      for (int i = 0; i < attrMap.getLength(); i++) {
        Attr attr = (Attr) attrMap.item(i);
        if (attr.getName().startsWith("p:")) {
          staticMetadata.replaceMetadata(attr.getName().substring(2),
              attr.getValue());
        }
      }

      if ((graphElem.getNodeName().equals("workflow") || graphElem
          .getNodeName().equals("conditions")) && this.executionType == null) {
        throw new Exception("workflow model '" + graphElem.getNodeName()
            + "' missing execution type");
      } else {
        this.executionType = graphElem.getNodeName();
      }

      if (!processorIds.contains(this.executionType))
        throw new Exception("Unsupported execution type id '"
            + this.executionType + "'");

      if (!checkValue(this.modelId) && !checkValue(this.modelIdRef)) {
        this.modelId = UUID.randomUUID().toString();
      }

      if (this.alias != null && !this.alias.equals("")) {
        this.modelId = this.alias;
      }
    }

    public Graph() {
      this.task = null;
      this.cond = null;
      this.workflow = null;
      this.modelIdRef = null;
      this.modelId = null;
      this.modelName = null;
      this.alias = null;
      this.executionType = null;
      this.minReqSuccessfulSubProcessors = null;
      this.excused = new Vector<String>();
      this.clazz = null;
      this.children = new Vector<Graph>();
      this.parent = null;
      this.timeout = -1;
      this.optional = false;
    }

    /**
     * @return the parent
     */
    public Graph getParent() {
      return parent;
    }

    /**
     * @param parent
     *          the parent to set
     */
    public void setParent(Graph parent) {
      this.parent = parent;
    }

    /**
     * @return the children
     */
    public List<Graph> getChildren() {
      return children;
    }

    /**
     * @param children
     *          the children to set
     */
    public void setChildren(List<Graph> children) {
      this.children = children;
    }

    /**
     * @return the modelIdRef
     */
    public String getModelIdRef() {
      return modelIdRef;
    }

    /**
     * @param modelIdRef
     *          the modelIdRef to set
     */
    public void setModelIdRef(String modelIdRef) {
      this.modelIdRef = modelIdRef;
    }

    /**
     * @return the modelId
     */
    public String getModelId() {
      return modelId;
    }

    /**
     * @param modelId
     *          the modelId to set
     */
    public void setModelId(String modelId) {
      this.modelId = modelId;
    }

    /**
     * @return the modelName
     */
    public String getModelName() {
      return modelName;
    }

    /**
     * @param modelName
     *          the modelName to set
     */
    public void setModelName(String modelName) {
      this.modelName = modelName;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
      return alias;
    }

    /**
     * @param alias
     *          the alias to set
     */
    public void setAlias(String alias) {
      this.alias = alias;
    }

    /**
     * @return the executionType
     */
    public String getExecutionType() {
      return executionType;
    }

    /**
     * @param executionType
     *          the executionType to set
     */
    public void setExecutionType(String executionType) {
      this.executionType = executionType;
    }

    /**
     * @return the minReqSuccessfulSubProcessors
     */
    public String getMinReqSuccessfulSubProcessors() {
      return minReqSuccessfulSubProcessors;
    }

    /**
     * @param minReqSuccessfulSubProcessors
     *          the minReqSuccessfulSubProcessors to set
     */
    public void setMinReqSuccessfulSubProcessors(
        String minReqSuccessfulSubProcessors) {
      this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
    }

    /**
     * @return the excused
     */
    public List<String> getExcused() {
      return excused;
    }

    /**
     * @param excused
     *          the excused to set
     */
    public void setExcused(List<String> excused) {
      this.excused = excused;
    }

    /**
     * @return the clazz
     */
    public String getClazz() {
      return clazz;
    }

    /**
     * @param clazz
     *          the clazz to set
     */
    public void setClazz(String clazz) {
      this.clazz = clazz;
    }

    /**
     * @return the task
     */
    public WorkflowTask getTask() {
      return task;
    }

    /**
     * @param task
     *          the task to set
     */
    public void setTask(WorkflowTask task) {
      this.task = task;
    }

    /**
     * @return the cond
     */
    public WorkflowCondition getCond() {
      return cond;
    }

    /**
     * @param cond
     *          the cond to set
     */
    public void setCond(WorkflowCondition cond) {
      this.cond = cond;
    }

    /**
     * @return the workflow
     */
    public ParentChildWorkflow getWorkflow() {
      return workflow;
    }

    /**
     * @param workflow
     *          the workflow to set
     */
    public void setWorkflow(ParentChildWorkflow workflow) {
      this.workflow = workflow;
    }

    public String toString() {
      return this.modelId;
    }

    /**
     * @return the timeout
     */
    public long getTimeout() {
      return timeout;
    }

    /**
     * @param timeout
     *          the timeout to set
     */
    public void setTimeout(long timeout) {
      this.timeout = timeout;
    }

    /**
     * @return the optional
     */
    public boolean isOptional() {
      return optional;
    }

    /**
     * @param optional
     *          the optional to set
     */
    public void setOptional(boolean optional) {
      this.optional = optional;
    }

  }

  private class ParentChildWorkflow extends Workflow {

    private Graph graph;

    public ParentChildWorkflow(Graph graph) {
      this.graph = graph;
    }

    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer("[workflow id=");
      buf.append(this.getId());
      buf.append(",name=");
      buf.append(this.getName());
      buf.append(",parent=");
      buf.append(this.graph.parent != null ? this.graph.parent.modelId : null);
      buf.append(",children=");
      buf.append(this.graph.children);
      buf.append(",executionType=");
      buf.append(this.graph.executionType);
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

  private class BranchRedirector implements WorkflowTaskInstance {

    public BranchRedirector() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(org.apache
     * .oodt.cas.metadata.Metadata,
     * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
     */
    @Override
    public void run(Metadata metadata, WorkflowTaskConfiguration config)
        throws WorkflowTaskInstanceException {
      XmlRpcWorkflowManagerClient wm = null;

      try {
        wm = new XmlRpcWorkflowManagerClient(new URL(
            metadata.getMetadata(CoreMetKeys.WORKFLOW_MANAGER_URL)));
        wm.sendEvent(config.getProperty("eventName"), metadata);
      } catch (Exception e) {
        throw new WorkflowTaskInstanceException(e.getMessage());
      }
    }

  }

}

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
package org.apache.oodt.cas.workflow.system;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.ThreadPoolWorkflowEngineFactory;
import org.apache.oodt.cas.workflow.engine.WorkflowEngine;
import org.apache.oodt.cas.workflow.repository.DataSourceWorkflowRepositoryFactory;
import org.apache.oodt.cas.workflow.repository.WorkflowRepository;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.XmlRpcStructFactory;
import org.apache.oodt.config.Component;
import org.apache.oodt.config.ConfigurationManager;
import org.apache.oodt.config.ConfigurationManagerFactory;
import org.apache.xmlrpc.WebServer;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory.getWorkflowEngineFromClassName;
import static org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory.getWorkflowRepositoryFromClassName;

/**
 * An XML RPC-based Workflow manager.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 * @deprecated replaced by avro-rpc
 */
@Deprecated
public class XmlRpcWorkflowManager implements WorkflowManager {

  private static final Logger LOG = Logger.getLogger(XmlRpcWorkflowManager.class.getName());

  public static final int DEFAULT_WEB_SERVER_PORT = 9001;
  public static final String XML_RPC_HANDLER_NAME = "workflowmgr";

  public static final String PROPERTIES_FILE_PROPERTY = "org.apache.oodt.cas.workflow.properties";
  public static final String WORKFLOW_ENGINE_FACTORY_PROPERTY = "workflow.engine.factory";
  public static final String ENGINE_RUNNER_FACTORY_PROPERTY = "workflow.engine.runner.factory";
  public static final String WORKFLOW_REPOSITORY_FACTORY_PROPERTY = "workflow.repo.factory";

  private WebServer webServer;
  private final WorkflowEngine engine;
  private WorkflowRepository repo;

  private ConfigurationManager configurationManager;

  public XmlRpcWorkflowManager() {
    this(DEFAULT_WEB_SERVER_PORT);
  }

  public XmlRpcWorkflowManager(int port) {
    Preconditions.checkArgument(port > 0, "Must specify a port greater than 0");

    List<String> propertiesFiles = new ArrayList<>();
    String configFile = System.getProperty(PROPERTIES_FILE_PROPERTY);
    if (configFile != null) {
      propertiesFiles.add(configFile);
    }

    configurationManager= ConfigurationManagerFactory.getConfigurationManager(Component.WORKFLOW_MANAGER,propertiesFiles);
    try {
      configurationManager.loadConfiguration();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unable to load configuration", e);
      throw new IllegalStateException("Unable to load configuration", e);
    }

    engine = getWorkflowEngineFromProperty();
    engine.setWorkflowManagerUrl(safeGetUrlFromString("http://"
                                                      + getHostname() + ":" + port));
    repo = getWorkflowRepositoryFromProperty();

    // start up the web server
    webServer = new WebServer(port);
    webServer.addHandler(XML_RPC_HANDLER_NAME, this);
    webServer.start();

    LOG.log(Level.INFO, "Workflow Manager started by "
                        + System.getProperty("user.name", "unknown"));
  }

  public boolean shutdown() {
    configurationManager.clearConfiguration();

    if (webServer != null) {
      webServer.shutdown();
      webServer = null;
      return true;
    } else {
      return false;
    }
  }

  public boolean refreshRepository() {
    repo = getWorkflowRepositoryFromProperty();
    return true;
  }

  public String executeDynamicWorkflow(Vector<String> taskIds, Hashtable metadata)
      throws RepositoryException, EngineException {
    return this.executeDynamicWorkflowCore(taskIds, metadata);
  }

  public String executeDynamicWorkflowCore(Vector<String> taskIds, Map metadata)
      throws RepositoryException, EngineException {
    if (taskIds == null || (taskIds.size() == 0)) {
      throw new RepositoryException(
          "Must specify task identifiers to build dynamic workflows!");
    }

    Workflow dynamicWorkflow = new Workflow();

    for (String taskId : taskIds) {
      WorkflowTask task = this.repo.getWorkflowTaskById(taskId);
      if (task == null) {
        throw new RepositoryException("Dynamic workflow task: [" + taskId
                                      + "] is not defined!");
      }
      dynamicWorkflow.getTasks().add(task);
    }

    dynamicWorkflow.setId(this.repo.addWorkflow(dynamicWorkflow));
    dynamicWorkflow.setName("Dynamic Workflow-" + dynamicWorkflow.getId());

    Metadata met = new Metadata();
    met.addMetadata(metadata);

    WorkflowInstance inst = this.engine.startWorkflow(dynamicWorkflow, met);
    return inst.getId();
  }

  public List getRegisteredEvents() throws RepositoryException {

    List events;
    Vector eventsVector = new Vector();

    try {
      events = repo.getRegisteredEvents();

      if (events != null) {
        for (Object event : events) {
          eventsVector.add(event);
        }

      }

      return eventsVector;

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting registered events from repository: Message: "
          + e.getMessage());
    }

  }

  public Map getFirstPage() {
    WorkflowInstancePage page = engine.getInstanceRepository()
                                      .getFirstPage();
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }
  }

  public Map getNextPage(Hashtable currentPage) {
    return this.getNextPageCore(currentPage);
  }

  public Map getNextPageCore(Map currentPage) {
    // first unpack current page
    WorkflowInstancePage currPage = XmlRpcStructFactory
        .getWorkflowInstancePageFromXmlRpc(currentPage);
    WorkflowInstancePage page = engine.getInstanceRepository().getNextPage(
        currPage);
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }
  }

  public Map getPrevPage(Hashtable currentPage) {
    return this.getNextPageCore(currentPage);
  }

  public Map getPrevPageCore(Map currentPage) {
    // first unpack current page
    WorkflowInstancePage currPage = XmlRpcStructFactory
        .getWorkflowInstancePageFromXmlRpc(currentPage);
    WorkflowInstancePage page = engine.getInstanceRepository().getPrevPage(
        currPage);
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }
  }

  public Map getLastPage() {
    WorkflowInstancePage page = engine.getInstanceRepository()
                                      .getLastPage();
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }
  }

  public Map paginateWorkflowInstances(int pageNum, String status)
      throws InstanceRepositoryException {
    WorkflowInstancePage page = engine.getInstanceRepository()
                                      .getPagedWorkflows(pageNum, status);
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }

  }

  public Map paginateWorkflowInstances(int pageNum)
      throws InstanceRepositoryException {
    WorkflowInstancePage page = engine.getInstanceRepository()
                                      .getPagedWorkflows(pageNum);
    if (page != null) {
      populateWorkflows(page.getPageWorkflows());
      return XmlRpcStructFactory.getXmlRpcWorkflowInstancePage(page);
    } else {
      return XmlRpcStructFactory
          .getXmlRpcWorkflowInstancePage(WorkflowInstancePage
              .blankPage());
    }
  }

  public Map getWorkflowInstanceMetadata(String wInstId) {
    Metadata met = engine.getWorkflowInstanceMetadata(wInstId);
    return met.getHashTable();
  }

  public List getWorkflowsByEvent(String eventName)
      throws RepositoryException {
    List workflows;
    Vector workflowList = new Vector();

    try {
      workflows = repo.getWorkflowsForEvent(eventName);

      if (workflows != null) {
        for (Object workflow1 : workflows) {
          Workflow w = (Workflow) workflow1;
          Map workflow = XmlRpcStructFactory
              .getXmlRpcWorkflow(w);
          workflowList.add(workflow);
        }
      }

      return workflowList;

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting workflows for event: " + eventName
          + " from repository: Message: " + e.getMessage());
    }
  }

  public boolean handleEvent(String eventName, Hashtable metadata)
      throws RepositoryException, EngineException {
    return this.handleEventCore(eventName, metadata);
  }

  public boolean handleEventCore(String eventName, Map metadata)
      throws RepositoryException, EngineException {
    LOG.log(Level.INFO, "WorkflowManager: Received event: " + eventName);

    List workflows;

    try {
      workflows = repo.getWorkflowsForEvent(eventName);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting workflows associated with event: "
          + eventName + ": Message: " + e.getMessage());
    }

    if (workflows != null) {
      for (Object workflow : workflows) {
        Workflow w = (Workflow) workflow;
        LOG.log(Level.INFO, "WorkflowManager: Workflow " + w.getName()
                            + " retrieved for event " + eventName);

        Metadata m = new Metadata();
        m.addMetadata(metadata);

        try {
          engine.startWorkflow(w, m);
        } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          throw new EngineException(
              "Engine exception when starting workflow: "
              + w.getName() + ": Message: "
              + e.getMessage());
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public Map getWorkflowInstanceById(String wInstId) {
    WorkflowInstance inst;

    try {
      inst = engine.getInstanceRepository().getWorkflowInstanceById(
          wInstId);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Error obtaining workflow instance with ID: [" + wInstId
          + "]: Message: " + e.getMessage());
      inst = new WorkflowInstance();
    }

    return XmlRpcStructFactory.getXmlRpcWorkflowInstance(inst);

  }

  public synchronized boolean stopWorkflowInstance(String workflowInstId) {
    engine.stopWorkflow(workflowInstId);
    return true;
  }

  public synchronized boolean pauseWorkflowInstance(String workflowInstId) {
    engine.pauseWorkflowInstance(workflowInstId);
    return true;
  }

  public synchronized boolean resumeWorkflowInstance(String workflowInstId) {
    engine.resumeWorkflowInstance(workflowInstId);
    return true;
  }

  public double getWorkflowWallClockMinutes(String workflowInstId) {
    return engine.getWallClockMinutes(workflowInstId);
  }

  public double getWorkflowCurrentTaskWallClockMinutes(String workflowInstId) {
    return engine.getCurrentTaskWallClockMinutes(workflowInstId);
  }

  public int getNumWorkflowInstancesByStatus(String status) throws InstanceRepositoryException {
    return engine.getInstanceRepository().getNumWorkflowInstancesByStatus(
        status);
  }

  public int getNumWorkflowInstances() throws InstanceRepositoryException {
    return engine.getInstanceRepository().getNumWorkflowInstances();
  }

  public List getWorkflowInstancesByStatus(String status)
      throws EngineException {
    List workflowInsts;

    Vector workflowInstances = new Vector();

    try {
      workflowInsts = engine.getInstanceRepository()
                            .getWorkflowInstancesByStatus(status);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting workflow instances by status: Message: ["
          + e.getMessage() + "]");
      return workflowInstances;
    }

    if (workflowInsts != null) {
      LOG.log(Level.INFO,
          "Getting workflow instances by status: retrieved: "
          + workflowInsts.size() + " instances");

      try {
        for (Object workflowInst : workflowInsts) {
          WorkflowInstance wInst = (WorkflowInstance) workflowInst;
          // pick up the description of the workflow
          Workflow wDesc = repo.getWorkflowById(wInst.getWorkflow()
                                                     .getId());
          // TODO: hack for now, fix this, we shouldn't have to cast
          // here, bad
          // design
          if (wDesc == null) {
            //Possible dynamic workflow for instance
            //reconsitute it from cache
            wDesc = wInst.getWorkflow();
            repo.addWorkflow(wDesc);
          }
          wInst.setWorkflow(wDesc);
          Map workflowInstance = XmlRpcStructFactory
              .getXmlRpcWorkflowInstance(wInst);
          workflowInstances.add(workflowInstance);
        }
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        throw new EngineException(
            "Exception getting workflow instances by statusfrom workflow engine: Message: "
            + e.getMessage());
      }
    }

    return workflowInstances;
  }

  public List getWorkflowInstances() throws EngineException {
    List workflowInsts;

    Vector workflowInstances = new Vector();

    try {
      workflowInsts = engine.getInstanceRepository()
                            .getWorkflowInstances();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "Exception getting workflow instances: Message: ["
          + e.getMessage() + "]");
      return workflowInstances;
    }

    if (workflowInsts != null) {
      LOG.log(Level.INFO, "Getting workflow instances: retrieved: "
                          + workflowInsts.size() + " instances");

      try {
        for (Object workflowInst : workflowInsts) {
          WorkflowInstance wInst = (WorkflowInstance) workflowInst;
          // pick up the description of the workflow
          Workflow wDesc = repo.getWorkflowById(wInst.getWorkflow()
                                                     .getId());
          if (wDesc == null) {
            //possible dynamic workflow
            //reconsitute it from cached instance
            wDesc = wInst.getWorkflow();
            //now save it
            repo.addWorkflow(wDesc);

          }
          // TODO: hack for now, fix this, we shouldn't have to cast
          // here, bad
          // design
          wInst.setWorkflow(wDesc);
          Map workflowInstance = XmlRpcStructFactory
              .getXmlRpcWorkflowInstance(wInst);
          workflowInstances.add(workflowInstance);
        }
        return workflowInstances;
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        throw new EngineException(
            "Exception getting workflow instances from workflow engine: Message: "
            + e.getMessage());
      }
    } else {
      return null;
    }
  }
  
  public synchronized boolean clearWorkflowInstances() throws InstanceRepositoryException{
    String numInsts = String.valueOf(this.getNumWorkflowInstances());
    LOG.info("Removing ["+numInsts+"] total workflow "
        + "instances from the instance repository.");
    this.engine.getInstanceRepository().clearWorkflowInstances();
    return true;
  }

  public List getWorkflows() throws RepositoryException {
    List workflowList = repo.getWorkflows();
    Vector workflows = new Vector();

    if (workflowList != null) {
      LOG.log(Level.INFO, "Getting workflows: retrieved: "
                          + workflowList.size() + " workflows");

      try {
        for (Object aWorkflowList : workflowList) {
          Workflow w = (Workflow) aWorkflowList;
          Map workflow = XmlRpcStructFactory
              .getXmlRpcWorkflow(w);
          workflows.add(workflow);
        }

        return workflows;
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        throw new RepositoryException(
            "Exception getting workflows from repository: Message: "
            + e.getMessage());
      }

    } else {
      return null;
    }

  }

  public Map getTaskById(String taskId) throws RepositoryException {
    try {
      WorkflowTask t = repo.getWorkflowTaskById(taskId);
      return XmlRpcStructFactory.getXmlRpcWorkflowTask(t);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting task by id: Message: " + e.getMessage());

    }
  }

  public Map getConditionById(String conditionId)
      throws RepositoryException {
    try {
      WorkflowCondition c = repo.getWorkflowConditionById(conditionId);
      return XmlRpcStructFactory.getXmlRpcWorkflowCondition(c);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting condition by id: Message: "
          + e.getMessage());
    }
  }

  public Map getWorkflowById(String workflowId)
      throws RepositoryException {
    try {
      Workflow workflow = repo.getWorkflowById(workflowId);
      return XmlRpcStructFactory.getXmlRpcWorkflow(workflow);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      throw new RepositoryException(
          "Exception getting workflow by id from the repository: Message: "
          + e.getMessage());
    }
  }

  public synchronized boolean updateMetadataForWorkflow(
      String workflowInstId, Hashtable metadata) {
    Metadata met = new Metadata();
    met.addMetadata(metadata);
    return this.engine.updateMetadata(workflowInstId, met);
  }

  public synchronized boolean updateWorkflowInstance(Hashtable workflowInst) {
    return this.updateWorkflowInstanceCore(workflowInst);
  }

  public synchronized boolean updateWorkflowInstanceCore(Map workflowInst) {
    WorkflowInstance wInst = XmlRpcStructFactory
        .getWorkflowInstanceFromXmlRpc(workflowInst);
    return doUpdateWorkflowInstance(wInst);

  }

  public synchronized boolean setWorkflowInstanceCurrentTaskStartDateTime(
      String wInstId, String startDateTimeIsoStr) {
    WorkflowInstance wInst;
    try {
      wInst = this.engine.getInstanceRepository()
                         .getWorkflowInstanceById(wInstId);
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return false;
    }
    wInst.setCurrentTaskStartDateTimeIsoStr(startDateTimeIsoStr);
    return doUpdateWorkflowInstance(wInst);
  }

  public synchronized boolean setWorkflowInstanceCurrentTaskEndDateTime(
      String wInstId, String endDateTimeIsoStr) {
    WorkflowInstance wInst;
    try {
      wInst = this.engine.getInstanceRepository()
                         .getWorkflowInstanceById(wInstId);
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return false;
    }
    wInst.setCurrentTaskEndDateTimeIsoStr(endDateTimeIsoStr);
    return doUpdateWorkflowInstance(wInst);
  }

  public synchronized boolean updateWorkflowInstanceStatus(
      String workflowInstanceId, String status) throws InstanceRepositoryException {
    WorkflowInstance wInst;
    wInst = engine.getInstanceRepository().getWorkflowInstanceById(
        workflowInstanceId);


    wInst.setStatus(status);
    return doUpdateWorkflowInstance(wInst);
  }

  public boolean isAlive() {
    return true;
  }

  public static void main(String[] args) throws IOException {
    int portNum = -1;
    String usage = "XmlRpcWorkflowManager --portNum <port number for xml rpc service>\n";

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--portNum")) {
        portNum = Integer.parseInt(args[++i]);
      }
    }

    if (portNum == -1) {
      System.err.println(usage);
      System.exit(1);
    }

    new XmlRpcWorkflowManager(portNum);

    for (; ; ) {
      try {
        Thread.currentThread().join();
      } catch (InterruptedException ignore) {
      }
    }
  }

  private static WorkflowEngine getWorkflowEngineFromProperty() {
    return getWorkflowEngineFromClassName(System.getProperty(
        WORKFLOW_ENGINE_FACTORY_PROPERTY,
        ThreadPoolWorkflowEngineFactory.class.getCanonicalName()));
  }

  private static WorkflowRepository getWorkflowRepositoryFromProperty() {
    return getWorkflowRepositoryFromClassName(System.getProperty(
        WORKFLOW_REPOSITORY_FACTORY_PROPERTY,
        DataSourceWorkflowRepositoryFactory.class.getCanonicalName()));
  }

  private String getHostname() {
    try {
      // Get hostname by textual representation of IP address
      InetAddress addr = InetAddress.getLocalHost();
      // Get the host name
      return addr.getHostName();
    } catch (UnknownHostException ignored) {
    }
    return null;
  }

  private URL safeGetUrlFromString(String urlStr) {
    try {
      return new URL(urlStr);
    } catch (MalformedURLException e) {
      return null;
    }
  }

  private boolean doUpdateWorkflowInstance(WorkflowInstance wInst) {
    try {
      engine.getInstanceRepository().updateWorkflowInstance(wInst);
      return true;
    } catch (InstanceRepositoryException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return false;
    }
  }

  private void populateWorkflows(List wInsts) {
    if (wInsts != null && wInsts.size() > 0) {
      for (Object wInst1 : wInsts) {
        WorkflowInstance wInst = (WorkflowInstance) wInst1;
        if (wInst.getWorkflow() == null || ((wInst.getWorkflow().getName() == null
                                             || wInst.getWorkflow().getId() == null))) {
          wInst.setWorkflow(safeGetWorkflowById(wInst.getWorkflow()
                                                     .getId()));
        } else {
          // check to see if the workflow exists in the
          // repo
          try {
            if (repo.getWorkflowById(wInst.getWorkflow().getId()) == null) {
              repo.addWorkflow(wInst.getWorkflow());
            }
          } catch (RepositoryException e) {
            LOG.log(Level.WARNING, "Attempting to look up workflow: [" + wInst.getWorkflow()
                                                                              .getId()
                                   + "] in populate workflows. Message: " + e.getMessage());
            LOG.log(Level.SEVERE, e.getMessage());
          }

        }
      }
    }
  }

  private Workflow safeGetWorkflowById(String workflowId) {
    try {
      return repo.getWorkflowById(workflowId);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING, "Error getting workflow by its id: ["
                             + workflowId + "]: Message: " + e.getMessage());
      return new Workflow();
    }
  }

}

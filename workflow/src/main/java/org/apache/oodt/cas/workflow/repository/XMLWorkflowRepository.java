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

//OODT imports

import org.apache.oodt.cas.workflow.examples.NoOpTask;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;
import org.apache.oodt.cas.workflow.util.XmlStructFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//JDK imports


/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * A {@link WorkflowRepository} that loads events, {@link Workflow}s,
 * {@link WorkflowTask}s and {@link WorkflowCondition}s from specialized xml
 * files. The WorkflowRepository is given an initial set of seed directory uris,
 * where it looks for the following files:
 * 
 * <ul>
 * <li>conditions.xml - defines workflow pre conditions</li>
 * <li>tasks.xml - defines workflow tasks</li>
 * <li>*.workflow.xml - individual workflow xml files specifying a single
 * workflow</li>
 * <li>events.xml - maps available workflows to event names</li>
 * </ul>
 * 
 * All of the WorkflowTasks, WorkflowConditions and Workflows themselves are
 * cached in memory by their ids (which are typically URNs).
 * </p>
 */
public class XMLWorkflowRepository implements WorkflowRepository {

    /* the list of directory URIs where workflow xml files live */
    private List workflowHomeUris = null;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(XMLWorkflowRepository.class
            .getName());

    /* our task map */
    private static ConcurrentHashMap taskMap = new ConcurrentHashMap();

    /* our condition map */
    private static ConcurrentHashMap conditionMap = new ConcurrentHashMap();

    /* our workflow map */
    private static ConcurrentHashMap workflowMap = new ConcurrentHashMap();

    /* our event map */
    private static ConcurrentHashMap eventMap = new ConcurrentHashMap();

    private static FileFilter workflowXmlFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile()
                    && pathname.toString().endsWith(".workflow.xml");
        }
    };

    /**
     * <p>
     * Constructs a new XMLWorkflowRepository with the given parameter
     * <code>uris</code>.
     * </p>
     * 
     * @param uris
     *            URIs pointing to directories that follow the XML workflow
     *            repository convention documented at the top of this class.
     */
    public XMLWorkflowRepository(List uris) {
        workflowHomeUris = uris;

        // load the tasks and conditions
        loadConditions(workflowHomeUris);
        loadTasks(workflowHomeUris);
        loadWorkflows(workflowHomeUris);
        loadEvents(workflowHomeUris);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getRegisteredEvents()
     */
    public List getRegisteredEvents() throws RepositoryException {
        return Arrays.asList(eventMap.keySet().toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowTaskById(java.lang.String)
     */
    public WorkflowTask getWorkflowTaskById(String taskId)
            throws RepositoryException {
        return (WorkflowTask) taskMap.get(taskId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowConditionById(java.lang.String)
     */
    public WorkflowCondition getWorkflowConditionById(String conditionId)
            throws RepositoryException {
        return (WorkflowCondition) conditionMap.get(conditionId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowByName(java.lang.String)
     */
    public Workflow getWorkflowByName(String workflowName)
            throws RepositoryException {
      for (Object o : workflowMap.keySet()) {
        String workflowId = (String) o;
        Workflow w = (Workflow) workflowMap.get(workflowId);

        if (w.getName().equals(workflowName)) {
          return w;
        }
      }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowById(java.lang.String)
     */
    public Workflow getWorkflowById(String workflowId)
            throws RepositoryException {
        return (Workflow) workflowMap.get(workflowId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflows()
     */
    public List getWorkflows() throws RepositoryException {
        return Arrays.asList(workflowMap.values().toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTasksByWorkflowId(java.lang.String)
     */
    public List getTasksByWorkflowId(String workflowId)
            throws RepositoryException {
        Workflow w = getWorkflowById(workflowId);
        return w.getTasks();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTasksByWorkflowName(java.lang.String)
     */
    public List getTasksByWorkflowName(String workflowName)
            throws RepositoryException {
        Workflow w = getWorkflowByName(workflowName);
        return w.getTasks();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getWorkflowsForEvent(java.lang.String)
     */
    public List getWorkflowsForEvent(String eventName)
            throws RepositoryException {
        return (List) eventMap.get(eventName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConditionsByTaskName(java.lang.String)
     */
    public List getConditionsByTaskName(String taskName)
            throws RepositoryException {
      for (Object o : taskMap.values()) {
        WorkflowTask t = (WorkflowTask) o;
        if (t.getTaskName().equals(taskName)) {
          return t.getConditions();
        }
      }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConditionsByTaskId(java.lang.String)
     */
    public List getConditionsByTaskId(String taskId) throws RepositoryException {
        WorkflowTask t = (WorkflowTask) taskMap.get(taskId);
        if (t != null) {
            return t.getConditions();
        } else {
          return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConfigurationByTaskId(java.lang.String)
     */
    public WorkflowTaskConfiguration getConfigurationByTaskId(String taskId)
            throws RepositoryException {
        WorkflowTask task = (WorkflowTask) taskMap.get(taskId);
        return task.getTaskConfig();
    }
    

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#addTask(org.apache.oodt.cas.workflow.structs.WorkflowTask)
     */
    @Override
    public String addTask(WorkflowTask task) throws RepositoryException {
      // check its conditions
      if(task.getPreConditions() != null && task.getPreConditions().size() > 0){
        for(WorkflowCondition cond: task.getPreConditions()){
          if(!conditionMap.containsKey(cond.getConditionId())){
            throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined pre condition ith id: ["+cond.getConditionId()+"]");            
          }          
        }
        
        for(WorkflowCondition cond: task.getPostConditions()){
          if(!conditionMap.containsKey(cond.getConditionId())){
            throw new RepositoryException("Reference in new task: ["+task.getTaskName()+"] to undefined post condition ith id: ["+cond.getConditionId()+"]");            
          }              
        }
      }
      
        String taskId = task.getTaskId() != null ? 
             task.getTaskId():UUID.randomUUID().toString();
        taskMap.put(taskId, task);
        return taskId;
    }
    

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#addWorkflow(org.apache.oodt.cas.workflow.structs.Workflow)
     */
    @Override
    public String addWorkflow(Workflow workflow) throws RepositoryException {
       // first check to see that its tasks are all present
      if(workflow.getTasks() == null || (workflow.getTasks().size() == 0)){
        throw new RepositoryException("Attempt to define a new worklfow: ["+workflow.getName()+"] with no tasks.");
      }
      
      for(WorkflowTask task: (List<WorkflowTask>)workflow.getTasks()){
        if(!taskMap.containsKey(task.getTaskId())){
          throw new RepositoryException("Reference in new workflow: ["+workflow.getName()+"] to undefined task with id: ["+task.getTaskId()+"]");
        }
        
        // check its conditions
        if(task.getConditions() != null && task.getConditions().size() > 0){
          for(WorkflowCondition cond: (List<WorkflowCondition>)task.getConditions()){
            if(!conditionMap.containsKey(cond.getConditionId())){
              throw new RepositoryException("Reference in new workflow: ["+workflow.getName()+"] to undefined condition ith id: ["+cond.getConditionId()+"]");
            }
          }
        }
      }
      
        String workflowId = workflow.getId();
		if (workflowId == null || (workflowId.equals(""))) {
			// generate its ID
			workflowId = UUID.randomUUID().toString();
			workflow.setId(workflowId);
		}
		workflowMap.put(workflowId, workflow);
		eventMap.put(workflowId, Collections.singletonList(workflow));
		return workflowId;
		
    }    
    
    /* (non-Javadoc)
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getConditionsByWorkflowId(java.lang.String)
     */
    @Override
    public List<WorkflowCondition> getConditionsByWorkflowId(String workflowId)
        throws RepositoryException {
      if(!workflowMap.containsKey(workflowId)) {
        throw new
            RepositoryException("Attempt to obtain conditions for a workflow: " +
                                "[" + workflowId + "] that does not exist!");
      }
      
      return ((Workflow) workflowMap.get(workflowId)).getConditions();
    }    
    

    /* (non-Javadoc)
     * @see org.apache.oodt.cas.workflow.repository.WorkflowRepository#getTaskById(java.lang.String)
     */
    @Override
    public WorkflowTask getTaskById(String taskId) throws RepositoryException {
      return (WorkflowTask) taskMap.get(taskId);
    }    

    /**
     * @param args
     */
    public static void main(String[] args) throws RepositoryException {
        String usage = "XmlWorkflowRepository <uri 1>...<uri n>\n";
        List uris;

        if (args.length == 0) {
            System.err.println(usage);
            System.exit(1);
        }

        uris = new Vector(args.length);

      for (String arg : args) {
        if (arg != null) {
          uris.add(arg);
        }
      }

        XMLWorkflowRepository repo = new XMLWorkflowRepository(uris);

        List workflows = repo.getWorkflows();

          for (Object workflow : workflows) {
            Workflow w = (Workflow) workflow;
            System.out.println("Workflow: [id=" + w.getId() + ", name="
                               + w.getName() + "]");

            System.out.println("Tasks: ");

            for (WorkflowTask task : w.getTasks()) {
              System.out.println("Task: [class="
                                 + task.getTaskInstanceClassName() + ", id="
                                 + task.getTaskId() + ", name=" + task.getTaskName()
                                 + ", order=" + task.getOrder() + ",reqMetFields="
                                 + task.getRequiredMetFields() + "]");
              System.out.println("Configuration: ");

              for (Object o : task.getTaskConfig().getProperties()
                                  .keySet()) {
                String key = (String) o;
                String value = (String) task.getTaskConfig()
                                            .getProperties().get(key);

                System.out.println("[name=" + key + ", value=" + value
                                   + "]");
              }

              System.out.println("Conditions: ");

              for (Object o : task.getConditions()) {
                WorkflowCondition condition = (WorkflowCondition) o;
                System.out.println("Condition: ["
                                   + condition.getClass().getName() + ", id="
                                   + condition.getConditionId() + ", name="
                                   + condition.getConditionName() + ", timeout="
                                   + condition.getTimeoutSeconds() + ", optional="
                                   + condition.isOptional() + ", order="
                                   + condition.getOrder() + "]");

                System.out.println("Configuration: ");
                for (String cKeyName : (Set<String>) (Set<?>) condition
                    .getCondConfig().getProperties().keySet()) {
                  System.out.println("[name=" + cKeyName + ", value="
                                     + condition.getCondConfig().getProperty(cKeyName) + "]");
                }
              }

            }

          }

    }

    private void loadTasks(List dirUris) {
        if (dirUris != null && dirUris.size() > 0) {
          for (Object dirUri1 : dirUris) {
            String dirUri = (String) dirUri1;

            try {
              File workflowDir = new File(new URI(dirUri));
              if (workflowDir.isDirectory()) {
                String workflowDirStr = workflowDir.getAbsolutePath();

                if (!workflowDirStr.endsWith("/")) {
                  workflowDirStr += "/";
                }

                Document taskRoot = getDocumentRoot(workflowDirStr
                                                    + "tasks.xml");

                Element taskElement = taskRoot.getDocumentElement();

                NodeList taskElemList = taskElement
                    .getElementsByTagName("task");

                if (taskElemList != null
                    && taskElemList.getLength() > 0) {
                  for (int j = 0; j < taskElemList.getLength(); j++) {
                    Element taskElem = (Element) taskElemList
                        .item(j);
                    WorkflowTask task = XmlStructFactory
                        .getWorkflowTask(taskElem, conditionMap);
                    if (task != null) {
                      taskMap.put(task.getTaskId(), task);
                    }
                  }

                }
              }
            } catch (URISyntaxException e) {
              LOG
                  .log(
                      Level.WARNING,
                      "DirUri: "
                      + dirUri
                      + " is not a directory: skipping task loading for it.");
            }

          }
        }
    }

    private void loadConditions(List dirUris) {
        if (dirUris != null && dirUris.size() > 0) {
          for (Object dirUri1 : dirUris) {
            String dirUri = (String) dirUri1;

            try {
              File workflowDir = new File(new URI(dirUri));
              if (workflowDir.isDirectory()) {
                String workflowDirStr = workflowDir.getAbsolutePath();

                if (!workflowDirStr.endsWith("/")) {
                  workflowDirStr += "/";
                }

                Document conditionRoot = getDocumentRoot(workflowDirStr
                                                         + "conditions.xml");

                Element conditionElement = conditionRoot
                    .getDocumentElement();

                NodeList conditionElemList = conditionElement
                    .getElementsByTagName("condition");

                if (conditionElemList != null
                    && conditionElemList.getLength() > 0) {
                  for (int j = 0; j < conditionElemList.getLength(); j++) {
                    Element conditionElem = (Element) conditionElemList
                        .item(j);
                    WorkflowCondition condition = XmlStructFactory
                        .getWorkflowCondition(conditionElem);
                    if (condition != null) {
                      conditionMap.put(
                          condition.getConditionId(),
                          condition);
                    }
                  }

                }
              }
            } catch (URISyntaxException e) {
              LOG
                  .log(
                      Level.WARNING,
                      "DirUri: "
                      + dirUri
                      + " is not a directory: skipping condition loading for it.");
            }

          }
        }
    }

    private void loadWorkflows(List dirUris) {
        if (dirUris != null && dirUris.size() > 0) {
          for (Object dirUri1 : dirUris) {
            String dirUri = (String) dirUri1;

            try {
              File workflowDir = new File(new URI(dirUri));
              if (workflowDir.isDirectory()) {


                // get all the workflow xml files
                File[] workflowFiles = workflowDir
                    .listFiles(workflowXmlFilter);

                if(workflowFiles!=null) {
                  for (File workflowFile : workflowFiles) {
                    String workflowXmlFile = workflowFile
                        .getAbsolutePath();
                    Document workflowRoot = getDocumentRoot(workflowXmlFile);

                    String workflowId = workflowRoot
                        .getDocumentElement().getAttribute("id");
                    if (workflowMap.get(workflowId) == null) {
                      Workflow w = XmlStructFactory.getWorkflow(
                          workflowRoot.getDocumentElement(),
                          taskMap, conditionMap);

                      if (w.getConditions() != null && w.getConditions().size() > 0) {
                        // add a virtual first task, with the conditions
                        w.getTasks().add(0, getGlobalWorkflowConditionsTask(w.getName(), w.getId(), w.getConditions()));
                      }
                      workflowMap.put(workflowId, w);
                    } else {
                      LOG
                          .log(
                              Level.FINE,
                              "Ignoring workflow file: "
                              + workflowXmlFile
                              + " when loading workflows, workflow id: "
                              + workflowId
                              + " already loaded");
                    }

                  }
                }
              }
            } catch (URISyntaxException e) {
              LOG
                  .log(
                      Level.WARNING,
                      "DirUri: "
                      + dirUri
                      + " is not a directory: skipping workflow loading for it.");
            }

          }
        }
    }

    private void loadEvents(List dirUris) {
        if (dirUris != null && dirUris.size() > 0) {
          for (Object dirUri1 : dirUris) {
            String dirUri = (String) dirUri1;

            try {
              File workflowDir = new File(new URI(dirUri));
              if (workflowDir.isDirectory()) {
                String workflowDirStr = workflowDir.getAbsolutePath();

                if (!workflowDirStr.endsWith("/")) {
                  workflowDirStr += "/";
                }

                Document eventRoot = getDocumentRoot(workflowDirStr
                                                     + "events.xml");

                Element eventElement = eventRoot.getDocumentElement();

                NodeList eventElemList = eventElement
                    .getElementsByTagName("event");

                if (eventElemList != null
                    && eventElemList.getLength() > 0) {
                  for (int j = 0; j < eventElemList.getLength(); j++) {
                    Element eventElem = (Element) eventElemList
                        .item(j);

                    String eventName = eventElem
                        .getAttribute("name");
                    Workflow w;

                    NodeList workflowNodeList = eventElem
                        .getElementsByTagName("workflow");

                    if (workflowNodeList != null
                        && workflowNodeList.getLength() > 0) {
                      List workflowList = new Vector();

                      for (int k = 0; k < workflowNodeList
                          .getLength(); k++) {
                        Element workflowElement = (Element) workflowNodeList
                            .item(k);
                        w = (Workflow) workflowMap
                            .get(workflowElement
                                .getAttribute("id"));

                        if (w != null) {
                          workflowList.add(w);
                        }
                      }

                      eventMap.put(eventName, workflowList);
                    }
                  }
                }
              }
            } catch (URISyntaxException e) {
              LOG
                  .log(
                      Level.WARNING,
                      "DirUri: "
                      + dirUri
                      + " is not a directory: skipping event loading for it.");
            }

          }
        }
    }

    private Document getDocumentRoot(String xmlFile) {
        // open up the XML file
        DocumentBuilderFactory factory;
        DocumentBuilder parser;
        Document document;
        InputSource inputSource;

        InputStream xmlInputStream;

        try {
            xmlInputStream = new File(xmlFile).toURI().toURL().openStream();
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when getting input stream from [" + xmlFile
                            + "]: returning null document root");
            return null;
        }

        inputSource = new InputSource(xmlInputStream);

        try {
            factory = DocumentBuilderFactory.newInstance();
            parser = factory.newDocumentBuilder();
            document = parser.parse(inputSource);
        } catch (Exception e) {
            LOG.warning("Unable to parse xml file [" + xmlFile + "]."
                    + "Reason is [" + e + "]");
            return null;
        }

        return document;
    }
    
    private WorkflowTask getGlobalWorkflowConditionsTask(String workflowName, String workflowId, List<WorkflowCondition> conditions){
      WorkflowTask task = new WorkflowTask();
      task.setConditions(conditions);
      task.setTaskConfig(new WorkflowTaskConfiguration());
      task.setTaskId(workflowId+"-global-conditions-eval");
      task.setTaskName(workflowName+"-global-conditions-eval");
      task.setTaskInstanceClassName(NoOpTask.class.getName());
      taskMap.put(task.getTaskId(), task);
      return task;
    }

}

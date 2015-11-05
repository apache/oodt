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

package org.apache.oodt.cas.workflow.util;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.CasMetadataException;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.commons.exceptions.CommonsException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;


/**
 * * A class for constructing Workflow Manager objects from XML {@link Node}s
 * and {@link Element}s.
 * 
 * @author mattmann
 * @version $Revsion$
 * 
 */
public final class XmlStructFactory {

  /* our log stream */
  public static Logger LOG = Logger.getLogger(XmlStructFactory.class.getName());

  private XmlStructFactory() throws InstantiationException {
    throw new InstantiationException("Don't instantiate XML Struct Factories!");
  }

  /**
   * <p>
   * Creates {@link Workflow}s from the XML Node and the map of existing
   * {@link WorkflowTask}s.
   * </p>
   * 
   * @param node
   *          The XML node to construct the Workflow from.
   * @param tasks
   *          The {@link Map} of existing {@link WorkflowTask}s.
   * 
   * @param conditions
   *          The {@link Map} of existing {@link WorkflowCondition}s.
   * 
   * @return A new {@link Workflow} created from the XML node.
   */
  public static Workflow getWorkflow(Node node, Map tasks,
                                     Map conditions) {
    Element workflowRoot = (Element) node;

    String id = workflowRoot.getAttribute("id");
    String name = workflowRoot.getAttribute("name");

    Workflow workflow = new Workflow();
    workflow.setName(name);
    workflow.setId(id);

    Element taskElem = getFirstElement("tasks", workflowRoot);
    Element conditionsElem = getFirstElement("conditions", workflowRoot);

    if (taskElem != null) {
      workflow.setTasks(getTasks(taskElem, tasks));
    }
    if (conditionsElem != null) {
      workflow.setConditions(getConditions(conditionsElem, conditions));
    }

    return workflow;
  }

  /**
   * <p>
   * Constructs a new {@link WorkflowTask} from the given XML node and
   * {@link Map} of {@link WorkflowCondition}s.
   * </p>
   * 
   * @param node
   *          The XML node to construct the {@link WorkflowTask} from.
   * @param conditions
   *          The {@link Map} of {@link WorkflowCondition}s to use when
   *          constructing the WorkflowTask.
   * @return A new {@link WorkflowTask} created from the given XML node.
   */
  public static WorkflowTask getWorkflowTask(Node node, Map conditions) {
    Element taskNode = (Element) node;

    String taskClassName = taskNode.getAttribute("class");
    WorkflowTask task = new WorkflowTask();
    task.setTaskInstanceClassName(taskClassName);

    task.setTaskId(taskNode.getAttribute("id"));
    task.setTaskName(taskNode.getAttribute("name"));

    // get the list of condition IDs for this task and then get the
    // conditions for them
    task.setConditions(getConditions(taskNode, conditions));

    Element reqMetFieldsElem = getFirstElement("requiredMetFields", taskNode);
    if (reqMetFieldsElem != null) {
      task.setRequiredMetFields(getRequiredMetFields(reqMetFieldsElem));
    }

    // load its configuration
    Element configElement = getFirstElement("configuration", taskNode);
    if (configElement != null) {
      task.setTaskConfig(new WorkflowTaskConfiguration(
          getConfiguration(configElement)));
    }

    return task;
  }

  /**
   * <p>
   * Constructs a new {@link WorkflowCondition} from the given XML Node.
   * </p>
   * 
   * @param node
   *          The XML node to construct the WorkflowCondition from.
   * @return A new {@link WorkflowCondition} from the given XML node.
   */
  public static WorkflowCondition getWorkflowCondition(Node node) {
    Element conditionElement = (Element) node;

    String conditionClassName = conditionElement.getAttribute("class");
    WorkflowCondition condition = new WorkflowCondition();
    condition.setConditionInstanceClassName(conditionClassName);
    condition.setConditionId(conditionElement.getAttribute("id"));
    condition.setConditionName(conditionElement.getAttribute("name"));
    condition
        .setTimeoutSeconds(Long.valueOf(conditionElement
            .getAttribute("timeout") != null
            && !conditionElement.getAttribute("timeout").equals("") ? conditionElement
            .getAttribute("timeout") : "-1"));
    condition.setOptional(Boolean.valueOf(conditionElement
        .getAttribute("optional")));

    // load its configuration
    Element configElement = getFirstElement("configuration", conditionElement);
    if (configElement != null) {
      condition.setCondConfig(new WorkflowConditionConfiguration(
          getConfiguration(configElement)));
    }

    return condition;

  }

  /**
   * 
   * @param node
   * @return
   */
  public static List getRequiredMetFields(Node node) {
    Element reqMetFieldsElem = (Element) node;

    NodeList reqMetFieldNodes = reqMetFieldsElem
        .getElementsByTagName("metfield");
    List reqFields = null;

    if (reqMetFieldNodes != null && reqMetFieldNodes.getLength() > 0) {
      reqFields = new Vector(reqMetFieldNodes.getLength());
      for (int i = 0; i < reqMetFieldNodes.getLength(); i++) {
        Element reqMetFieldElem = (Element) reqMetFieldNodes.item(i);
        String reqFieldName = reqMetFieldElem.getAttribute("name");
        reqFields.add(reqFieldName);

      }
    }

    return reqFields;
  }

  /**
   * <p>
   * Constructs a new {@link WorkflowTaskConfiguration} from the given XML node.
   * </p>
   * 
   * @param node
   *          The XML node to construct the WorkflowTaskConfiguration from.
   * @return A new {@link WorkflowTaskConfiguration} constructed from the given
   *         XML node.
   */
  public static Properties getConfiguration(Node node) {
    Element configNode = (Element) node;

    NodeList configProperties = configNode.getElementsByTagName("property");


    Properties properties = new Properties();
    for (int i = 0; i < configProperties.getLength(); i++) {
      Element propElem = (Element) configProperties.item(i);
      String value = propElem.getAttribute("value");

      boolean doReplace = Boolean.valueOf(propElem.getAttribute("envReplace"));
      if (doReplace) {
        value = PathUtils.replaceEnvVariables(value);
      }
      properties.put(propElem.getAttribute("name"), value);
    }

    return properties;
  }

  public static Metadata getConfigurationAsMetadata(Node configNode)
      throws ParseException, CommonsException, CasMetadataException {
    Metadata curMetadata = new Metadata();
    NodeList curGrandChildren = configNode.getChildNodes();
    for (int k = 0; k < curGrandChildren.getLength(); k++) {
      if (curGrandChildren.item(k).getNodeName().equals("property")) {
        Element property = (Element) curGrandChildren.item(k);
        String delim = property.getAttribute("delim");
        String envReplace = property.getAttribute("envReplace");
        String name = property.getAttribute("name");
        String value = property.getAttribute("value");
        if (Boolean.parseBoolean(envReplace)) {
          value = PathUtils.doDynamicReplacement(value);
        }
        List<String> values = new Vector<String>();
        if (delim.length() > 0) {
          values.addAll(Arrays.asList(value.split("\\" + delim)));
        } else {
          values.add(value);
        }
        curMetadata.replaceMetadata(name, values);
      }
    }
    return curMetadata;
  }

  private static List<WorkflowTask> getTasks(Element rootNode, Map tasks) {
    NodeList taskList = rootNode.getElementsByTagName("task");
    List<WorkflowTask> workflowTasks = null;

    if (taskList != null && taskList.getLength() > 0) {
      workflowTasks = new Vector<WorkflowTask>(taskList.getLength());

      for (int i = 0; i < taskList.getLength(); i++) {
        Element taskElement = (Element) taskList.item(i);

        WorkflowTask t = (WorkflowTask) tasks.get(taskElement
            .getAttribute("id"));

        if (t != null) {
          WorkflowTask workflowTask = new WorkflowTask();
          workflowTask.setTaskInstanceClassName(t.getTaskInstanceClassName());
          workflowTask.setConditions(t.getConditions());
          workflowTask.setTaskId(t.getTaskId());
          workflowTask.setTaskConfig(t.getTaskConfig());
          workflowTask.setTaskName(t.getTaskName());
          workflowTask.setOrder(i + 1);
          workflowTask.setRequiredMetFields(t.getRequiredMetFields());
          workflowTasks.add(workflowTask);
        }
      }

    }

    return workflowTasks;
  }

  private static List<WorkflowCondition> getConditions(Element rootNode,
                                                       Map conditions) {
    List<WorkflowCondition> conditionList = new Vector<WorkflowCondition>();
    NodeList conditionNodes = rootNode.getElementsByTagName("condition");

    if (conditionNodes != null && conditionNodes.getLength() > 0) {
      conditionList = new Vector<WorkflowCondition>(conditionNodes.getLength());

      for (int i = 0; i < conditionNodes.getLength(); i++) {
        Element conditionNode = (Element) conditionNodes.item(i);
        WorkflowCondition condition = (WorkflowCondition) conditions
            .get(conditionNode.getAttribute("id"));

        if (condition != null) {
          WorkflowCondition workflowCondition = new WorkflowCondition();
          workflowCondition.setConditionInstanceClassName(condition
              .getConditionInstanceClassName());
          workflowCondition.setConditionId(condition.getConditionId());
          workflowCondition.setConditionName(condition.getConditionName());
          workflowCondition.setOrder(i + 1);
          workflowCondition.setTimeoutSeconds(condition.getTimeoutSeconds());
          workflowCondition.setOptional(condition.isOptional());
          workflowCondition.setCondConfig(condition.getCondConfig());
          conditionList.add(workflowCondition);
        }
      }

    }

    return conditionList;
  }

  private static Element getFirstElement(String name, Element root) {
    NodeList list = root.getElementsByTagName(name);
    if (list.getLength()>0) {
      return (Element) list.item(0);
    } else {
      return null;
    }
  }

  private static String getSimpleElementText(Element node) {
    if (node.getChildNodes().item(0) instanceof Text) {
      return node.getChildNodes().item(0).getNodeValue();
    } else {
      return null;
    }
  }

  private static String getElementText(String elemName, Element root) {
    Element elem = getFirstElement(elemName, root);
    return getSimpleElementText(elem);
  }
}

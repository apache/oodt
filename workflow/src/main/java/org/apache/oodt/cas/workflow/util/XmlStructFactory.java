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
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.HashMap;
import java.util.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>
 * A class for constructing Workflow Manager objects from XML {@link Node}s and
 * {@link Element}s.
 * </p>
 */
public final class XmlStructFactory {

    /* our log stream */
    public static Logger LOG = Logger.getLogger(XmlStructFactory.class
            .getName());

    private XmlStructFactory() throws InstantiationException {
        throw new InstantiationException(
                "Don't instantiate XML Struct Factories!");
    }

    /**
     * <p>
     * Creates {@link Workflow}s from the XML Node and the map of existing
     * {@link WorkflowTask}s.
     * </p>
     * 
     * @param node
     *            The XML node to construct the Workflow from.
     * @param tasks
     *            The {@link HashMap} of existing {@link WorkflowTask}s.
     * @return A new {@link Workflow} created from the XML node.
     */
    public static Workflow getWorkflow(Node node, HashMap tasks) {
        Element workflowRoot = (Element) node;

        String id = workflowRoot.getAttribute("id");
        String name = workflowRoot.getAttribute("name");

        Workflow workflow = new Workflow();
        workflow.setName(name);
        workflow.setId(id);

        Element taskElem = getFirstElement("tasks", workflowRoot);

        NodeList taskList = taskElem.getElementsByTagName("task");

        if (taskList != null && taskList.getLength() > 0) {
            List workflowTasks = new Vector(taskList.getLength());

            for (int i = 0; i < taskList.getLength(); i++) {
                Element taskElement = (Element) taskList.item(i);

                WorkflowTask t = (WorkflowTask) tasks.get(taskElement
                        .getAttribute("id"));

                if (t != null) {
                    WorkflowTask workflowTask = new WorkflowTask();
                    workflowTask.setTaskInstanceClassName(t
                            .getTaskInstanceClassName());
                    workflowTask.setConditions(t.getConditions());
                    workflowTask.setTaskId(t.getTaskId());
                    workflowTask.setTaskConfig(t.getTaskConfig());
                    workflowTask.setTaskName(t.getTaskName());
                    workflowTask.setOrder(i + 1);
                    workflowTask.setRequiredMetFields(t.getRequiredMetFields());
                    workflowTasks.add(workflowTask);
                }
            }
            workflow.setTasks(workflowTasks);
        }

        return workflow;
    }

    /**
     * <p>
     * Constructs a new {@link WorkflowTask} from the given XML node and
     * {@link HashMap} of {@link WorkflowCondition}s.
     * </p>
     * 
     * @param node
     *            The XML node to construct the {@link WorkflowTask} from.
     * @param conditions
     *            The {@link HashMap} of {@link WorkflowCondition}s to use when
     *            constructing the WorkflowTask.
     * @return A new {@link WorkflowTask} created from the given XML node.
     */
    public static WorkflowTask getWorkflowTask(Node node, HashMap conditions) {
        Element taskNode = (Element) node;

        String taskClassName = taskNode.getAttribute("class");
        WorkflowTask task = new WorkflowTask();
        task.setTaskInstanceClassName(taskClassName);

        task.setTaskId(taskNode.getAttribute("id"));
        task.setTaskName(taskNode.getAttribute("name"));

        // get the list of condition IDs for this task and then get the
        // conditions for them
        Element conditionRoot = getFirstElement("conditions", taskNode);
        if (conditionRoot != null) {
            NodeList conditionNodes = conditionRoot
                    .getElementsByTagName("condition");

            if (conditionNodes != null && conditionNodes.getLength() > 0) {
                List conditionList = new Vector(conditionNodes.getLength());

                for (int i = 0; i < conditionNodes.getLength(); i++) {
                    Element conditionNode = (Element) conditionNodes.item(i);
                    WorkflowCondition condition = (WorkflowCondition) conditions
                            .get(conditionNode.getAttribute("id"));

                    if (condition != null) {
                        WorkflowCondition workflowCondition = new WorkflowCondition();
                        workflowCondition
                                .setConditionInstanceClassName(condition
                                        .getConditionInstanceClassName());
                        workflowCondition.setConditionId(condition
                                .getConditionId());
                        workflowCondition.setConditionName(condition
                                .getConditionName());
                        workflowCondition.setOrder(i + 1);
                        workflowCondition.setCondConfig(condition.getTaskConfig());
                        conditionList.add(workflowCondition);
                    }
                }

                task.setConditions(conditionList);
            }

        }

        Element reqMetFieldsElem = getFirstElement("requiredMetFields",
                taskNode);
        if (reqMetFieldsElem != null) {
            task.setRequiredMetFields(getRequiredMetFields(reqMetFieldsElem));
        }

        // load its configuration
        Element configElement = getFirstElement("configuration", taskNode);
        if (configElement != null) {
            task.setTaskConfig(new WorkflowTaskConfiguration(getConfiguration(configElement)));
        }

        return task;
    }

    /**
     * <p>
     * Constructs a new {@link WorkflowCondition} from the given XML Node.
     * </p>
     * 
     * @param node
     *            The XML node to construct the WorkflowCondition from.
     * @return A new {@link WorkflowCondition} from the given XML node.
     */
    public static WorkflowCondition getWorkflowCondition(Node node) {
        Element conditionElement = (Element) node;

        String conditionClassName = conditionElement.getAttribute("class");
        WorkflowCondition condition = new WorkflowCondition();
        condition.setConditionInstanceClassName(conditionClassName);
        condition.setConditionId(conditionElement.getAttribute("id"));
        condition.setConditionName(conditionElement.getAttribute("name"));
        
        // load its configuration
        Element configElement = getFirstElement("configuration", conditionElement);
        if (configElement != null) {
            condition.setCondConfig(new WorkflowConditionConfiguration(getConfiguration(configElement)));
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
     * Constructs a new {@link WorkflowTaskConfiguration} from the given XML
     * node.
     * </p>
     * 
     * @param node
     *            The XML node to construct the WorkflowTaskConfiguration from.
     * @return A new {@link WorkflowTaskConfiguration} constructed from the
     *         given XML node.
     */
    public static Properties getConfiguration(Node node) {
        Element configNode = (Element) node;

        NodeList configProperties = configNode.getElementsByTagName("property");

        if (configProperties == null) {
            return null;
        }

        Properties properties = new Properties();
        for (int i = 0; i < configProperties.getLength(); i++) {
            Element propElem = (Element) configProperties.item(i);
            String value = propElem.getAttribute("value");

            boolean doReplace = Boolean.valueOf(
                    propElem.getAttribute("envReplace")).booleanValue();
            if (doReplace) {
                value = PathUtils.replaceEnvVariables(value);
            }
            properties.put(propElem.getAttribute("name"), value);
        }

        return properties;
    }

    private static Element getFirstElement(String name, Element root) {
        NodeList list = root.getElementsByTagName(name);
        if (list != null) {
            return (Element) list.item(0);
        } else
            return null;
    }

    private static String getSimpleElementText(Element node) {
        if (node.getChildNodes().item(0) instanceof Text) {
            return node.getChildNodes().item(0).getNodeValue();
        } else
            return null;
    }

    private static String getElementText(String elemName, Element root) {
        Element elem = getFirstElement(elemName, root);
        return getSimpleElementText(elem);
    }
}

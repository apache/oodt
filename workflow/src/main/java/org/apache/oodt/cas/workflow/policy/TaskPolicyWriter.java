//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

//OODT imports
import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.cas.workflow.repository.XMLWorkflowRepository;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A writer class to write out {@link WorkflowTask} policy that is managed by
 * the {@link XMLWorkflowRepository}
 * </p>.
 */
public final class TaskPolicyWriter implements TaskPolicyMetKeys {

    private static final String TASKS_XML_FILE_NAME = "tasks.xml";

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(TaskPolicyWriter.class
            .getName());

    private TaskPolicyWriter() throws InstantiationException {
        throw new InstantiationException("Don't construct writers!");
    }

    public static void writeTask(PolicyAwareWorkflowTask updateTask, Map taskMap) {
        String taskXmlFileFullPath = ensureEndingSlash(updateTask
                .getPolicyDirPath())
                + TASKS_XML_FILE_NAME;

        // do the update
        if (taskMap.containsKey(updateTask.getTaskId())) {
            taskMap.put(updateTask.getTaskId(), updateTask);
        }

        // subset the task map list to only be tasks from the given
        // updateTask.getPolicyDirPath
        Map taskSubsetMap = subsetByPolicyDirPath(taskMap, updateTask
                .getPolicyDirPath());

        try {
            XMLUtils.writeXmlToStream(getTaskXmlDocument(taskSubsetMap),
                    new FileOutputStream(new File(taskXmlFileFullPath)));
            LOG.log(Level.INFO, "Successfully updated task policy file: ["
                    + taskXmlFileFullPath + "]");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.log(Level.WARNING, "Unable to write task policy file: ["
                    + taskXmlFileFullPath + "]: file not found");
        }

    }

    private static Document getTaskXmlDocument(Map policyAwareTasks) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element root = (Element) document
                    .createElement(CAS_TASKS_OUTER_ELEM);
            root.setAttribute(CAS_XML_NS_DESC, CAS_NS);
            document.appendChild(root);

            if (policyAwareTasks != null && policyAwareTasks.size() > 0) {
                for (Iterator i = policyAwareTasks.keySet().iterator(); i
                        .hasNext();) {
                    String taskId = (String) i.next();
                    PolicyAwareWorkflowTask task = (PolicyAwareWorkflowTask) policyAwareTasks
                            .get(taskId);

                    Element taskElem = (Element) document
                            .createElement(TASK_ELEM);
                    taskElem.setAttribute(TASK_ID_ATTR, task.getTaskId());
                    taskElem.setAttribute(TASK_NAME_ATTR, task.getTaskName());
                    taskElem.setAttribute(TASK_INST_CLASS_ATTR, task
                            .getTaskInstanceClassName());

                    if (task.getConditions() != null
                            && task.getConditions().size() > 0) {
                        Element conditionsElem = (Element) document
                                .createElement(TASK_CONDITIONS_ELEM);
                        for (Iterator j = task.getConditions().iterator(); j
                                .hasNext();) {
                            WorkflowCondition cond = (WorkflowCondition) j
                                    .next();
                            Element condElem = (Element) document
                                    .createElement(TASK_COND_ELEM);
                            condElem.setAttribute(TASK_COND_ID_ATTR, cond
                                    .getConditionId());
                            conditionsElem.appendChild(condElem);
                        }

                        taskElem.appendChild(conditionsElem);
                    }

                    if (task.getRequiredMetFields() != null
                            && task.getRequiredMetFields().size() > 0) {
                        Element reqMetFieldsElem = (Element) document
                                .createElement(TASK_REQ_MET_FIELDS_ELEM);

                        for (Iterator j = task.getRequiredMetFields()
                                .iterator(); j.hasNext();) {
                            String metField = (String) j.next();
                            Element reqMetFieldElem = (Element) document
                                    .createElement(TASK_REQ_MET_FIELD_ELEM);
                            reqMetFieldElem.setAttribute(
                                    TASK_REQ_MET_FIELD_NAME_ATTR, metField);
                            reqMetFieldsElem.appendChild(reqMetFieldElem);
                        }

                        taskElem.appendChild(reqMetFieldsElem);
                    }
                    if (task.getTaskConfig() != null
                            && task.getTaskConfig().getProperties().keySet()
                                    .size() > 0) {
                        Element taskConfigElem = (Element) document
                                .createElement(TASK_CONFIG_ELEM);
                        for (Iterator j = task.getTaskConfig().getProperties()
                                .keySet().iterator(); j.hasNext();) {
                            String propName = (String) j.next();
                            String propVal = task.getTaskConfig().getProperty(
                                    propName);

                            EnvSavingConfiguration config = (EnvSavingConfiguration) task
                                    .getTaskConfig();
                            Element configPropElem = (Element) document
                                    .createElement(PROPERTY_ELEM);
                            if (config.isReplace(propName)) {
                                configPropElem.setAttribute(
                                        PROPERTY_ELEM_ENVREPLACE_ATTR, String
                                                .valueOf(true));
                            }

                            configPropElem.setAttribute(
                                    PROPERTY_ELEM_NAME_ATTR, propName);
                            configPropElem.setAttribute(
                                    PROPERTY_ELEM_VALUE_ATTR, propVal);
                            taskConfigElem.appendChild(configPropElem);
                        }

                        taskElem.appendChild(taskConfigElem);
                    }

                    root.appendChild(taskElem);
                }
            }

            return document;
        } catch (ParserConfigurationException pce) {
            LOG.log(Level.WARNING, "Error generating tasks xml document!: "
                    + pce.getMessage());
        }

        return null;
    }

    private static String ensureEndingSlash(String path) {
        String fixedPath = path;

        if (!fixedPath.endsWith("/")) {
            fixedPath += "/";
        }

        return fixedPath;
    }

    private static Map subsetByPolicyDirPath(Map origMap, String policyDirPath) {
        Map newMap = new TreeMap(new Comparator() {

            public int compare(Object o1, Object o2) {
                String t1 = (String) o1;
                String t2 = (String) o2;

                return t1.compareTo(t2);
            }

        });

        if (origMap != null && origMap.keySet() != null
                && origMap.keySet().size() > 0) {
            for (Iterator i = origMap.keySet().iterator(); i.hasNext();) {
                String taskId = (String) i.next();
                PolicyAwareWorkflowTask task = (PolicyAwareWorkflowTask) origMap
                        .get(taskId);
                if (task.getPolicyDirPath().equals(policyDirPath)) {
                    newMap.put(taskId, task);
                }
            }
        }

        return newMap;
    }

}

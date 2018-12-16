//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

//OODT imports
import org.apache.oodt.cas.workflow.repository.XMLWorkflowRepository;//for javadoc
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.oodt.cas.workflow.util.XmlStructFactory;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A reader to read in the {@link WorkflowTask} policy managed by the
 * {@link XMLWorkflowRepository}
 * </p>.
 */
public final class TaskPolicyReader {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(TaskPolicyReader.class
            .getName());

    private TaskPolicyReader() throws InstantiationException {
        throw new InstantiationException("Don't construct reader classes!");
    }

    public static Map loadTasks(List dirUris) {

        HashMap conditionMap = loadConditions(dirUris);
        Map taskMap = new TreeMap(new Comparator() {

            public int compare(Object o1, Object o2) {
                String t1 = (String) o1;
                String t2 = (String) o2;

                return t1.compareTo(t2);
            }

        });

        if (dirUris != null && dirUris.size() > 0) {
            for (Iterator i = dirUris.iterator(); i.hasNext();) {
                String dirUri = (String) i.next();

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
                                task.setTaskConfig(EnvVarSavingConfigReader
                                        .getConfiguration(taskElem));
                                PolicyAwareWorkflowTask pTask = new PolicyAwareWorkflowTask(
                                        task);
                                pTask.setPolicyDirPath(workflowDirStr);
                                if (task != null) {
                                    taskMap.put(pTask.getTaskId(), pTask);
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

        return taskMap;
    }

    private static HashMap loadConditions(List dirUris) {
        HashMap conditionMap = new HashMap();

        if (dirUris != null && dirUris.size() > 0) {
            for (Iterator i = dirUris.iterator(); i.hasNext();) {
                String dirUri = (String) i.next();

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

        return conditionMap;
    }

    private static Document getDocumentRoot(String xmlFile) {
        // open up the XML file
        DocumentBuilderFactory factory = null;
        DocumentBuilder parser = null;
        Document document = null;
        InputSource inputSource = null;

        InputStream xmlInputStream = null;

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

}

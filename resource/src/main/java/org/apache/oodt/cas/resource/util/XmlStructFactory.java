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


package org.apache.oodt.cas.resource.util;

//JDK imports

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.resource.structs.Job;
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.commons.xml.XMLUtils;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author woollard
 * @author bfoster
 * @version $Revsion$
 * 
 * <p>
 * A class for constructing Resource Manager objects from XML {@link Node}s and
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

    public static ResourceNode getNodes(Node node) {
        Element resourceNodeRoot = (Element) node;

        String id = null;
        URL ip = null;
        int capacity = 0;

        try {
            id = resourceNodeRoot.getAttribute("nodeId");
            ip = new URL(Boolean.parseBoolean(resourceNodeRoot
					.getAttribute("envReplace")) ? PathUtils
					.doDynamicReplacement(resourceNodeRoot.getAttribute("ip"))
					: resourceNodeRoot.getAttribute("ip"));
            capacity = Integer.valueOf(resourceNodeRoot.getAttribute("capacity"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
        }

        return new ResourceNode(id, ip, capacity);
    }

    public static List getQueueAssignment(Node node) {
        Vector queues = new Vector();

        Element resourceNodeRoot = (Element) node;
        Element queueElem = XMLUtils
                .getFirstElement("queues", resourceNodeRoot);
        NodeList queueList = queueElem.getElementsByTagName("queue");

        if (queueList != null && queueList.getLength() > 0) {
            for (int i = 0; i < queueList.getLength(); i++) {
                Element queueElement = (Element) queueList.item(i);

                String queueId = queueElement.getAttribute("name");
                queues.add(queueId);
            }
        }

        return queues;
    }

    public static JobSpec getJobSpec(Node node) {
        Element jobNodeElem = (Element) node;

        String jobId = jobNodeElem.getAttribute("id");
        String jobName = jobNodeElem.getAttribute("name");
        Element instClassElem = XMLUtils.getFirstElement("instanceClass",
                jobNodeElem);
        String instClass = instClassElem.getAttribute("name");
        String queue = XMLUtils.getElementText("queue", jobNodeElem);
        Integer load = Integer.parseInt(XMLUtils.getElementText(
            "load", jobNodeElem));

        Element inputClass = XMLUtils
                .getFirstElement("inputClass", jobNodeElem);
        String inputClassName = inputClass.getAttribute("name");

        // now read the properties defined, if any
        Element propertiesOuterRoot = XMLUtils.getFirstElement("properties",
                inputClass);
        Properties inputConfigProps = null;

        if (propertiesOuterRoot != null) {
            inputConfigProps = new Properties();
            NodeList propNodeList = propertiesOuterRoot
                    .getElementsByTagName("property");

            if (propNodeList != null && propNodeList.getLength() > 0) {
                for (int i = 0; i < propNodeList.getLength(); i++) {
                    Element propElem = (Element) propNodeList.item(i);
                    String propName = propElem.getAttribute("name");
                    String propValue = propElem.getAttribute("value");

                    if (propName != null && propValue != null) {
                        inputConfigProps.setProperty(propName, propValue);
                    }
                }
            }
        }

        Job job = new Job();
        job.setId(jobId);
        job.setName(jobName);
        job.setJobInstanceClassName(instClass);
        job.setJobInputClassName(inputClassName);
        job.setQueueName(queue);
        job.setLoadValue(load);

        JobInput in = GenericResourceManagerObjectFactory
                .getJobInputFromClassName(inputClassName);
        if (inputConfigProps != null) {
            in.configure(inputConfigProps);
        }

        return new JobSpec(in, job);
    }

}

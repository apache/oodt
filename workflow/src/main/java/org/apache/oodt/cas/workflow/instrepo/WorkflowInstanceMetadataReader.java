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


package org.apache.oodt.cas.workflow.instrepo;

//OODT imports

import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.apache.oodt.commons.exceptions.CommonsException;
import org.apache.oodt.commons.xml.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//JDK imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public final class WorkflowInstanceMetadataReader implements
        WorkflowInstanceMetMapKeys {

    /* our log stream */
    private static Logger LOG = Logger
            .getLogger(WorkflowInstanceMetadataReader.class.getName());

    private WorkflowInstanceMetadataReader() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static WorkflowInstanceMetMap parseMetMapFile(String mapFilePath)
        throws CommonsException, WorkflowException {
        Document doc = getDocumentRoot(mapFilePath);
        Element rootElem = doc.getDocumentElement();
        WorkflowInstanceMetMap map = new WorkflowInstanceMetMap();

        // make sure that there is a default tag
        Element defaultElem = DOMUtil.getFirstElement(rootElem,
                DEFAULT_WORKFLOW_MAP);

        if (defaultElem == null) {
            throw new WorkflowException("file: [" + mapFilePath
                    + "] must specify a default workflow to field map!");
        }

        map.addDefaultFields(readFields(defaultElem));

        NodeList workflowMapNodes = rootElem
                .getElementsByTagName(WORKFLOW_TAG_NAME);

        if (workflowMapNodes != null && workflowMapNodes.getLength() > 0) {
            for (int i = 0; i < workflowMapNodes.getLength(); i++) {
                Element workflowMapElem = (Element) workflowMapNodes.item(i);
                String workflowId = workflowMapElem
                        .getAttribute(WORKFLOW_TAG_ID_ATTR);
                List workflowFields = readFields(workflowMapElem);
                map.addWorkflowToMap(workflowId, workflowFields);
            }
        }
        
        return map;

    }

    private static List readFields(Element rootFldElem) {
        NodeList defaultFldNodes = rootFldElem.getElementsByTagName(FIELD_TAG);
        List fields = null;

        if (defaultFldNodes != null && defaultFldNodes.getLength() > 0) {
            fields = new Vector(defaultFldNodes.getLength());
            for (int i = 0; i < defaultFldNodes.getLength(); i++) {
                Element defaultFldElem = (Element) defaultFldNodes.item(i);
                fields.add(defaultFldElem.getAttribute(FIELD_TAG_NAME_ATTR));
            }
        }

        return fields;
    }

    private static Document getDocumentRoot(String xmlFile) {
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

}

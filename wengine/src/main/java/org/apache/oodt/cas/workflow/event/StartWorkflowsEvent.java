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
package org.apache.oodt.cas.workflow.event;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//JAVAX imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//DOM imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineLocal;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 *	Event which triggers a list of model ids (workflows)
 *
 */
public class StartWorkflowsEvent extends WorkflowEngineEvent {

	private static final Logger LOG = Logger.getLogger(StartWorkflowsEvent.class.getName());
	
	private String xmlFile;
	private List<String> modelIds;
	
	@Override
	public void performAction(WorkflowEngineLocal engine, Metadata inputMetadata) throws Exception {
		if (xmlFile != null) {
			for (String modelId : getModelIdsFromXmlFile(xmlFile))
				engine.startWorkflow(modelId, inputMetadata);
		}else if (modelIds != null) {
			for (String modelId : this.modelIds)
				engine.startWorkflow(modelId, inputMetadata);
		}else {
			throw new Exception("Must Specify an xml file or list of model ids!");
		}
	}

	public String getXmlFile() {
		return xmlFile;
	}

	public void setXmlFile(String xmlFile) {
		this.xmlFile = xmlFile;
	}

	public List<String> getModelIds() {
		return modelIds;
	}

	public void setModelIds(List<String> modelIds) {
		this.modelIds = modelIds;
	}

	private List<String> getModelIdsFromXmlFile(String xmlFile) {
		List<String> modelIdsFromXml = new Vector<String>();

        Document eventRoot = getDocumentRoot(new File(xmlFile).getAbsolutePath());

        Element eventElement = eventRoot.getDocumentElement();

        NodeList eventElemList = eventElement.getElementsByTagName("event");
        if (eventElemList != null && eventElemList.getLength() > 0) {
            for (int j = 0; j < eventElemList.getLength(); j++) {
                Element eventElem = (Element) eventElemList.item(j);

                NodeList workflowNodeList = eventElem.getElementsByTagName("workflow");
                if (workflowNodeList != null && workflowNodeList.getLength() > 0) {
                    for (int k = 0; k < workflowNodeList
                            .getLength(); k++) {
                        Element workflowElement = (Element) workflowNodeList
                                .item(k);
                        modelIdsFromXml.add(workflowElement.getAttribute("id"));
                    }
                }
            }
        }

        return modelIdsFromXml;
	}
	
    private Document getDocumentRoot(String xmlFile) {
        // open up the XML file
        DocumentBuilderFactory factory = null;
        DocumentBuilder parser = null;
        Document document = null;
        InputSource inputSource = null;

        InputStream xmlInputStream = null;

        try {
            xmlInputStream = new File(xmlFile).toURL().openStream();
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

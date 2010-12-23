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
package org.apache.oodt.cas.workflow.processor.map;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//JAVAX imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//OODT imports
import org.apache.oodt.cas.workflow.processor.WorkflowProcessor;

//DOM imports
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * XML based executing type to WorkflowProcessor mapping
 * </p>.
 */
public class XmlBasedProcessorMap implements WorkflowProcessorMap {

	private static final Logger LOG = Logger.getLogger(XmlBasedProcessorMap.class.getName());
	
	private String xmlFile;
	
	public XmlBasedProcessorMap(String xmlFile) {
		this.xmlFile = xmlFile;
	}
	
	public Map<String, Class<? extends WorkflowProcessor>> loadMapping() {
		Map<String, Class<? extends WorkflowProcessor>> processorMap = new HashMap<String, Class<? extends WorkflowProcessor>>();
		
        Document eventRoot = getDocumentRoot(new File(this.xmlFile).getAbsolutePath());
        Element eventElement = eventRoot.getDocumentElement();
        NodeList workflowNodeList = eventElement.getElementsByTagName("processor");
        if (workflowNodeList != null && workflowNodeList.getLength() > 0) {
            for (int k = 0; k < workflowNodeList.getLength(); k++) {
            	try {
            		Element workflowElement = (Element) workflowNodeList.item(k);
            		processorMap.put(workflowElement.getAttribute("id"), (Class<? extends WorkflowProcessor>) Class.forName(workflowElement.getAttribute("class")));
            	}catch (Exception e) {
            		e.printStackTrace();
            	}
            }
        }
        return processorMap;
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

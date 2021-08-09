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


package org.apache.oodt.cas.workflow.lifecycle;

//OODT imports

import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.apache.oodt.commons.exceptions.CommonsException;
import org.apache.oodt.commons.xml.DOMUtil;
import org.apache.oodt.commons.xml.XMLUtils;
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
 * A reader for the <code>workflow-lifecycle.xml</code> file.
 * </p>.
 */
public final class WorkflowLifecyclesReader implements WorkflowLifecycleMetKeys {

    /* our log stream */
    private static Logger LOG = Logger.getLogger(WorkflowLifecyclesReader.class
            .getName());

    private WorkflowLifecyclesReader() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static List parseLifecyclesFile(String lifecyclesFilePath)
        throws CommonsException, WorkflowException {
        Document doc = getDocumentRoot(lifecyclesFilePath);
        Element rootElem = doc.getDocumentElement();
        List lifecycles = new Vector();

        // make sure that there is a default tag
        Element defaultElem = DOMUtil.getFirstElement(rootElem,
                DEFAULT_LIFECYCLE);

        if (defaultElem == null) {
            throw new WorkflowException("file: [" + lifecyclesFilePath
                    + "] must specify a default workflow lifecycle!");
        }

        WorkflowLifecycle defaultLifecycle = readLifecycle(defaultElem, true);
        lifecycles.add(defaultLifecycle);

        NodeList lifecycleNodes = defaultElem
                .getElementsByTagName(LIFECYCLE_TAG_NAME);
        if (lifecycleNodes != null && lifecycleNodes.getLength() > 0) {
            for (int i = 0; i < lifecycleNodes.getLength(); i++) {
                Element lifecycleElem = (Element) lifecycleNodes.item(i);
                lifecycles.add(readLifecycle(lifecycleElem));
            }
        }

        return lifecycles;

    }

    private static WorkflowLifecycle readLifecycle(Element lifecycleElem) {
        return readLifecycle(lifecycleElem, false);
    }

    private static WorkflowLifecycle readLifecycle(Element lifecycleElem,
            boolean isDefault) {
        WorkflowLifecycle lifecycle = new WorkflowLifecycle();
        String lifecycleName = isDefault ? WorkflowLifecycle.DEFAULT_LIFECYCLE
                : lifecycleElem.getAttribute(LIFECYCLE_TAG_NAME_ATTR);
        lifecycle.setName(lifecycleName);
        lifecycle.setWorkflowId(WorkflowLifecycle.NO_WORKFLOW_ID);
        addStagesToLifecycle(lifecycle, lifecycleElem);

        return lifecycle;
    }

    private static void addStagesToLifecycle(WorkflowLifecycle lifecycle,
            Element lifecycleElem) {
        NodeList stagesNodes = lifecycleElem
                .getElementsByTagName(STAGE_ELEM_NAME);

        if (stagesNodes != null && stagesNodes.getLength() > 0) {
            for (int i = 0; i < stagesNodes.getLength(); i++) {
                Element stageElem = (Element) stagesNodes.item(i);
                WorkflowLifecycleStage stage = new WorkflowLifecycleStage();
                stage.setName(stageElem.getAttribute(STAGE_TAG_NAME_ATTR));
                stage.setOrder(i+1);
                stage.setStates(readStates(stageElem, stage));
                lifecycle.addStage(stage);
            }
        }
    }
    
    private static List<WorkflowState> readStates(Element stageElem, WorkflowLifecycleStage category){
      List<WorkflowState> states = new Vector<WorkflowState>();
      NodeList statusNodeList = stageElem.getElementsByTagName(STATUS_TAG_NAME);
      if(statusNodeList != null && statusNodeList.getLength() > 0){
        for(int i=0; i < statusNodeList.getLength(); i++){
          Element statusElem = (Element)statusNodeList.item(i);
          // see if its name is specified via the name attribute, otherwise
          // read it in back compat mode
          
          if(statusElem.getAttribute("name") != null && 
              !statusElem.getAttribute("name").equals("")){
            String statusName = statusElem.getAttribute("name");
            String description = XMLUtils.getElementText("description", statusElem);
            WorkflowState state = new WorkflowState();
            state.setCategory(category);
            state.setName(statusName);
            state.setDescription(description);
            states.add(state);
          }
          else{
            // back compat mode
            String statusName = XMLUtils.getSimpleElementText(statusElem);
            WorkflowState state = new WorkflowState();
            state.setName(statusName);
            state.setMessage(statusName);
            state.setCategory(category);
            states.add(state);
          }
        }
      }
      
      return states;
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

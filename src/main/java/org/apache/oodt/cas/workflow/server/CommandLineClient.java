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
package org.apache.oodt.cas.workflow.server;

//JDK imports
import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

//OODT imports
import org.apache.oodt.commons.option.CmdLineOptionInstance;
import org.apache.oodt.commons.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClientFactory;
import org.apache.oodt.cas.workflow.server.action.WorkflowEngineServerAction;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Client Utility for sending commands to servers
 * <p>
 */
public class CommandLineClient {

	private static final Logger LOG = Logger.getLogger(CommandLineClient.class.getName());
	
	public static void main(String[] args) throws Exception {
		String propertiesFile = System.getProperty("org.apache.oodt.cas.workflow.properties.file");
		if (propertiesFile != null)
			System.getProperties().load(new FileInputStream(propertiesFile));
		String configFile = PathUtils.doDynamicReplacement(System.getProperty("org.apache.oodt.cas.workflow.client.config.file", "classpath:/org/apache/oodt/cas/catalog/config/catserv-client-config.xml"));
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { configFile });
        List<CmdLineOptionInstance> optionInstances = CmdLineOptionUtils.loadValidateAndHandleInstances(appContext, args);
        CmdLineOptionInstance clientInstance = CmdLineOptionUtils.getOptionInstanceByName("clientFactoryBeanId", optionInstances);
		WorkflowEngineClientFactory weClientFactory = null;
		String clientFactoryBeanId = "DefaultWorkflowEngineClientFactory";
        if (clientInstance == null)
        	weClientFactory = (WorkflowEngineClientFactory) appContext.getBean(clientFactoryBeanId, WorkflowEngineClientFactory.class);
        else
        	weClientFactory = (WorkflowEngineClientFactory) appContext.getBean(clientFactoryBeanId = clientInstance.getValues().get(0), WorkflowEngineClientFactory.class);
		WorkflowEngineClient weClient = weClientFactory.createEngine();
		CmdLineOptionInstance actionInstance = CmdLineOptionUtils.getOptionInstanceByName("action", optionInstances);
        WorkflowEngineServerAction action = ((WorkflowEngineServerAction) appContext.getBean(actionInstance.getValues().get(0), WorkflowEngineServerAction.class));
        if (action.passesPreConditions(weClient)) {
        	try {
        		action.performAction(weClient);
        	}catch (Exception e) {
        		LOG.log(Level.SEVERE, "Action '" + action.getId() + "' failed to execute on engine server via client factory bean '" + clientFactoryBeanId + "' : " + e.getMessage());
        		e.printStackTrace();
        	}
        }
	}
}

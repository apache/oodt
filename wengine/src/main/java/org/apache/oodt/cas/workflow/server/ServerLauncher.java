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

//OODT imports
import org.apache.oodt.commons.option.CmdLineOptionInstance;
import org.apache.oodt.commons.option.OptionHelpException;
import org.apache.oodt.commons.option.util.CmdLineOptionUtils;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelServer;
import org.apache.oodt.cas.workflow.server.channel.CommunicationChannelServerFactory;
import org.apache.oodt.cas.workflow.util.Serializer;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Utility for launching CommunicationChannelServers
 * <p>
 */
public class ServerLauncher {
		
	private ServerLauncher() throws InstantiationException {}
	
	public static void main(String[] args) throws Exception {
		String propertiesFile = System.getProperty("org.apache.oodt.cas.workflow.properties.file");
		if (propertiesFile != null)
			System.getProperties().load(new FileInputStream(propertiesFile));
		String configFile = PathUtils.doDynamicReplacement(System.getProperty("org.apache.oodt.cas.workflow.server.config.file", "classpath:/org/apache/oodt/cas/workflow/config/catserv-server-config.xml"));
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { configFile }, false);
		appContext.setClassLoader(new Serializer().getClassLoader());
		appContext.refresh();
        List<CmdLineOptionInstance> optionInstances = null;
        CmdLineOptionInstance instance = null;
        try {
        	optionInstances = CmdLineOptionUtils.loadValidateAndHandleInstances(appContext, args);
        	instance = CmdLineOptionUtils.getOptionInstanceByName("serverFactoryBeanId", optionInstances);
        }catch (OptionHelpException e) {}
        CommunicationChannelServerFactory serverFactory = null;
		String serverFactoryBeanId = "DefaultServerFactory";
		if (instance == null)
			serverFactory = (CommunicationChannelServerFactory) appContext.getBean(serverFactoryBeanId, CommunicationChannelServerFactory.class);
		else
			serverFactory = (CommunicationChannelServerFactory) appContext.getBean(serverFactoryBeanId = instance.getValues().get(0), CommunicationChannelServerFactory.class);
        CommunicationChannelServer communicationChannelServer = serverFactory.createCommunicationChannelServer();
		communicationChannelServer.startup();
		System.out.println("\n---- Launched Server '" +  serverFactoryBeanId + "' on port: " + serverFactory.getPort() + " ----");
	}

}

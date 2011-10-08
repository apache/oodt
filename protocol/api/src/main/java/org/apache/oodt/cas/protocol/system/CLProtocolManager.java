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
package org.apache.oodt.cas.protocol.system;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.protocol.action.ProtocolAction;
import org.apache.oodt.cas.protocol.config.ProtocolConfig;
import org.apache.oodt.cas.protocol.config.SpringProtocolConfig;
import org.apache.oodt.commons.option.CmdLineOptionInstance;
import org.apache.oodt.commons.option.util.CmdLineOptionUtils;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Command-Line {@link ProtocolManager} which {@link ProtocolAction}s to be
 * invoked
 * 
 * @author bfoster
 */
public class CLProtocolManager extends ProtocolManager {

	public CLProtocolManager(ProtocolConfig protocolConfig) {
		super(protocolConfig);
	}

	public static void main(String[] args) throws Exception {
		String protocolConfig = PathUtils
				.doDynamicReplacement(System
						.getProperty(
								"org.apache.oodt.cas.protocol.manager.config.file",
								"classpath:/org/apache/oodt/cas/protocol/actions/protocol-config.xml"));

		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(
				protocolConfig);
		List<CmdLineOptionInstance> optionInstances = CmdLineOptionUtils
				.loadValidateAndHandleInstances(appContext, args);
		CmdLineOptionInstance actionInstance = CmdLineOptionUtils
				.getOptionInstanceByName("action", optionInstances);
		String actionArgValue = actionInstance.getValues().get(0);
		ProtocolAction action = ((ProtocolAction) appContext.getBean(
				actionArgValue, ProtocolAction.class));
		try {
			action.performAction(new CLProtocolManager(
					new SpringProtocolConfig(protocolConfig)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

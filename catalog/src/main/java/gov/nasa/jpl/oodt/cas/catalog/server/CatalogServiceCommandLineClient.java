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

package gov.nasa.jpl.oodt.cas.catalog.server;

//JDK imports
import java.io.FileInputStream;
import java.util.List;

//Spring imports
import org.springframework.context.support.FileSystemXmlApplicationContext;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.server.action.CatalogServiceServerAction;
import gov.nasa.jpl.oodt.cas.catalog.system.impl.CatalogServiceClient;
import gov.nasa.jpl.oodt.cas.catalog.system.impl.CatalogServiceClientFactory;
import gov.nasa.jpl.oodt.cas.catalog.util.Serializer;
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOptionInstance;
import gov.nasa.jpl.oodt.cas.commons.option.util.CmdLineOptionUtils;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Client Utility for sending commands to servers
 * <p>
 */
public class CatalogServiceCommandLineClient {

	public static void main(String[] args) throws Exception {
		String propertiesFile = System.getProperty("gov.nasa.jpl.oodt.cas.catalog.properties.file");
		if (propertiesFile != null)
			System.getProperties().load(new FileInputStream(propertiesFile));
		String configFile = PathUtils.doDynamicReplacement(System.getProperty("gov.nasa.jpl.oodt.cas.catalog.client.config.file", "classpath:/gov/nasa/jpl/oodt/cas/catalog/config/catserv-client-config.xml"));
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { configFile }, false);
//		appContext.setClassLoader(new Serializer().getClassLoader());
		appContext.refresh();
        List<CmdLineOptionInstance> optionInstances = CmdLineOptionUtils.loadValidateAndHandleInstances(appContext, args);
        CmdLineOptionInstance instance = CmdLineOptionUtils.getOptionInstanceByName("clientFactoryBeanId", optionInstances);
		CatalogServiceClientFactory csClientFactory = (CatalogServiceClientFactory) appContext.getBean(instance.getValues().get(0), CatalogServiceClientFactory.class);
		CatalogServiceClient csClient = csClientFactory.createCatalogService();
        instance = CmdLineOptionUtils.getOptionInstanceByName("action", optionInstances);
		((CatalogServiceServerAction) appContext.getBean(instance.getValues().get(0), CatalogServiceServerAction.class)).performAction(csClient);
	}
}

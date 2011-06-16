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
package org.apache.oodt.cas.protocol.config;

//JDK imports
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Spring imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.protocol.ProtocolFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Spring Framework base {@link ProtocolConfig} which loads {@link ProtocolFactory}s
 * from a Spring xml bean file.
 *
 * @author bfoster
 */
public class SpringProtocolConfig implements ProtocolConfig {

	protected Map<String, List<ProtocolFactory>> factoryMap;
	
	public SpringProtocolConfig(String configFile) {
		Validate.notNull(configFile, "SpringProtocolConfig configFile cannnot be NULL");
		factoryMap = new HashMap<String, List<ProtocolFactory>>();
		loadFactories(new FileSystemXmlApplicationContext(configFile));
	}
	
	private void loadFactories(FileSystemXmlApplicationContext appContext) {
		Collection<ProtocolFactory> protocolFactories = appContext.getBeansOfType(ProtocolFactory.class).values();
		for (ProtocolFactory factory : protocolFactories) {
			List<ProtocolFactory> factories = factoryMap.get(factory.getSchema());
			if (factories == null) {
				factories = new ArrayList<ProtocolFactory>();
			}
			factories.add(factory);
			factoryMap.put(factory.getSchema(), factories);
		}
	}
	
	public List<ProtocolFactory> getAllFactories() {
		ArrayList<ProtocolFactory> factories = new ArrayList<ProtocolFactory>();
		for (List<ProtocolFactory> groupOfFactories : factoryMap.values()) {
			factories.addAll(groupOfFactories);
		}
		return factories;
	}
	
	public List<ProtocolFactory> getFactoriesBySite(URI site) {
		Validate.notNull(site, "URI site cannot be NULL");
		List<ProtocolFactory> factories = factoryMap.get(site.getScheme());
		if (factories == null) {
			factories = new ArrayList<ProtocolFactory>();
		}
		return factories;
	}

}

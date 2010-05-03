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

package gov.nasa.jpl.oodt.cas.catalog.server.option;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import gov.nasa.jpl.oodt.cas.catalog.server.action.CatalogServiceServerAction;
import gov.nasa.jpl.oodt.cas.catalog.util.Serializer;
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOption;
import gov.nasa.jpl.oodt.cas.commons.option.handler.CmdLineOptionHandler;

public class PrintSupportedActionsHandler extends CmdLineOptionHandler {

	protected String beanRepo;
	
	@Override
	public String getCustomOptionUsage(CmdLineOption option) {
		return "";
	}

	@Override
	public void handleOption(CmdLineOption option, List<String> values) {
		FileSystemXmlApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] { this.beanRepo }, false);
		appContext.setClassLoader(new Serializer().getClassLoader());
		appContext.refresh();
		PrintStream ps = new PrintStream(System.out);
        ps.println("Actions:");
        for (CatalogServiceServerAction action : ((Map<String, CatalogServiceServerAction>) appContext.getBeansOfType(CatalogServiceServerAction.class)).values()) {
            ps.println("  Action:");
            ps.println("    Id: " + action.getId());
            ps.println("    Description: " + action.getDescription());
            ps.println();
        }
        ps.close();
	}

	public void setBeanRepo(String beanRepo) {
		this.beanRepo = beanRepo;
	}
	
}

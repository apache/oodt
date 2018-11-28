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


package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.util.Properties;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>A specialized set of metadata properties for a {@link WorkflowTask}.</p>
 *
 */
public class WorkflowTaskConfiguration {

	/* the task configuration properties */
	private Properties taskProperties = null;
	
	/**
	 * <p>
	 * Default Constructor
	 * </p>.
	 */
	public WorkflowTaskConfiguration() {
		taskProperties = new Properties();
	}

	/**
	 * <p>
	 * Construct a new WorkflowTaskConfiguration from a java Properties object.
	 * </p>
	 * 
	 * @param properties
	 *            The task configuration properties.
	 */
	public WorkflowTaskConfiguration(Properties properties) {
		taskProperties = properties;
	}

	/**
	 * <p>
	 * Adds the property denoted by the given <code>name></code> and
	 * <code>value</code>.
	 * </p>
	 * 
	 * @param name
	 *            The property name.
	 * @param value
	 *            The property value.
	 */
	public void addConfigProperty(String name, String value) {
		taskProperties.setProperty(name, value);
	}

	/**
	 * 
	 * @param propName
	 *            The property to get the value for.
	 * @return The String property value for the specified propName.
	 */
	public String getProperty(String propName) {
		return taskProperties.getProperty(propName);
	}

	/**
	 * 
	 * @return The {@link Properties} for configuring this WorkflowTask.
	 */
	public Properties getProperties() {
		return taskProperties;
	}

}

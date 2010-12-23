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
package org.apache.oodt.cas.workflow.engine.runner;

//OODT imports
import org.apache.oodt.cas.resource.structs.JobInput;
import org.apache.oodt.cas.workflow.instance.TaskInstance;
import org.apache.oodt.cas.workflow.util.Serializer;

//JDK imports
import java.util.Hashtable;
import java.util.Properties;

/**
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 *
 *	A TaskInstance JobInput for running in CAS-Resource
 *
 */
public class ResourceJobInput implements JobInput {

	protected TaskInstance workflowInstance;
	private static final String KEY = "WorkflowInstanceXml";
	
	public String getId() {
		return workflowInstance.getModelId();
	}

	public void read(Object object) {
		this.workflowInstance = new Serializer().deserializeObject(TaskInstance.class, (String) ((Hashtable) object).get(KEY));
	}

	public Object write() {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put(KEY, new Serializer().serializeObject(this.workflowInstance));
		return table;
	}

	public void configure(Properties properties) {
		//do nothing
	}
	
}
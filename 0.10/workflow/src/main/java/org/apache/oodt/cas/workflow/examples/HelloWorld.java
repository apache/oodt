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


package org.apache.oodt.cas.workflow.examples;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * @author mattmann
 * @version $Revsion$
 * 
 * <p>A simple task that takes in the static configuration
 * parameter, "Person", and echos the String "Hello ${person}."</p>
 */
public class HelloWorld implements WorkflowTaskInstance {

	/**
	 * 
	 */
	public HelloWorld() {
		
	}

	/* (non-Javadoc)
	 * @see org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(java.util.Map, 
	 * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
	 */
	public void run(Metadata metadata, WorkflowTaskConfiguration config) {
		System.out.println("Hello World: "+config.getProperties().get("Person"));
	}

}

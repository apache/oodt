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
package org.apache.oodt.cas.workflow.model;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.priority.Priority;

//JDK imports
import java.util.List;

/**
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowModel {

	private String id;
	private String name;
	private String executionType;
	private String instanceClass;
	private Priority priority;
	private int minReqSuccessfulSubProcessors;
	private List<String> excusedSubProcessorIds;    
	private Metadata staticMetadata;
    
    public WorkflowModel(String id, String name, String executionType, String instanceClass, Priority priority, int minReqSuccessfulSubProcessors, List<String> excusedSubProcessorIds, Metadata staticMetadata) {
    	this.id = id;
    	this.name = name;
    	this.executionType = executionType;
    	this.instanceClass = instanceClass;
    	this.priority = priority;
    	this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
    	this.excusedSubProcessorIds = excusedSubProcessorIds;
        this.staticMetadata = staticMetadata;
    }

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getExecutionType() {
		return this.executionType;
	}

	public String getInstanceClass() {
		return this.instanceClass;
	}
	
	public Priority getPriority() {
		return this.priority;
	}
	
	public int getMinReqSuccessfulSubProcessors() {
		return this.minReqSuccessfulSubProcessors;
	}

	public List<String> getExcusedSubProcessorIds() {
		return this.excusedSubProcessorIds;
	}

	public Metadata getStaticMetadata() {
    	return this.staticMetadata;
    }
    
}

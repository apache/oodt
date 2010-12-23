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
package org.apache.oodt.cas.workflow.processor;

//JDK imports
import java.util.Date;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Store WorkflowProcessor critial dates
 * </p>.
 */
public class ProcessorInfo {

	private Date creationDate;
	private Date readyDate;
	private Date executionDate;
	private Date completionDate;
	
	ProcessorInfo() {
		this.creationDate = new Date();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public Date getReadyDate() {
		return readyDate;
	}
	
	void markReadyDate() {
		if (this.readyDate == null)
			this.readyDate = new Date();
	}
	
	public Date getExecutionDate() {
		return executionDate;
	}
	
	void markExecutionDate() {
		if (this.executionDate == null)
			this.executionDate = new Date();
	}

	public Date getCompletionDate() {
		return completionDate;
	}
	
	void markCompletionDate() {
		if (this.completionDate == null)
			this.completionDate = new Date();
	}
	
}

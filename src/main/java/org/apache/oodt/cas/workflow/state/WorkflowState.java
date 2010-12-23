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
package org.apache.oodt.cas.workflow.state;

//JDK imports
import java.util.Date;
import java.util.Vector;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * The state of a WorkflowProcessor
 * <p>
 */
public abstract class WorkflowState {

	protected String message;
	protected Vector<WorkflowState> subStates;
	protected Date startTime;
	public enum Category { INITIAL, WAITING, RUNNING, RESULTS, TRANSITION, HOLDING, DONE }
	
	public WorkflowState(String message) {
		this.message = message;
		this.startTime = new Date();
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Date getStartTime() {
		return this.startTime;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof WorkflowState) 
			return ((WorkflowState) obj).getName().equals(this.getName());
		else
			return false;
	}
		
	public String toString() {
		return this.getName() + " : " + this.getMessage();
	}
	
	public abstract String getName();
	
	public abstract String getDescription();
	
	public abstract Category getCategory();
	
}

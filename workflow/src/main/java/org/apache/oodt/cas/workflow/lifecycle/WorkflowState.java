/**
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
package org.apache.oodt.cas.workflow.lifecycle;

//JDK imports
import java.util.Date;

/**
 * 
 * The state of a WorkflowProcessor
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowState {

  private String name;
  private String description;
	private String message;
	private Date startTime;
	private WorkflowLifecycleStage category;
	private WorkflowState prevState;
	
	
	public WorkflowState(){
	  this.startTime = null;
	  this.name = null;
	  this.description = null;
	  this.message = null;
	  this.category = null;
	  this.prevState = null;
	}
	
	public WorkflowState(String message) {
	  this();
		this.message = message;
		this.startTime = new Date();
	}
	
	public void setMessage(String message){
	  this.message = message;
	}
	
	public void setStartTime(Date startTime){
	  this.startTime = startTime;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Date getStartTime() {
		return this.startTime;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof WorkflowState) {
		  return ((WorkflowState) obj).getName().equals(this.getName());
		} else {
		  return false;
		}
	}
		
	public String toString() {
		return this.getName() + " ["+this.getCategory()+"] : " + this.getMessage();
	}

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the category
   */
  public WorkflowLifecycleStage getCategory() {
    return category;
  }

  /**
   * @param category the category to set
   */
  public void setCategory(WorkflowLifecycleStage category) {
    this.category = category;
  }

  /**
   * @return the prevState
   */
  public WorkflowState getPrevState() {
    return prevState;
  }

  /**
   * @param prevState the prevState to set
   */
  public void setPrevState(WorkflowState prevState) {
    this.prevState = prevState;
  }

  @Override
  public int hashCode() {
	int result = name != null ? name.hashCode() : 0;
	result = 31 * result + (description != null ? description.hashCode() : 0);
	result = 31 * result + (message != null ? message.hashCode() : 0);
	result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
	result = 31 * result + (category != null ? category.hashCode() : 0);
	result = 31 * result + (prevState != null ? prevState.hashCode() : 0);
	return result;
  }
}

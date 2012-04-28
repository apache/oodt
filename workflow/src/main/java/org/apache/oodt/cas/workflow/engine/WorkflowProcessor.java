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
package org.apache.oodt.cas.workflow.engine;

//JDK imports
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;
import org.apache.oodt.cas.workflow.util.WorkflowUtils;
import org.apache.oodt.cas.workflow.structs.Priority;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;

/**
 * 
 * Abstract WorkflowProcessor.
 * 
 * @author mattmann
 * @author bfoster
 * 
 */
public abstract class WorkflowProcessor implements WorkflowProcessorListener,
    Comparable<WorkflowProcessor> {

  public static final String LOCAL_KEYS = "WorkflowProcessor/Local/Keys";

  private WorkflowInstance workflowInstance;
  private String executionType;
  private List<String> excusedSubProcessorIds;
  private WorkflowState state;
  private List<WorkflowProcessor> subProcessors;
  private List<WorkflowProcessorListener> listeners;
  private WorkflowProcessor preConditions;
  private WorkflowProcessor postConditions;
  private ProcessorDateTimeInfo ProcessorDateTimeInfo;
  private Priority priority;
  private int minReqSuccessfulSubProcessors;
  private Metadata staticMetadata;
  private Metadata dynamicMetadata;
  private boolean isConditionProcessor;
  private int timesBlocked;
  protected WorkflowUtils wutils;
  protected WorkflowLifecycleManager lifecycleMgr;

  public WorkflowProcessor() {
    this.state = lifecycleMgr.getDefaultLifecycle().createState("Null", "initial", "");
    this.listeners = new Vector<WorkflowProcessorListener>();
    this.ProcessorDateTimeInfo = new ProcessorDateTimeInfo();
    this.staticMetadata = new Metadata();
    this.dynamicMetadata = new Metadata();
    this.excusedSubProcessorIds = new Vector<String>();
    this.minReqSuccessfulSubProcessors = -1;
    this.isConditionProcessor = false;
    this.timesBlocked = 0;
    this.workflowInstance = new WorkflowInstance();
    this.wutils = new WorkflowUtils(lifecycleMgr);
  }
  
  /**
   * @return the workflowInstance
   */
  public WorkflowInstance getWorkflowInstance() {
    return workflowInstance;
  }

  /**
   * @param workflowInstance the workflowInstance to set
   */
  public void setWorkflowInstance(WorkflowInstance workflowInstance) {
    this.workflowInstance = workflowInstance;
  }

  /**
   * @return the executionType
   */
  public String getExecutionType() {
    return executionType;
  }

  /**
   * @param executionType the executionType to set
   */
  public void setExecutionType(String executionType) {
    this.executionType = executionType;
  }

  /**
   * @return the excusedSubProcessorIds
   */
  public List<String> getExcusedSubProcessorIds() {
    return excusedSubProcessorIds;
  }

  /**
   * @param excusedSubProcessorIds the excusedSubProcessorIds to set
   */
  public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
    this.excusedSubProcessorIds = excusedSubProcessorIds;
  }

  /**
   * @return the state
   */
  public WorkflowState getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(WorkflowState state) {
    this.state = state;
  }

  /**
   * @return the subProcessors
   */
  public List<WorkflowProcessor> getSubProcessors() {
    return subProcessors;
  }

  /**
   * @param subProcessors the subProcessors to set
   */
  public void setSubProcessors(List<WorkflowProcessor> subProcessors) {
    this.subProcessors = subProcessors;
  }

  /**
   * @return the listeners
   */
  public List<WorkflowProcessorListener> getListeners() {
    return listeners;
  }

  /**
   * @param listeners the listeners to set
   */
  public void setListeners(List<WorkflowProcessorListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * @return the preConditions
   */
  public WorkflowProcessor getPreConditions() {
    return preConditions;
  }

  /**
   * @param preConditions the preConditions to set
   */
  public void setPreConditions(WorkflowProcessor preConditions) {
    this.preConditions = preConditions;
  }

  /**
   * @return the postConditions
   */
  public WorkflowProcessor getPostConditions() {
    return postConditions;
  }

  /**
   * @param postConditions the postConditions to set
   */
  public void setPostConditions(WorkflowProcessor postConditions) {
    this.postConditions = postConditions;
  }

  /**
   * @return the processorDateTimeInfo
   */
  public ProcessorDateTimeInfo getProcessorDateTimeInfo() {
    return ProcessorDateTimeInfo;
  }

  /**
   * @param processorDateTimeInfo the processorDateTimeInfo to set
   */
  public void setProcessorDateTimeInfo(ProcessorDateTimeInfo processorDateTimeInfo) {
    ProcessorDateTimeInfo = processorDateTimeInfo;
  }

  /**
   * @return the priority
   */
  public Priority getPriority() {
    return priority;
  }

  /**
   * @param priority the priority to set
   */
  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  /**
   * @return the minReqSuccessfulSubProcessors
   */
  public int getMinReqSuccessfulSubProcessors() {
    return minReqSuccessfulSubProcessors;
  }

  /**
   * @param minReqSuccessfulSubProcessors the minReqSuccessfulSubProcessors to set
   */
  public void setMinReqSuccessfulSubProcessors(int minReqSuccessfulSubProcessors) {
    this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
  }

  /**
   * @return the staticMetadata
   */
  public Metadata getStaticMetadata() {
    return staticMetadata;
  }

  /**
   * @param staticMetadata the staticMetadata to set
   */
  public void setStaticMetadata(Metadata staticMetadata) {
    this.staticMetadata = staticMetadata;
  }

  /**
   * @return the dynamicMetadata
   */
  public Metadata getDynamicMetadata() {
    return dynamicMetadata;
  }

  /**
   * @param dynamicMetadata the dynamicMetadata to set
   */
  public void setDynamicMetadata(Metadata dynamicMetadata) {
    this.dynamicMetadata = dynamicMetadata;
  }

  /**
   * @return the isConditionProcessor
   */
  public boolean isConditionProcessor() {
    return isConditionProcessor;
  }

  /**
   * @param isConditionProcessor the isConditionProcessor to set
   */
  public void setConditionProcessor(boolean isConditionProcessor) {
    this.isConditionProcessor = isConditionProcessor;
  }

  /**
   * @return the timesBlocked
   */
  public int getTimesBlocked() {
    return timesBlocked;
  }

  /**
   * @param timesBlocked the timesBlocked to set
   */
  public void setTimesBlocked(int timesBlocked) {
    this.timesBlocked = timesBlocked;
  }

  /**
   * @return the wutils
   */
  public WorkflowUtils getWutils() {
    return wutils;
  }

  /**
   * @param wutils the wutils to set
   */
  public void setWutils(WorkflowUtils wutils) {
    this.wutils = wutils;
  }

  /**
   * @return the lifecycleMgr
   */
  public WorkflowLifecycleManager getLifecycleMgr() {
    return lifecycleMgr;
  }

  /**
   * @param lifecycleMgr the lifecycleMgr to set
   */
  public void setLifecycleMgr(WorkflowLifecycleManager lifecycleMgr) {
    this.lifecycleMgr = lifecycleMgr;
  }

  
  
  
  //FIXME: grab the rest of this class from the wengine-branch 
  //and drop it in and start working through the errors.
}

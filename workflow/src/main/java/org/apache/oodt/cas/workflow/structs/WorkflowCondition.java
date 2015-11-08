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

/**
 * 
 * A WorkflowCondition is some pre-condition that must evaluate to true in order
 * for a particular {@link WorkflowTask} to be permitted to execute .
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowCondition {

  /* the name of the condition */
  private String conditionName = null;

  /* the id for the condition */
  private String conditionId = null;

  /* the actual portion of the condition that performs the work */
  protected String conditionInstanceClassName = null;

  /* the order that this condition comes in */
  protected int order = -1;

  /* the static configuration parameters for the condition */
  protected WorkflowConditionConfiguration condConfig;
  
  private long timeoutSeconds;
  
  private boolean optional;

  /**
   * <p>
   * Default Constructor
   * </p>
   * 
   */
  public WorkflowCondition() {
    this(null, null, null, -1);
  }

  /**
   * <p>
   * Constructs a new WorkflowCondition with the specified parameters.
   * </p>
   * 
   * @param conditionName
   *          The display name of the condition.
   * @param conditionId
   *          The ID for this condition.
   * @param instanceClass
   *          The particular instance class name attached to this
   *          WorkflowCondition.
   * @param order
   *          The order in which this condition should be checked for a
   *          particular WorkflowTask.
   */
  public WorkflowCondition(String conditionName, String conditionId,
      String instanceClass, int order) {
    this.conditionName = conditionName;
    this.conditionId = conditionId;
    this.conditionInstanceClassName = instanceClass;
    this.order = order;
    this.timeoutSeconds = -1;
    this.condConfig = new WorkflowConditionConfiguration();
    this.optional = false;
  }

  /**
   * @return Returns the taskConfig.
   */
  @Deprecated
  public WorkflowConditionConfiguration getTaskConfig() {
    return this.condConfig;
  }

  /**
   */
  public void setCondConfig(WorkflowConditionConfiguration condConfig) {
    this.condConfig = condConfig;
  }

  /**
   * 
   * @return Returns the condConfig
   */
  public WorkflowConditionConfiguration getCondConfig() {
    return this.condConfig;
  }

  /**
   * @return Returns the conditionId.
   */
  public String getConditionId() {
    return conditionId;
  }

  /**
   * @param conditionId
   *          The conditionId to set.
   */
  public void setConditionId(String conditionId) {
    this.conditionId = conditionId;
  }

  /**
   * @return Returns the conditionName.
   */
  public String getConditionName() {
    return conditionName;
  }

  /**
   * @param conditionName
   *          The conditionName to set.
   */
  public void setConditionName(String conditionName) {
    this.conditionName = conditionName;
  }

  /**
   * @return Returns the conditionInstanceClassName.
   */
  public String getConditionInstanceClassName() {
    return conditionInstanceClassName;
  }

  /**
   * @param conditionInstanceClassName
   *          The conditionInstanceClassName to set.
   */
  public void setConditionInstanceClassName(String conditionInstanceClassName) {
    this.conditionInstanceClassName = conditionInstanceClassName;
  }

  /**
   * @return Returns the order.
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order
   *          The order to set.
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * @return the timeoutSeconds
   */
  public long getTimeoutSeconds() {
    return timeoutSeconds;
  }

  /**
   * @param timeoutSeconds the timeoutSeconds to set
   */
  public void setTimeoutSeconds(long timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }

  /**
   * @return the optional
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * @param optional the optional to set
   */
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

}

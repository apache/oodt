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

package org.apache.oodt.pcs.health;


import java.io.Serializable;

/**
 * 
 * A container representing Job health status in the PCS
 * 
 * @author mattmann
 * @version $Revision$
 */
public class JobHealthStatus implements Serializable {

  private String status;

  private int numPipelines;

  public JobHealthStatus() {

  }

  /**
   * Constructs a JobHealthStatus with the given parameters.
   * 
   * @param status
   *          The Job status, one of {@link org.apache.oodt.cas.workflow.structs.WorkflowStatus#STARTED}, or any of
   *          the other WorkflowStatus keys.
   * 
   * @param numPipelines
   *          The number of {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance}s with the given
   *          {@link org.apache.oodt.cas.workflow.structs.WorkflowStatus}.
   */
  public JobHealthStatus(String status, int numPipelines) {
    this.status = status;
    this.numPipelines = numPipelines;
  }

  /**
   * @return the numPipelines
   */
  public int getNumPipelines() {
    return numPipelines;
  }

  /**
   * @param numPipelines
   *          the numPipelines to set
   */
  public void setNumPipelines(int numPipelines) {
    this.numPipelines = numPipelines;
  }

  /**
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

}

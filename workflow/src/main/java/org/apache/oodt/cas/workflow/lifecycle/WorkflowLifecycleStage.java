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
import java.util.List;
import java.util.Vector;

/**
 * 
 * A particular step (or Stage) in a {@link WorkflowLifecycle}
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowLifecycleStage {

  private String name;

  private int order;

  private List<WorkflowState> states;

  /**
   * Default Constructor.
   * 
   */
  public WorkflowLifecycleStage() {
    states = new Vector<WorkflowState>();
  }

  /**
   * Constructs a new WorkflowLifecycleSage with the given parameters.
   * 
   * @param name
   *          The name of the WorkflowLifeCycleStage.
   * @param states
   *          The {@link List} of String states that are part of this particular
   *          stage.
   * 
   * @param order
   *          The ordering of this State in a {@List} of States that make
   *          up a {@link WorkflowLifeCycle}.
   */
  public WorkflowLifecycleStage(String name, List<WorkflowState> states,
      int order) {
    this.name = name;
    this.states = states;
    this.order = order;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the states
   */
  public List<WorkflowState> getStates() {
    return states;
  }

  /**
   * @param states
   *          the states to set
   */
  public void setStates(List<WorkflowState> states) {
    this.states = states;
  }

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

  /**
   * @param order
   *          the order to set
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return this.name.hashCode() + Integer.valueOf(this.order).hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object stage) {
    return this.name.equals(((WorkflowLifecycleStage) stage).getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.name;
  }

}

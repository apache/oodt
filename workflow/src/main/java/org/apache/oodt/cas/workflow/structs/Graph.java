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

package org.apache.oodt.cas.workflow.structs;

//JDK imports

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.exceptions.WorkflowException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

//OODT imports

/**
 * 
 * A representation of data flow and control flow relationship associated with a
 * {@link Workflow}.
 * 
 * @author mattmann
 * @author bfsoter
 * 
 */
public class Graph {

  private WorkflowTask task;

  private WorkflowCondition cond;

  private ParentChildWorkflow workflow;

  private String modelIdRef;

  private String modelId;

  private String modelName;

  private String alias;

  private String executionType;

  private String minReqSuccessfulSubProcessors;

  private List<String> excused;

  private String clazz;

  private long timeout;

  private boolean optional;

  private Graph parent;

  private List<Graph> children;

  public static final List<String> processorIds = Arrays.asList(new String[] {
      "sequential", "parallel", "condition", "task" });

  public Graph(Element graphElem, Metadata staticMetadata) throws WorkflowException {
    this();
    this.modelId = graphElem.getAttribute("id");
    this.modelName = graphElem.getAttribute("name");
    this.clazz = graphElem.getAttribute("class");
    this.modelIdRef = graphElem.getAttribute("id-ref");
    this.excused.addAll(Arrays.asList(graphElem.getAttribute("excused").split(
        ",")));
    this.alias = graphElem.getAttribute("alias");
    this.minReqSuccessfulSubProcessors = graphElem.getAttribute("min");
    this.executionType = graphElem.getAttribute("execution");
    this.timeout = Long.valueOf(graphElem.getAttribute("timeout") != null
        && !graphElem.getAttribute("timeout").equals("") ? graphElem
        .getAttribute("timeout") : "-1");
    this.optional = Boolean.valueOf(graphElem.getAttribute("optional"));

    NamedNodeMap attrMap = graphElem.getAttributes();
    for (int i = 0; i < attrMap.getLength(); i++) {
      Attr attr = (Attr) attrMap.item(i);
      if (attr.getName().startsWith("p:")) {
        staticMetadata.replaceMetadata(attr.getName().substring(2),
            attr.getValue());
      }
    }

    if ((graphElem.getNodeName().equals("workflow") || graphElem.getNodeName()
        .equals("conditions")) && this.executionType.equals("")) {
      throw new WorkflowException("workflow model '" + graphElem.getNodeName()
          + "' missing execution type");
    } else {
      this.executionType = graphElem.getNodeName();
    }

    if (!processorIds.contains(this.executionType)) {
      throw new WorkflowException("Unsupported execution type id '"
                                  + this.executionType + "'");
    }

    if (!checkValue(this.modelId) && !checkValue(this.modelIdRef)) {
      this.modelId = UUID.randomUUID().toString();
    }

    if (this.alias != null && !this.alias.equals("")) {
      this.modelId = this.alias;
    }
  }

  public Graph() {
    this.task = null;
    this.cond = null;
    this.workflow = null;
    this.modelIdRef = null;
    this.modelId = null;
    this.modelName = null;
    this.alias = null;
    this.executionType = null;
    this.minReqSuccessfulSubProcessors = null;
    this.excused = new Vector<String>();
    this.clazz = null;
    this.children = new Vector<Graph>();
    this.parent = null;
    this.timeout = -1;
    this.optional = false;
  }

  /**
   * @return the parent
   */
  public Graph getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent(Graph parent) {
    this.parent = parent;
  }

  /**
   * @return the children
   */
  public List<Graph> getChildren() {
    return children;
  }

  /**
   * @param children
   *          the children to set
   */
  public void setChildren(List<Graph> children) {
    this.children = children;
  }

  /**
   * @return the modelIdRef
   */
  public String getModelIdRef() {
    return modelIdRef;
  }

  /**
   * @param modelIdRef
   *          the modelIdRef to set
   */
  public void setModelIdRef(String modelIdRef) {
    this.modelIdRef = modelIdRef;
  }

  /**
   * @return the modelId
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * @param modelId
   *          the modelId to set
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return modelName;
  }

  /**
   * @param modelName
   *          the modelName to set
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  /**
   * @return the alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * @param alias
   *          the alias to set
   */
  public void setAlias(String alias) {
    this.alias = alias;
  }

  /**
   * @return the executionType
   */
  public String getExecutionType() {
    return executionType;
  }

  /**
   * @param executionType
   *          the executionType to set
   */
  public void setExecutionType(String executionType) {
    this.executionType = executionType;
  }

  /**
   * @return the minReqSuccessfulSubProcessors
   */
  public String getMinReqSuccessfulSubProcessors() {
    return minReqSuccessfulSubProcessors;
  }

  /**
   * @param minReqSuccessfulSubProcessors
   *          the minReqSuccessfulSubProcessors to set
   */
  public void setMinReqSuccessfulSubProcessors(
      String minReqSuccessfulSubProcessors) {
    this.minReqSuccessfulSubProcessors = minReqSuccessfulSubProcessors;
  }

  /**
   * @return the excused
   */
  public List<String> getExcused() {
    return excused;
  }

  /**
   * @param excused
   *          the excused to set
   */
  public void setExcused(List<String> excused) {
    this.excused = excused;
  }

  /**
   * @return the clazz
   */
  public String getClazz() {
    return clazz;
  }

  /**
   * @param clazz
   *          the clazz to set
   */
  public void setClazz(String clazz) {
    this.clazz = clazz;
  }

  /**
   * @return the task
   */
  public WorkflowTask getTask() {
    return task;
  }

  /**
   * @param task
   *          the task to set
   */
  public void setTask(WorkflowTask task) {
    this.task = task;
  }

  /**
   * @return the cond
   */
  public WorkflowCondition getCond() {
    return cond;
  }

  /**
   * @param cond
   *          the cond to set
   */
  public void setCond(WorkflowCondition cond) {
    this.cond = cond;
  }

  /**
   * @return the workflow
   */
  public ParentChildWorkflow getWorkflow() {
    return workflow;
  }

  /**
   * @param workflow
   *          the workflow to set
   */
  public void setWorkflow(ParentChildWorkflow workflow) {
    this.workflow = workflow;
  }

  public String toString() {
    return this.modelId;
  }

  /**
   * @return the timeout
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * @param timeout
   *          the timeout to set
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * @return the optional
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * @param optional
   *          the optional to set
   */
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  /**
   * @return the processorIds
   */
  public List<String> getProcessorIds() {
    return processorIds;
  }
  
  /**
   * 
   * @return True is {@link #cond} isn't null, false otherwise.
   */
  public boolean isCondition(){
    return this.cond != null;
  }
  
  /**
   * 
   * @return True if {@link #workflow} isn't null, false othewise.
   */
  public boolean isWorkflow(){
    return this.workflow != null;
  }
  
  /**
   * 
   * @return True if {@link #task} isn't null, false otherwise.
   */
  public boolean isTask(){
    return this.task != null;
  }

  private boolean checkValue(String value) {
    return value != null && !value.equals("");
  }

}

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

package org.apache.oodt.cas.workflow.gui.model;

//JDK imports
import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * 
 * 
 * Represents a node in the Workflow Model graph.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class ModelNode {

  private String id;
  private String alias;
  private boolean isRef;
  private File file;
  private boolean textVisible;
  private boolean entryPoint;
  private List<String> configGroups;
  private String executionType;
  private String modelId;
  private String modelName;
  private String instanceClass;
  private List<String> excusedSubProcessorIds;
  private Metadata staticMetadata;
  private long timeout;
  private boolean optional;

  public ModelNode(File file) {
    super();
    this.file = file;
    this.id = UUID.randomUUID().toString();
    this.isRef = false;
    this.executionType = "task";
    this.textVisible = true;
    this.entryPoint = false;
    this.configGroups = new Vector<String>();
    this.modelId = null;
    this.modelName = null;
    this.instanceClass = null;
    this.excusedSubProcessorIds = new Vector<String>();
    this.staticMetadata = new Metadata();
    this.timeout = -1;
    this.optional = false;
  }

  public ModelNode(File file, String modelId) {
    this(file);
    this.setModelId(modelId);
    this.setModelName(modelId);
  }

  public ModelNode(File file, String modelId, boolean isRef) {
    this(file, modelId);
    this.isRef = isRef;
  }

  public String getId() {
    return this.id;
  }

  public void setAlias(String alias) {
  	this.alias = alias;
  }

  public String getAlias() {
  	return alias;
  }

  public void setIsRef(boolean isRef) {
    this.isRef = isRef;
  }

  public boolean isRef() {
    return this.isRef;
  }

  public File getFile() {
    return this.file;
  }

  public void setExtendsConfig(List<String> configGroups) {
    this.configGroups.clear();
    this.configGroups.addAll(configGroups);
  }

  public List<String> getExtendsConfig() {
    return this.configGroups;
  }

  public void setTextVisible(boolean textVisible) {
    this.textVisible = textVisible;
  }

  public void setEntryPoint(boolean entryPoint) {
    this.entryPoint = entryPoint;
  }

  public boolean isEntryPoint() {
    return this.entryPoint;
  }

  public boolean isParentType() {
    return !(this.getExecutionType().equals("task") || this.getExecutionType()
        .equals("condition"));
  }

  public Color getColor() {
    if (this.isParentType()) {
      if (this.getExecutionType().equals("sequential"))
        return new Color(100, 149, 237);
      else if (this.getExecutionType().equals("parallel"))
        return new Color(143, 188, 143);
      else
        return Color.darkGray;
    } else {
      if (this.getExecutionType().equals("task"))
        return Color.orange;
      else
        return Color.cyan;
    }
  }

  public Color getGradientColor() {
    if (this.isParentType()) {
      if (this.getExecutionType().equals("sequential"))
        return new Color(200, 200, 200);
      else if (this.getExecutionType().equals("parallel"))
        return new Color(200, 200, 200);
      else
        return Color.white;
    } else {
      return Color.darkGray;
    }
  }

  public String getModelId() {
    return modelId;
  }

  public String getModelName() {
    if (modelName == null) {
      return modelId;
    } else {
      return modelName;
    }
  }

  public String getExecutionType() {
    return executionType;
  }

  public String getInstanceClass() {
    return instanceClass;
  }

  public List<String> getExcusedSubProcessorIds() {
    if (this.excusedSubProcessorIds == null)
      this.excusedSubProcessorIds = new Vector<String>();
    return this.excusedSubProcessorIds;
  }

  public Metadata getStaticMetadata() {
    return staticMetadata != null ? this.staticMetadata
        : (this.staticMetadata = new Metadata());
  }

  public int hashCode() {
    return this.getId().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof ModelNode)
      return this.getId().equals(((ModelNode) obj).getId());
    else
      return false;
  }

  public String toString() {
    if (this.textVisible) {
      if (this.isParentType())
        return this.getModelName() + " : " + this.getExecutionType();
      else
        return this.getModelName();
    } else {
      return null;
    }
  }

  public ModelNode clone() {
    ModelNode clone = new ModelNode(this.file);
    clone.id = this.id;
    if (this.excusedSubProcessorIds != null)
      clone.excusedSubProcessorIds = new Vector<String>(
          this.excusedSubProcessorIds);
    clone.executionType = this.executionType;
    clone.instanceClass = this.instanceClass;
    clone.modelId = this.modelId;
    clone.modelName = this.modelName;
    clone.staticMetadata = null;
    clone.textVisible = this.textVisible;
    if (this.staticMetadata != null)
      clone.staticMetadata = new Metadata(this.staticMetadata);
    return clone;
  }

  /**
   * @param modelId
   *          the modelId to set
   */
  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  /**
   * @param modelName
   *          the modelName to set
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  /**
   * @param instanceClass
   *          the instanceClass to set
   */
  public void setInstanceClass(String instanceClass) {
    this.instanceClass = instanceClass;
  }

  /**
   * @param excusedSubProcessorIds
   *          the excusedSubProcessorIds to set
   */
  public void setExcusedSubProcessorIds(List<String> excusedSubProcessorIds) {
    this.excusedSubProcessorIds = excusedSubProcessorIds;
  }



  /**
   * @return the configGroups
   */
  public List<String> getConfigGroups() {
    return configGroups;
  }



  /**
   * @param configGroups the configGroups to set
   */
  public void setConfigGroups(List<String> configGroups) {
    this.configGroups = configGroups;
  }



  /**
   * @return the textVisible
   */
  public boolean isTextVisible() {
    return textVisible;
  }



  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }



  /**
   * @param isRef the isRef to set
   */
  public void setRef(boolean isRef) {
    this.isRef = isRef;
  }



  /**
   * @param file the file to set
   */
  public void setFile(File file) {
    this.file = file;
  }



  /**
   * @param executionType the executionType to set
   */
  public void setExecutionType(String executionType) {
    this.executionType = executionType;
  }



  /**
   * @param staticMetadata the staticMetadata to set
   */
  public void setStaticMetadata(Metadata staticMetadata) {
    this.staticMetadata = staticMetadata;
  }

  /**
   * @return the timeout
   */
  public long getTimeout() {
    return timeout;
  }

  /**
   * @param timeout the timeout to set
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
   * @param optional the optional to set
   */
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

}

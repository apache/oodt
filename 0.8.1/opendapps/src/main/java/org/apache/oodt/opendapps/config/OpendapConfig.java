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

package org.apache.oodt.opendapps.config;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * The configuration object for the {@link OpendapProfileHandler}.
 * 
 */
public class OpendapConfig {

  private List<DapRoot> roots;

  private List<RewriteSpec> rewriteSpecs;

  private List<ConstantSpec> constSpecs;

  private List<DatasetMetElem> datasetMetSpecs;
  
  private ProcessingInstructions processingInstructions;

  public OpendapConfig() {
    this.roots = new Vector<DapRoot>();
    this.rewriteSpecs = new Vector<RewriteSpec>();
    this.constSpecs = new Vector<ConstantSpec>();
    this.datasetMetSpecs = new Vector<DatasetMetElem>();
    this.processingInstructions = new ProcessingInstructions();
  }

  /**
   * @return the roots
   */
  public List<DapRoot> getRoots() {
    return roots;
  }

  /**
   * @param roots
   *          the roots to set
   */
  public void setRoots(List<DapRoot> roots) {
    this.roots = roots;
  }

  /**
   * @return the rewriteSpecs
   */
  public List<RewriteSpec> getRewriteSpecs() {
    return rewriteSpecs;
  }

  /**
   * @param rewriteSpecs
   *          the rewriteSpecs to set
   */
  public void setRewriteSpecs(List<RewriteSpec> rewriteSpecs) {
    this.rewriteSpecs = rewriteSpecs;
  }

  /**
   * @return the constSpecs
   */
  public List<ConstantSpec> getConstSpecs() {
    return constSpecs;
  }

  /**
   * @param constSpecs
   *          the constSpecs to set
   */
  public void setConstSpecs(List<ConstantSpec> constSpecs) {
    this.constSpecs = constSpecs;
  }

  /**
   * @return the datasetMetSpecs
   */
  public List<DatasetMetElem> getDatasetMetSpecs() {
    return datasetMetSpecs;
  }

  /**
   * @param datasetMetSpecs
   *          the datasetMetSpecs to set
   */
  public void setDatasetMetSpecs(List<DatasetMetElem> datasetMetSpecs) {
    this.datasetMetSpecs = datasetMetSpecs;
  }
  
  /**
   * Returns all processing instructions.
   * 
   * @return the processingInstructions
   */
  public ProcessingInstructions getProcessingInstructions() {
  	return this.processingInstructions;
  }
  
  /**
   * Adds a processing instruction.
   * 
   * @param key
   * @param value
   */
  public void addProcessingInstruction(String key, String value) {
  	this.processingInstructions.addInstruction(key, value);
  }

}

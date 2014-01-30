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

package org.apache.oodt.cas.workflow.gui.perspective.view.impl;

//JDK imports
import java.util.UUID;

import org.apache.oodt.cas.workflow.gui.model.ModelNode;

/**
 * 
 * An ID'ed edge identified by {@link UUID#randomUUID()}.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class IdentifiableEdge {

  String id;
  
  ModelNode from;
  
  ModelNode to;

  public IdentifiableEdge(ModelNode from, ModelNode to) {
    id = UUID.randomUUID().toString();
    this.from = from;
    this.to = to;
  }

  public String toString() {
    return "";
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the from
   */
  public ModelNode getFrom() {
    return from;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(ModelNode from) {
    this.from = from;
  }

  /**
   * @return the to
   */
  public ModelNode getTo() {
    return to;
  }

  /**
   * @param to the to to set
   */
  public void setTo(ModelNode to) {
    this.to = to;
  }
}

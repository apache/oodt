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

package org.apache.oodt.cas.workflow.gui.util;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.ModelNode;

/**
 * 
 * 
 * Wrapper class for representing a line between two {@link ModelNode}s.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class Line {

  private ModelNode fromModel;
  private ModelNode toModel;

  public Line(ModelNode fromModel, ModelNode toModel) {
    this.fromModel = fromModel;
    this.toModel = toModel;
  }

  public ModelNode getFromModel() {
    return fromModel;
  }

  public ModelNode getToModel() {
    return toModel;
  }

  public boolean equals(Object obj) {
    if (obj instanceof Line) {
      if (this.fromModel == null && this.toModel == null)
        return ((Line) obj).fromModel == null && ((Line) obj).toModel == null;
      else if (this.fromModel == null)
        return ((Line) obj).fromModel == null
            && ((Line) obj).toModel.equals(this.toModel);
      else if (this.toModel == null)
        return ((Line) obj).fromModel.equals(this.fromModel)
            && ((Line) obj).toModel == null;
      else
        return ((Line) obj).fromModel.equals(this.fromModel)
            && ((Line) obj).toModel.equals(this.toModel);
    } else {
      return false;
    }
  }

  public String toString() {
    return this.fromModel.getModelId() + " -> " + this.toModel.getModelId();
  }

}

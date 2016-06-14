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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;

//JDK imports
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * 
 * 
 * The graph to display per workflow in the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class ModelGraph {

  private ModelGraph parent;
  private ModelNode model;
  private ModelGraph preConditions;
  private Vector<ModelGraph> children;
  private ModelGraph postConditions;
  private boolean isPreCondition;
  private boolean isPostCondition;

  public ModelGraph(ModelNode model) {
    this.isPreCondition = isPostCondition = false;
    this.model = model;
    this.children = new Vector<ModelGraph>();
    if (this.model.isParentType()) {
      this.addChild(new ModelGraph(GuiUtils.createDummyNode()));
    }
  }

  public void setIsRef(boolean isRef) {
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(this);
    while (!stack.empty()) {
      ModelGraph curGraph = stack.pop();
      curGraph.getModel().setIsRef(isRef);
      stack.addAll(curGraph.getChildren());
      if (curGraph.getPreConditions() != null) {
        stack.add(curGraph.getPreConditions());
      }
      if (curGraph.getPostConditions() != null) {
        stack.add(curGraph.getPostConditions());
      }
    }
  }

  public boolean isCondition() {
    return this.isPreCondition || this.isPostCondition;
  }

  public boolean isExcused() {
    return this.parent != null
        && this.parent.getModel().getExcusedSubProcessorIds()
            .contains(this.getModel().getModelId());
  }

  public String getId() {
    return model.getId();
  }

  public void setParent(ModelGraph parent) {
    if (this.parent != null) {
      if (this.isCondition() && !this.parent.isCondition()) {
        if (this.isPreCondition) {
          this.parent.preConditions = null;
        } else {
          this.parent.postConditions = null;
        }
      } else {
        this.parent.removeChild(this);
      }
    }
    this.parent = parent;
    if (!this.getModel().isRef() && parent != null && parent.getModel().isRef()) {
      this.getModel().setIsRef(true);
    }
  }

  public ModelGraph getParent() {
    return this.parent;
  }

  public ModelGraph getRootParent() {
    if (this.parent == null) {
      return this;
    } else {
      return this.parent.getRootParent();
    }
  }

  public List<ModelGraph> getPathFromRootParent() {
    Vector<ModelGraph> path = new Vector<ModelGraph>();
    ModelGraph curGraph = this;
    while (curGraph != null) {
      path.add(0, curGraph);
      curGraph = this.parent;
    }
    return path;
  }

  public void addChild(ModelGraph graph) {
    if (this.children.size() == 1
        && GuiUtils.isDummyNode(this.children.get(0).getModel())) {
      this.children.clear();
    }
    this.children.add(graph);
    graph.setParent(this);
  }

  public void removeChild(ModelGraph graph) {
    this.children.remove(graph);
    this.getModel().getExcusedSubProcessorIds()
        .remove(graph.getModel().getModelId());
    graph.parent = null;
    if (this.children.size() == 0) {
      this.addChild(new ModelGraph(GuiUtils.createDummyNode()));
    }
  }

  public List<ModelGraph> getChildren() {
    if (this.getModel().isParentType() && children.size() == 0) {
      this.addChild(new ModelGraph(GuiUtils.createDummyNode()));
    }
    return children;
  }

  public boolean hasChildren() {
    if (this.children.size() == 1
        && GuiUtils.isDummyNode(this.children.get(0).getModel())) {
      return false;
    } else {
      return this.children.size() > 0;
    }
  }

  public ModelNode getModel() {
    return model;
  }

  public void setModel(ModelNode model) {
    this.model = model;
  }

  public Metadata getInheritedStaticMetadata(ViewState state) {
    Metadata m = new Metadata();
    if (this.parent != null) {
      m.replaceMetadata(this.parent.getInheritedStaticMetadata(state));
      if (this.parent.getModel().getStaticMetadata() != null) {
        m.replaceMetadata(this.parent.getModel().getStaticMetadata());
      }
      if (this.parent.getModel().getExtendsConfig() != null) {
        for (String configGroup : this.parent.getModel().getExtendsConfig()) {
          m.replaceMetadata(state.getGlobalConfigGroups().get(configGroup)
                                 .getMetadata());
        }
      }
    }
    return m;
  }

  public ModelGraph getPreConditions() {
    return preConditions;
  }

  public void setPreConditions(ModelGraph preConditions) {
    if (this.preConditions != null) {
      this.preConditions.setParent(null);
    }
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(preConditions);
    while (!stack.empty()) {
      ModelGraph graph = stack.pop();
      graph.isPreCondition = true;
      stack.addAll(graph.getChildren());
    }
    (this.preConditions = preConditions).setParent(this);
  }

  public ModelGraph getPostConditions() {
    return postConditions;
  }

  public void setPostConditions(ModelGraph postConditions) {
    if (this.postConditions != null) {
      this.postConditions.setParent(null);
    }
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(postConditions);
    while (!stack.empty()) {
      ModelGraph graph = stack.pop();
      graph.isPostCondition = true;
      stack.addAll(graph.getChildren());
    }
    (this.postConditions = postConditions).setParent(this);
  }

  public ModelGraph recursiveFind(String id) {
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(this);
    while (!stack.empty()) {
      ModelGraph curGraph = stack.pop();
      if (curGraph.getId().equals(id)) {
        return curGraph;
      }
      stack.addAll(curGraph.getChildren());
      if (curGraph.getPreConditions() != null) {
        stack.add(curGraph.getPreConditions());
      }
      if (curGraph.getPostConditions() != null) {
        stack.add(curGraph.getPostConditions());
      }
    }
    return null;
  }

  public ModelGraph recursiveFindByModelId(String modelId) {
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(this);
    while (!stack.empty()) {
      ModelGraph curGraph = stack.pop();
      if (curGraph.getModel().getModelId().equals(modelId)) {
        return curGraph;
      }
      stack.addAll(curGraph.getChildren());
      if (curGraph.getPreConditions() != null) {
        stack.add(curGraph.getPreConditions());
      }
      if (curGraph.getPostConditions() != null) {
        stack.add(curGraph.getPostConditions());
      }
    }
    return null;
  }

  public List<ModelGraph> getLeafNodes() {
    Vector<ModelGraph> leafNodes = new Vector<ModelGraph>();
    Stack<ModelGraph> stack = new Stack<ModelGraph>();
    stack.add(this);
    while (!stack.empty()) {
      ModelGraph curGraph = stack.pop();
      if (curGraph.getChildren().size() == 0) {
        leafNodes.add(curGraph);
      } else {
        stack.addAll(curGraph.getChildren());
      }
    }
    return leafNodes;
  }

  public int hashCode() {
    return this.getId().hashCode();
  }

  public boolean equals(Object obj) {
    if (obj instanceof ModelGraph) {
      return this.getId().equals(((ModelGraph) obj).getId());
    } else {
      return false;
    }
  }

  public String toString() {
    return this.getModel().getModelId();
  }

  public ModelGraph clone() {
    ModelNode cloneNode = this.model.clone();
    ModelGraph clone = new ModelGraph(cloneNode);
    for (ModelGraph child : this.children) {
      clone.addChild(child.clone());
    }
    if (this.preConditions != null) {
      clone.setPreConditions(this.preConditions.clone());
    }
    if (this.postConditions != null) {
      clone.setPostConditions(this.postConditions.clone());
    }
    return clone;
  }
}

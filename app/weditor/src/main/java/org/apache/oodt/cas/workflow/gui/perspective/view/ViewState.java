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

package org.apache.oodt.cas.workflow.gui.perspective.view;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.model.repo.XmlWorkflowModelRepository.ConfigGroup;
import org.apache.oodt.cas.workflow.gui.perspective.view.View.Mode;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;

//JDK imports
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

/**
 * 
 * The current state of a particular Workflow GUI editor view.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class ViewState {

  private static final ConcurrentHashMap<String, Stack<ViewState>> undoHistory = new ConcurrentHashMap<String, Stack<ViewState>>();

  private ModelGraph selected;
  private List<ModelGraph> graphs;
  private String id;
  private String currentMetGroup;
  private Metadata properties;
  private Mode mode;
  private File file;
  private boolean change = false;
  private Map<String, ConfigGroup> globalConfigGroups;

  public ViewState(File file, ModelGraph selected, List<ModelGraph> graphs,
      Map<String, ConfigGroup> globalConfigGroups) {
    this.selected = selected;
    this.graphs = new Vector<ModelGraph>(graphs);
    this.id = UUID.randomUUID().toString();
    this.currentMetGroup = null;
    this.properties = new Metadata();
    this.mode = Mode.EDIT;
    this.file = file;
    this.globalConfigGroups = globalConfigGroups;
  }

  public Map<String, ConfigGroup> getGlobalConfigGroups() {
    return this.globalConfigGroups;
  }

  public void addGlobalConfigGroup(String groupName, ConfigGroup configGroup) {
    this.globalConfigGroups.put(groupName, configGroup);
  }

  public void removeGlobalConfigGroup(String groupName) {
    this.globalConfigGroups.remove(groupName);
  }

  public File getFile() {
    return this.file;
  }

  public String getId() {
    return this.id;
  }

  public boolean containsProperty(String key) {
    return this.properties.containsGroup(key);
  }

  public void setProperty(String key, String value) {
    Vector<String> values = new Vector<String>();
    values.add(value);
    this.setProperty(key, values);
  }

  public void setProperty(String key, List<String> values) {
    this.properties.replaceMetadata(key, values);
    this.change = true;
  }

  public String getFirstPropertyValue(String key) {
    List<String> values = this.getProperty(key);
    if (values == null || values.size() == 0) {
      return null;
    }
    return values.get(0);
  }

  public List<String> getProperty(String key) {
    return this.properties.getAllMetadata(key);
  }

  public void removeProperty(String key) {
    this.properties.removeMetadata(key);
    this.change = true;
  }

  public List<String> getKeysRecur(String subGroup) {
    Vector<String> keys = new Vector<String>();
    for (String key : this.properties.getAllKeys()) {
      if (key.contains(subGroup)) {
        keys.add(key);
      }
    }
    return keys;
  }

  public void setSelected(ModelGraph selected) {
    if (this.selected == null || selected == null
        || !this.selected.equals(selected)) {
      this.currentMetGroup = null;
      this.selected = selected;
      this.change = true;
    }
  }

  public ModelGraph getSelected() {
    if (this.mode.equals(Mode.EDIT)) {
      return this.selected;
    } else {
      return null;
    }
  }

  public Set<String> getGraphIds() {
    HashSet<String> graphIds = new HashSet<String>();
    for (ModelGraph graph : this.getGraphs()) {
      graphIds.add(graph.getModel().getModelId());
    }
    return graphIds;
  }

  public List<ModelGraph> getGraphs() {
    return this.graphs;
  }

  public void removeGraph(ModelGraph graph) {
    this.graphs.remove(graph);
  }

  public void addGraph(ModelGraph graph) {
    this.graphs.add(graph);
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    this.change = true;
  }

  public Mode getMode() {
    return this.mode;
  }

  public void setCurrentMetGroup(String currentMetGroup) {
    this.currentMetGroup = currentMetGroup;
    this.change = true;
  }

  public String getCurrentMetGroup() {
    return this.currentMetGroup;
  }

  public boolean hasChanged() {
    return this.change;
  }

  public void save() {
    if (this.change) {
      Stack<ViewState> stack = undoHistory.get(this.id);
      if (stack == null) {
        stack = new Stack<ViewState>();
      }
      if (stack.size() >= 100) {
        stack.remove(stack.size() - 1);
      }
      stack.push(this.clone());
      undoHistory.put(this.id, stack);
      this.change = false;
    }
  }

  public void undo() {
    Stack<ViewState> stack = undoHistory.get(this.id);
    if (stack != null && !stack.empty()) {
      this.clone(stack.pop());
      System.out.println(this.getGraphIds());
      this.change = false;
    }
  }

  public void clone(ViewState state) {
    this.graphs = null;
    this.selected = null;
    if (state.graphs != null) {
      this.graphs = new Vector<ModelGraph>();
      for (ModelGraph graph : state.graphs) {
        this.graphs.add(graph.clone());
      }
      if (state.selected != null) {
        this.selected = GuiUtils.find(this.graphs, state.selected.getModel()
                                                                 .getModelId());
      }
    }
    this.properties = new Metadata(state.properties);
    this.id = state.id;
    this.currentMetGroup = state.currentMetGroup;
    this.mode = state.mode;
  }

  public ViewState clone() {
    List<ModelGraph> cloneGraphs = null;
    ModelGraph selected = null;
    if (this.graphs != null) {
      cloneGraphs = new Vector<ModelGraph>();
      for (ModelGraph graph : this.graphs) {
        cloneGraphs.add(graph.clone());
      }
      if (this.selected != null) {
        selected = GuiUtils.find(cloneGraphs, this.selected.getModel()
                                                           .getModelId());
      }
    }
    ViewState clone = new ViewState(this.file, selected, cloneGraphs,
        this.globalConfigGroups);
    clone.id = this.id;
    clone.file = this.file;
    clone.currentMetGroup = this.currentMetGroup;
    clone.properties = new Metadata(this.properties);
    clone.mode = this.mode;
    return clone;
  }

  public int hashCode() {
    return this.id.hashCode();
  }

  public boolean equals(Object obj) {
    return obj instanceof ViewState && this.id.equals(((ViewState) obj).id);
  }

  public String toString() {
    return this.getId();
  }

}

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

package org.apache.oodt.cas.workflow.gui.perspective;

//JDK imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.perspective.view.View.Mode;

/**
 * 
 * A multi-state display perspective.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public abstract class MultiStatePerspective extends Perspective {

  private static final long serialVersionUID = -6410768713084872977L;

  private final Map<String, ViewState> states = new HashMap<String, ViewState>();

  private Mode mode;

  public MultiStatePerspective(String name) {
    super(name);
    this.mode = Mode.EDIT;
  }

  public void addState(ViewState state) {
    this.setState(state);
  }

  public void setState(ViewState state) {
    super.setState(state);
    state.setMode(this.mode);
    if (!this.states.containsKey(state.getId())) {
      this.states.put(state.getId(), state);
      this.handleAddState(state);
    }
  }

  public void removeState(ViewState state) {
    this.states.remove(state);
    this.handleRemoveState(state);
    super.setState(this.getActiveState());
  }

  public ViewState getState(String stateId) {
    return this.states.get(stateId);
  }

  public List<ViewState> getStates() {
    return new Vector<ViewState>(this.states.values());
  }

  public Set<String> getStateIds() {
    return this.states.keySet();
  }

  public void setMode(Mode mode) {
    this.mode = mode;
    for (ViewState state : states.values())
      state.setMode(mode);
    this.refresh();
  }

  public void reset() {
    super.reset();
    this.mode = Mode.EDIT;
    states.clear();
  }

  public abstract void handleAddState(ViewState state);

  public abstract void handleRemoveState(ViewState state);

  public abstract ViewState getActiveState();

}

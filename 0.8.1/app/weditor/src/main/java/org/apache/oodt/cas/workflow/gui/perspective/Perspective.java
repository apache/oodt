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
import javax.swing.JPanel;

//OODT imports
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewListener;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.perspective.view.View.Mode;

/**
 * 
 * 
 * A view listener and jpanel for keeping the Workflow perspective in sync.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public abstract class Perspective extends JPanel implements ViewListener {

  private static final long serialVersionUID = -3343805159396435882L;

  private ViewState state;
  private String name;

  public Perspective(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public ViewState getState() {
    return this.state;
  }

  public void setState(ViewState state) {
    this.state = state;
  }

  public void setMode(Mode mode) {
    this.state.setMode(mode);
    this.refresh();
  }

  public void save() {
    if (this.state != null)
      this.state.save();
  }

  public void undo() {
    if (this.state != null) {
      this.state.undo();
      this.refresh();
    }
  }

  public void reset() {
    this.state = null;
  }

  public abstract void refresh();

  public abstract View getActiveView();

}

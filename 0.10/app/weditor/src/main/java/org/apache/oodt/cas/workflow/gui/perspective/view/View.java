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

//JDK imports
import java.util.UUID;
import java.util.Vector;
import javax.swing.JPanel;

/**
 * 
 * 
 * View abstract base class.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public abstract class View extends JPanel {

  private static final long serialVersionUID = -708692459667309413L;

  public enum Mode {
    DELETE, EDIT, MOVE, ZOOM_IN, ZOOM_OUT;
  }

  private Vector<ViewListener> listeners;
  private String id;
  private boolean isPrimary;

  public static final String DISPLAY_GRAPH_IDS = "DisplayGraphIds";

  public View(String name) {
    super();
    this.id = UUID.randomUUID().toString();
    if (name != null)
      this.setName(name);
    this.listeners = new Vector<ViewListener>();
  }

  public void setPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  public boolean isPrimary() {
    return this.isPrimary;
  }

  public String getId() {
    return this.id;
  }

  public void registerListener(ViewListener listener) {
    listeners.add(listener);
  }

  public void deregisterListener(ViewListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyListeners(ViewChange<?> change) {
    for (ViewListener listener : listeners)
      listener.stateChangeNotify(change);
  }

  public void notifyListeners() {
    this.notifyListeners(new ViewChange.REFRESH_VIEW(this, this));
  }

  public int hashCode() {
    return this.id.hashCode();
  }

  public boolean equals(Object obj) {
    return obj instanceof View && ((View) obj).id.equals(this.id);
  }

  public abstract void refreshView(ViewState state);

}

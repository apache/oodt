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

package org.apache.oodt.cas.workflow.gui.menu;

//JDK imports
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * 
 * 
 * Edit menu driver for the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class EditMenu extends JMenu {

  private static final long serialVersionUID = -8368744697132421274L;

  private JMenuItem undoItem;

  public EditMenu() {
    super("Edit");
    this.add(this.undoItem = new JMenuItem("Undo"));
  }

  public JMenuItem getUndo() {
    return this.undoItem;
  }

}

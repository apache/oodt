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
 * File menu driver for the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class FileMenu extends JMenu {

  private static final long serialVersionUID = -5702987396916441718L;

  private JMenuItem newWorkspace;
  private JMenuItem newProject;
  private JMenuItem openWorkspace;
  private JMenuItem importItem;
  private JMenuItem saveItem;
  private JMenu newMenu, openMenu, importMenu;

  public FileMenu() {
    super("File");
    this.add(newMenu = new JMenu("New"));
    newMenu.add(this.newWorkspace = new JMenuItem("Workspace"));
    newMenu.add(this.newProject = new JMenuItem("Project"));
    this.add(openMenu = new JMenu("Open"));
    openMenu.add(this.openWorkspace = new JMenuItem("Workspace"));
    this.add(importMenu = new JMenu("Import"));
    importMenu.add(this.importItem = new JMenuItem("Project"));
    this.add(this.saveItem = new JMenuItem("Save Project"));
  }

  public JMenuItem getOpenWorkspace() {
    return this.openWorkspace;
  }

  public JMenuItem getImport() {
    return this.importItem;
  }

  public JMenuItem getNewWorkspace() {
    return this.newWorkspace;
  }

  public JMenuItem getNewProject() {
    return this.newProject;
  }

  public JMenuItem getSave() {
    return this.saveItem;
  }

}

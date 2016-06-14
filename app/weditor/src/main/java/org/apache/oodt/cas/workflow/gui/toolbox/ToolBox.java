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

package org.apache.oodt.cas.workflow.gui.toolbox;

//JDK imports
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * 
 * 
 * The box button and its panel that wrap the {@link Tool}.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class ToolBox extends JPanel {

  private static final long serialVersionUID = -2671454524968238519L;

  private JPanel titlePanel;
  private JPanel boxPanel;
  private List<Tool> tools;
  private Tool selectedTool;

  public ToolBox(List<Tool> tools) {
    super();
    this.tools = tools;
    this.titlePanel = new JPanel();
    this.titlePanel.setBorder(new EtchedBorder());
    this.titlePanel.add(new JLabel("Toolbox"));

    this.boxPanel = new JPanel();
    this.boxPanel.setBorder(new EtchedBorder());
    this.boxPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    for (Tool tool : tools) {
      tool.setToolBox(this);
      this.boxPanel.add(tool);
    }
    this.add(this.boxPanel);
  }

  public void setSelected(Tool selectedTool) {
    this.selectedTool = selectedTool;
    this.selectedTool.setSelected(true);
    for (Tool tool : tools) {
      if (!this.selectedTool.equals(tool)) {
        tool.setSelected(false);
      }
    }
  }

  public Tool getSelected() {
    return this.selectedTool;
  }

}

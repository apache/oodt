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
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//OODT imports
import org.apache.oodt.cas.workflow.gui.perspective.view.MultiStateView;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewChange;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;

/**
 * 
 * 
 * Shows the files/workflows associated with a project in the Workflow Editor
 * GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class TreeProjectView extends MultiStateView {

  private static final long serialVersionUID = 1867428699533484861L;

  private JTree tree;

  private ViewState selectedState;

  public TreeProjectView(String name) {
    super(name);
    this.setLayout(new BorderLayout());
  }

  @Override
  public void refreshView(final ViewState activeState,
      final List<ViewState> states) {
    this.removeAll();
    this.selectedState = activeState;
    DefaultMutableTreeNode selected = null;
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Projects");
    for (ViewState state : states)
      if (selectedState != null
          && state.getFile().equals(selectedState.getFile()))
        root.add(selected = new DefaultMutableTreeNode(state.getFile()
            .getName()));
      else
        root.add(new DefaultMutableTreeNode(state.getFile().getName()));

    tree = new JTree(root);
    tree.setEditable(true);
    tree.getModel().addTreeModelListener(new TreeModelListener() {

      public void treeNodesChanged(TreeModelEvent e) {
        //TODO: something with the view nodes here
      }

      public void treeNodesInserted(TreeModelEvent e) {
      }

      public void treeNodesRemoved(TreeModelEvent e) {
      }

      public void treeStructureChanged(TreeModelEvent e) {
      }

    });
    tree.addMouseListener(new MouseListener() {

      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          if (tree.getSelectionPath() != null) {
            DefaultMutableTreeNode selectedComp = (DefaultMutableTreeNode) tree
                .getSelectionPath().getLastPathComponent();
            String stateName = (String) ((DefaultMutableTreeNode) selectedComp)
                .getUserObject();
            for (ViewState state : states) {
              if (state.getFile().getName().equals(stateName)) {
                selectedState = state;
                if (e.getClickCount() == 2)
                  TreeProjectView.this
                      .notifyListeners(new ViewChange.NEW_ACTIVE_STATE(
                          selectedState, TreeProjectView.this));
                break;
              }
            }
          }
        }
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

    });
    if (selected != null)
      tree.setSelectionPath(new TreePath(new DefaultMutableTreeNode[] { root,
          selected }));
    else if (states.size() > 0)
      tree.setSelectionPath(new TreePath(new DefaultMutableTreeNode[] { root,
          (DefaultMutableTreeNode) root.getChildAt(0) }));

    tree.setRootVisible(false);
    this.setBorder(new EtchedBorder());
    JLabel panelName = new JLabel("Workspace Explorer");
    panelName.setBorder(new EtchedBorder());
    this.add(panelName, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(tree,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    this.add(scrollPane, BorderLayout.CENTER);

    this.revalidate();
  }

}

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
import java.awt.Color;
import java.awt.Component;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewChange;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;

/**
 * 
 * The default Workflow GUI editor view shell.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class DefaultTreeView extends View {

  private static final long serialVersionUID = -8295070597651190576L;

  private JTree tree;
  private PopupMenu actionsMenu, orderSubMenu;
  private JScrollPane scrollPane;

  private static final String EXPAND_STATIC_METADATA = "DefaultTreeView/expand/static-metadata";
  private static final String EXPAND_PRECONDITIONS = "DefaultTreeView/expand/pre-conditions";
  private static final String EXPAND_POSTCONDITIONS = "DefaultTreeView/expand/post-conditions";

  public DefaultTreeView(String name) {
    super(name);
    this.setLayout(new BorderLayout());
  }

  public TreePath getTreePath(DefaultMutableTreeNode node, ModelGraph graph) {
    if (node.getUserObject().equals(graph)) {
      return new TreePath(node.getPath());
    } else {
      for (int i = 0; i < node.getChildCount(); i++) {
        // System.out.println("i: " + ((DefaultMutableTreeNode)
        // node.getChildAt(i)).getUserObject());
        TreePath treePath = this.getTreePath(
            (DefaultMutableTreeNode) node.getChildAt(i), graph);
        if (treePath != null)
          return treePath;
      }
      return null;
    }
  }

  private TreePath getTreePath(TreePath currentPath, ViewState state) {
    String lookingForPath = state.getCurrentMetGroup();
    Stack<DefaultMutableTreeNode> stack = new Stack<DefaultMutableTreeNode>();
    DefaultMutableTreeNode baseNode = (DefaultMutableTreeNode) currentPath
        .getLastPathComponent();
    for (int i = 0; i < baseNode.getChildCount(); i++)
      stack.push((DefaultMutableTreeNode) baseNode.getChildAt(i));
    while (!stack.empty()) {
      DefaultMutableTreeNode node = stack.pop();
      if (node.getUserObject().equals("static-metadata")) {
        for (int i = 0; i < node.getChildCount(); i++)
          stack.push((DefaultMutableTreeNode) node.getChildAt(i));
      } else if (node.getUserObject() instanceof HashMap) {
        String key = (String) ((HashMap<String, String>) node.getUserObject())
            .keySet().iterator().next();
        if (lookingForPath.equals(key)) {
          return new TreePath(node.getPath());
        } else if (lookingForPath.startsWith(key + "/")) {
          lookingForPath = lookingForPath
              .substring(lookingForPath.indexOf("/") + 1);
          stack.clear();
          for (int i = 0; i < node.getChildCount(); i++)
            stack.add((DefaultMutableTreeNode) node.getChildAt(i));
        }
      }
    }
    return currentPath;
  }

  private void resetProperties(ViewState state) {
    state.setProperty(EXPAND_STATIC_METADATA, "false");
    state.setProperty(EXPAND_PRECONDITIONS, "false");
    state.setProperty(EXPAND_POSTCONDITIONS, "false");
  }

  @Override
  public void refreshView(final ViewState state) {
    Rectangle visibleRect = null;
    if (this.tree != null)
      visibleRect = this.tree.getVisibleRect();

    this.removeAll();

    this.actionsMenu = this.createPopupMenu(state);

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("WORKFLOWS");
    for (ModelGraph graph : state.getGraphs())
      root.add(this.buildTree(graph, state));
    tree = new JTree(root);
    tree.setShowsRootHandles(true);
    tree.setRootVisible(false);
    tree.add(this.actionsMenu);

    if (state.getSelected() != null) {
      // System.out.println("SELECTED: " + state.getSelected());
      TreePath treePath = this.getTreePath(root, state.getSelected());
      if (state.getCurrentMetGroup() != null) {
        treePath = this.getTreePath(treePath, state);
      } else if (Boolean.parseBoolean(state
          .getFirstPropertyValue(EXPAND_STATIC_METADATA))) {
        DefaultMutableTreeNode baseNode = (DefaultMutableTreeNode) treePath
            .getLastPathComponent();
        for (int i = 0; i < baseNode.getChildCount(); i++) {
          if (((DefaultMutableTreeNode) baseNode.getChildAt(i)).getUserObject()
              .equals("static-metadata")) {
            treePath = new TreePath(
                ((DefaultMutableTreeNode) baseNode.getChildAt(i)).getPath());
            break;
          }
        }
      } else if (Boolean.parseBoolean(state
          .getFirstPropertyValue(EXPAND_PRECONDITIONS))) {
        if (treePath == null)
          treePath = this.getTreePath(root, state.getSelected()
              .getPreConditions());
        DefaultMutableTreeNode baseNode = (DefaultMutableTreeNode) treePath
            .getLastPathComponent();
        for (int i = 0; i < baseNode.getChildCount(); i++) {
          if (((DefaultMutableTreeNode) baseNode.getChildAt(i)).getUserObject()
              .equals("pre-conditions")) {
            treePath = new TreePath(
                ((DefaultMutableTreeNode) baseNode.getChildAt(i)).getPath());
            break;
          }
        }
      } else if (Boolean.parseBoolean(state
          .getFirstPropertyValue(EXPAND_POSTCONDITIONS))) {
        if (treePath == null)
          treePath = this.getTreePath(root, state.getSelected()
              .getPostConditions());
        DefaultMutableTreeNode baseNode = (DefaultMutableTreeNode) treePath
            .getLastPathComponent();
        for (int i = 0; i < baseNode.getChildCount(); i++) {
          if (((DefaultMutableTreeNode) baseNode.getChildAt(i)).getUserObject()
              .equals("post-conditions")) {
            treePath = new TreePath(
                ((DefaultMutableTreeNode) baseNode.getChildAt(i)).getPath());
            break;
          }
        }
      }
      this.tree.expandPath(treePath);
      this.tree.setSelectionPath(treePath);
    }

    tree.addTreeSelectionListener(new TreeSelectionListener() {

      public void valueChanged(TreeSelectionEvent e) {
        if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
          DefaultTreeView.this.resetProperties(state);
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath()
              .getLastPathComponent();
          if (node.getUserObject() instanceof ModelGraph) {
            state.setSelected((ModelGraph) node.getUserObject());
            state.setCurrentMetGroup(null);
            DefaultTreeView.this.notifyListeners();
          } else if (node.getUserObject().equals("static-metadata")
              || node.getUserObject().equals("pre-conditions")
              || node.getUserObject().equals("post-conditions")) {
            state.setSelected((ModelGraph) ((DefaultMutableTreeNode) node
                .getParent()).getUserObject());
            state.setCurrentMetGroup(null);
            state.setProperty(EXPAND_STATIC_METADATA, Boolean.toString(node
                .getUserObject().equals("static-metadata")));
            state.setProperty(EXPAND_PRECONDITIONS,
                Boolean.toString(node.getUserObject().equals("pre-conditions")));
            state.setProperty(EXPAND_POSTCONDITIONS, Boolean.toString(node
                .getUserObject().equals("post-conditions")));
            DefaultTreeView.this.notifyListeners();
          } else if (node.getUserObject() instanceof HashMap) {
            DefaultMutableTreeNode metNode = null;
            String group = null;
            Object[] path = e.getPath().getPath();
            for (int i = path.length - 1; i >= 0; i--) {
              if (((DefaultMutableTreeNode) path[i]).getUserObject() instanceof ModelGraph) {
                metNode = (DefaultMutableTreeNode) path[i];
                break;
              } else if (((DefaultMutableTreeNode) path[i]).getUserObject() instanceof HashMap) {
                if (group == null)
                  group = (String) ((HashMap<String, String>) ((DefaultMutableTreeNode) path[i])
                      .getUserObject()).keySet().iterator().next();
                else
                  group = (String) ((HashMap<String, String>) ((DefaultMutableTreeNode) path[i])
                      .getUserObject()).keySet().iterator().next()
                      + "/"
                      + group;
              }
            }
            ModelGraph graph = (ModelGraph) metNode.getUserObject();
            state.setSelected(graph);
            state.setCurrentMetGroup(group);
            DefaultTreeView.this.notifyListeners();
          } else {
            state.setSelected(null);
            DefaultTreeView.this.notifyListeners();
          }
        }
      }

    });
    tree.setCellRenderer(new TreeCellRenderer() {

      public Component getTreeCellRendererComponent(JTree tree, Object value,
          boolean selected, boolean expanded, boolean leaf, int row,
          boolean hasFocus) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node.getUserObject() instanceof String) {
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          JLabel label = new JLabel((String) node.getUserObject());
          label.setForeground(Color.blue);
          panel.add(label, BorderLayout.CENTER);
          panel.setBackground(selected ? Color.lightGray : Color.white);
          return panel;
        } else if (node.getUserObject() instanceof ModelGraph) {
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          JLabel iconLabel = new JLabel(((ModelGraph) node.getUserObject())
              .getModel().getExecutionType() + ": ");
          iconLabel.setForeground(((ModelGraph) node.getUserObject())
              .getModel().getColor());
          iconLabel.setBackground(Color.white);
          JLabel idLabel = new JLabel(((ModelGraph) node.getUserObject())
              .getModel().getModelName());
          idLabel.setBackground(Color.white);
          panel.add(iconLabel, BorderLayout.WEST);
          panel.add(idLabel, BorderLayout.CENTER);
          panel.setBackground(selected ? Color.lightGray : Color.white);
          return panel;
        } else if (node.getUserObject() instanceof HashMap) {
          JPanel panel = new JPanel();
          panel.setLayout(new BorderLayout());
          String group = (String) ((HashMap<String, String>) node
              .getUserObject()).keySet().iterator().next();
          JLabel nameLabel = new JLabel(group + " : ");
          nameLabel.setForeground(Color.blue);
          nameLabel.setBackground(Color.white);
          JLabel valueLabel = new JLabel(((HashMap<String, String>) node
              .getUserObject()).get(group));
          valueLabel.setForeground(Color.darkGray);
          valueLabel.setBackground(Color.white);
          panel.add(nameLabel, BorderLayout.WEST);
          panel.add(valueLabel, BorderLayout.EAST);
          panel.setBackground(selected ? Color.lightGray : Color.white);
          return panel;
        } else {
          return new JLabel();
        }
      }

    });
    tree.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3
            && DefaultTreeView.this.tree.getSelectionPath() != null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) DefaultTreeView.this.tree
              .getSelectionPath().getLastPathComponent();
          if (node.getUserObject() instanceof String
              && !(node.getUserObject().equals("pre-conditions") || node
                  .getUserObject().equals("post-conditions")))
            return;
          orderSubMenu.setEnabled(node.getUserObject() instanceof ModelGraph
              && !((ModelGraph) node.getUserObject()).isCondition()
              && ((ModelGraph) node.getUserObject()).getParent() != null);
          DefaultTreeView.this.actionsMenu.show(DefaultTreeView.this.tree,
              e.getX(), e.getY());
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
    this.scrollPane = new JScrollPane(tree,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    this.add(this.scrollPane, BorderLayout.CENTER);
    if (visibleRect != null)
      this.tree.scrollRectToVisible(visibleRect);

    this.revalidate();
  }

  private DefaultMutableTreeNode buildTree(ModelGraph graph, ViewState state) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(graph);
    DefaultMutableTreeNode metadataNode = new DefaultMutableTreeNode(
        "static-metadata");
    Metadata staticMetadata = new Metadata();
    if (graph.getInheritedStaticMetadata(state) != null)
      staticMetadata.replaceMetadata(graph.getInheritedStaticMetadata(state));
    if (graph.getModel().getStaticMetadata() != null)
      staticMetadata.replaceMetadata(graph.getModel().getStaticMetadata());
    this.addMetadataNodes(metadataNode, staticMetadata);
    if (!metadataNode.isLeaf())
      node.add(metadataNode);

    if (graph.getPreConditions() != null) {
      DefaultMutableTreeNode preConditions = new DefaultMutableTreeNode(
          "pre-conditions");
      List<ModelGraph> leafNodes = graph.getPreConditions().getLeafNodes();
      for (ModelGraph cond : leafNodes) {
        DefaultMutableTreeNode condNode = new DefaultMutableTreeNode(cond);
        preConditions.add(condNode);
      }
      node.add(preConditions);
    }
    if (graph.getPostConditions() != null) {
      DefaultMutableTreeNode postConditions = new DefaultMutableTreeNode(
          "post-conditions");
      List<ModelGraph> leafNodes = graph.getPostConditions().getLeafNodes();
      for (ModelGraph cond : leafNodes) {
        DefaultMutableTreeNode condNode = new DefaultMutableTreeNode(cond);
        postConditions.add(condNode);
      }
      node.add(postConditions);
    }
    for (ModelGraph child : graph.getChildren())
      if (!GuiUtils.isDummyNode(child.getModel()))
        node.add(this.buildTree(child, state));
    return node;
  }

  private void addMetadataNodes(DefaultMutableTreeNode metadataNode,
      Metadata staticMetadata) {
    for (String group : staticMetadata.getGroups()) {
      Object userObj = null;
      if (staticMetadata.getMetadata(group) != null) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(group,
            StringUtils.join(staticMetadata.getAllMetadata(group), ","));
        userObj = map;
      } else {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(group, null);
        userObj = map;
      }
      DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(userObj);
      metadataNode.add(groupNode);
      this.addMetadataNodes(groupNode, staticMetadata.getSubMetadata(group));
    }
  }

  private PopupMenu createPopupMenu(final ViewState state) {
    final String ACTIONS_POP_MENU_NAME = "Actions";
    final String VIEW_CONDITION_MAP = "View...";
    PopupMenu actionsMenu = new PopupMenu(ACTIONS_POP_MENU_NAME);
    actionsMenu.add(new MenuItem(VIEW_CONDITION_MAP));
    actionsMenu.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(VIEW_CONDITION_MAP)) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
              .getSelectionPath().getLastPathComponent();
          ModelGraph graphToFocus = null;
          if (Boolean.parseBoolean(state
              .getFirstPropertyValue(EXPAND_PRECONDITIONS))
              || Boolean.parseBoolean(state
                  .getFirstPropertyValue(EXPAND_POSTCONDITIONS))) {
            // if (node.getUserObject() instanceof String &&
            // (node.getUserObject().equals("pre-conditions") ||
            // node.getUserObject().equals("post-conditions"))) {
            ModelGraph graph = state.getSelected();
            if (Boolean.parseBoolean(state
                .getFirstPropertyValue(EXPAND_PRECONDITIONS)))
              graphToFocus = graph.getPreConditions();
            else
              graphToFocus = graph.getPostConditions();
          } else if (node.getUserObject() instanceof ModelGraph) {
            graphToFocus = (ModelGraph) node.getUserObject();
          }
          DefaultTreeView.this.notifyListeners(new ViewChange.NEW_VIEW(
              graphToFocus, DefaultTreeView.this));
        }
      }

    });

    final String ORDER_SUB_POP_MENU_NAME = "Order";
    final String TO_FRONT_ITEM_NAME = "Move To Front";
    final String TO_BACK_ITEM_NAME = "Move To Back";
    final String FORWARD_ITEM_NAME = "Move Forward";
    final String BACKWARDS_ITEM_NAME = "Move Backwards";
    actionsMenu.add(orderSubMenu = new PopupMenu(ORDER_SUB_POP_MENU_NAME));
    orderSubMenu.add(new MenuItem(TO_FRONT_ITEM_NAME));
    orderSubMenu.add(new MenuItem(TO_BACK_ITEM_NAME));
    orderSubMenu.add(new MenuItem(FORWARD_ITEM_NAME));
    orderSubMenu.add(new MenuItem(BACKWARDS_ITEM_NAME));
    orderSubMenu.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        ModelGraph graph = state.getSelected();
        ModelGraph parent = graph.getParent();
        if (e.getActionCommand().equals(TO_FRONT_ITEM_NAME)) {
          if (parent.getChildren().remove(graph))
            parent.getChildren().add(0, graph);
        } else if (e.getActionCommand().equals(TO_BACK_ITEM_NAME)) {
          if (parent.getChildren().remove(graph))
            parent.getChildren().add(graph);
        } else if (e.getActionCommand().equals(FORWARD_ITEM_NAME)) {
          int index = parent.getChildren().indexOf(graph);
          if (index != -1) {
            parent.getChildren().remove(index);
            parent.getChildren().add(Math.max(0, index + 1), graph);
          }
        } else if (e.getActionCommand().equals(BACKWARDS_ITEM_NAME)) {
          int index = parent.getChildren().indexOf(graph);
          if (index != -1) {
            parent.getChildren().remove(index);
            parent.getChildren().add(Math.max(0, index - 1), graph);
          }
        }
        DefaultTreeView.this.notifyListeners();
        DefaultTreeView.this.refreshView(state);
      }

    });
    return actionsMenu;
  }

}
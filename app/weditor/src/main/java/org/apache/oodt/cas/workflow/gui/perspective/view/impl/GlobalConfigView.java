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
import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.workflow.gui.model.repo.XmlWorkflowModelRepository.ConfigGroup;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;

/**
 * 
 * Displays information about global config properties loaded from the
 * Workflows.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class GlobalConfigView extends View {

  private static final long serialVersionUID = 3899104909278232407L;
  private JTree tree;
  private JTabbedPane tabbedPane;
  private Map<String, ConfigGroup> globalConfig;

  public GlobalConfigView(String name) {
    super(name);
    this.setLayout(new BorderLayout());
  }

  @Override
  public void refreshView(ViewState state) {

    Rectangle visibleRect = null;
    if (this.tree != null)
      visibleRect = this.tree.getVisibleRect();

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("GlobalConfig");

    if (state != null && state.getGlobalConfigGroups() != null) {
      if (globalConfig != null
          && globalConfig.keySet().equals(
              state.getGlobalConfigGroups().keySet())
          && globalConfig.values().equals(
              state.getGlobalConfigGroups().values()))
        return;

      this.removeAll();

      for (ConfigGroup group : (globalConfig = state.getGlobalConfigGroups())
          .values()) {
        HashSet<String> keys = new HashSet<String>();
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
            new Group(group.getName()));
        root.add(groupNode);
        for (String key : group.getMetadata().getAllKeys()) {
          keys.add(key);
          DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(new Key(
              key));
          groupNode.add(keyNode);
          DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(
              new Value(StringUtils.join(group.getMetadata()
                  .getAllMetadata(key), ",")));
          keyNode.add(valueNode);
        }
        if (group.getExtends() != null) {
          List<String> extendsGroups = new Vector<String>(group.getExtends());
          Collections.reverse(extendsGroups);
          for (String extendsGroup : extendsGroups) {
            List<String> groupKeys = state.getGlobalConfigGroups()
                .get(extendsGroup).getMetadata().getAllKeys();
            groupKeys.removeAll(keys);
            if (groupKeys.size() > 0) {
              for (String key : groupKeys) {
                if (!keys.contains(key)) {
                  keys.add(key);
                  DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(
                      new ExtendsKey(extendsGroup, key));
                  groupNode.add(keyNode);
                  DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(
                      new ExtendsValue(StringUtils.join(state
                          .getGlobalConfigGroups().get(extendsGroup)
                          .getMetadata().getAllMetadata(key), ",")));
                  keyNode.add(valueNode);
                }
              }
            }
          }
        }
      }

      tree = new JTree(root);
      tree.setShowsRootHandles(true);
      tree.setRootVisible(false);

      tree.setCellRenderer(new TreeCellRenderer() {

        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
          if (node.getUserObject() instanceof Key) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel label = new JLabel(node.getUserObject().toString());
            label.setForeground(Color.darkGray);
            panel.add(label, BorderLayout.CENTER);
            panel.setBackground(selected ? Color.lightGray : Color.white);
            return panel;
          } else if (node.getUserObject() instanceof ExtendsKey) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            ExtendsKey key = (ExtendsKey) node.getUserObject();
            JLabel groupLabel = new JLabel("(" + key.getGroup() + ") ");
            groupLabel.setForeground(Color.black);
            JLabel keyLabel = new JLabel(key.getValue());
            keyLabel.setForeground(Color.gray);
            panel.add(groupLabel, BorderLayout.WEST);
            panel.add(keyLabel, BorderLayout.CENTER);
            panel.setBackground(selected ? Color.lightGray : Color.white);
            return panel;
          } else if (node.getUserObject() instanceof Group) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            JLabel label = new JLabel(node.getUserObject().toString());
            label.setForeground(Color.black);
            label.setBackground(Color.white);
            panel.add(label, BorderLayout.CENTER);
            panel.setBackground(selected ? Color.lightGray : Color.white);
            return panel;
          } else if (node.getUserObject() instanceof Value) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EtchedBorder(1));
            JLabel label = new JLabel(node.getUserObject().toString());
            label.setForeground(Color.black);
            panel.add(label, BorderLayout.CENTER);
            panel.setBackground(selected ? Color.lightGray : Color.white);
            return panel;
          } else if (node.getUserObject() instanceof ExtendsValue) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EtchedBorder(1));
            JLabel label = new JLabel(node.getUserObject().toString());
            label.setForeground(Color.gray);
            panel.add(label, BorderLayout.CENTER);
            panel.setBackground(selected ? Color.lightGray : Color.white);
            return panel;
          } else {
            return new JLabel();
          }
        }

      });
    }

    this.setBorder(new EtchedBorder());
    JLabel panelName = new JLabel("Global-Config Groups");
    panelName.setBorder(new EtchedBorder());
    this.add(panelName, BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(tree,
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Tree", scrollPane);
    tabbedPane.addTab("Table", new JPanel());

    this.add(tabbedPane, BorderLayout.CENTER);

    if (visibleRect != null)
      this.tree.scrollRectToVisible(visibleRect);

    this.revalidate();
  }

  public class StringNode {
    private String value;

    public StringNode(String value) {
      this.value = value;
    }

    public String getValue() {
      return this.value;
    }

    public String toString() {
      return this.value;
    }
  }

  public class Key extends StringNode {
    public Key(String value) {
      super(value);
    }
  }

  public class ExtendsKey extends StringNode {
    private String group;

    public ExtendsKey(String group, String value) {
      super(value);
      this.group = group;
    }

    public String getGroup() {
      return this.group;
    }
  }

  public class ExtendsValue extends StringNode {
    public ExtendsValue(String value) {
      super(value);
    }
  }

  public class Value extends StringNode {
    public Value(String value) {
      super(value);
    }
  }

  public class Group extends StringNode {
    public Group(String group) {
      super(group);
    }
  }

}

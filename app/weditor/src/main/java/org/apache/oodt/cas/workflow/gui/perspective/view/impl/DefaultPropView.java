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

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.gui.model.ModelGraph;
import org.apache.oodt.cas.workflow.gui.perspective.view.View;
import org.apache.oodt.cas.workflow.gui.perspective.view.ViewState;
import org.apache.oodt.cas.workflow.gui.util.GuiUtils;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;


/**
 * 
 * 
 * The default view displaying a workflow property (for a task, or a condition,
 * or set of workflows).
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public class DefaultPropView extends View {

  private static final long serialVersionUID = -5521047300551974898L;

  private JTable table;
  private JPopupMenu tableMenu;
  private JMenuItem override;
  private JMenuItem delete;
  private static final String OVERRIDE = "Override";
  private static final String DELETE = "Delete";
  private int DEFAULT_PRIORITY = 5;

  public DefaultPropView(String name) {
    super(name);
    this.setLayout(new BorderLayout());
  }

  private JTable createTable(final ViewState state) {
    JTable table;
    final ModelGraph selected = state.getSelected();
    if (selected != null) {
      final Vector<Vector<String>> rows = new Vector<Vector<String>>();
      ConcurrentHashMap<String, String> keyToGroupMap = new ConcurrentHashMap<String, String>();
      Metadata staticMet = selected.getModel().getStaticMetadata();
      Metadata inheritedMet = selected.getInheritedStaticMetadata(state);
      Metadata completeMet = new Metadata();
      if (staticMet != null) {
        completeMet.replaceMetadata(staticMet.getSubMetadata(state
            .getCurrentMetGroup()));
      }
      if (selected.getModel().getExtendsConfig() != null) {
        for (String configGroup : selected.getModel().getExtendsConfig()) {
          Metadata extendsMetadata = state.getGlobalConfigGroups()
              .get(configGroup).getMetadata()
              .getSubMetadata(state.getCurrentMetGroup());
          for (String key : extendsMetadata.getAllKeys()) {
            if (!completeMet.containsKey(key)) {
              keyToGroupMap.put(key, configGroup);
              completeMet.replaceMetadata(key,
                  extendsMetadata.getAllMetadata(key));
            }
          }
        }
      }
      if (inheritedMet != null) {
        Metadata inheritedMetadata = inheritedMet.getSubMetadata(state
            .getCurrentMetGroup());
        for (String key : inheritedMetadata.getAllKeys()) {
          if (!completeMet.containsKey(key)) {
            keyToGroupMap.put(key, "__inherited__");
            completeMet.replaceMetadata(key,
                inheritedMetadata.getAllMetadata(key));
          }
        }
      }
      List<String> keys = completeMet.getAllKeys();
      Collections.sort(keys);
      for (String key : keys) {
        if (key.endsWith("/envReplace")) {
          continue;
        }
        String values = StringUtils.join(completeMet.getAllMetadata(key), ",");
        Vector<String> row = new Vector<String>();
        row.add(keyToGroupMap.get(key));
        row.add(key);
        row.add(values);
        row.add(Boolean.toString(Boolean.parseBoolean(completeMet
            .getMetadata(key + "/envReplace"))));
        rows.add(row);
      }
      table = new JTable();
      table.setModel(new AbstractTableModel() {
        public String getColumnName(int col) {
          switch (col) {
          case 0:
            return "group";
          case 1:
            return "key";
          case 2:
            return "values";
          case 3:
            return "envReplace";
          default:
            return null;
          }
        }

        public int getRowCount() {
          return rows.size() + 1;
        }

        public int getColumnCount() {
          return 4;
        }

        public Object getValueAt(int row, int col) {
          if (row >= rows.size()) {
            return null;
          }
          String value = rows.get(row).get(col);
          if (value == null && col == 3) {
            return "false";
          }
          if (value == null && col == 0) {
            return "__local__";
          }
          return value;
        }

        public boolean isCellEditable(int row, int col) {
          if (row >= rows.size()) {
            return selected.getModel().getStaticMetadata()
                           .containsGroup(state.getCurrentMetGroup());
          }
          if (col == 0) {
            return false;
          }
          String key = rows.get(row).get(1);
          return key == null
                 || (selected.getModel().getStaticMetadata() != null && selected
              .getModel().getStaticMetadata()
              .containsKey(getKey(key, state)));
        }

        public void setValueAt(Object value, int row, int col) {
          if (row >= rows.size()) {
            Vector<String> newRow = new Vector<String>(Arrays
                .asList(new String[] { null, null, null, null }));
            newRow.add(col, (String) value);
            rows.add(newRow);
          } else {
            Vector<String> rowValues = rows.get(row);
            rowValues.add(col, (String) value);
            rowValues.remove(col + 1);
          }
          this.fireTableCellUpdated(row, col);
        }

      });
      MyTableListener tableListener = new MyTableListener(state);
      table.getModel().addTableModelListener(tableListener);
      table.getSelectionModel().addListSelectionListener(tableListener);
    } else {
      table = new JTable(new Vector<Vector<String>>(), new Vector<String>(
          Arrays.asList(new String[] { "key", "values", "envReplace" })));
    }

    table.setSelectionBackground(Color.cyan);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableCellRenderer cellRenderer = new TableCellRenderer() {

      public Component getTableCellRendererComponent(JTable table,
          Object value, boolean isSelected, boolean hasFocus, int row,
          int column) {
        JLabel field = new JLabel((String) value);
        if (column == 0) {
          field.setForeground(Color.gray);
        } else {
          if (isSelected) {
            field.setBorder(new EtchedBorder(1));
          }
          if (table.isCellEditable(row, 1)) {
            field.setForeground(Color.black);
          } else {
            field.setForeground(Color.gray);
          }
        }
        return field;
      }

    };
    TableColumn groupCol = table.getColumnModel().getColumn(0);
    groupCol.setPreferredWidth(75);
    groupCol.setCellRenderer(cellRenderer);
    TableColumn keyCol = table.getColumnModel().getColumn(1);
    keyCol.setPreferredWidth(200);
    keyCol.setCellRenderer(cellRenderer);
    TableColumn valuesCol = table.getColumnModel().getColumn(2);
    valuesCol.setPreferredWidth(300);
    valuesCol.setCellRenderer(cellRenderer);
    TableColumn envReplaceCol = table.getColumnModel().getColumn(3);
    envReplaceCol.setPreferredWidth(75);
    envReplaceCol.setCellRenderer(cellRenderer);

    table.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3
            && DefaultPropView.this.table.getSelectedRow() != -1) {
          int row = DefaultPropView.this.table.getSelectedRow();
          String key = getKey(
              (String) DefaultPropView.this.table.getValueAt(row, 1), state);
          Metadata staticMet = state.getSelected().getModel()
              .getStaticMetadata();
          override.setVisible(staticMet == null || !staticMet.containsKey(key));
          delete.setVisible(staticMet != null && staticMet.containsKey(key));
          tableMenu.show(DefaultPropView.this.table, e.getX(), e.getY());
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

    return table;
  }

  @Override
  public void refreshView(final ViewState state) {
    this.removeAll();

    tableMenu = new JPopupMenu("TableMenu");
    this.add(tableMenu);
    override = new JMenuItem(OVERRIDE);
    override.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int row = DefaultPropView.this.table.getSelectedRow();
        String key = getKey(
            (String) DefaultPropView.this.table.getValueAt(row, 1), state);
        Metadata staticMet = state.getSelected().getModel().getStaticMetadata();
        if (staticMet == null) {
          staticMet = new Metadata();
        }
        if (e.getActionCommand().equals(OVERRIDE)) {
          if (!staticMet.containsKey(key)) {
            staticMet.addMetadata(key,
                (String) DefaultPropView.this.table.getValueAt(row, 2));
            String envReplace = (String) DefaultPropView.this.table.getValueAt(
                row, 3);
            if (Boolean.valueOf(envReplace)) {
              staticMet.addMetadata(key + "/envReplace", envReplace);
            }
            state.getSelected().getModel().setStaticMetadata(staticMet);
            DefaultPropView.this.notifyListeners();
          }
        }
      }
    });
    delete = new JMenuItem(DELETE);
    delete.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        int row = DefaultPropView.this.table.getSelectedRow();
        String key = getKey(
            (String) DefaultPropView.this.table.getValueAt(row, 1), state);
        Metadata staticMet = state.getSelected().getModel().getStaticMetadata();
        if (staticMet == null) {
          staticMet = new Metadata();
        }
        staticMet.removeMetadata(key);
        staticMet.removeMetadata(key + "/envReplace");
        state.getSelected().getModel().setStaticMetadata(staticMet);
        DefaultPropView.this.notifyListeners();
      }

    });
    tableMenu.add(override);
    tableMenu.add(delete);

    if (state.getSelected() != null) {
      JPanel masterPanel = new JPanel();
      masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
      masterPanel.add(this.getModelIdPanel(state.getSelected(), state));
      masterPanel.add(this.getModelNamePanel(state.getSelected(), state));
      if (!state.getSelected().getModel().isParentType()) {
        masterPanel.add(this.getInstanceClassPanel(state.getSelected(), state));
      }
      masterPanel.add(this.getExecutionTypePanel(state.getSelected(), state));
      masterPanel.add(this.getPriorityPanel(state));
      masterPanel.add(this.getExecusedIds(state.getSelected()));
      if (state.getSelected().getModel().getExecutionType().equals("condition")) {
        masterPanel.add(this.getTimeout(state.getSelected(), state));
        masterPanel.add(this.getOptional(state.getSelected(), state));
      }
      JScrollPane scrollPane = new JScrollPane(table = this.createTable(state),
          JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
          JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
      scrollPane.getVerticalScrollBar().setUnitIncrement(10);
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.setBorder(new EtchedBorder());
      final JLabel metLabel = new JLabel("Static Metadata");
      metLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      final JLabel extendsLabel = new JLabel("<extends>");
      extendsLabel.setFont(new Font("Serif", Font.PLAIN, 10));
      extendsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      extendsLabel.addMouseListener(new MouseListener() {

        private JScrollPane availableScroller;
        private JScrollPane mineScroller;
        private JList mineList;
        private JList availableList;
        private DefaultListModel mineModel;
        private DefaultListModel availableModel;

        public void mouseClicked(MouseEvent e) {
          final JPopupMenu popup = new JPopupMenu();
          popup.setLayout(new BorderLayout());

          JPanel main = new JPanel();
          main.setLayout(new BoxLayout(main, BoxLayout.X_AXIS));

          JPanel mine = new JPanel();
          mine.setBorder(new EtchedBorder());
          mine.setLayout(new BorderLayout());
          JLabel mineLabel = new JLabel("Mine");
          mineScroller = new JScrollPane(mineList = createJList(
              mineModel = new DefaultListModel(), state.getSelected()
                  .getModel().getExtendsConfig()));
          mineScroller.setPreferredSize(new Dimension(250, 80));
          mine.add(mineLabel, BorderLayout.NORTH);
          mine.add(mineScroller, BorderLayout.CENTER);

          JPanel available = new JPanel();
          available.setBorder(new EtchedBorder());
          available.setLayout(new BorderLayout());
          JLabel availableLabel = new JLabel("Available");
          Vector<String> availableGroups = new Vector<String>(state
              .getGlobalConfigGroups().keySet());
          availableGroups.removeAll(state.getSelected().getModel()
              .getExtendsConfig());
          availableScroller = new JScrollPane(availableList = this.createJList(
              availableModel = new DefaultListModel(), availableGroups));
          availableScroller.setPreferredSize(new Dimension(250, 80));
          available.add(availableLabel, BorderLayout.NORTH);
          available.add(availableScroller, BorderLayout.CENTER);

          JPanel buttons = new JPanel();
          buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
          JButton addButton = new JButton("<---");
          addButton.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
              String selected = availableList.getSelectedValue().toString();
              Vector<String> extendsConfig = new Vector<String>(state
                  .getSelected().getModel().getExtendsConfig());
              extendsConfig.add(selected);
              state.getSelected().getModel().setExtendsConfig(extendsConfig);
              availableModel.remove(availableList.getSelectedIndex());
              mineModel.addElement(selected);
              popup.revalidate();
              DefaultPropView.this.notifyListeners();
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
          JButton removeButton = new JButton("--->");
          removeButton.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
              String selected = mineList.getSelectedValue().toString();
              Vector<String> extendsConfig = new Vector<String>(state
                  .getSelected().getModel().getExtendsConfig());
              extendsConfig.remove(selected);
              state.getSelected().getModel().setExtendsConfig(extendsConfig);
              mineModel.remove(mineList.getSelectedIndex());
              availableModel.addElement(selected);
              popup.revalidate();
              DefaultPropView.this.notifyListeners();
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
          buttons.add(addButton);
          buttons.add(removeButton);

          main.add(mine);
          main.add(buttons);
          main.add(available);
          popup.add(main, BorderLayout.CENTER);
          popup.show(extendsLabel, e.getX(), e.getY());
        }

        public void mouseEntered(MouseEvent e) {
          extendsLabel.setForeground(Color.blue);
        }

        public void mouseExited(MouseEvent e) {
          extendsLabel.setForeground(Color.black);
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        private JList createJList(DefaultListModel model,
            final List<String> list) {
          for (String value : list) {
            model.addElement(value);
          }
          JList jList = new JList(model);
          jList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
          jList.setLayoutOrientation(JList.VERTICAL);
          return jList;
        }
      });
      JLabel metGroupLabel = new JLabel("(Sub-Group: "
          + (state.getCurrentMetGroup() != null ? state.getCurrentMetGroup()
              : "<base>") + ")");
      metGroupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      JPanel labelPanel = new JPanel();
      labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
      JPanel top = new JPanel();
      top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
      top.add(extendsLabel);
      top.add(metLabel);
      labelPanel.add(top);
      labelPanel.add(metGroupLabel);
      panel.add(labelPanel, BorderLayout.NORTH);
      panel.add(scrollPane, BorderLayout.CENTER);
      masterPanel.add(panel);
      this.add(masterPanel);
    } else {
      this.add(new JPanel());
    }
    this.revalidate();
  }

  private JPanel getTimeout(final ModelGraph graph, final ViewState state) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("Timeout:"), BorderLayout.NORTH);
    JTextField field = new JTextField(String.valueOf(graph.getModel()
        .getTimeout()), 50);
    field.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!graph.getModel().getModelId().equals(e.getActionCommand())) {
          graph.getModel().setTimeout(Long.valueOf(
              e.getActionCommand() != null && 
              !e.getActionCommand().equals("") ? 
                  e.getActionCommand():"-1"));
          DefaultPropView.this.notifyListeners();
          DefaultPropView.this.refreshView(state);
        }
      }

    });
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel getModelIdPanel(final ModelGraph graph, final ViewState state) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("ModelId:"), BorderLayout.NORTH);
    JTextField field = new JTextField(graph.getModel().getModelId(), 50);
    field.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!graph.getModel().getModelId().equals(e.getActionCommand())) {
          GuiUtils.updateGraphModelId(state, graph.getModel().getId(),
              e.getActionCommand());
          DefaultPropView.this.notifyListeners();
          DefaultPropView.this.refreshView(state);
        }
      }

    });
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel getModelNamePanel(final ModelGraph graph, final ViewState state) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("ModelName:"), BorderLayout.NORTH);
    JTextField field = new JTextField(graph.getModel().getModelName(), 50);
    field.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!graph.getModel().getModelName().equals(e.getActionCommand())) {
          graph.getModel().setModelName(e.getActionCommand());
          DefaultPropView.this.notifyListeners();
          DefaultPropView.this.refreshView(state);
        }
      }

    });
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel getInstanceClassPanel(final ModelGraph graph,
      final ViewState state) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("InstanceClass:"), BorderLayout.NORTH);
    JTextField field = new JTextField(graph.getModel().getInstanceClass(), 50);
    field.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (graph.getModel().getInstanceClass() == null
            || !graph.getModel().getInstanceClass()
                .equals(e.getActionCommand())) {
          graph.getModel().setInstanceClass(e.getActionCommand());
          DefaultPropView.this.notifyListeners();
          DefaultPropView.this.refreshView(state);
        }
      }

    });
    panel.add(field, BorderLayout.CENTER);
    return panel;
  }

  private JPanel getPriorityPanel(final ViewState state) {
    JPanel panel = new JPanel();
    panel.setBorder(new EtchedBorder());
    panel.setLayout(new BorderLayout());
    panel.add(new JLabel("Priority:  "), BorderLayout.WEST);
    final JLabel priorityLabel = new JLabel(String.valueOf(DEFAULT_PRIORITY));
    panel.add(priorityLabel, BorderLayout.CENTER);
    JSlider slider = new JSlider(0, 100, (int) 5 * 10);
    slider.setMajorTickSpacing(10);
    slider.setMinorTickSpacing(1);
    slider.setPaintLabels(true);
    slider.setPaintTicks(true);
    slider.setSnapToTicks(false);
    Format f = new DecimalFormat("0.0");
    Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
    for (int i = 0; i <= 10; i += 2) {
      JLabel label = new JLabel(f.format(i));
      label.setFont(label.getFont().deriveFont(Font.PLAIN));
      labels.put(i * 10, label);
    }
    slider.setLabelTable(labels);
    slider.addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {
        double value = ((JSlider) e.getSource()).getValue() / 10.0;
        priorityLabel.setText(value + "");
        priorityLabel.revalidate();
        if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
          // FIXME: deal with priorities
          DefaultPropView.this.notifyListeners();
        }
      }

    });

    panel.add(slider, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel getExecutionTypePanel(final ModelGraph graph,
      final ViewState state) {
    JPanel panel = new JPanel();
    panel.setBorder(new EtchedBorder());
    panel.setLayout(new BorderLayout());
    panel.add(new JLabel("ExecutionType:"), BorderLayout.WEST);
    JComboBox comboBox = new JComboBox();
    if (graph.hasChildren()) {
      comboBox.addItem("parallel");
      comboBox.addItem("sequential");
    } else if (graph.getModel().getExecutionType().equals("task")) {
      comboBox.addItem("parallel");
      comboBox.addItem("sequential");
      comboBox.addItem("task");
    } else if (graph.isCondition()
        || graph.getModel().getExecutionType().equals("condition")) {
      comboBox.addItem("parallel");
      comboBox.addItem("sequential");
      comboBox.addItem("condition");
    } else {
      comboBox.addItem("parallel");
      comboBox.addItem("sequential");
      comboBox.addItem("task");
    }
    comboBox.setSelectedItem(graph.getModel().getExecutionType());
    comboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (!graph.getModel().getExecutionType().equals(e.getItem())) {
          graph.getModel().setExecutionType((String) e.getItem());
          DefaultPropView.this.notifyListeners();
          DefaultPropView.this.refreshView(state);
        }
      }
    });
    panel.add(comboBox, BorderLayout.CENTER);
    return panel;
  }

  private JPanel getOptional(final ModelGraph graph, final ViewState state) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("Optional:"), BorderLayout.NORTH);
    JPanel checkBoxes = new JPanel();
    checkBoxes.setLayout(new GridLayout(1, 1));
    Checkbox checkbox = new Checkbox("ignore", graph.getModel().isOptional());
    checkBoxes.add(checkbox);
        checkbox.addItemListener(new ItemListener() {

          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
              graph.getModel().setOptional(false);
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
              graph.getModel().setOptional(true);
            } else {
              return;
            }
            DefaultPropView.this.notifyListeners();
            DefaultPropView.this.refreshView(state);
          }

        });
    panel.add(checkBoxes, BorderLayout.CENTER);
    return panel;
  }  
  
  
  private JPanel getExecusedIds(final ModelGraph graph) {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(new EtchedBorder());
    panel.add(new JLabel("ExcusedSubProcessorIds:"), BorderLayout.NORTH);
    JPanel checkBoxes = new JPanel();
    checkBoxes.setLayout(new GridLayout(graph.getChildren().size(), 1));
    if (graph.hasChildren()) {
      for (ModelGraph childGraph : graph.getChildren()) {
        final String modelId = childGraph.getModel().getModelId();
        Checkbox checkbox = new Checkbox(modelId, graph.getModel()
            .getExcusedSubProcessorIds().contains(modelId));
        checkBoxes.add(checkbox);
        checkbox.addItemListener(new ItemListener() {

          public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
              graph.getModel().getExcusedSubProcessorIds().remove(modelId);
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
              graph.getModel().getExcusedSubProcessorIds().add(modelId);
            } else {
              return;
            }
            DefaultPropView.this.notifyListeners();
          }

        });
      }
    }
    panel.add(checkBoxes, BorderLayout.CENTER);
    return panel;
  }

  public class MyTableListener implements TableModelListener,
      ListSelectionListener {

    String oldKey, oldValue, oldEnvReplace;

    private ViewState state;

    public MyTableListener(ViewState state) {
      this.state = state;
    }

    public void tableChanged(TableModelEvent e) {
      System.out.println(oldKey + " " + oldValue + " " + oldEnvReplace);
      if (e.getType() == TableModelEvent.UPDATE) {
        Metadata staticMet = state.getSelected().getModel().getStaticMetadata();
        if (staticMet == null) {
          staticMet = new Metadata();
        }
        if (e.getColumn() == 1) {
          String newGrouplessKey = (String) table.getValueAt(e.getFirstRow(),
              e.getColumn());
          if (newGrouplessKey.equals("")
              || (newGrouplessKey.equals("envReplace") && !state.getSelected()
                  .getModel().getStaticMetadata()
                  .containsGroup(state.getCurrentMetGroup()))) {
            notifyListeners();
            return;
          }
          String newKey = getKey(newGrouplessKey, state);
          System.out.println("newKey: " + newKey);
          if (oldKey != null) {
            staticMet.replaceMetadata(newKey, staticMet.getAllMetadata(oldKey));
            if (staticMet.containsKey(oldKey + "/envReplace")) {
              staticMet.replaceMetadata(newKey,
                  staticMet.getAllMetadata(oldKey + "/envReplace"));
            }
            if (!newKey.equals(oldKey)) {
              staticMet.removeMetadata(oldKey);
            }
            notifyListeners();
          } else {
            staticMet.replaceMetadata(oldKey = newKey, (String) null);
          }
        } else if (e.getColumn() == 2) {
          if (oldKey != null) {
            String newValue = (String) table.getValueAt(e.getFirstRow(),
                e.getColumn());
            if (oldKey.endsWith("/envReplace")) {
              newValue = newValue.toLowerCase();
              if (newValue.equals("false")) {
                staticMet.removeMetadata(oldKey);
              } else {
                staticMet.replaceMetadata(oldKey,
                    Arrays.asList(newValue.split(",")));
              }
            } else {
              staticMet.replaceMetadata(oldKey,
                  Arrays.asList(newValue.split(",")));
            }
            notifyListeners();
          }
        } else if (e.getColumn() == 3) {
          if (oldKey != null) {
            String newEnvReplace = ((String) table.getValueAt(e.getFirstRow(),
                e.getColumn())).toLowerCase();
            if (newEnvReplace.equals("true")) {
              staticMet.replaceMetadata(oldKey + "/envReplace", newEnvReplace);
            } else {
              staticMet.removeMetadata(oldKey + "/envReplace");
            }
            notifyListeners();
          }
        }
        state.getSelected().getModel().setStaticMetadata(staticMet);
      }

    }

    public void valueChanged(ListSelectionEvent e) {
      oldKey = getKey((String) table.getValueAt(e.getFirstIndex(), 1), state);
      oldValue = (String) table.getValueAt(e.getFirstIndex(), 2);
      oldEnvReplace = (String) table.getValueAt(e.getFirstIndex(), 3);
    }

  }

  private String getKey(String key, ViewState state) {
    if (key != null && state.getCurrentMetGroup() != null) {
      return state.getCurrentMetGroup() + "/" + key;
    } else {
      return key;
    }
  }

}

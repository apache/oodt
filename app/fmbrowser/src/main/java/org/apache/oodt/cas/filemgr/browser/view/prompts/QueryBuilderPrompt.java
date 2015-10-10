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

package org.apache.oodt.cas.filemgr.browser.view.prompts;

import org.apache.oodt.cas.filemgr.browser.controller.WindowListener;
import org.apache.oodt.cas.filemgr.browser.model.CasDB;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class QueryBuilderPrompt extends JFrame {

  protected CasDB database;
  protected WindowListener listener;

  protected TypePanel tPanel;
  protected QuerySelectionPanel qPanel;
  protected JScrollPane scrollPane;
  protected BuiltQueryPane builtPanel;
  protected SearchPanel sPanel;

  public QueryBuilderPrompt(CasDB db, WindowListener l) {

    database = db;
    listener = l;

    this.setName("Query Builder");
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    Dimension d = new Dimension(500, 400);
    this.setMinimumSize(d);
    this.setMaximumSize(d);
    this.setPreferredSize(d);

    this.getContentPane().setLayout(
        new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    this.getContentPane().setBackground(Color.WHITE);

    tPanel = new TypePanel(this);
    qPanel = new QuerySelectionPanel(this);
    sPanel = new SearchPanel(this);
    builtPanel = new BuiltQueryPane(this);

    scrollPane = new JScrollPane(qPanel);
    Dimension scrollDim = new Dimension(500, 220);
    scrollPane.setMaximumSize(scrollDim);
    scrollPane.setMinimumSize(scrollDim);
    scrollPane.setPreferredSize(scrollDim);

    this.getContentPane().add(tPanel);
    this.getContentPane().add(scrollPane);
    this.getContentPane().add(builtPanel);
    this.getContentPane().add(sPanel);
  }

  public org.apache.oodt.cas.filemgr.structs.Query getQuery() {
    return qPanel.getCasQuery();
  }

  public String getQueryString() {
    return qPanel.getQuery();
  }

  public String getProductType() {
    return tPanel.getType();
  }

  private class QueryPanel extends JPanel {

    private JComboBox elements;
    private JComboBox ops;
    private JPanel placeholder;
    private boolean showOp;

    public QueryPanel(QueryBuilderPrompt prompt) {
      Dimension d = new Dimension(460, 35);
      this.setMaximumSize(d);
      this.setMinimumSize(d);
      this.setPreferredSize(d);
      this.setBackground(Color.WHITE);
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      showOp = false;
      ops = new JComboBox(new String[] { "AND", "OR", "NOT" });
      Dimension opsDim = new Dimension(75, 25);
      ops.setMaximumSize(opsDim);
      ops.setMinimumSize(opsDim);
      ops.setPreferredSize(opsDim);
      ops.setBackground(Color.WHITE);
      ops.setVisible(false);

      placeholder = new JPanel();
      placeholder.setBackground(Color.WHITE);
      placeholder.setMaximumSize(opsDim);
      placeholder.setMinimumSize(opsDim);
      placeholder.setPreferredSize(opsDim);

      elements = new JComboBox(prompt.database
          .getAvailableElements(prompt.tPanel.getType()));
      elements.setBackground(Color.WHITE);
      Dimension dElem = new Dimension(150, 25);
      elements.setMaximumSize(dElem);
      elements.setMinimumSize(dElem);
      elements.setPreferredSize(dElem);

      this.add(ops);
      this.add(placeholder);
      this.add(elements);
    }

    public void addOp() {
      showOp = true;
      ops.setVisible(true);
      placeholder.setVisible(false);
    }

    public String getElement() {
      return elements.getSelectedItem().toString();
    }

    public String getOp() {
      String op = "";
      if (showOp) {
        op = ops.getSelectedItem().toString();
      }
      return op;
    }
  }

  private class TermQueryPanel extends QueryPanel {

    private JTextField text;

    public TermQueryPanel(QueryBuilderPrompt prompt) {
      super(prompt);

      text = new JTextField();
      Dimension dText = new Dimension(150, 25);
      text.setPreferredSize(dText);
      text.setMaximumSize(dText);
      text.setMinimumSize(dText);
      text.setBackground(Color.WHITE);
      text.addFocusListener(prompt.builtPanel);
      text.addActionListener(prompt.builtPanel);

      this.add(new JLabel("  Matches  "));
      this.add(text);
    }

    public String getText() {
      return text.getText();
    }

  }

  private class RangeQueryPanel extends QueryPanel {

    private JTextField start;
    private JTextField stop;

    public RangeQueryPanel(QueryBuilderPrompt prompt) {
      super(prompt);

      start = new JTextField();
      stop = new JTextField();

      Dimension dText = new Dimension(60, 25);
      start.setMaximumSize(dText);
      start.setMinimumSize(dText);
      start.setPreferredSize(dText);
      start.addFocusListener(prompt.builtPanel);
      start.addActionListener(prompt.builtPanel);

      stop.setMaximumSize(dText);
      stop.setMinimumSize(dText);
      stop.setPreferredSize(dText);
      stop.addFocusListener(prompt.builtPanel);
      stop.addActionListener(prompt.builtPanel);

      this.add(new JLabel("  Between  "));
      this.add(start);
      this.add(new JLabel(" And "));
      this.add(stop);
    }

    public String getStart() {
      return start.getText();
    }

    public String getStop() {
      return stop.getText();
    }

  }

  private class QuerySelectionPanel extends JPanel implements ActionListener {

    private JButton addTerm;
    private JButton addRange;
    private QueryBuilderPrompt prompt;

    public QuerySelectionPanel(QueryBuilderPrompt prompt) {
      this.prompt = prompt;
      this.setBackground(Color.WHITE);

      EmptyBorder line1 = new EmptyBorder(2, 2, 2, 2);
      LineBorder line2 = new LineBorder(Color.BLACK, 1);
      CompoundBorder cp = new CompoundBorder(line1, line2);
      this.setBorder(cp);
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      JPanel buttonPanel = new JPanel();
      Dimension buttonDim = new Dimension(460, 30);
      buttonPanel.setMaximumSize(buttonDim);
      buttonPanel.setMinimumSize(buttonDim);
      buttonPanel.setPreferredSize(buttonDim);
      buttonPanel.setBackground(Color.WHITE);
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

      addTerm = new JButton("Add Term Criteria");
      addTerm.setBackground(Color.WHITE);
      addTerm.addActionListener(this);

      addRange = new JButton("Add Range Criteria");
      addRange.setBackground(Color.WHITE);
      addRange.addActionListener(this);

      buttonPanel.add(addTerm);
      buttonPanel.add(addRange);
      this.add(buttonPanel);
    }

    public void actionPerformed(ActionEvent arg0) {
      if (arg0.getActionCommand().equals("Add Term Criteria")) {
        TermQueryPanel tq = new TermQueryPanel(prompt);
        if (this.getComponentCount() > 1)
          tq.addOp();
        int insertOrder = this.getComponentCount() - 1;
        if (insertOrder < 0)
          insertOrder = 0;
        this.add(tq, insertOrder);
        this.validate();
        prompt.scrollPane.validate();
      } else if (arg0.getActionCommand().equals("Add Range Criteria")) {
        RangeQueryPanel rq = new RangeQueryPanel(prompt);
        if (this.getComponentCount() > 1)
          rq.addOp();
        int insertOrder = this.getComponentCount() - 1;
        if (insertOrder < 0)
          insertOrder = 0;
        this.add(rq, insertOrder);
        this.validate();
        prompt.scrollPane.validate();
      }
    }

    public String getQuery() {
      StringBuilder q = new StringBuilder();
      for (int i = 0; i < this.getComponentCount(); i++) {
        Component c = this.getComponent(i);
        if (c instanceof TermQueryPanel) {
          q.append(((TermQueryPanel) c).getOp()).append(" ");
          q.append(((TermQueryPanel) c).getElement()).append(":");
          q.append(((TermQueryPanel) c).getText()).append(" ");
        } else if (c instanceof RangeQueryPanel) {
          q.append(((RangeQueryPanel) c).getOp()).append(" ");
          q.append(((RangeQueryPanel) c).getElement()).append(":[");
          q.append(((RangeQueryPanel) c).getStart()).append(" TO ");
          q.append(((RangeQueryPanel) c).getStop()).append("] ");
        }
      }

      return q.toString();
    }

    public org.apache.oodt.cas.filemgr.structs.Query getCasQuery() {
      org.apache.oodt.cas.filemgr.structs.Query q = new org.apache.oodt.cas.filemgr.structs.Query();
      for (int i = 0; i < this.getComponentCount(); i++) {
        Component c = this.getComponent(i);
        String element;
        if (c instanceof TermQueryPanel) {
          element = database.getElementID(((TermQueryPanel) c).getElement());
          String criteria = ((TermQueryPanel) c).getText();
          if (!element.equals("") && !criteria.equals("")) {
            TermQueryCriteria tc = new TermQueryCriteria();
            tc.setElementName(element);
            tc.setValue(criteria);
            q.addCriterion(tc);
          }
        } else if (c instanceof RangeQueryPanel) {
          element = database.getElementID(((RangeQueryPanel) c).getElement());
          String startCriteria = ((RangeQueryPanel) c).getStart();
          String stopCriteria = ((RangeQueryPanel) c).getStop();
          if (!element.equals("") && !startCriteria.equals("")
              && !stopCriteria.equals("")) {
            RangeQueryCriteria rt = new RangeQueryCriteria();
            rt.setElementName(element);
            rt.setStartValue(startCriteria);
            rt.setEndValue(stopCriteria);
            q.addCriterion(rt);
          }
        }
      }

      return q;
    }

  }

  private class SearchPanel extends JPanel {
    private JButton search;

    public SearchPanel(QueryBuilderPrompt prompt) {
      Dimension d = new Dimension(500, 40);
      this.setMaximumSize(d);
      this.setMinimumSize(d);
      this.setPreferredSize(d);
      EmptyBorder line1 = new EmptyBorder(2, 2, 2, 2);
      LineBorder line2 = new LineBorder(Color.BLACK, 1);
      CompoundBorder cp = new CompoundBorder(line1, line2);
      this.setBorder(cp);
      this.setLayout(new BorderLayout());
      this.setBackground(Color.WHITE);

      search = new JButton("Search");
      search.setName("AdvancedQuery");
      search.addActionListener(prompt.listener);
      search.setBackground(Color.WHITE);

      JPanel buttonPanel = new JPanel();
      buttonPanel.setBackground(Color.WHITE);
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      buttonPanel.add(search);
      this.add(buttonPanel, BorderLayout.EAST);
    }

  }

  private class TypePanel extends JPanel {

    private JComboBox types;

    public TypePanel(QueryBuilderPrompt prompt) {

      Dimension d = new Dimension(500, 40);
      this.setMaximumSize(d);
      this.setMinimumSize(d);
      this.setPreferredSize(d);
      EmptyBorder line1 = new EmptyBorder(2, 2, 2, 2);
      LineBorder line2 = new LineBorder(Color.BLACK, 1);
      CompoundBorder cp = new CompoundBorder(line1, line2);
      this.setBorder(cp);

      types = new JComboBox(database.getAvailableTypes());
      types.setBackground(Color.WHITE);
      Dimension tDim = new Dimension(200, 30);
      types.setMaximumSize(tDim);
      types.setMinimumSize(tDim);
      types.setPreferredSize(tDim);

      this.setBackground(Color.WHITE);
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      JLabel label = new JLabel("        Product Type:   ");

      this.add(label);
      this.add(types);
    }

    public String getType() {
      return types.getSelectedItem().toString();
    }
  }

  private class BuiltQueryPane extends JPanel implements FocusListener,
      ActionListener {

    private QueryBuilderPrompt p;
    private JTextArea field;

    public BuiltQueryPane(QueryBuilderPrompt prompt) {

      Dimension d = new Dimension(500, 75);
      this.setMaximumSize(d);
      this.setMinimumSize(d);
      this.setPreferredSize(d);

      EmptyBorder line1 = new EmptyBorder(2, 2, 2, 2);
      LineBorder line2 = new LineBorder(Color.BLACK, 1);
      CompoundBorder cp = new CompoundBorder(line1, line2);
      this.setBorder(cp);
      this.setLayout(new BorderLayout());

      field = new JTextArea();
      Font font = new Font("san-serif", Font.PLAIN, 10);
      field.setFont(font);
      field.setBackground(Color.WHITE);
      field.setLineWrap(true);
      field.setWrapStyleWord(true);

      this.add(field, BorderLayout.CENTER);
    }

    public void focusGained(FocusEvent arg0) {
    }

    public void focusLost(FocusEvent arg0) {
      field.setText(qPanel.getQuery());
    }

    public void actionPerformed(ActionEvent arg0) {
      field.setText(qPanel.getQuery());
    }
  }

}

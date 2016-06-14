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

package org.apache.oodt.cas.filemgr.browser.view.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class QueryField extends JPanel implements ActionListener {

  private JTextField text;
  private JButton button;
  private JComboBox types;

  public QueryField(ActionListener listener) {

    // setbackground an size for panel
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    setBackground(Color.WHITE);
    setMinimumSize(new Dimension(500, 50));
    setPreferredSize(new Dimension(500, 50));

    text = new JTextField(30);
    button = new JButton("Search");
    String[] blankType = { "" };
    types = new JComboBox(blankType);

    // set background and size for textbox and combo
    text.setForeground(Color.BLACK);
    text.setBackground(Color.WHITE);
    text.setMinimumSize(new Dimension(100, 25));
    text.setPreferredSize(new Dimension(100, 25));
    text.setMaximumSize(new Dimension(100, 25));
    text.addActionListener(this);
    Dimension typeDim = new Dimension(100, 25);
    types.setMaximumSize(typeDim);
    types.setMinimumSize(typeDim);
    types.setPreferredSize(typeDim);
    types.setBackground(Color.WHITE);

    // set button
    button.setBackground(Color.WHITE);
    button.addActionListener(listener);
    button.setName("Query");

    add(new JLabel("ProductType:  "));
    add(types);
    add(new JLabel("    Query:  "));
    add(text);
    add(button);
  }

  public String getQueryString() {
    return text.getText();
  }

  public void clearQuery() {
    text.setText("");
  }

  public String getProductType() {
    return types.getSelectedItem().toString();
  }

  public void updateTypes(String[] typeNames) {
    this.remove(types);
    types = new JComboBox(typeNames);
    types.setBackground(Color.WHITE);
    Dimension typeDim = new Dimension(100, 25);
    types.setMaximumSize(typeDim);
    types.setMinimumSize(typeDim);
    types.setPreferredSize(typeDim);
    add(types, 1);
    this.repaint();
  }

  public void actionPerformed(ActionEvent arg0) {
    button.doClick();
  }
}

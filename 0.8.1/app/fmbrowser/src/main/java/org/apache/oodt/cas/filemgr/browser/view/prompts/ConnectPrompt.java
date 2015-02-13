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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class ConnectPrompt extends JFrame implements ActionListener {

  private CASField topPanel;
  private ConnectButton bottomPanel;

  public ConnectPrompt(ActionListener listener) {
    this.setName("New Connection");
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    Dimension d = new Dimension(400, 100);
    this.setMinimumSize(d);
    this.setMaximumSize(d);
    this.setPreferredSize(d);

    this.getContentPane().setLayout(
        new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    this.getContentPane().setBackground(Color.WHITE);

    topPanel = new CASField(this);
    bottomPanel = new ConnectButton(listener);
    this.add(topPanel);
    this.add(bottomPanel);
  }

  public String getCASUrl() {
    return topPanel.casRef.getText();
  }

  private class CASField extends JPanel {

    protected JTextField casRef;

    protected CASField(ActionListener listener) {
      this.setBackground(Color.WHITE);
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      Dimension d = new Dimension(400, 30);
      this.setMaximumSize(d);
      this.setMinimumSize(d);
      this.setPreferredSize(d);

      EmptyBorder line1 = new EmptyBorder(5, 10, 5, 5);
      this.setBorder(line1);

      this.add(new JLabel("CAS File Manager URL:  "));
      casRef = new JTextField();
      casRef.addActionListener(listener);
      this.add(casRef);
    }
  }

  private class ConnectButton extends JPanel {

    protected JButton connect;
    protected JButton cancel;

    protected ConnectButton(ActionListener listener) {
      this.setBackground(Color.WHITE);
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      EmptyBorder line1 = new EmptyBorder(5, 20, 5, 5);
      this.setBorder(line1);

      this.add(new JLabel("                          "));
      connect = new JButton("Connect");
      connect.setBackground(Color.WHITE);
      connect.addActionListener(listener);
      cancel = new JButton("Cancel");
      cancel.setBackground(Color.WHITE);
      cancel.addActionListener(listener);
      cancel.setName("ConnectCancel");
      this.add(cancel);
      this.add(connect);
    }
  }

  public void actionPerformed(ActionEvent arg0) {
    bottomPanel.connect.doClick();
  }

}

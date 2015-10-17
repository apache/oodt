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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class BottomPane extends JPanel {

  public JLabel statusMessage;

  public BottomPane() {

    // set background and panel size
    setBackground(Color.WHITE);
    EmptyBorder line1 = new EmptyBorder(4, 10, 4, 2);
    LineBorder line2 = new LineBorder(Color.BLACK, 1);
    EmptyBorder line3 = new EmptyBorder(4, 10, 4, 10);
    CompoundBorder cb1 = new CompoundBorder(line1, line2);
    CompoundBorder cb2 = new CompoundBorder(cb1, line3);
    setBorder(cb2);

    JLabel statusLabel = new JLabel(" Status: ");
    statusLabel.setFont(new Font("san-serif", Font.PLAIN, 10));

    statusMessage = new JLabel(" Disconnected");
    statusMessage.setFont(new Font("san-serif", Font.PLAIN, 10));
    statusMessage.setForeground(Color.RED);
    statusMessage.setBackground(Color.WHITE);

    // set layout
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    add(statusLabel);
    add(statusMessage);
  }

  public void changeStatus(String status) {
    statusMessage.setText(status);
    this.repaint();
  }

}

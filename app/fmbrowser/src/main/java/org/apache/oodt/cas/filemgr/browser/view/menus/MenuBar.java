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

package org.apache.oodt.cas.filemgr.browser.view.menus;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;

public class MenuBar extends JMenuBar {

  private JMenu fileMenu;
  private JMenu queryMenu;
  private JMenu helpMenu;

  private JMenuItem queryItem;
  private JMenuItem aboutItem;
  private JMenuItem clearItem;
  private JMenuItem unhideItem;
  private JMenuItem exportItem;
  private JMenuItem exitItem;
  private JMenuItem sortItem;
  private JMenuItem advancedItem;
  private JMenuItem connectItem;

  public MenuBar(ActionListener windowListener, ActionListener tableListener) {
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    queryMenu = new JMenu("Query");
    queryMenu.setMnemonic(KeyEvent.VK_Q);
    helpMenu = new JMenu("Help");

    // build help menu
    queryItem = new JMenuItem("Query Language");
    queryItem.addActionListener(windowListener);
    aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(windowListener);
    helpMenu.add(queryItem);
    helpMenu.addSeparator();
    helpMenu.add(aboutItem);

    // build query menu
    clearItem = new JMenuItem("Clear Query");
    clearItem.addActionListener(windowListener);
    advancedItem = new JMenuItem("Query Builder");
    advancedItem.addActionListener(windowListener);
    sortItem = new JMenuItem("Sort");
    sortItem.addActionListener(windowListener);
    unhideItem = new JMenuItem("Unhide Columns");
    unhideItem.addActionListener(tableListener);
    queryMenu.add(unhideItem);
    queryMenu.add(advancedItem);
    queryMenu.add(sortItem);
    queryMenu.addSeparator();
    queryMenu.add(clearItem);

    // build file menu
    connectItem = new JMenuItem("Connect...");
    connectItem.addActionListener(windowListener);
    exportItem = new JMenuItem("Export Table");
    exportItem.addActionListener(tableListener);
    exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(windowListener);
    fileMenu.add(connectItem);
    fileMenu.add(exportItem);
    fileMenu.addSeparator();
    fileMenu.add(exitItem);

    add(fileMenu);
    add(queryMenu);
    add(helpMenu);
  }

  public void changeConnectStatus() {
    if (connectItem.getActionCommand().equals("Connect...")) {
      connectItem.setActionCommand("Disconnect");
      connectItem.setText("Disconnect");
    } else {
      connectItem.setActionCommand("Connect...");
      connectItem.setText("Connect...");
    }
  }
}

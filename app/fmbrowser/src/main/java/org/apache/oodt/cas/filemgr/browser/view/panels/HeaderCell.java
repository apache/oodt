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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.border.LineBorder;

import org.apache.oodt.cas.filemgr.browser.view.GuiParams;

public class HeaderCell extends Cell {

  private int colNum;

  public HeaderCell(MouseListener listener, int colNum) {
    super();
    this.setBackground(Color.LIGHT_GRAY);
    this.setBorder(null);
    Dimension size = new Dimension(GuiParams.DEFAULT_CELL_WIDTH - 2,
        GuiParams.DEFAULT_CELL_HEIGHT);
    setMinimumSize(size);
    setMaximumSize(size);
    setPreferredSize(size);
    Font f = new Font("san-serif", Font.BOLD, 11);
    text.setFont(f);

    this.colNum = colNum;
    this.addMouseListener(listener);
  }

  public int getColNum() {
    return colNum;
  }

}

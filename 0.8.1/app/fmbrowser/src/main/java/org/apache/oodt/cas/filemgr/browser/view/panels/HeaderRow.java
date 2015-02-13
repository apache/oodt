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
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class HeaderRow extends JPanel {

  private HeaderCell cells[];
  private int numCells;

  public HeaderRow(MouseListener listener, int numCells) {
    // create cells
    this.numCells = numCells;
    cells = new HeaderCell[numCells];
    for (int i = 0; i < numCells; i++) {
      cells[i] = new HeaderCell(listener, i);
    }

    // set background, etc.
    this.setBackground(Color.WHITE);
    this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    for (int i = 0; i < numCells; i++) {
      this.add(cells[i]);
      this.add(new HeaderSpacer(listener, i));
    }
  }

  public int getNumCols() {
    return numCells;
  }

  public void hideCol(int colNum) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    c.setVisible(false);
    HeaderSpacer sp = (HeaderSpacer) this.getComponent(trueNum + 1);
    sp.setVisible(false);
  }

  public void unhideCol(int colNum) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    c.setVisible(true);
    HeaderSpacer sp = (HeaderSpacer) this.getComponent(trueNum + 1);
    sp.setVisible(true);
  }

  public void setText(int colNum, String text) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    c.setText(text);
    c.repaint();
  }

  public String getText(int colNum) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    return c.getText();
  }

  public int getWidth(int colNum) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    return c.getWidth();
  }

  public void changeWidth(int colNum, int newWidth) {
    int trueNum = colNum * 2;
    HeaderCell c = (HeaderCell) this.getComponent(trueNum);
    int change = c.getWidth() - newWidth;
    c.setWidth(newWidth);
    for (int i = trueNum + 1; i < this.getComponentCount(); i++) {
      this.getComponent(i).setLocation(this.getComponent(i).getX() - change,
          this.getComponent(i).getY());
    }
    this.repaint();
  }
}

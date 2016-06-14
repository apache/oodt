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

import org.apache.oodt.cas.filemgr.browser.view.GuiParams;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class QueryPane extends JPanel {

  private QueryField field;

  public QueryPane(ActionListener listener) {
    super(new BorderLayout());

    // set background and panel size
    setBackground(Color.WHITE);
    Dimension paneSize = new Dimension();
    paneSize.width = GuiParams.WINDOW_WIDTH;
    paneSize.height = (int) (GuiParams.WINDOW_HEIGHT * (0.1));

    // set border
    EmptyBorder line1 = new EmptyBorder(2, 10, 2, 2);
    LineBorder line2 = new LineBorder(Color.BLACK, 1);
    CompoundBorder cp = new CompoundBorder(line1, line2);
    this.setBorder(cp);

    // add query field to pane
    field = new QueryField(listener);
    add(field, BorderLayout.EAST);
  }

  public String getQuery() {
    return field.getQueryString();
  }

  public void clearQuery() {
    field.clearQuery();
  }

  public String getType() {
    return field.getProductType();
  }

  public void updateTypes(String[] types) {
    field.updateTypes(types);
  }
}

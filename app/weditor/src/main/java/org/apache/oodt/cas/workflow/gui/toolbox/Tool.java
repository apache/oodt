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

package org.apache.oodt.cas.workflow.gui.toolbox;

//JDK imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 * 
 * One of the tool buttons at the top of the Workflow Editor GUI.
 * 
 * @author bfoster
 * @author mattmann
 * 
 */
public abstract class Tool extends JPanel {

  private static final long serialVersionUID = -373351385009724404L;

  private Image image, selectedImage;
  private boolean selected = false;
  private ToolBox toolBox;

  public Tool() {
    this("?");
  }

  public Tool(String text) {
    super();
    this.setLayout(new BorderLayout());
    this.add(new JLabel(text), BorderLayout.CENTER);
    this.setup();
  }

  public Tool(Image image, Image selectedImage) {
    super();
    this.setLayout(new BorderLayout());
    this.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
    this.image = image;
    this.selectedImage = selectedImage;
    this.setup();
  }

  public void setToolBox(ToolBox toolBox) {
    this.toolBox = toolBox;
  }

  public boolean isSelected() {
    return this.selected;
  }

  public void setSelected(boolean selected) {
    if (image != null) {
      Tool.this.removeAll();
      if (this.selected = selected) {
        Tool.this.add(new JLabel(new ImageIcon(selectedImage)),
            BorderLayout.CENTER);
      } else {
        Tool.this.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
      }
      Tool.this.revalidate();
    }
  }

  private void setup() {
    this.setPreferredSize(new Dimension(30, 30));
    this.addMouseListener(new MouseListener() {

      public void mouseClicked(MouseEvent e) {
        Tool.this.onClick();
        Tool.this.toolBox.setSelected(Tool.this);
      }

      public void mouseEntered(MouseEvent e) {
        if (!selected && selectedImage != null) {
          Tool.this.removeAll();
          Tool.this.add(new JLabel(new ImageIcon(selectedImage)),
              BorderLayout.CENTER);
          Tool.this.revalidate();
        }
      }

      public void mouseExited(MouseEvent e) {
        if (!selected && image != null) {
          Tool.this.removeAll();
          Tool.this.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
          Tool.this.revalidate();
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

    });
  }

  public abstract void onClick();

}

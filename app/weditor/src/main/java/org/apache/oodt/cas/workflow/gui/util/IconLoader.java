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

package org.apache.oodt.cas.workflow.gui.util;

//JDK imports
import java.awt.Image;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 
 * 
 * Loads the appropriate classpath-level icon image for Tools, etc., that are
 * part of the Workflow Editor GUI.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class IconLoader {

  public static final int CREATE = 0;
  public static final int CREATE_SELECTED = 1;
  public static final int DELETE = 2;
  public static final int DELETE_SELECTED = 3;
  public static final int EDIT = 4;
  public static final int EDIT_SELECTED = 5;
  public static final int MOVE = 6;
  public static final int MOVE_SELECTED = 7;
  public static final int ZOOM_IN = 8;
  public static final int ZOOM_IN_SELECTED = 9;
  public static final int ZOOM_OUT = 10;
  public static final int ZOOM_OUT_SELECTED = 11;
  public static final int ZOOM_CURSOR = 12;

  private IconLoader() {
  }

  public static Image getIcon(int icon) throws IOException {
    switch (icon) {
    case CREATE:
      return ImageIO.read(IconLoader.class.getResource("create.jpg"));
    case CREATE_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("create-sel.jpg"));
    case DELETE:
      return ImageIO.read(IconLoader.class.getResource("delete.jpg"));
    case DELETE_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("delete-sel.jpg"));
    case EDIT:
      return ImageIO.read(IconLoader.class.getResource("edit.jpg"));
    case EDIT_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("edit-sel.jpg"));
    case MOVE:
      return ImageIO.read(IconLoader.class.getResource("move.jpg"));
    case MOVE_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("move-sel.jpg"));
    case ZOOM_IN:
      return ImageIO.read(IconLoader.class.getResource("zoom-in.jpg"));
    case ZOOM_IN_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("zoom-in-sel.jpg"));
    case ZOOM_OUT:
      return ImageIO.read(IconLoader.class.getResource("zoom-out.jpg"));
    case ZOOM_OUT_SELECTED:
      return ImageIO.read(IconLoader.class.getResource("zoom-out-sel.jpg"));
    case ZOOM_CURSOR:
      return ImageIO.read(IconLoader.class.getResource("cursor-zoom.png"));
    default:
      return null;
    }
  }

}

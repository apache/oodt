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

package org.apache.oodt.pcs.opsui.config;

import java.util.List;
import java.util.Vector;

import org.apache.oodt.pcs.opsui.BasePage;
import org.apache.oodt.pcs.opsui.config.filemgr.FileManagerConfigPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.TabbedPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ConfigPage extends BasePage {

  /**
   * @param parameters
   */
  public ConfigPage(PageParameters parameters) {
    super(parameters);

    List<ITab> tabs = new Vector<ITab>();
    tabs.add(new AbstractTab(new Model<String>("File Manager")) {
      @Override
      public Panel getPanel(String id) {
        return new FileManagerConfigPage(id);
      }
    });

    tabs.add(new AbstractTab(new Model<String>("Workflow Manager")) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.extensions.markup.html.tabs.AbstractTab#getPanel(
       * java.lang.String)
       */
      @Override
      public Panel getPanel(String id) {
        // TODO Auto-generated method stub
        return null;
      }
    });

    tabs.add(new AbstractTab(new Model<String>("Resource Manager")) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.extensions.markup.html.tabs.AbstractTab#getPanel(
       * java.lang.String)
       */
      @Override
      public Panel getPanel(String arg0) {
        // TODO Auto-generated method stub
        return null;
      }
    });

    tabs.add(new AbstractTab(new Model<String>("PGE Configuration")) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.extensions.markup.html.tabs.AbstractTab#getPanel(
       * java.lang.String)
       */
      @Override
      public Panel getPanel(String arg0) {
        // TODO Auto-generated method stub
        return null;
      }
    });

    TabbedPanel tabbedPanel = new TabbedPanel("tabs", tabs);
    tabbedPanel.setSelectedTab(getTabIdx(tabs,
        parameters.getString("tab", "File Manager")));

    add(tabbedPanel);
  }

  private int getTabIdx(List<ITab> tabs, String tabName) {
    for (int i = 0; i < tabs.size(); i++) {
      ITab tab = tabs.get(i);
      if (tab.getTitle().getObject().equals(tabName)) {
        return i;
      }
    }

    return -1;
  }

}

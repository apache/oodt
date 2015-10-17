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

package org.apache.oodt.cas.webcomponents.filemgr.browser.metadata;

//JDK imports
import java.util.Collections;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;

//Wicket imports
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * {@link Metadata}-view popup.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MetadataBrowser extends Panel {

  private static final long serialVersionUID = 5276544059589968409L;

  private FileManagerConn fm;

  public MetadataBrowser(String componentId, String fmUrlStr, String productId) {
    super(componentId);
    this.fm = new FileManagerConn(fmUrlStr);
    final SerializableMetadata met = new SerializableMetadata(this.fm
        .getMetadata(this.fm.safeGetProductById(productId)));
    setDefaultModel(new Model(met));

    add(new Label("no_prod_met_display") {
      /*
       * (non-Javadoc)
       *
       * @see org.apache.wicket.Component#isVisible()
       */
      @Override
      public boolean isVisible() {
        return false;
      }
    });

    List<String> metKeys = met.getAllKeys();
    Collections.sort(metKeys);

    add(new ListView<String>("met_elem", new ListModel<String>(metKeys)) {
      /*
       * (non-Javadoc)
       *
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<String> item) {
        item.add(new Label("met_elem_name", item.getModelObject()));

        item
            .add(new ListView<String>("met_values_list",
                new ListModel<String>(met.getAllMetadata(item
                    .getModelObject()))) {
              /*
               * (non-Javadoc)
               *
               * @see
               * org.apache.wicket.markup.html.list.ListView#populateItem(
               * org.apache.wicket.markup.html.list.ListItem)
               */
              @Override
              protected void populateItem(ListItem<String> item) {
                item.add(new Label("met_value", item.getModelObject()));
              }
            });

      }
    });

  }

}

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

package org.apache.oodt.cas.webcomponents.filemgr.browser.product;


//OODT imports
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;

//Wicket imports
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.ListModel;

//JDK imports
import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * Component responsible for showing a {@link org.apache.oodt.cas.filemgr.structs.Product}s
 * {@link List} of {@link Reference}s.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class ProductRefsBrowser extends Panel {
  
  private static final long serialVersionUID = -2278188309669737798L;

  private static final Logger LOG = Logger.getLogger(ProductRefsBrowser.class.getName());
  
  private FileManagerConn fm;
  
  public ProductRefsBrowser(String componentId, String fmUrlStr, String productId){
    super(componentId);
    this.fm = new FileManagerConn(fmUrlStr);
    List<Reference> refs = null;
    try {
      refs = fm.getProductReferences(fm.getFm().getProductById(productId));
    } catch (CatalogException e1) {
      e1.printStackTrace();
    }
    if (refs != null && refs.size() > 0) {
      add(new Label("no_prod_ref_display") {
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

      add(new ListView<Reference>("ref_list", new ListModel<Reference>(refs)) {
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
         * .wicket.markup.html.list.ListItem)
         */
        @Override
        protected void populateItem(ListItem<Reference> refItem) {
          Reference r = refItem.getModelObject();
          String filePath = null;
          try {
            filePath = new File(new URI(r.getDataStoreReference()))
                .getAbsolutePath();
          } catch (Exception ignore) {
          }

          refItem.add(new Label("ref_file_path", filePath));
          refItem.add(new Label("ref_file_size", String
              .valueOf(r.getFileSize())));
          try {
            refItem.add(new Label("ref_pct_transferred", NumberFormat
                .getPercentInstance().format(
                    fm.getFm()
                        .getRefPctTransferred(r))));
          } catch (DataTransferException e) {
            LOG.log(Level.WARNING,
                "Unable to determine product reference size: Reason: "
                    + e.getMessage());
            refItem.add(new Label("ref_pct_transferred", "N/A"));
          }

        }
      });
    } else {
      add(new Label("no_prod_ref_display", "No Product References!"));
    }    
  }

}

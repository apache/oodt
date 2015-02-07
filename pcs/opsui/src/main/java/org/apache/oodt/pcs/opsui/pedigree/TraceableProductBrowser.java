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

package org.apache.oodt.pcs.opsui.pedigree;

//JDK imports
import java.io.File;
import java.net.URI;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;
import org.apache.oodt.cas.webcomponents.filemgr.browser.product.ProductBrowser;
import org.apache.oodt.pcs.opsui.OpsuiApp;
import org.apache.oodt.pcs.webcomponents.trace.Trace;

//Wicket imports
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * Extends the existing {@link ProductBrowser} and adds a Web form and
 * associated pedigree tree container to expose the {@link Trace} component.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class TraceableProductBrowser extends ProductBrowser {

  private static final long serialVersionUID = 5512878676145737818L;

  private static final Logger LOG = Logger
      .getLogger(TraceableProductBrowser.class.getName());

  /**
   * @param componentId
   * @param fmUrlStr
   * @param productId
   */
  public TraceableProductBrowser(String componentId, final String fmUrlStr,
      final String productId, final boolean enableNotCat, final List<String> excludes) {
    super(componentId, fmUrlStr, productId);
    final FileManagerConn fm = new FileManagerConn(fmUrlStr);
    final Product product = fm.safeGetProductById(productId);
    final OpsuiApp app = (OpsuiApp)getApplication();

    Form traceForm = new Form("trace_form");
    traceForm.add(new Button("trace_button") {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.form.Button#onSubmit()
       */
      @Override
      public void onSubmit() {
        Trace tracer = new Trace("pedigree", fmUrlStr, enableNotCat, excludes,
            product);
        tracer.setVisible(true);
        getParent().getParent().replace(tracer);
        setVisible(false);
      }

    });
    add(traceForm);
    add(new WebMarkupContainer("pedigree").setVisible(false));

    List<Reference> refs = fm.getProductReferences(product);
    if (refs != null && refs.size() > 0) {
      replace(new Label("no_prod_ref_display") {
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

      replace(new ListView<Reference>("ref_list", new ListModel<Reference>(refs)) {
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
          
          ExternalLink refLink = new ExternalLink("ref_file_path_link", 
              "/" + app.getRootContext() + "/data?productID="+productId);
          refLink.add(new Label("ref_file_path", filePath));
          refItem.add(refLink);
          refItem.add(new Label("ref_file_size",
              String.valueOf(r.getFileSize())));
          try {
            refItem.add(new Label("ref_pct_transferred", NumberFormat
                .getPercentInstance()
                .format(fm.getFm().getRefPctTransferred(r))));
          } catch (DataTransferException e) {
            LOG.log(
                Level.WARNING,
                "Unable to determine product reference size: Reason: "
                    + e.getMessage());
            refItem.add(new Label("ref_pct_transferred", "N/A"));
          }

        }
      });
    } else {
      replace(new Label("no_prod_ref_display", "No Product References!"));
    }

  }
  

}

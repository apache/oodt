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
import java.util.Collections;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.webcomponents.filemgr.FileManagerConn;
import org.apache.oodt.cas.webcomponents.filemgr.browser.product.ProductBrowser;
import org.apache.oodt.pcs.webcomponents.trace.Trace;

//Wicket imports
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;

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

  /**
   * @param componentId
   * @param fmUrlStr
   * @param productId
   */
  public TraceableProductBrowser(String componentId, final String fmUrlStr,
      String productId) {
    super(componentId, fmUrlStr, productId);
    FileManagerConn fm = new FileManagerConn(fmUrlStr);
    final Product product = fm.safeGetProductById(productId);

    Form traceForm = new Form("trace_form");
    traceForm.add(new Button("trace_button") {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.form.Button#onSubmit()
       */
      @Override
      public void onSubmit() {
        Trace tracer = new Trace("pedigree", fmUrlStr, true,
            Collections.EMPTY_LIST, product);
        tracer.setVisible(true);
        getParent().getParent().replace(tracer);
        setVisible(false);
      }

    });
    add(traceForm);
    add(new WebMarkupContainer("pedigree").setVisible(false));

  }

}

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


package org.apache.oodt.pcs.opsui;

//OODT imports
import org.apache.oodt.pcs.opsui.pedigree.TraceableProductBrowser;

//Wicket imports
import org.apache.wicket.PageParameters;

/**
 *
 * Shows a product, its metadata, and its references, all 
 * on a single page.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class ProductBrowserPage extends BasePage {

  public ProductBrowserPage(PageParameters parameters){
    super(parameters);
    add(new TraceableProductBrowser("prod_browser_component", 
        app.getFmUrlStr(),
        parameters.getString("id"), app.isEnabledTraceNotCatProducts(), 
        app.getTraceExcludedProductTypeList()));
  }
}

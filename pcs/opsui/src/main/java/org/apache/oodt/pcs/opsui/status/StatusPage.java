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

package org.apache.oodt.pcs.opsui.status;

//OODT imports
import org.apache.oodt.pcs.opsui.ProductBrowserPage;
import org.apache.oodt.pcs.opsui.BasePage;
import org.apache.oodt.pcs.opsui.OpsuiApp;
import org.apache.oodt.pcs.opsui.WorkflowInstanceViewerPage;
import org.apache.oodt.pcs.webcomponents.health.HealthMonitor;

//Wicket imports
import org.apache.wicket.PageParameters;

/**
 * 
 * A wicket controller for exposing the super awesome power of the
 * {@link org.apache.oodt.pcs.tools.PCSHealthMonitor}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class StatusPage extends BasePage {

  /**
   * @param parameters
   * @throws InstantiationException
   */
  public StatusPage(PageParameters parameters) throws InstantiationException {
    super(parameters);

    OpsuiApp app = (OpsuiApp) getApplication();
    String fmUrlStr = app.getFmUrlStr();
    String wmUrlStr = app.getWmUrlStr();
    String rmUrlStr = app.getRmUrlStr();
    String crawlerConfFilePath = app.getCrawlerConfFilePath();
    String statesFilePath = app.getStatesFilePath();
    add(new HealthMonitor("health_monitor", fmUrlStr, wmUrlStr, rmUrlStr,
        crawlerConfFilePath, statesFilePath, ProductBrowserPage.class,
        WorkflowInstanceViewerPage.class));
  }

}

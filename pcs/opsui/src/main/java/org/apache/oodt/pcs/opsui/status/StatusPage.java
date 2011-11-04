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

//JDK imports
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.pcs.health.CrawlerStatus;
import org.apache.oodt.pcs.health.PCSDaemonStatus;
import org.apache.oodt.pcs.health.PCSHealthMonitorReport;
import org.apache.oodt.pcs.opsui.BasePage;
import org.apache.oodt.pcs.opsui.OpsuiApp;
import org.apache.oodt.pcs.tools.PCSHealthMonitor;

//Wicket imports
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

/**
 * 
 * Describe your class here.
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
    PCSHealthMonitor mon = new PCSHealthMonitor(fmUrlStr, wmUrlStr, rmUrlStr,
        crawlerConfFilePath, statesFilePath);
    PCSHealthMonitorReport report = mon.getReport();

    add(new Label("report_date", report.getCreateDateIsoFormat()));
    add(new Label("fmurl", report.getFmStatus().getUrlStr()));
    add(new Label("wmurl", report.getWmStatus().getUrlStr()));
    add(new Label("rmurl", report.getRmStatus().getUrlStr()));

    add(new Image("fmstatus_icon", getUpOrDownArrowRef(report.getFmStatus()
        .getStatus())));
    add(new Image("wmstatus_icon", getUpOrDownArrowRef(report.getWmStatus()
        .getStatus())));
    add(new Image("rmstatus_icon", getUpOrDownArrowRef(report.getRmStatus()
        .getStatus())));

    List<CrawlerStatus> crawlerStatusList = report.getCrawlerStatus();
    add(new ListView<CrawlerStatus>("crawler_status_list", crawlerStatusList) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<CrawlerStatus> statusItem) {
        CrawlerStatus status = statusItem.getModelObject();
        String statusString = status.getInfo().getCrawlerName() + " ("
            + status.getCrawlHost() + ":" + status.getInfo().getCrawlerPort()
            + ")";
        statusItem.add(new Label("crawler_name_and_url", statusString));
        statusItem.add(new Image("crawler_status_icon",
            getUpOrDownArrowRef(status.getStatus())));
      }
    });

  }

  private ResourceReference getUpOrDownArrowRef(String status) {
    return new ResourceReference(StatusPage.class, "icon_arrow_"
        + status.toLowerCase() + ".gif");
  }

  private static List getTopN(List statuses, int topN) {
    List subset = new Vector();
    if (statuses != null && statuses.size() > 0) {
      int numGobble = topN <= statuses.size() ? topN : statuses.size();
      for (int i = 0; i < numGobble; i++) {
        Object status = statuses.get(i);
        subset.add(status);
      }
    }

    return subset;
  }

  private static void sortByStatus(List statusList) {
    Collections.sort(statusList, new Comparator() {

      public int compare(Object o1, Object o2) {
        if (o1 instanceof CrawlerStatus) {
          CrawlerStatus stat1 = (CrawlerStatus) o1;
          CrawlerStatus stat2 = (CrawlerStatus) o2;

          return stat1.getStatus().compareTo(stat2.getStatus());
        } else if (o1 instanceof PCSDaemonStatus) {
          PCSDaemonStatus stat1 = (PCSDaemonStatus) o1;
          PCSDaemonStatus stat2 = (PCSDaemonStatus) o2;

          return stat1.getStatus().compareTo(stat2.getStatus());
        } else
          return 0;
      }

    });
  }

}

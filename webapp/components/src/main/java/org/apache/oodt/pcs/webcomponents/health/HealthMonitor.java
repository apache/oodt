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

package org.apache.oodt.pcs.webcomponents.health;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.pcs.health.CrawlerHealth;
import org.apache.oodt.pcs.health.CrawlerStatus;
import org.apache.oodt.pcs.health.JobHealthStatus;
import org.apache.oodt.pcs.health.PCSDaemonStatus;
import org.apache.oodt.pcs.health.PCSHealthMonitorReport;
import org.apache.oodt.pcs.tools.PCSHealthMonitor;
import org.apache.oodt.pcs.util.FileManagerUtils;

//Wicket imports
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * A wicket controller for exposing the super awesome power of the
 * {@link PCSHealthMonitor}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class HealthMonitor extends Panel {

  /**
   * @throws InstantiationException
   */
  public HealthMonitor(String id, String fmUrlStr, String wmUrlStr,
      String rmUrlStr, String crawlerConfFilePath, String statesFilePath,
      final Class<? extends WebPage> productBrowser,
      final Class<? extends WebPage> instancesPage)
      throws InstantiationException {
    super(id);
    PCSHealthMonitor mon = new PCSHealthMonitor(fmUrlStr, wmUrlStr, rmUrlStr,
        crawlerConfFilePath, statesFilePath);
    final PCSHealthMonitorReport report = mon.getReport();

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

    ListModel crawlerStatusListModel = new ListModel(report.getCrawlerStatus());
    add(new VisibilityAndSortToggler("crawler_toggler",
        "crawler_status_showall", "crawler_status_hide", "crawler_status_sort",
        "crawler_status_unsort", "crawler_status_more", crawlerStatusListModel));

    add(new ListView<CrawlerStatus>("crawler_status_list",
        crawlerStatusListModel) {
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

    ListModel batchStubStatusListModel = new ListModel(
        report.getBatchStubStatus());
    add(new VisibilityAndSortToggler("batch_stub_toggler",
        "batch_stub_showall", "batch_stub_hide", "batch_stub_sort",
        "batch_stub_unsort", "batch_stub_more", batchStubStatusListModel));

    add(new ListView<PCSDaemonStatus>("batch_stub_list",
        batchStubStatusListModel) {

      @Override
      protected void populateItem(ListItem<PCSDaemonStatus> item) {
        item.add(new Label("batch_stub_url", item.getModelObject().getUrlStr()));
        item.add(new Image("batch_stub_status_icon", getUpOrDownArrowRef(item
            .getModelObject().getStatus())));

      }
    });

    List<JobHealthStatus> jobHealthStatusList = report.getJobHealthStatus();
    add(new ListView<JobHealthStatus>("jobstatus_list", jobHealthStatusList) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(final ListItem<JobHealthStatus> item) {
        item.add(new Label("status_name", item.getModelObject().getStatus()));
        Link<String> countLink = new Link<String>("jobstatus_count_link",
            new Model<String>(item.getModelObject().getStatus())) {

          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("pageNum", "1");
            params.add("status", getModelObject());
            setResponsePage(instancesPage, params);
          }
        };
        countLink.add(new Label("status_num_jobs", String.valueOf(item
            .getModelObject().getNumPipelines())));
        item.add(countLink);
      }
    });

    List<Product> prodList = report.getLatestProductsIngested();
    final FileManagerUtils fm = new FileManagerUtils(fmUrlStr);

    add(new ListView<Product>("file_health_list", prodList) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<Product> item) {
        final Product product = item.getModelObject();
        product.setProductType(fm.safeGetProductTypeById(product
            .getProductType().getProductTypeId()));
        product.setProductReferences(fm.safeGetProductReferences(product));
        final Metadata prodMet = fm.safeGetMetadata(product);
        final String filePath = fm.getFilePath(product);

        Link link = new Link("view_product_link") {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", product.getProductId());
            setResponsePage(productBrowser, params);
          }
        };

        link.add(new Label("file_path", filePath));
        item.add(link);
        item.add(new Label("file_ingest_datetime", prodMet.getMetadata("CAS."
            + CoreMetKeys.PRODUCT_RECEVIED_TIME)));

      }
    });

    ListModel crawlerHealthListModel = new ListModel(
        report.getCrawlerHealthStatus());
    add(new VisibilityToggler("crawler_health_toggler",
        "crawler_health_showall", "crawler_health_hide", "crawler_health_more",
        crawlerHealthListModel));

    add(new ListView<CrawlerHealth>("crawler_health_list",
        crawlerHealthListModel) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<CrawlerHealth> item) {
        CrawlerHealth health = item.getModelObject();
        item.add(new Label("crawler_name", health.getCrawlerName()));
        item.add(new Label("num_crawls", String.valueOf(health.getNumCrawls())));
        item.add(new Label("avg_crawl_time", String.valueOf(health
            .getAvgCrawlTime())));
      }
    });

  }

  private ResourceReference getUpOrDownArrowRef(String status) {
    return new ResourceReference(HealthMonitor.class, "icon_arrow_"
        + status.toLowerCase() + ".gif");
  }

}

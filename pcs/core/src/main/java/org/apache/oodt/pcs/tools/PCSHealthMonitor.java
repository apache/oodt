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

package org.apache.oodt.pcs.tools;

//JDK imports
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

//APACHE imports
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.apache.oodt.cas.resource.system.extern.AvroRpcBatchStub;
import org.apache.oodt.cas.crawl.daemon.CrawlDaemonController;
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.commons.date.DateUtils;
import org.apache.oodt.pcs.health.CrawlInfo;
import org.apache.oodt.pcs.health.CrawlPropertiesFile;
import org.apache.oodt.pcs.health.CrawlerHealth;
import org.apache.oodt.pcs.health.CrawlerStatus;
import org.apache.oodt.pcs.health.JobHealthStatus;
import org.apache.oodt.pcs.health.PCSDaemonStatus;
import org.apache.oodt.pcs.health.PCSHealthMonitorMetKeys;
import org.apache.oodt.pcs.health.PCSHealthMonitorReport;
import org.apache.oodt.pcs.health.WorkflowStatesFile;
import org.apache.oodt.pcs.util.FileManagerUtils;
import org.apache.oodt.pcs.util.ResourceManagerUtils;
import org.apache.oodt.pcs.util.WorkflowManagerUtils;
import org.apache.xmlrpc.XmlRpcClient;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//APACHE imports
//OODT imports

/**
 * 
 * A tool to monitor the health of the PCS.
 * 
 * @author mattmann
 * @version $Revision$
 */
public final class PCSHealthMonitor implements CoreMetKeys,
    PCSHealthMonitorMetKeys {
  public static final double DOUBLE = 1000.0;
  public static final double DOUBLE1 = 1000.0;
  private static Logger LOG = Logger.getLogger(PCSHealthMonitor.class.getName());
  private FileManagerUtils fm;

  private WorkflowManagerUtils wm;

  private ResourceManagerUtils rm;

  private CrawlPropertiesFile crawlProps;

  private WorkflowStatesFile statesFile;

  public PCSHealthMonitor(String fmUrlStr, String wmUrlStr, String rmUrlStr,
      String crawlPropFilePath, String statesFilePath)
      throws InstantiationException {
    this.fm = new FileManagerUtils(fmUrlStr);
    this.wm = new WorkflowManagerUtils(wmUrlStr);
    this.rm = new ResourceManagerUtils(rmUrlStr);
    this.crawlProps = new CrawlPropertiesFile(crawlPropFilePath);
    this.statesFile = new WorkflowStatesFile(statesFilePath);
  }

  public PCSHealthMonitorReport getReport() {
    PCSHealthMonitorReport report = new PCSHealthMonitorReport();
    report.setGenerationDate(new Date());
    report.setFmStatus(getFileManagerStatus());
    report.setWmStatus(getWorkflowManagerStatus());
    report.setRmStatus(getResourceManagerStatus());
    report.setBatchStubStatus(getBatchStubStatus());
    report.setCrawlerStatus(getCrawlerStatus());
    report.setLatestProductsIngested(getProductHealth());
    report.setJobHealthStatus(getJobStatusHealth());
    report.setCrawlerHealthStatus(getIngestHealth());

    return report;
  }

  public void quickPrintMonitorToConsole() {
    System.out.println(HEADER_AND_FOOTER);
    System.out.println(REPORT_BANNER);
    System.out.println("Generated on: "
        + DateUtils.toString(Calendar.getInstance()));
    System.out.println("");
    System.out.println("Service Status:");
    System.out.println("");

    System.out.println(FILE_MANAGER_DAEMON_NAME
        + getStrPadding(FILE_MANAGER_DAEMON_NAME, WORKFLOW_MANAGER_DAEMON_NAME)
        + ":\t[" + this.fm.getFmUrl() + "]: " + printUp(getFmUp()));

    System.out.println(WORKFLOW_MANAGER_DAEMON_NAME + ":\t["
        + this.wm.getWmUrl() + "]: " + printUp(getWmUp()));

    System.out.println(RESOURCE_MANAGER_DAEMON_NAME + ":\t["
        + this.rm.getResmgrUrl() + "]: " + printUp(getRmUp()));

    quickPrintBatchStubs();

    System.out.println("");
    System.out.println("Crawlers:");
    quickPrintCrawlers();
    System.out.println("");

    System.out.println("PCS Health: ");
    System.out.println("");
    System.out.println("Files:");
    System.out.println(SECTION_SEPARATOR);

    quickPrintProductHealth();

    System.out.println("");
    System.out.println("Jobs:");
    System.out.println(SECTION_SEPARATOR);

    quickPrintJobStatusHealth();

    System.out.println("");
    System.out.println("Ingest:");
    System.out.println(SECTION_SEPARATOR);

    quickPrintIngestStatusHealth();

    System.out.println(HEADER_AND_FOOTER);

  }

  public void printMonitorToConsole(PCSHealthMonitorReport report) {

    System.out.println(HEADER_AND_FOOTER);
    System.out.println(REPORT_BANNER);
    System.out.println("Generated on: " + report.getCreateDateIsoFormat());
    System.out.println("");
    System.out.println("Service Status:");
    System.out.println("");

    System.out.println(report.getFmStatus().getDaemonName()
        + getStrPadding(report.getFmStatus().getDaemonName(), report
            .getWmStatus().getDaemonName()) + ":\t["
        + report.getFmStatus().getUrlStr() + "]: "
        + report.getFmStatus().getStatus());

    System.out.println(report.getWmStatus().getDaemonName() + ":\t["
        + report.getWmStatus().getUrlStr() + "]: "
        + report.getWmStatus().getStatus());

    System.out.println(report.getRmStatus().getDaemonName() + ":\t["
        + report.getRmStatus().getUrlStr() + "]: "
        + report.getRmStatus().getStatus());

    printBatchStubs(report);

    System.out.println("");
    System.out.println("Crawlers:");
    printCrawlers(report);
    System.out.println("");

    System.out.println("PCS Health: ");
    System.out.println("");
    System.out.println("Files:");
    System.out.println(SECTION_SEPARATOR);

    printProductHealth(report);

    System.out.println("");
    System.out.println("Jobs:");
    System.out.println(SECTION_SEPARATOR);

    printJobStatusHealth(report);

    System.out.println("");
    System.out.println("Ingest:");
    System.out.println(SECTION_SEPARATOR);

    printIngestStatusHealth(report);

    System.out.println(HEADER_AND_FOOTER);

  }

  public static void main(String[] args) throws InstantiationException {
    String usage = "PCSHealthMonitor <fm url> <wm url> <rm url> <crawler xml file path> <workflow states xml file path>\n";
    String fmUrlStr, wmUrlStr, rmUrlStr;
    String crawlerXmlFilePath, workflowStateXmlPath;

    if (args.length != 5) {
      System.err.println(usage);
      System.exit(1);
    }

    fmUrlStr = args[0];
    wmUrlStr = args[1];
    rmUrlStr = args[2];
    crawlerXmlFilePath = args[3];
    workflowStateXmlPath = args[4];

    PCSHealthMonitor mon = new PCSHealthMonitor(fmUrlStr, wmUrlStr, rmUrlStr,
        crawlerXmlFilePath, workflowStateXmlPath);
    try {
      mon.quickPrintMonitorToConsole();
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
  }

  private PCSDaemonStatus getFileManagerStatus() {
    PCSDaemonStatus fmStatus = new PCSDaemonStatus();

    fmStatus.setDaemonName(FILE_MANAGER_DAEMON_NAME);
    fmStatus.setStatus(printUp(getFmUp()));
    fmStatus.setUrlStr(this.fm.getFmUrl().toString());

    return fmStatus;
  }

  private PCSDaemonStatus getWorkflowManagerStatus() {
    PCSDaemonStatus wmStatus = new PCSDaemonStatus();

    wmStatus.setDaemonName(WORKFLOW_MANAGER_DAEMON_NAME);
    wmStatus.setStatus(printUp(getWmUp()));
    wmStatus.setUrlStr(this.wm.getWmUrl().toString());

    return wmStatus;
  }

  private PCSDaemonStatus getResourceManagerStatus() {
    PCSDaemonStatus rmStatus = new PCSDaemonStatus();

    rmStatus.setDaemonName(RESOURCE_MANAGER_DAEMON_NAME);
    rmStatus.setStatus(printUp(getRmUp()));
    rmStatus.setUrlStr(this.rm.getResmgrUrl().toString());

    return rmStatus;
  }

  private List getBatchStubStatus() {
    List batchStubStatus = new Vector();

    if (getRmUp()) {
      // only print if the resource manager is up
      List resNodes = rm.safeGetResourceNodes();

      if (resNodes != null && resNodes.size() > 0) {
        for (Object resNode : resNodes) {
          ResourceNode node = (ResourceNode) resNode;
          PCSDaemonStatus batchStatus = new PCSDaemonStatus();
          batchStatus.setDaemonName(BATCH_STUB_DAEMON_NAME);
          batchStatus.setUrlStr(node.getIpAddr().toString());
          batchStatus.setStatus(printUp(getBatchStubUp(node)));
          batchStubStatus.add(batchStatus);
        }
      }
    }

    return batchStubStatus;
  }

  private List getCrawlerStatus() {
    List crawlers = this.crawlProps.getCrawlers();
    String crawlHost = this.crawlProps.getCrawlHost();
    List statuses = new Vector();

    if (crawlers != null && crawlers.size() > 0) {
      Collections.sort(crawlers, new Comparator() {
        public int compare(Object o1, Object o2) {
          CrawlInfo c1 = (CrawlInfo) o1;
          CrawlInfo c2 = (CrawlInfo) o2;

          return c1.getCrawlerName().compareTo(c2.getCrawlerName());
        }

      });

      for (Object crawler : crawlers) {
        CrawlInfo info = (CrawlInfo) crawler;
        String crawlerUrlStr = "http://" + crawlHost + ":"
                               + info.getCrawlerPort();
        CrawlerStatus status = new CrawlerStatus();
        status.setInfo(info);
        status.setStatus(printUp(getCrawlerUp(crawlerUrlStr)));
        status.setCrawlHost(crawlHost);
        statuses.add(status);
      }
    }

    return statuses;

  }

  private List getProductHealth() {
    if (getFmUp()) {
      return this.fm.safeGetTopNProducts(TOP_N_PRODUCTS);
    } else {
      return new Vector();
    }
  }

  private List getJobStatusHealth() {
    if (!getWmUp()) {
      return new Vector();
    }

    List statuses = new Vector();
    List states = this.statesFile.getStates();

    if (states != null && states.size() > 0) {
      for (Object state1 : states) {
        String state = (String) state1;
        int numPipelines = this.wm.safeGetNumWorkflowInstancesByStatus(state);
        if (numPipelines == -1) {
          numPipelines = 0;
        }

        JobHealthStatus jobStatus = new JobHealthStatus();
        jobStatus.setStatus(state);
        jobStatus.setNumPipelines(numPipelines);
        statuses.add(jobStatus);
      }
    }

    return statuses;
  }

  private List getIngestHealth() {
    if (this.crawlProps.getCrawlers().size()==0) {
      return new Vector();
    }

    List statuses = new Vector();

    for (Object o : this.crawlProps.getCrawlers()) {
      CrawlInfo info = (CrawlInfo) o;
      String crawlUrlStr = "http://" + this.crawlProps.getCrawlHost() + ":"
                           + info.getCrawlerPort();
      try {
        CrawlDaemonController controller = new CrawlDaemonController(
            crawlUrlStr);
        CrawlerHealth health = new CrawlerHealth();
        health.setCrawlerName(info.getCrawlerName());
        health.setNumCrawls(controller.getNumCrawls());
        health
            .setAvgCrawlTime((double) (controller.getAverageCrawlTime() / DOUBLE));
        statuses.add(health);

      } catch (Exception e) {
        CrawlerHealth health = new CrawlerHealth();
        health.setCrawlerName(info.getCrawlerName());
        health.setNumCrawls(CRAWLER_DOWN_INT);
        health.setAvgCrawlTime(CRAWLER_DOWN_DOUBLE);
        statuses.add(health);
      }

    }

    return statuses;
  }

  private void printIngestStatusHealth(PCSHealthMonitorReport report) {
    if (report.getCrawlerHealthStatus() != null
        && report.getCrawlerHealthStatus().size() > 0) {
      for (Object o : report.getCrawlerHealthStatus()) {
        CrawlerHealth health = (CrawlerHealth) o;
        System.out.print(health.getCrawlerName() + ":");
        if (health.getNumCrawls() == CRAWLER_DOWN_INT) {
          System.out.println(" DOWN");
        } else {
          System.out.println("");
          System.out.println("Number of Crawls: " + health.getNumCrawls());
          System.out.println("Average Crawl Time (seconds): "
                             + health.getAvgCrawlTime());
          System.out.println("");
        }

      }

    }
  }

  private void printJobStatusHealth(PCSHealthMonitorReport report) {
    if (report.getJobHealthStatus() != null
        && report.getJobHealthStatus().size() > 0) {
      for (Object o : report.getJobHealthStatus()) {
        JobHealthStatus status = (JobHealthStatus) o;
        System.out.println(status.getNumPipelines() + " pipelines "
                           + status.getStatus());
      }
    }
  }

  private void printProductHealth(PCSHealthMonitorReport report) {
    if (report.getLatestProductsIngested() != null
        && report.getLatestProductsIngested().size() > 0) {
      System.out.println("Latest " + TOP_N_PRODUCTS + " products ingested:");
      for (Object o : report.getLatestProductsIngested()) {
        Product p = (Product) o;
        p.setProductType(fm.safeGetProductTypeById(p.getProductType()
                                                    .getProductTypeId()));
        p.setProductReferences(fm.safeGetProductReferences(p));
        Metadata prodMet = fm.safeGetMetadata(p);
        System.out.println(fm.getFilePath(p) + " at: "
                           + prodMet.getMetadata("CAS." + PRODUCT_RECEVIED_TIME));
      }

    }
  }

  private void printBatchStubs(PCSHealthMonitorReport report) {
    if (report.getBatchStubStatus() != null
        && report.getBatchStubStatus().size() > 0) {
      for (Object o : report.getBatchStubStatus()) {
        PCSDaemonStatus batchStatus = (PCSDaemonStatus) o;
        System.out.println("> " + batchStatus.getDaemonName() + ": ["
                           + batchStatus.getUrlStr() + "]: " + batchStatus.getStatus());
      }

    }
  }

  private void printCrawlers(PCSHealthMonitorReport report) {
    if (report.getCrawlerStatus() != null
        && report.getCrawlerStatus().size() > 0) {
      List crawlers = this.crawlProps.getCrawlers();
      String biggestString = getBiggestString(crawlers);

      for (Object o : report.getCrawlerStatus()) {
        CrawlerStatus status = (CrawlerStatus) o;
        String crawlerUrlStr = "http://" + status.getCrawlHost() + ":"
                               + status.getInfo().getCrawlerPort();
        System.out.println(getStrPadding(status.getInfo().getCrawlerName(),
            biggestString)
                           + status.getInfo().getCrawlerName()
                           + ": ["
                           + crawlerUrlStr
                           + "]: "
                           + status.getStatus());
      }

    }

  }

  private void quickPrintCrawlers() {
    List crawlers = this.crawlProps.getCrawlers();
    String crawlHost = this.crawlProps.getCrawlHost();

    if (crawlers != null && crawlers.size() > 0) {
      Collections.sort(crawlers, new Comparator() {
        public int compare(Object o1, Object o2) {
          CrawlInfo c1 = (CrawlInfo) o1;
          CrawlInfo c2 = (CrawlInfo) o2;

          return c1.getCrawlerName().compareTo(c2.getCrawlerName());
        }

      });

      String biggestString = getBiggestString(crawlers);
      for (Object crawler : crawlers) {
        CrawlInfo info = (CrawlInfo) crawler;
        String crawlerUrlStr = "http://" + crawlHost + ":"
                               + info.getCrawlerPort();
        System.out.println(getStrPadding(info.getCrawlerName(), biggestString)
                           + info.getCrawlerName() + ": [" + crawlerUrlStr + "]: "
                           + printUp(getCrawlerUp(crawlerUrlStr)));
      }
    }

  }

  private void quickPrintBatchStubs() {
    List resNodes;

    if (getRmUp()) {
      // only print if the resource manager is up
      resNodes = rm.safeGetResourceNodes();

      if (resNodes != null && resNodes.size() > 0) {
        for (Object resNode : resNodes) {
          ResourceNode node = (ResourceNode) resNode;
          System.out.println("> " + BATCH_STUB_DAEMON_NAME + ": ["
                             + node.getIpAddr() + "]: " + printUp(getBatchStubUp(node)));
        }
      }
    }
  }

  private void quickPrintJobStatusHealth() {
    if (!getWmUp()) {
      return;
    }

    List states = this.statesFile.getStates();

    if (states != null && states.size() > 0) {
      for (Object state1 : states) {
        String state = (String) state1;
        int numPipelines = this.wm.safeGetNumWorkflowInstancesByStatus(state);
        if (numPipelines == -1) {
          numPipelines = 0;
        }
        System.out.println(numPipelines + " pipelines " + state);

      }
    }
  }

  private void quickPrintProductHealth() {
    if (getFmUp()) {
      System.out.println("Latest " + TOP_N_PRODUCTS + " products ingested:");

      List prods = this.fm.safeGetTopNProducts(TOP_N_PRODUCTS);

      if (prods != null && prods.size() > 0) {
        for (Object prod : prods) {
          Product p = (Product) prod;
          p.setProductType(fm.safeGetProductTypeById(p.getProductType()
                                                      .getProductTypeId()));
          p.setProductReferences(fm.safeGetProductReferences(p));
          Metadata prodMet = fm.safeGetMetadata(p);
          System.out.println(fm.getFilePath(p) + " at: "
                             + prodMet.getMetadata("CAS." + PRODUCT_RECEVIED_TIME));
        }
      }

    }
  }

  private void quickPrintIngestStatusHealth() {
    if (this.crawlProps.getCrawlers().size()==0) {
      return;
    }

    for (Object o : this.crawlProps.getCrawlers()) {
      CrawlInfo info = (CrawlInfo) o;
      String crawlUrlStr = "http://" + this.crawlProps.getCrawlHost() + ":"
                           + info.getCrawlerPort();
      try {
        CrawlDaemonController controller = new CrawlDaemonController(
            crawlUrlStr);
        System.out.println(info.getCrawlerName() + ":");
        System.out.println("Number of Crawls: " + controller.getNumCrawls());
        System.out.println("Average Crawl Time (seconds): "
                           + (double) (controller.getAverageCrawlTime() / DOUBLE1));
        System.out.println("");

      } catch (Exception e) {
        System.out.println(info.getCrawlerName() + ": DOWN");
      }

    }
  }

  private String getBiggestString(List crawlInfos) {
    int biggestStrSz = Integer.MIN_VALUE;
    String biggestStr = null;

    for (Object crawlInfo : crawlInfos) {
      CrawlInfo info = (CrawlInfo) crawlInfo;
      String crawlInfoName = info.getCrawlerName();
      if (crawlInfoName.length() > biggestStrSz) {
        biggestStr = crawlInfoName;
        biggestStrSz = biggestStr.length();
      }
    }

    return biggestStr;
  }

  private String getStrPadding(String initString, String compareString) {
    int sizeInitStr = initString.length();
    int sizeCompareStr = compareString.length();

    int diff = Math.abs(sizeInitStr - sizeCompareStr);
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < diff; i++) {
      buf.append(" ");
    }

    return buf.toString();
  }

  private boolean getBatchStubUp(ResourceNode node) {

    NettyTransceiver client;
    AvroRpcBatchStub proxy;
    try {
      client = new NettyTransceiver(new InetSocketAddress(node.getIpAddr().getHost(), node.getIpAddr().getPort()));
      proxy = (AvroRpcBatchStub) SpecificRequestor.getClient(AvroRpcBatchStub.class, client);
      return proxy.isAlive();
    } catch (IOException e) {
      return false;
    }
  }

  private boolean getCrawlerUp(String crawlUrlStr) {
    CrawlDaemonController controller;

    try {
      controller = new CrawlDaemonController(crawlUrlStr);
      return controller.isRunning();
    } catch (Exception e) {
      return false;
    }
  }

  private boolean getFmUp() {
    return fm.getFmgrClient() != null && fm.getFmgrClient().isAlive();
  }

  private boolean getWmUp() {
    try {
      wm.getClient().getRegisteredEvents();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean getRmUp() {
    try {
      rm.getClient().getNodes();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String printUp(boolean upFlag) {
    return upFlag ? STATUS_UP : STATUS_DOWN;
  }

}

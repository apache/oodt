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

package org.apache.oodt.pcs.services;

//JDK imports
import java.net.MalformedURLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//JAX-RS imports
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

//JSON imports
import net.sf.json.JSONObject;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.pcs.health.CrawlerHealth;
import org.apache.oodt.pcs.health.CrawlerStatus;
import org.apache.oodt.pcs.health.JobHealthStatus;
import org.apache.oodt.pcs.health.PCSDaemonStatus;
import org.apache.oodt.pcs.health.PCSHealthMonitorMetKeys;
import org.apache.oodt.pcs.health.PCSHealthMonitorReport;
import org.apache.oodt.pcs.tools.PCSHealthMonitor;
import org.apache.oodt.pcs.util.FileManagerUtils;

/**
 * 
 * The JAX-RS resource exposing the {@link PCSHealthMonitor}.
 * 
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
@Path("health")
public class HealthResource extends PCSService {

  private static final long serialVersionUID = -7768836001459227323L;

  private static final Logger LOG = Logger.getLogger(HealthResource.class
      .getName());

  private PCSHealthMonitor mon;

  public HealthResource() throws MalformedURLException, InstantiationException {
    super();
    mon = new PCSHealthMonitor(PCSService.conf.getFmUrl().toString(),
        PCSService.conf.getWmUrl().toString(), PCSService.conf.getRmUrl()
            .toString(), PCSService.conf.getCrawlerConfigFilePath(),
        PCSService.conf.getWorkflowStatusesFilePath());
  }

  @GET
  @Path("report")
  @Produces("text/plain")
  public String healthReport() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("daemonStatus", this.encodeDaemonOutput(report));
    output.put("crawlerStatus", this.encodeCrawlerHealthReportOutput(report));
    output.put("latestFiles", this.encodeLatestFilesOutput(report));
    output.put("jobHealth", this.encodeJobHealthStatusList(report));
    output.put("ingestHealth", this.encodeIngestHealthList(report));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/ingest")
  @Produces("text/plain")
  public String ingestReport() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("ingestHealth", this.encodeIngestHealthList(report));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/ingest/{cname}")
  @Produces("text/plain")
  public String ingestReportByName(@PathParam("cname") String crawlerName) {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("ingestHealth", this
        .encodeIngestHealthList(report, crawlerName));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/jobs")
  @Produces("text/plain")
  public String jobsReport() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("jobHealth", this.encodeJobHealthStatusList(report));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/jobs/{state}")
  @Produces("text/plain")
  public String jobsReportByState(@PathParam("state") String jobState) {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("jobHealth", this.encodeJobHealthStatusList(report, jobState));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/daemon")
  @Produces("text/plain")
  public String daemonReport() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("daemonStatus", this.encodeDaemonOutput(report));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/daemon/{dname}")
  @Produces("text/plain")
  public String daemonReportByName(@PathParam("dname") String daemonName) {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("daemonStatus", this.encodeDaemonOutput(report, daemonName));
    return this.encodeReportAsJson(output);

  }

  @GET
  @Path("report/crawlers")
  @Produces("text/plain")
  public String crawlerHealthReport() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("crawlerStatus", this.encodeCrawlerHealthReportOutput(report));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/crawlers/{cname}")
  @Produces("text/plain")
  public String getCrawlerHealthReportByName(
      @PathParam("cname") String crawlerName) {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("crawlerStatus", this.encodeCrawlerHealthReportOutput(report,
        crawlerName));
    return this.encodeReportAsJson(output);
  }

  @GET
  @Path("report/latestfiles")
  @Produces("text/plain")
  public String getLatestIngestedFiles() {
    PCSHealthMonitorReport report = mon.getReport();
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("generated", report.getCreateDateIsoFormat());
    output.put("latestFiles", this.encodeLatestFilesOutput(report));
    return this.encodeReportAsJson(output);
  }

  private String encodeReportAsJson(Map<String, Object> reportHash) {
    JSONObject response = new JSONObject();
    response.put("report", reportHash);
    return response.toString();
  }

  private Map<String, Object> encodeCrawlerHealth(CrawlerHealth health) {
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("crawler", health.getCrawlerName());
    output.put("avgCrawlTime", health.getAvgCrawlTime());
    output.put("numCrawls", health.getNumCrawls());
    return output;
  }

  private Map<String, Object> encodeJobStatus(JobHealthStatus status) {
    Map<String, Object> output = new ConcurrentHashMap<String, Object>();
    output.put("state", status.getStatus());
    output.put("numJobs", status.getNumPipelines());
    return output;
  }

  private void encodeLatestFile(List<Object> latestFilesOutput, Product p)
      throws MalformedURLException {
    FileManagerUtils fm = new FileManagerUtils(PCSService.conf.getFmUrl());
    p.setProductType(fm.safeGetProductTypeById(p.getProductType()
        .getProductTypeId()));
    p.setProductReferences(fm.safeGetProductReferences(p));
    Metadata prodMet = fm.safeGetMetadata(p);
    if (prodMet == null) {
      prodMet = new Metadata();
    }
    Map<String, Object> fileOutput = new ConcurrentHashMap<String, Object>();
    fileOutput.put("filepath", fm.getFilePath(p));
    fileOutput.put("receivedTime", prodMet.getMetadata("CAS."
        + CoreMetKeys.PRODUCT_RECEVIED_TIME) != null ? prodMet
        .getMetadata("CAS." + CoreMetKeys.PRODUCT_RECEVIED_TIME) : "UNKNOWN");
    latestFilesOutput.add(fileOutput);
  }

  private Map<String, String> encodeCrawlerStatus(CrawlerStatus status) {
    Map<String, String> output = new ConcurrentHashMap<String, String>();
    output.put("crawlerName", status.getInfo().getCrawlerName());
    output.put("crawlerPort", status.getInfo().getCrawlerPort());
    output.put("url", status.getCrawlHost());
    output.put("status", status.getStatus());
    return output;
  }

  private List<Object> encodeIngestHealthList(PCSHealthMonitorReport report,
      String... crawlerName) {
    List<Object> crawlerHealthList = new Vector<Object>();
    if (crawlerName.length > 0) {
      boolean found = false;
      for (CrawlerHealth ch : (List<CrawlerHealth>) (List<?>) report
          .getCrawlerHealthStatus()) {
        if (ch.getCrawlerName().equals(crawlerName[0])) {
          crawlerHealthList.add(this.encodeCrawlerHealth(ch));
          found = true;
          break;
        }
      }
      if (!found) {
        throw new ResourceNotFoundException(
            "No ingest crawler found with name: [" + crawlerName[0] + "]");
      }
    } else {
      for (CrawlerHealth ch : (List<CrawlerHealth>) (List<?>) report
          .getCrawlerHealthStatus()) {
        crawlerHealthList.add(this.encodeCrawlerHealth(ch));
      }
    }
    return crawlerHealthList;
  }

  private List<Object> encodeJobHealthStatusList(PCSHealthMonitorReport report,
      String... jobState) {
    List<Object> jobStatusList = new Vector<Object>();
    if (jobState.length > 0) {
      boolean found = false;
      for (JobHealthStatus js : (List<JobHealthStatus>) (List<?>) report
          .getJobHealthStatus()) {
        if (js.getStatus().equals(jobState[0])) {
          jobStatusList.add(this.encodeJobStatus(js));
          found = true;
          break;
        }
      }
      if (!found) {
        throw new ResourceNotFoundException(
            "Unable to find any jobs with associated state: [" + jobState[0]
            + "]");
      }
    } else {
      for (JobHealthStatus js : (List<JobHealthStatus>) (List<?>) report
          .getJobHealthStatus()) {
        jobStatusList.add(this.encodeJobStatus(js));
      }
    }
    return jobStatusList;
  }

  private Map<String, Object> encodeLatestFilesOutput(
      PCSHealthMonitorReport report) {
    Map<String, Object> latestFilesOutput = new ConcurrentHashMap<String, Object>();
    latestFilesOutput.put("topN", PCSHealthMonitor.TOP_N_PRODUCTS);
    List<Object> latestFilesList = new Vector<Object>();
    if (report != null && 
        report.getLatestProductsIngested() != null && 
        report.getLatestProductsIngested().size() > 0){
      for (Product prod : (List<Product>) (List<?>) report
          .getLatestProductsIngested()) {
        try {
          this.encodeLatestFile(latestFilesList, prod);
        } catch (MalformedURLException e) {
          LOG.log(Level.WARNING, "Unable to encode latest file: ["
              + prod.getProductName() + "]: error: Message: " + e.getMessage());
        }
      }
    }
    latestFilesOutput.put("files", latestFilesList);
    return latestFilesOutput;
  }

  private List<Object> encodeCrawlerHealthReportOutput(
      PCSHealthMonitorReport report, String... crawlerName) {
    List<Object> crawlerOutput = new Vector<Object>();
    if (crawlerName.length > 0) {
      boolean found = false;
      for (CrawlerStatus cs : (List<CrawlerStatus>) (List<?>) report
          .getCrawlerStatus()) {
        if (cs.getInfo().getCrawlerName().equals(crawlerName[0])) {
          crawlerOutput.add(this.encodeCrawlerStatus(cs));
          found = true;
          break;
        }
      }
      if (!found) {
        throw new ResourceNotFoundException(
            "Unable to find any crawlers with name: [" + crawlerName[0] + "]");
      }
    } else {
      for (CrawlerStatus cs : (List<CrawlerStatus>) (List<?>) report
          .getCrawlerStatus()) {
        crawlerOutput.add(this.encodeCrawlerStatus(cs));
      }
    }

    return crawlerOutput;
  }

  private Map<String, Object> encodeDaemonOutput(PCSHealthMonitorReport report,
      String... daemonName) {
    Map<String, Object> daemonOutput = new ConcurrentHashMap<String, Object>();
    if (daemonName.length > 0) {
      if (daemonName[0].equals("fm")) {
        daemonOutput.put("fm", this.encodeDaemonStatus(report.getFmStatus()));
      } else if (daemonName[0].equals("wm")) {
        daemonOutput.put("wm", this.encodeDaemonStatus(report.getWmStatus()));
      } else if (daemonName[0].equals("rm")) {
        daemonOutput.put("rm", this.encodeDaemonStatus(report.getRmStatus()));
      } else if (daemonName[0].equals("stubs")) {
        if (report.getRmStatus().getStatus().equals(
            PCSHealthMonitorMetKeys.STATUS_UP)) {
          // print out the batch stubs
          List<Object> stubs = new Vector<Object>();
          for (PCSDaemonStatus bStatus : (List<PCSDaemonStatus>) (List<?>) report
              .getBatchStubStatus()) {
            stubs.add(this.encodeDaemonStatus(bStatus));
          }
          daemonOutput.put("stubs", stubs);
        } else {
          throw new ResourceNotFoundException(
              "Resource Manager not running so no batch stubs to check.");
        }
      } else {
        throw new ResourceNotFoundException("Daemon not found");
      }
    } else {
      daemonOutput.put("fm", this.encodeDaemonStatus(report.getFmStatus()));
      daemonOutput.put("wm", this.encodeDaemonStatus(report.getWmStatus()));
      daemonOutput.put("rm", this.encodeDaemonStatus(report.getRmStatus()));
      if (report.getRmStatus().getStatus().equals(
          PCSHealthMonitorMetKeys.STATUS_UP)) {
        // print out the batch stubs
        List<Object> stubs = new Vector<Object>();
        for (PCSDaemonStatus bStatus : (List<PCSDaemonStatus>) (List<?>) report
            .getBatchStubStatus()) {
          stubs.add(this.encodeDaemonStatus(bStatus));
        }
        daemonOutput.put("stubs", stubs);
      }
    }
    return daemonOutput;
  }

  private Map<String, String> encodeDaemonStatus(PCSDaemonStatus status) {
    Map<String, String> output = new ConcurrentHashMap<String, String>();
    output.put("daemon", status.getDaemonName());
    output.put("url", status.getUrlStr());
    output.put("status", status.getStatus());
    return output;
  }
}

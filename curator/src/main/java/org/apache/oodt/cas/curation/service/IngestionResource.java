/*
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


package org.apache.oodt.cas.curation.service;

//OODT imports
import org.apache.oodt.cas.curation.structs.IngestionTask;
import org.apache.oodt.cas.curation.util.DateUtils;
import org.apache.oodt.cas.curation.util.ExtractorConfigReader;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//JAX-RS imports
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

//JSON imports
import net.sf.json.JSONObject;

/**
 * 
 * Leverages CAS {@link Ingester} interface to ingest Products into the CAS File
 * Manager via CAS Curator and a REST-ful interface.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
@Path("ingest")
public class IngestionResource extends CurationService {

  private static final long serialVersionUID = -7514150767897700936L;

  private static final Logger LOG = Logger.getLogger(IngestionResource.class
      .getName());

  private static final String DATA_TRANSFER_SERVICE = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  private static final String RESP_SUCCESS = "success";

  private IngestionTaskList taskList;

  public IngestionResource() {
    super();
    this.taskList = new IngestionTaskList();
    IngestionTask task = new IngestionTask();
    task.setCreateDate(new Date());

  }

  @Context
  UriInfo uriInfo;

  @GET
  @Path("create")
  @Produces("text/plain")
  public String createTask(@QueryParam("files") String fileList,
      @QueryParam("numfiles") Integer numFiles,
      @QueryParam("metExtCfgId") String metExtractorConfigId,
      @QueryParam("policy") String policy,
      @QueryParam("ptype") String productType) {

    IngestionTask newTask = new IngestionTask();
    newTask.setCreateDate(new Date());
    try {
      newTask.setExtConf(ExtractorConfigReader.readFromDirectory(new File(
          CurationService.config.getMetExtrConfUploadPath()),
          metExtractorConfigId));
    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = "Unable to load extractor config from metExtCfgId: ["
          + metExtractorConfigId + "]";
      LOG.log(Level.WARNING, errorMsg);
      return errorMsg;
    }
    newTask.setFileList(deducePaths(Arrays.asList(fileList.split(","))));
    newTask.setPolicy(policy);
    newTask.setProductType(productType);
    newTask.setStatus(IngestionTask.NOT_STARTED);
    return this.taskList.addIngestionTask(newTask);
  }

  @GET
  @Path("list")
  @Produces("text/plain")
  public String getIngestTaskList(
      @QueryParam("format") @DefaultValue(FORMAT_HTML) String format) {
    if (format.equals(FORMAT_HTML)) {
      return this.encodeTaskListAsHTML(this.taskList.getTaskList());
    } else if (format.equals(FORMAT_JSON)) {
      return this.encodeTaskListAsJSON(this.taskList.getTaskList());
    } else {
      return "Unsupported Format!";
    }
  }

  @GET
  @Path("start")
  @Produces("text/plain")
  public String doIngest(@QueryParam("taskId") String ingestTaskId) {
    IngestionTask task = this.taskList.getIngestionTaskById(ingestTaskId);
    if (task == null) {
      String errorMsg = "Task with ID [" + ingestTaskId
          + "] is not being managed by this Ingestion Resource!";
      LOG.log(Level.WARNING, errorMsg);
      return this.encodeIngestResponseAsJSON(false, errorMsg);
    }

    Ingester ingest = this.configureIngester();
    MetadataResource metService = new MetadataResource();
    for (String file : task.getFileList()) {
      Metadata fileMet = null;
      try {
        String vFilePath = this.getVirtualPath(CurationService.config
            .getStagingAreaPath(), file);
        LOG.log(Level.FINE,
            "IngestionResource: getting staging metadata for virtual path: ["
                + vFilePath + "]");
        fileMet = metService.getStagingMetadata(vFilePath, task.getExtConf()
            .getIdentifier(), false);
      } catch (Exception e) {
        e.printStackTrace();
        return this.encodeIngestResponseAsHTML(false, e.getMessage());
      }

      try {
        ingest.ingest(safeGetUrl(CurationService.config.getFileMgrURL()),
            new File(file), fileMet);
      } catch (IngestException e) {
        e.printStackTrace();
        return this.encodeIngestResponseAsHTML(false, e.getMessage());
      }

      // set task status to success
      task.setStatus(IngestionTask.FINISHED);
    }

    return this.encodeIngestResponseAsHTML(true, null);
  }

  private String encodeTaskListAsHTML(List<IngestionTask> taskList) {
    StringBuffer out = new StringBuffer();

    for (IngestionTask task : taskList) {
      out.append("<tr>");
      out.append("<td>");
      out.append(task.getId());
      out.append("</td><td>");
      out.append(DateUtils.getDateAsISO8601String(task.getCreateDate()));
      out.append("</td><td>");
      out.append(task.getFileList().size());
      out.append("</td><td>");
      out.append(task.getPolicy());
      out.append("</td><td>");
      out.append(task.getProductType());
      out.append("</td><td>");
      out.append(task.getExtConf().getIdentifier());
      out.append("</td><td>");
      out.append(task.getExtConf().getConfigFiles().size());
      out.append("</td><td id='");
      out.append(task.getId());
      out.append("_Status'>");
      out.append(task.getStatus());
      out.append("</td>");
      if (!task.getStatus().equals(IngestionTask.FINISHED)) {
        out
            .append("<td><input type=\"button\" rel=\"_taskid_\" value=\"Start\" onclick=\"startIngestionTask('");
        out.append(task.getId());
        out.append("')\"/></td>");
      } else {
        out.append("<td>N/A</td>");
      }

      out.append("</tr>");
    }
    return out.toString();
  }

  private String encodeTaskListAsJSON(List<IngestionTask> taskList) {
    List<Map<String, String>> jsonFriendlyTaskList = new Vector<Map<String, String>>();
    for (IngestionTask task : taskList) {
      Map<String, String> taskPropMap = new HashMap<String, String>();
      taskPropMap.put("id", task.getId());
      taskPropMap.put("createDate", DateUtils.getDateAsISO8601String(task
          .getCreateDate()));
      taskPropMap.put("policy", task.getPolicy());
      taskPropMap.put("productType", task.getProductType());
      taskPropMap.put("status", task.getStatus());
      taskPropMap.put("fileList", task.getFileList().toString());
      taskPropMap.put("extractorClass", task.getExtConf().getClassName());
      taskPropMap.put("extractorConfFiles", task.getExtConf().getConfigFiles()
          .toString());
      jsonFriendlyTaskList.add(taskPropMap);
    }

    JSONObject resObj = new JSONObject();
    resObj.put("taskList", jsonFriendlyTaskList);
    return resObj.toString();

  }

  private String encodeIngestResponseAsHTML(boolean success, String msg) {
    StringBuffer out = new StringBuffer();
    if (success) {
      out.append("Success");
    } else {
      out.append(msg);
    }
    return out.toString();
  }

  private String encodeIngestResponseAsJSON(boolean success, String msg) {
    Map<String, Object> resMap = new HashMap<String, Object>();
    resMap.put("success", success);
    resMap.put("msg", msg);
    JSONObject resObj = new JSONObject();
    resObj.putAll(resMap);
    return resObj.toString();

  }

  private Ingester configureIngester() {
    StdIngester ingest = new StdIngester(DATA_TRANSFER_SERVICE);
    return ingest;
  }

  private URL safeGetUrl(String urlStr) {
    try {
      return new URL(urlStr);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private List<String> deducePaths(List<String> vPaths) {
    List<String> absolutePaths = new Vector<String>();
    String stagingIngestPath = CurationService.config.getStagingAreaPath();
    if (!stagingIngestPath.endsWith("/")) {
      stagingIngestPath += "/";
    }

    for (String vPath : vPaths) {
      String realPath = stagingIngestPath + vPath;
      absolutePaths.add(realPath);
    }

    return absolutePaths;
  }

  private String getVirtualPath(String stagingAreaPath, String fullFilePath) {
    int startIdx = stagingAreaPath.length();
    try {
      return fullFilePath.substring(startIdx);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  class IngestionTaskList {

    private HashMap<String, IngestionTask> taskMap;

    public IngestionTaskList() {
      this.taskMap = new HashMap<String, IngestionTask>();
    }

    public synchronized String addIngestionTask(IngestionTask task) {
      this.provideTaskId(task);
      taskMap.put(task.getId(), task);
      return task.getId();
    }

    public synchronized void removeIngestionTask(String taskId) {
      taskMap.remove(taskId);
    }

    public IngestionTask getIngestionTaskById(String taskId) {
      return taskMap.get(taskId);
    }

    public List<IngestionTask> getTaskList() {
      List<IngestionTask> taskList = Arrays.asList(taskMap.values().toArray(
          new IngestionTask[taskMap.values().size()]));
      Collections.sort(taskList, new Comparator<IngestionTask>() {

        public int compare(IngestionTask o1, IngestionTask o2) {
          if (o1.getCreateDate().before(o2.getCreateDate())) {
            return -1;
          } else if (o1.getCreateDate().equals(o2.getCreateDate())) {
            return 0;
          } else
            return 1;
        }
      });
      return taskList;
    }

    private void provideTaskId(IngestionTask task) {
      UUID id = UUID.randomUUID();
      task.setId(id.toString());
    }

  }

}

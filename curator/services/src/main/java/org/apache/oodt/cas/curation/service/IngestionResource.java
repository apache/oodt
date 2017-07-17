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

import org.apache.oodt.cas.curation.structs.ExtractorConfig;
//OODT imports
import org.apache.oodt.cas.curation.structs.IngestionTask;
import org.apache.oodt.cas.curation.util.DateUtils;
import org.apache.oodt.cas.curation.util.ExtractorConfigReader;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.Metadata;

import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

//JDK imports
//JAX-RS imports
//JSON imports

/**
 * 
 * Leverages CAS {@link Ingester} interface to ingest Products into the CAS File
 * Manager via CAS Curator and a REST-ful interface.
 * 
 * @author mattmann
 * @author mjoyce
 * @version $Revision$
 * 
 */
@Path("ingest")
public class IngestionResource extends CurationService {

  private static final long serialVersionUID = -7514150767897700936L;

  private static final Logger LOG = Logger
      .getLogger(IngestionResource.class.getName());

  private static final String DATA_TRANSFER_SERVICE = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";

  private static final String RESP_SUCCESS = "success";

  private IngestionTaskList taskList;

  private String taskListSaveLocPath;

  public IngestionResource() {
    super();
    this.taskList = new IngestionTaskList();
    this.taskListSaveLocPath = "/tmp/tasklist.xml";
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
      newTask.setExtConf(ExtractorConfigReader.readFromDirectory(
          new File(CurationService.config.getMetExtrConfUploadPath()),
          metExtractorConfigId));
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      String errorMsg = "Unable to load extractor config from metExtCfgId: ["
          + metExtractorConfigId + "]";
      LOG.log(Level.WARNING, errorMsg);
      return errorMsg;
    }
    newTask.setFileList(deducePaths(Arrays.asList(fileList.split(","))));
    newTask.setPolicy(policy);
    newTask.setProductType(productType);
    newTask.setStatus(IngestionTask.NOT_STARTED);
    String addedTaskID = this.taskList.addIngestionTask(newTask);
    saveTaskListState();
    return addedTaskID;
  }

  @GET
  @Path("init")
  @Produces("text/plain")
  public void initTaskList() {
    loadSavedTaskListState();
  }

  @GET
  @Path("remove")
  @Produces("text/plain")
  public void removeTask(@QueryParam("taskId") String ingestTaskId) {
    this.taskList.removeIngestionTask(ingestTaskId);
    saveTaskListState();
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
      Metadata fileMet;
      try {
        String vFilePath = this
            .getVirtualPath(CurationService.config.getStagingAreaPath(), file);
        LOG.log(Level.FINE,
            "IngestionResource: getting staging metadata for virtual path: ["
                + vFilePath + "]");
        fileMet = metService.getStagingMetadata(vFilePath,
            task.getExtConf().getIdentifier(), false);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, e.getMessage());
        return this.encodeIngestResponseAsHTML(false, e.getMessage());
      }

      try {
        ingest.ingest(safeGetUrl(CurationService.config.getFileMgrURL()),
            new File(file), fileMet);
      } catch (IngestException e) {
        LOG.log(Level.SEVERE, e.getMessage());
        return this.encodeIngestResponseAsHTML(false, e.getMessage());
      }

      // set task status to success
      task.setStatus(IngestionTask.FINISHED);
    }

    String response = this.encodeIngestResponseAsHTML(true, null);
    saveTaskListState();
    return response;

  }

  private String encodeTaskListAsHTML(List<IngestionTask> taskList) {
    StringBuilder out = new StringBuilder();

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
        out.append(
            "<td><input type=\"button\" rel=\"_taskid_\" value=\"Start\" onclick=\"startIngestionTask('");
        out.append(task.getId());
        out.append("')\"/></td>");
      } else {
        out.append(
            "<td><input type=\"button\" rel=\"_taskid_\" value=\"Remove\" onclick=\"removeIngestionTask('");
        out.append(task.getId());
        out.append("')\"></td>");
      }

      out.append("</tr>");
    }
    return out.toString();
  }

  private String encodeTaskListAsJSON(List<IngestionTask> taskList) {
    List<Map<String, String>> jsonFriendlyTaskList = new Vector<Map<String, String>>();
    for (IngestionTask task : taskList) {
      Map<String, String> taskPropMap = new ConcurrentHashMap<String, String>();
      taskPropMap.put("id", task.getId());
      taskPropMap.put("createDate",
          DateUtils.getDateAsISO8601String(task.getCreateDate()));
      taskPropMap.put("policy", task.getPolicy());
      taskPropMap.put("productType", task.getProductType());
      taskPropMap.put("status", task.getStatus());
      taskPropMap.put("fileList", task.getFileList().toString());
      taskPropMap.put("extractorClass", task.getExtConf().getClassName());
      taskPropMap.put("extractorConfFiles",
          task.getExtConf().getConfigFiles().toString());
      jsonFriendlyTaskList.add(taskPropMap);
    }

    JSONObject resObj = new JSONObject();
    resObj.put("taskList", jsonFriendlyTaskList);
    return resObj.toString();

  }

  private String encodeIngestResponseAsHTML(boolean success, String msg) {
    StringBuilder out = new StringBuilder();
    if (success) {
      out.append("Success");
    } else {
      out.append(msg);
    }
    return out.toString();
  }

  private String encodeIngestResponseAsJSON(boolean success, String msg) {
    Map<String, Object> resMap = new ConcurrentHashMap<String, Object>();
    resMap.put("success", success);
    resMap.put("msg", msg);
    JSONObject resObj = new JSONObject();
    resObj.putAll(resMap);
    return resObj.toString();

  }

  private Ingester configureIngester() {
    return new StdIngester(DATA_TRANSFER_SERVICE);
  }

  private URL safeGetUrl(String urlStr) {
    try {
      return new URL(urlStr);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
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
      LOG.log(Level.SEVERE, e.getMessage());
      return null;
    }
  }

  private void saveTaskListState() {
    this.taskList.exportTaskListAsXMLToFile(this.taskListSaveLocPath);
  }

  private void loadSavedTaskListState() {
    File state = new File(this.taskListSaveLocPath);
    if (state.exists())
      parseXMLState(state);
  }

  private void parseXMLState(File stateFile) {
    try {
      Document document = buildXMLDocument(stateFile);
      rebuildTaskList(document);
    } catch (Exception e) {
      LOG.log(Level.WARNING,
          "parseXMLState: Unable to process saved TaskList state");
    }
  }

  private Document buildXMLDocument(File stateFile)
      throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
        .newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document document = docBuilder.parse(stateFile);
    document.getDocumentElement().normalize();
    return document;
  }

  private void rebuildTaskList(Document document)
      throws FileNotFoundException, IOException, ParseException {

    NodeList tasks = document.getElementsByTagName("Task");

    for (int i = 0; i < tasks.getLength(); i++) {
      Element taskElement = (Element) tasks.item(i);

      String id = getNodeValue(taskElement, "ID");
      String dateString = getNodeValue(taskElement, "CreateDate");
      List<String> newFileList = getTaskFileList(taskElement);
      String policy = getNodeValue(taskElement, "Policy");
      String productType = getNodeValue(taskElement, "ProductType");
      String status = getNodeValue(taskElement, "Status");
      String[] extConfParams = getTaskExtractorConfigParams(taskElement);

      IngestionTask newTask = createTaskFromParameters(id, dateString,
          newFileList, policy, productType, status, extConfParams);

      this.taskList.addIngestionTaskWithoutIdGen(newTask);
    }
  }

  private List<String> getTaskFileList(Element parent) {
    NodeList files = parent.getElementsByTagName("File");
    List<String> newFileList = new Vector<String>();

    for (int i = 0; i < files.getLength(); i++) {
      Element fileNode = (Element) files.item(i);
      newFileList.add(fileNode.getFirstChild().getNodeValue());
    }

    return newFileList;
  }

  private String[] getTaskExtractorConfigParams(Element parent) {
    String[] params = new String[2];

    NodeList extData = parent.getElementsByTagName("ExtractorConfig");
    Element extDataElm = (Element) extData.item(0);
    params[0] = extDataElm.getElementsByTagName("UploadPath").item(0)
        .getFirstChild().getNodeValue();
    params[1] = extDataElm.getElementsByTagName("ID").item(0).getFirstChild()
        .getNodeValue();

    return params;
  }

  private IngestionTask createTaskFromParameters(String id, String dateString,
      List<String> fileList, String policy, String productType, String status,
      final String[] extConfParams)
      throws FileNotFoundException, IOException, ParseException {

    Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",
        Locale.ENGLISH).parse(dateString);
    ExtractorConfig extConf = ExtractorConfigReader
        .readFromDirectory(new File(extConfParams[0]), extConfParams[1]);

    IngestionTask newTask = new IngestionTask();
    newTask.setId(id);
    newTask.setCreateDate(date);
    newTask.setFileList(fileList);
    newTask.setPolicy(policy);
    newTask.setProductType(productType);
    newTask.setStatus(status);
    newTask.setExtConf(extConf);

    return newTask;
  }

  private String getNodeValue(Element element, String tag) {
    NodeList nodeList = element.getElementsByTagName(tag).item(0)
        .getChildNodes();
    return ((Node) nodeList.item(0)).getNodeValue();
  }

  class IngestionTaskList {

    private ConcurrentHashMap<String, IngestionTask> taskMap;

    public IngestionTaskList() {
      this.taskMap = new ConcurrentHashMap<String, IngestionTask>();
    }

    public int getNumberOfTasks() {
      return taskMap.size();
    }

    public synchronized String addIngestionTask(IngestionTask task) {
      this.provideTaskId(task);
      taskMap.put(task.getId(), task);
      return task.getId();
    }

    public synchronized void removeIngestionTask(String taskId) {
      taskMap.remove(taskId);
    }

    public synchronized String addIngestionTaskWithoutIdGen(
        IngestionTask task) {
      taskMap.put(task.getId(), task);
      return task.getId();
    }

    public IngestionTask getIngestionTaskById(String taskId) {
      return taskMap.get(taskId);
    }

    public List<IngestionTask> getTaskList() {
      List<IngestionTask> taskList = Arrays.asList(
          taskMap.values().toArray(new IngestionTask[taskMap.values().size()]));
      Collections.sort(taskList, new Comparator<IngestionTask>() {

        public int compare(IngestionTask o1, IngestionTask o2) {
          if (o1.getCreateDate().before(o2.getCreateDate())) {
            return -1;
          } else if (o1.getCreateDate().equals(o2.getCreateDate())) {
            return 0;
          } else {
            return 1;
          }
        }
      });
      return taskList;
    }

    // TODO: write method to load ingest task list from XML file
    public void exportTaskListAsXMLToFile(String fileName) {
      try {
        Document xmlDocument = generateXMLDocument();

        OutputFormat format = new OutputFormat(xmlDocument);
        format.setIndenting(true);

        // Output the Document content
        FileOutputStream fos = new FileOutputStream(fileName);
        XMLSerializer serializer = new XMLSerializer(fos, format);
        serializer.asDOMSerializer();
        serializer.serialize(xmlDocument.getDocumentElement());
        fos.close();
      } catch (ParserConfigurationException e) {
        LOG.log(Level.WARNING,
            "IngestionTaskList: Unable to generate XML from task list when exporting to file.");
      } catch (FileNotFoundException e) {
        LOG.log(Level.WARNING,
            "IngestionTaskList: Unable to open file for XML output at "
                + fileName);
      } catch (IOException e) {
        LOG.log(Level.WARNING,
            "IngestionTaskList: IOException while serializing XML.");
      }
    }

    public Document getTaskListAsXML() throws ParserConfigurationException {
      return generateXMLDocument();
    }

    private Document generateXMLDocument() throws ParserConfigurationException {
      DocumentBuilderFactory documentFactory = DocumentBuilderFactory
          .newInstance();
      DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

      Document xmlDocument = documentBuilder.newDocument();

      // Add root "Tasks" node
      Element root = xmlDocument.createElement("Tasks");
      xmlDocument.appendChild(root);

      // For each task, generate the following...
      List<IngestionTask> taskList = getTaskList();
      for (IngestionTask task : taskList) {
        Element taskNode = xmlDocument.createElement("Task");
        root.appendChild(taskNode);

        // Task ID Node
        Element ID = xmlDocument.createElement("ID");
        ID.appendChild(xmlDocument.createTextNode(task.getId()));
        taskNode.appendChild(ID);

        // Task creation date Node
        Element createDate = xmlDocument.createElement("CreateDate");
        createDate.appendChild(
            xmlDocument.createTextNode(task.getCreateDate().toString()));
        taskNode.appendChild(createDate);

        // Task file list Node
        Element fileList = xmlDocument.createElement("Files");
        for (String file : task.getFileList()) {
          // Individual file Node
          Element fileNode = xmlDocument.createElement("File");
          fileNode.appendChild(xmlDocument.createTextNode(file));
          fileList.appendChild(fileNode);
        }
        taskNode.appendChild(fileList);

        // Task policy Node
        Element policy = xmlDocument.createElement("Policy");
        policy.appendChild(xmlDocument.createTextNode(task.getPolicy()));
        taskNode.appendChild(policy);

        // Task product type Node
        Element productType = xmlDocument.createElement("ProductType");
        productType
            .appendChild(xmlDocument.createTextNode(task.getProductType()));
        taskNode.appendChild(productType);

        // Task status Node
        Element status = xmlDocument.createElement("Status");
        status.appendChild(xmlDocument.createTextNode(task.getStatus()));
        taskNode.appendChild(status);

        // Task extractor configuration Node
        Element extractorConf = xmlDocument.createElement("ExtractorConfig");
        // Extractor configuration upload path Node
        Element uploadPath = xmlDocument.createElement("UploadPath");
        uploadPath.appendChild(xmlDocument
            .createTextNode(CurationService.config.getMetExtrConfUploadPath()));
        extractorConf.appendChild(uploadPath);
        // Extractor configuration ID Node
        Element extractID = xmlDocument.createElement("ID");
        extractID.appendChild(
            xmlDocument.createTextNode(task.getExtConf().getIdentifier()));
        extractorConf.appendChild(extractID);
        taskNode.appendChild(extractorConf);
      }

      return xmlDocument;
    }

    private void provideTaskId(IngestionTask task) {
      UUID id = UUID.randomUUID();
      task.setId(id.toString());
    }

  }

}

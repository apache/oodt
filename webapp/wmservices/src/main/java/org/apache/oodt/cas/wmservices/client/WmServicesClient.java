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

package org.apache.oodt.cas.wmservices.client;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.oodt.cas.wmservices.repository.PackagedWorkflowManager;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

/**
 * Client class to connect to cas-wm-services
 * 
 * @author vratnakar
 */
public class WmServicesClient {
  String serverurl;
  String service = "/service/";

  /**
   * Constructor
   * 
   * @param serverurl
   *          the directory where workflow files exist
   */
  public WmServicesClient(String serverurl) {
    this.serverurl = serverurl;
  }

  /**
   * Add a Packaged workflow
   * 
   * @param workflowId
   *          id of the workflow
   * @param workflow
   *          the workflow to be added
   * @return true if operation successful
   * @throws RepositoryException
   */
  public boolean addPackagedWorkflow(String workflowId, Workflow workflow)
      throws RepositoryException {

    try {
      PackagedWorkflowManager editor = new PackagedWorkflowManager();
      String xml = editor.serializeWorkflow(workflow);

      // Now Make a POST call to the servlet with this xml
      String result = this.query("POST", "addPackagedRepositoryWorkflow", "workflowID",
          workflowId, "workflowXML", xml);
      return Boolean.parseBoolean(result);
    } catch (Exception e) {
      throw new RepositoryException("Could not add packaged workflow: "
          + e.getMessage());
    }
  }

  /**
   * Delete a Packaged workflow
   * 
   * @param workflowId
   *          id of the workflow to be deleted
   * @return true if operation successful
   * @throws RepositoryException
   */
  public boolean deletePackagedWorkflow(String workflowId)
      throws RepositoryException {
    try {
      // Now Make a POST call to the servlet with this xml
      String result = this.query("POST", "deletePackagedRepositoryWorkflow", "workflowID",
          workflowId);
      return Boolean.parseBoolean(result);
    } catch (Exception e) {
      throw new RepositoryException("Could not delete packaged workflow: "
          + e.getMessage());
    }
  }
  
  // Private functions

  private String query(String method, String op, Object... args) {
    String url = this.serverurl + this.service + op;
    try {
      String params = "";
      for (int i = 0; i < args.length; i += 2) {
        if (i > 0)
          params += "&";
        params += args[i] + "="
            + URLEncoder.encode(args[i + 1].toString(), "UTF-8");
      }
      if ("GET".equals(method)) {
        URL urlobj = new URL(url + "?" + params);
        return IOUtils.toString(urlobj.openStream());
      } else {
        URL urlobj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) urlobj.openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(params);
        out.flush();
        out.close();

        String result = IOUtils.toString(con.getInputStream());
        con.disconnect();
        return result;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}

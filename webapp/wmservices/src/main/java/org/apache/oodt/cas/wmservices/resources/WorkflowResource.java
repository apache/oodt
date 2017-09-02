/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.wmservices.resources;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.oodt.cas.wmservices.repository.PackagedWorkflowManager;
import org.apache.oodt.cas.workflow.structs.Workflow;

/**
 * Jax-RS server functions for adding/deleting workflows
 * 
 * @author vratnakar
 */
public class WorkflowResource extends AbstractWorkflowServiceResource {
  private static final Logger LOGGER = Logger.getLogger(WorkflowResource.class
      .getName());

  /**
   * Default constructor.
   */
  public WorkflowResource() {
  }

  /**
   * Add Packaged Repository Workflow
   * 
   * @param workflowID
   *          id of the workflow
   * @param workflowXML
   *          xml representation of the workflow
   * @return true if addition successful
   */
  @POST
  @Path("/addPackagedRepositoryWorkflow")
  public boolean addPackagedRepositoryWorkflow(
      @FormParam("workflowID") String workflowID,
      @FormParam("workflowXML") String workflowXML) throws Exception {
    try {
      PackagedWorkflowManager pwmanager = new PackagedWorkflowManager();
      Workflow workflow = pwmanager.parsePackagedWorkflow(workflowID, workflowXML);
      if(workflow == null)
        return false;
      String workflowDir = this.getContextPkgReposDir().getAbsolutePath();
      pwmanager.addWorkflow(workflow, workflowDir);
      return getContextClient().refreshRepository();
    } catch (Exception e) {
      String message = "Unable to add workflow. ";
      message += e.getMessage();
      LOGGER.log(Level.SEVERE, message);
      throw e;
    }
  }

  /**
   * Delete Packaged Repository Workflow
   * 
   * @param workflowID
   *          id of the workflow to delete
   * @return true if deletion successful
   */
  @DELETE
  @Path("/deletePackagedRepositoryWorkflow")
  public boolean deletePackagedRepositoryWorkflow(
      @FormParam("workflowID") String workflowID) throws Exception {
    try {
      File wflowFile = getPackagedRepositoryWorkflowFile(workflowID);
      if (wflowFile.delete())
        return getContextClient().refreshRepository();
      else
        return false;
    } catch (Exception e) {
      String message = "Unable to delete workflow. ";
      message += e.getMessage();
      LOGGER.log(Level.SEVERE, message);
      throw e;
    }
  }

  // Private functions
  private File getPackagedRepositoryWorkflowFile(String workflowID)
      throws Exception {
    return new File(this.getContextPkgReposDir().getAbsolutePath()
        + File.separator + workflowID + ".xml");
  }
}

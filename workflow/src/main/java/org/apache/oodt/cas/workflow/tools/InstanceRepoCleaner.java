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

package org.apache.oodt.cas.workflow.tools;

import org.apache.oodt.cas.workflow.instrepo.LuceneWorkflowInstanceRepository;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowStatus;
import org.apache.oodt.cas.workflow.system.WorkflowManagerClient;
import org.apache.oodt.cas.workflow.system.rpc.RpcCommunicationFactory;
import org.apache.oodt.commons.date.DateUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * Cleans a workflow manager instance repository of ghost jobs that will never
 * execute and cleans up job history and repository status.
 * 
 * @author mattmann
 * 
 */
public class InstanceRepoCleaner implements Closeable {

  /* PGE task statuses */
  public static final String STAGING_INPUT = "STAGING INPUT";

  public static final String CONF_FILE_BUILD = "BUILDING CONFIG FILE";

  public static final String RUNNING_PGE = "PGE EXEC";

  public static final String CRAWLING = "CRAWLING";

  private static final Logger LOG = Logger.getLogger(InstanceRepoCleaner.class.getName());

  private WorkflowManagerClient wm;

  private LuceneWorkflowInstanceRepository rep;

  public InstanceRepoCleaner() {
  }

  public InstanceRepoCleaner(String wmUrlStr) throws MalformedURLException {
    this.wm = RpcCommunicationFactory.createClient(new URL(wmUrlStr));
  }

  public void setInstanceRepo(String idxPath) {
    this.rep = new LuceneWorkflowInstanceRepository(idxPath, 1000);
  }

  public static void main(String[] args) throws Exception {
    String usage = "InstanceRepoCleaner [options]\n"
        + "<workflow manager url>\n" + "--idxPath <path>\n";
    if (args.length != 1 && args.length != 2) {
      System.err.println(usage);
      System.exit(1);
    }

    InstanceRepoCleaner clean = null;
    try {
      if (args.length == 1) {
        String wmUrlStr = args[0];
        clean = new InstanceRepoCleaner(wmUrlStr);
      } else {
        String idxPath = args[1];
        clean = new InstanceRepoCleaner();
        clean.setInstanceRepo(idxPath);
      }
      clean.cleanRepository();
    } finally {
      if (clean != null) {
        clean.close();
      }
    }
  }

  public void cleanRepository() throws Exception {
    WorkflowInstancePage page = wm != null ? wm.getFirstPage() : rep.getFirstPage();
    while (page != null && page.getPageWorkflows() != null && page.getPageWorkflows().size() > 0) {

      LOG.log(Level.INFO,
          "Cleaning workflow instances: page: [" + page.getPageNum() + "] of ["
              + page.getTotalPages() + "]: page size: [" + page.getPageSize()
              + "]");

      for (WorkflowInstance inst : (List<WorkflowInstance>) page.getPageWorkflows()) {
        if (inst.getStatus().equals(WorkflowStatus.CREATED)
            || inst.getStatus().equals(WorkflowStatus.STARTED)
            || inst.getStatus().equals(WorkflowStatus.QUEUED)
            || inst.getStatus().equals(WorkflowStatus.RESMGR_SUBMIT)
            || inst.getStatus().equals(CONF_FILE_BUILD)
            || inst.getStatus().equals(CRAWLING)
            || inst.getStatus().equals(RUNNING_PGE)
            || inst.getStatus().equals(STAGING_INPUT)) {
          String endDateTimeIsoStr = DateUtils.toString(Calendar.getInstance());
          LOG.log(Level.INFO, "Updated workflow instance id: [" + inst.getId()
              + "]: setting end date time to: [" + endDateTimeIsoStr + "]");
          LOG.log(Level.INFO, "Existing status: [" + inst.getStatus()
              + "]: setting to [" + WorkflowStatus.FINISHED + "]");
          inst.setEndDateTimeIsoStr(endDateTimeIsoStr);
          if (inst.getStartDateTimeIsoStr() == null || (inst
                                                            .getStartDateTimeIsoStr().equals(""))) {
            inst.setStartDateTimeIsoStr(endDateTimeIsoStr);
          }
          inst.setStatus(WorkflowStatus.FINISHED);
          if (wm != null) {
            wm.updateWorkflowInstance(inst);
          } else {
            rep.updateWorkflowInstance(inst);
          }
        }
      }

      if (page.isLastPage()) {
        LOG.log(Level.INFO, "Last set of workflow instances cleaned.");
        break;
      }

      page = wm != null ? wm.getNextPage(page) : rep.getNextPage(page);
    }
  }

  @Override
  public void close() throws IOException {
    if (wm != null) {
      wm.close();
    }
  }
}

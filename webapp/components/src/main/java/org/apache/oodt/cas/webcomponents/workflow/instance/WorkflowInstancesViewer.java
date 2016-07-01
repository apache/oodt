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

package org.apache.oodt.cas.webcomponents.workflow.instance;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.webcomponents.workflow.pagination.WorkflowPagePaginator;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceMetMap;
import org.apache.oodt.cas.workflow.instrepo.WorkflowInstanceMetadataReader;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleManager;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.value.ValueMap;

import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowInstancesViewer extends Panel {

  private static Logger LOG = Logger.getLogger(WorkflowInstancesViewer.class.getName());
  private static final long serialVersionUID = -311004303658412137L;

  private WorkflowMgrConn wm;

  private int pageNum;

  private int startIdx;

  private int endIdx;

  private int totalWorkflowInsts;

  private static final int PAGE_SIZE = 20;

  /**
   * @param id the id
   * @param workflowUrlStr the workflow url
   * @param status the status
   * @param pageNum the page number
   * @param wStatuses the workflow statuses
   * @param lifecycleFilePath the lifecycle path
   * @param metInstanceFilePath the met instance file path
   * @param workflowViewer the workflow viewer
   * @param workflowTaskViewer the workflow task viewer
   * @param workflowInstViewer the workflow instviewer
   */
  public WorkflowInstancesViewer(String id, String workflowUrlStr,
      final String status, int pageNum, List<String> wStatuses,
      final String lifecycleFilePath, final String metInstanceFilePath,
      final Class<? extends WebPage> workflowViewer,
      final Class<? extends WebPage> workflowTaskViewer,
      final Class<? extends WebPage> workflowInstViewer) {
    super(id);
    this.wm = new WorkflowMgrConn(workflowUrlStr);
    this.pageNum = pageNum;
    WorkflowInstancePage page;
    System.out.println("STATUS IS "+status);
    if (status.equals("ALL")) {
      page = this.wm.safeGetWorkflowInstPageByStatus(pageNum);
    } else {
      page = this.wm.safeGetWorkflowInstPageByStatus(pageNum, status);
    }

    this.computeStartEndIdx(page);
    add(new ListView<String>("workflow_statuses", new ListModel<String>(
        wStatuses)) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<String> item) {
        Link<String> wStatusLink = new Link<String>("wstatus_link", new Model(
            item.getModelObject())) {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("status", getModelObject());
            setResponsePage(getPage().getClass(), params);

          }
        };

        Label wStatusLabel = new Label("wstatus", item.getModelObject());
        if (item.getModelObject().equals(status)) {
          wStatusLabel.add(new SimpleAttributeModifier("class", "selected"));
        }
        wStatusLink.add(wStatusLabel);
        item.add(wStatusLink);
      }
    });

    add(new Label("start_idx", String.valueOf(this.startIdx)));
    add(new Label("end_idx", String.valueOf(this.endIdx)));
    add(new Label("num_insts", String.valueOf(this.totalWorkflowInsts)));

    add(new ListView<WorkflowInstance>("workflow_insts",
        new ListModel<WorkflowInstance>(page.getPageWorkflows())) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<WorkflowInstance> item) {
        WorkflowInstance inst = item.getModelObject();
        Link<String> workflowLink = new Link<String>("workflow_link", new Model<String>(inst
            .getWorkflow().getId())) {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(workflowViewer, params);
          }
        };
        workflowLink.add(new Label("workflow_name", inst.getWorkflow().getName()));
        item.add(workflowLink);

        String instMetString = getWorkflowInstMet(inst, metInstanceFilePath);
        item.add(new Label("inst_met_display", instMetString));

        ResourceReference imgRef = new ResourceReference(WorkflowInstancesViewer.class,
            "percentImage.png");
        imgRef.bind(getApplication());
        String resRefString = getRequestCycle().urlFor(imgRef, new ValueMap()).toString();
        item.add(new Label("winst_display", "display('" + resRefString
            + "', 'winst_" + inst.getId() + "_progress', "
            + getPctComplete(inst, lifecycleFilePath) + ", 1);").setEscapeModelStrings(false));

        item.add(new Label("winst_status", inst.getStatus()));
        item.add(new Label("winst_wallclock_mins", formatWallClockMins(wm
            .safeGetWorkflowWallClockMinutes(inst))));
        item.add(new Label("winst_task_wallclock_mins", formatWallClockMins(wm
            .safeGetWorkflowCurrentTaskWallClockMinutes(inst))));

        Link<String> taskLink = new Link<String>("task_link", new Model<String>(inst
            .getCurrentTaskId())) {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(workflowTaskViewer, params);

          }
        };
        taskLink.add(new Label("task_name", getTaskNameFromTaskId(inst, inst.getCurrentTaskId())));
        item.add(taskLink);

      }
    });
    
    add(new WorkflowPagePaginator("paginator", page, status, workflowInstViewer));

  }

  private void computeStartEndIdx(WorkflowInstancePage page) {
    if (page.getTotalPages() == 1) {
      this.totalWorkflowInsts = page.getPageWorkflows().size();
      this.pageNum = 1;
    } else if (page.getTotalPages() == 0) {
      this.totalWorkflowInsts = 0;
      this.pageNum = 1;
    } else {
      this.totalWorkflowInsts = (page.getTotalPages() - 1) * PAGE_SIZE;
      this.pageNum = page.getPageNum();

      // get the last page
      WorkflowInstancePage lastPage;
      lastPage = wm.safeGetWorkflowInstPageByStatus(page.getTotalPages());
      this.totalWorkflowInsts += lastPage.getPageWorkflows().size();

    }
    this.endIdx = this.totalWorkflowInsts != 0 ? Math.min(
        this.totalWorkflowInsts, (PAGE_SIZE) * (this.pageNum)) : 0;
    this.startIdx = this.totalWorkflowInsts != 0 ? ((this.pageNum - 1) * PAGE_SIZE) + 1
        : 0;
  }

  private String getWorkflowInstMet(WorkflowInstance inst, String metMapFilePath) {
    WorkflowInstanceMetMap wInstMetMap;
    String metString = null;

    try {
      wInstMetMap = WorkflowInstanceMetadataReader
          .parseMetMapFile(metMapFilePath);
      Metadata instMetadata = wm.getWM().getWorkflowInstanceMetadata(
          inst.getId());
      List<String> wInstFields = wInstMetMap.getFieldsForWorkflow(inst
          .getWorkflow().getId()) != null ? wInstMetMap
          .getFieldsForWorkflow(inst.getWorkflow().getId()) : wInstMetMap
          .getDefaultFields();
      StringBuilder metStrBuf = new StringBuilder();

      for (String wInstField : wInstFields) {
        metStrBuf.append(wInstField);
        metStrBuf.append(":");
        metStrBuf.append(instMetadata.getMetadata(wInstField));
        metStrBuf.append(",");
      }

      metStrBuf.deleteCharAt(metStrBuf.length() - 1);
      metString = metStrBuf.toString();

    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }

    return metString;

  }

  private String formatWallClockMins(double wallClockMins) {
    NumberFormat fn = NumberFormat.getNumberInstance();
    fn.setMaximumFractionDigits(2);
    fn.setMinimumFractionDigits(2);
    return fn.format(wallClockMins);
  }

  private String getPctComplete(WorkflowInstance inst, String lifecycleFilePath) {
    WorkflowLifecycleManager lifecycleMgr = null;
    try {
      lifecycleMgr = new WorkflowLifecycleManager(lifecycleFilePath);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return lifecycleMgr != null ? WorkflowLifecycleManager
        .formatPct((lifecycleMgr.getPercentageComplete(inst)) * 100.0) : null;
  }
  
  private String getTaskNameFromTaskId(WorkflowInstance w, String taskId) {
    if (w.getWorkflow() != null && w.getWorkflow().getTasks() != null
            && w.getWorkflow().getTasks().size() > 0) {
        for(WorkflowTask task: w.getWorkflow().getTasks()){
            if (task.getTaskId().equals(taskId)) {
                return task.getTaskName();
            }
        }

        return null;
    } else {
      return null;
    }
}

}

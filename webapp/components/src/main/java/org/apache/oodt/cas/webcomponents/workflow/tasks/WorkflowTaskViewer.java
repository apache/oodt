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

package org.apache.oodt.cas.webcomponents.workflow.tasks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowTaskViewer extends Panel {

  private static final long serialVersionUID = -1895109076537364697L;

  private WorkflowMgrConn wm;

  /**
   * @param id
   */
  public WorkflowTaskViewer(String id, String wmUrlStr, String taskId, final Class<? extends WebPage> conditionPage) {
    super(id);
    this.wm = new WorkflowMgrConn(wmUrlStr);
    final WorkflowTask task = this.wm.safeGetTaskById(taskId);

    add(new Label("workflow_task_id", task.getTaskId()));
    add(new Label("workflow_task_name", task.getTaskName()));
    add(new Label("workflow_task_class", task.getTaskInstanceClassName()));

    List<String> taskConfigMetKeyNames = Arrays.asList(task.getTaskConfig()
        .getProperties().keySet().toArray(
            new String[task.getTaskConfig().getProperties().size()]));
    Collections.sort(taskConfigMetKeyNames);

    add(new ListView<String>("workflow_config", new ListModel<String>(taskConfigMetKeyNames)){
    
      @Override
      protected void populateItem(ListItem<String> item) {
        String configMetKeyName = item.getModelObject();
        String configMetKeyValue = task.getTaskConfig().getProperty(configMetKeyName);
        
        item.add(new Label("workflow_config_pname", configMetKeyName));
        item.add(new Label("workflow_config_pvalue", configMetKeyValue));
      }
    });
    
    add(new ListView<WorkflowCondition>("workflow_conditions", new ListModel<WorkflowCondition>(task.getConditions())){
      /* (non-Javadoc)
       * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<WorkflowCondition> item) {
         WorkflowCondition cond = item.getModelObject();
         final PageParameters params = new PageParameters();
         params.add("id", cond.getConditionId());
         Link condLink = new Link("condition_page_link"){
               /* (non-Javadoc)
               * @see org.apache.wicket.markup.html.link.Link#onClick()
               */
              @Override
              public void onClick() {
                setResponsePage(conditionPage, params);
              }
          };
          condLink.add(new Label("condition_plink_name", cond.getConditionName()));
          item.add(condLink);
      }
    });
  }

}

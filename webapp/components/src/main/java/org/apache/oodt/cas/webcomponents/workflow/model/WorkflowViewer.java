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

package org.apache.oodt.cas.webcomponents.workflow.model;

import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * Describe your class here.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowViewer extends Panel {

  private static final long serialVersionUID = -157601650785829792L;

  private final WorkflowMgrConn wm;

  public WorkflowViewer(String id, String wmUrlStr, final String workflowId,
      final Class<? extends WebPage> taskPage) {
    super(id);
    this.wm = new WorkflowMgrConn(wmUrlStr);
    final IModel<Workflow> wModel = new LoadableDetachableModel<Workflow>() {

      @Override
      protected Workflow load() {
        return wm.safeGetWorkflowById(workflowId);
      }
    };

    add(new Label("workflow_id", new PropertyModel<Workflow>(wModel, "id")));
    add(new Label("workflow_name", new PropertyModel<Workflow>(wModel, "name")));

    int numTasks = wModel.getObject().getTasks().size();
    String width = String.valueOf((82 * numTasks) + (115 * (numTasks - 1)));
    String height = "82";
    WebMarkupContainer wTable = new WebMarkupContainer("workflow_model_tbl");
    wTable.add(new SimpleAttributeModifier("width", width));
    wTable.add(new SimpleAttributeModifier("height", height));

    wTable.add(new ListView<WorkflowTask>("tasks", new ListModel<WorkflowTask>(
        wModel.getObject().getTasks())) {

      @Override
      protected void populateItem(ListItem<WorkflowTask> item) {
        final PageParameters params = new PageParameters();
        params.add("id", item.getModelObject().getTaskId());
        Link taskLink = new Link("task_link") {
          /*
           * (non-Javadoc)
           * 
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            setResponsePage(taskPage, params);
          }
        };
        String taskName = summarizeWords(item.getModelObject().getTaskName(),
            16, 16);
        taskLink.add(new Label("task_name", taskName).setRenderBodyOnly(true));
        item.add(taskLink);

        if (item.getIndex() == wModel.getObject().getTasks().size() - 1) {
          item.add(new WebMarkupContainer("task_arrow").setVisible(false));
        } else {
          item.add(new WebMarkupContainer("task_arrow"));
        }

      }
    });
    add(wTable);

  }

  /**
   * <p>
   * Summarizes a given String of words (the <code>orig</code> parameter), and
   * limits the size of the individual words in the string by the given
   * <code>wordThreshold</code>, and limits the final size of the final
   * summarized word string by the given <code>maxLengthTotal</code>.
   * </p>
   * 
   * @param orig
   *          The original String to summarize.
   * @param wordThreshhold
   *          The maximum amount of characters for any given word in the string.
   * @param maxLengthTotal
   *          The maximum final size of the summarized set of words.
   * @return A summarized string.
   */
  private String summarizeWords(String orig, int wordThreshhold,
      int maxLengthTotal) {
    String[] words = orig.split(" ");
    StringBuilder summarizedString = new StringBuilder();

    for (String word : words) {
      summarizedString.append(word.substring(0, Math.min(wordThreshhold, word
          .length())));
      summarizedString.append(" ");
    }
    
    // add '...' to end of summarized string if applicable
    if (summarizedString.length() > maxLengthTotal) {
    	return summarizedString.substring(0,
            Math.min(maxLengthTotal, summarizedString.length()) - 3) + "...";
    } else {
      return summarizedString.toString();
    }
  }

}

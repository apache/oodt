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


package org.apache.oodt.cas.webcomponents.workflow.event;

import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import java.util.List;

/**
 *
 * Describe your class here.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class EventToWorkflowViewer extends Panel {

  private static final long serialVersionUID = -5120511045763328237L;

  private final WorkflowMgrConn wm;

  public EventToWorkflowViewer(String id, String workflowUrlStr, final Class<? extends WebPage> viewerPage) {
    super(id);
    this.wm = new WorkflowMgrConn(workflowUrlStr);
    WebMarkupContainer wTable = new WebMarkupContainer("wtable");
    wTable.setVisible(false);
    PropertyModel<List<Workflow>> workflowsModel = new PropertyModel<List<Workflow>>(this, "workflows");
    ListView<Workflow> workflowView = new ListView<Workflow>("workflow_list", workflowsModel) {
      private static final long serialVersionUID = 5894604290395257941L;

      @Override
      protected void populateItem(ListItem<Workflow> item) {
        Link<String> wLink = new Link<String>("workflow_link", new Model(item.getModelObject().getId())){
          
           /* (non-Javadoc)
           * @see org.apache.wicket.markup.html.link.Link#onClick()
           */
          @Override
          public void onClick() {
            PageParameters params = new PageParameters();
            params.add("id", getModelObject());
            setResponsePage(viewerPage, params);
          }
        };
        
        wLink.add(new Label("workflow_name", item.getModelObject().getName()));
        item.add(wLink);
      }
    };

    EventWorkflowForm form = 
      new EventWorkflowForm("event_workflow_frm", workflowsModel, wTable);

    wTable.add(workflowView);
    add(wTable);
    add(form);
  }
  
  class EventWorkflowForm extends Form<List<Workflow>>{

    private static final long serialVersionUID = -3209079518977783358L;
    
    private String selectedEvent;
    
    private WebMarkupContainer wTable;

    public EventWorkflowForm(String id, IModel<List<Workflow>> model, WebMarkupContainer wTable) {
      super(id, model);
      add(new DropDownChoice<String>("event_list", new PropertyModel(this, "selectedEvent"), 
          wm.safeGetRegisteredEvents()));
      this.wTable = wTable;
      
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.form.Form#onSubmit()
     */
    @Override
    protected void onSubmit() {
       List<Workflow> workflows = wm.safeGetWorkflowsByEvent(this.selectedEvent);
       setDefaultModelObject(workflows);
       if(workflows != null && workflows.size() > 0){
         this.wTable.setVisible(true);
         this.wTable.add(new Label("selectedEvent", this.selectedEvent));
       }
    }
  
    public String getSelectedEvent(){
      return this.selectedEvent;
    }
    
  }
  
  


}

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

package org.apache.oodt.cas.webcomponents.workflow.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.wicket.markup.html.basic.Label;
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
public class WorkflowConditionViewer extends Panel {

  private static final long serialVersionUID = 7861466388954745105L;

  private WorkflowMgrConn wm;

  public WorkflowConditionViewer(String id, String wmUrlStr, String conditionId) {
    super(id);
    this.wm = new WorkflowMgrConn(wmUrlStr);
    final WorkflowCondition cond = this.wm.safeGetConditionById(conditionId);
    add(new Label("condition_id", cond.getConditionId()));
    add(new Label("condition_name", cond.getConditionName()));
    add(new Label("condition_class", cond.getConditionInstanceClassName()));
    final WorkflowConditionConfiguration config = cond.getCondConfig() != null ? 
        cond.getCondConfig():new WorkflowConditionConfiguration();
    List<String> condConfigKeyNames = Arrays.asList(config
        .getProperties().keySet().toArray(
            new String[config.getProperties().size()]));
    Collections.sort(condConfigKeyNames);
    add(new ListView<String>("cond_config", new ListModel<String>(
        condConfigKeyNames)) {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.apache.wicket.markup.html.list.ListView#populateItem(org.apache
       * .wicket.markup.html.list.ListItem)
       */
      @Override
      protected void populateItem(ListItem<String> item) {
        String keyName = item.getModelObject();
        String keyVal = config.getProperty(keyName);
        item.add(new Label("cond_pname", keyName));
        item.add(new Label("cond_pvalue", keyVal));
      }
    });

  }

}

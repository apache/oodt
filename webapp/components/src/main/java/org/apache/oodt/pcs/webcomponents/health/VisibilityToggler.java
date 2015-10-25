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

package org.apache.oodt.pcs.webcomponents.health;

//JDK imports
import java.util.List;
import java.util.Vector;

//Wicket imports
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * Toggles a show/hide link for displaying PCSDaemonStatus and CrawlerStatus in
 * the PCS OPSUI gui.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class VisibilityToggler extends WebMarkupContainer {

  private static final long serialVersionUID = -6325486547078308461L;

  /**
   * @param id
   */
  public VisibilityToggler(String id, String showLinkId, String hideLinkId,
      String moreId, final ListModel model) {
    super(id, model);

    Link<Link> showLink;
    Link<Link> hideLink = null;
    final Vector allStatusList = (Vector) ((Vector) model.getObject()).clone();

    // subset the model
    model.setObject(subsetModelObject(model.getObject()));
    final WebMarkupContainer moreComponent = new WebMarkupContainer(moreId);
    add(moreComponent);

    showLink = new Link<Link>(showLinkId, new Model<Link>(null)) {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        Vector obj = (Vector) model.getObject();
        obj.clear();
        obj.addAll(allStatusList);
        model.setObject(obj);
        moreComponent.setVisible(false);
        getModelObject().setVisible(true);
        setVisible(false);
      }
    };

    hideLink = new Link<Link>(hideLinkId, new Model<Link>(showLink)) {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        Vector obj = (Vector) model.getObject();
        obj.clear();
        obj.addAll(getTopN(allStatusList, 3));
        model.setObject(obj);
        getModelObject().setVisible(true);
        setVisible(false);
        moreComponent.setVisible(true);
      }
    };

    hideLink.setVisible(false);
    showLink.setModelObject(hideLink);
    hideLink.setModelObject(showLink);

    add(showLink);
    add(hideLink);
  }

  private static List subsetModelObject(Object obj) {
    List objList = (List) obj;
    return getTopN(objList, 3);
  }

  private static List getTopN(List statuses, int topN) {
    List subset = new Vector();
    if (statuses != null && statuses.size() > 0) {
      int numGobble = topN <= statuses.size() ? topN : statuses.size();
      for (int i = 0; i < numGobble; i++) {
        Object status = statuses.get(i);
        subset.add(status);
      }
    }

    return subset;
  }

}

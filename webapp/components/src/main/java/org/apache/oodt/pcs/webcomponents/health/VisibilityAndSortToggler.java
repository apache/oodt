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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.pcs.health.CrawlerStatus;
import org.apache.oodt.pcs.health.PCSDaemonStatus;

//Wicket imports
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;

/**
 * 
 * Toggles both a show/hide link and a sort/unsort link for displaying
 * PCSDaemonStatus and CrawlerStatus in the PCS OPSUI gui.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
class VisibilityAndSortToggler extends VisibilityToggler {

  private static final long serialVersionUID = 9003502303473741937L;

  /**
   * @param id
   */
  public VisibilityAndSortToggler(String id, String showLinkId,
      String hideLinkId, String sortLinkId, String unsortLinkId, String moreId,
      final ListModel model) {
    super(id, showLinkId, hideLinkId, moreId, model);

    Link<Link> sortLink;
    Link<Link> unsortLink = null;

    sortLink = new Link<Link>(sortLinkId, new Model<Link>(null)) {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        Vector obj = (Vector) model.getObject();
        sortByStatus(obj);
        model.setObject(obj);
        getModelObject().setVisible(true);
        setVisible(false);
      }
    };

    unsortLink = new Link<Link>(unsortLinkId, new Model<Link>(sortLink)) {
      /*
       * (non-Javadoc)
       * 
       * @see org.apache.wicket.markup.html.link.Link#onClick()
       */
      @Override
      public void onClick() {
        Vector obj = (Vector) model.getObject();
        sortByName(obj);
        model.setObject(obj);
        getModelObject().setVisible(true);
        setVisible(false);
      }
    };

    unsortLink.setVisible(false);
    sortLink.setModelObject(unsortLink);
    unsortLink.setModelObject(sortLink);

    add(sortLink);
    add(unsortLink);

  }

  private static void sortByStatus(List statusList) {
    Collections.sort(statusList, new Comparator() {

      public int compare(Object o1, Object o2) {
        if (o1 instanceof CrawlerStatus) {
          CrawlerStatus stat1 = (CrawlerStatus) o1;
          CrawlerStatus stat2 = (CrawlerStatus) o2;

          return stat1.getStatus().compareTo(stat2.getStatus());
        } else if (o1 instanceof PCSDaemonStatus) {
          PCSDaemonStatus stat1 = (PCSDaemonStatus) o1;
          PCSDaemonStatus stat2 = (PCSDaemonStatus) o2;

          return stat1.getStatus().compareTo(stat2.getStatus());
        } else {
          return 0;
        }
      }

    });
  }

  private static void sortByName(List statusList) {
    Collections.sort(statusList, new Comparator() {

      public int compare(Object o1, Object o2) {
        if (o1 instanceof CrawlerStatus) {
          CrawlerStatus stat1 = (CrawlerStatus) o1;
          CrawlerStatus stat2 = (CrawlerStatus) o2;

          return stat1.getInfo().getCrawlerName()
              .compareTo(stat2.getInfo().getCrawlerName());
        } else if (o1 instanceof PCSDaemonStatus) {
          PCSDaemonStatus stat1 = (PCSDaemonStatus) o1;
          PCSDaemonStatus stat2 = (PCSDaemonStatus) o2;

          return stat1.getDaemonName().compareTo(stat2.getDaemonName());
        } else {
          return 0;
        }
      }

    });
  }

}

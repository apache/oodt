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

package org.apache.oodt.cas.webcomponents.workflow.pagination;

//JDK imports
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;

//Wicket imports
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * 
 * A paginator component that expands to a +/- 5 page
 * window to paginate a {@link WorkflowInstancePage}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class WorkflowPagePaginator extends Panel {

  private static final long serialVersionUID = 6277536371513974225L;

  private WorkflowInstancePage page;

  private String status;
  
  /**
   * @param id
   */
  public WorkflowPagePaginator(String id, WorkflowInstancePage page,
      final String status, final Class<? extends WebPage> instPage) {
    super(id);
    this.page = page;
    this.status = status;

    if (this.page != null && this.page.getPageWorkflows() != null
        && this.page.getPageWorkflows().size() > 0) {

      int numPages = this.page.getTotalPages();
      int currPage = this.page.getPageNum();
      int windowSize = 10;

      int startPage = Math.max(1, (currPage - (windowSize / 2)));
      int endPage = Math.min(currPage + (windowSize / 2), numPages);
      List<PageNumDisplay> pnums = this.getPageNumDisplay(startPage, endPage,
          currPage);

      add(new ListView<PageNumDisplay>("page_repeater", pnums) {

        @Override
        protected void populateItem(ListItem<PageNumDisplay> item) {
          PageParameters params = new PageParameters();
          params.add("status", status);
          params.put("pageNum", item.getModelObject().getNum());
          BookmarkablePageLink pageLink = new BookmarkablePageLink("pageLink",
              instPage, params);
          pageLink.add(new Label("pageNum", String.valueOf(item
              .getModelObject().getNum())));
          pageLink.add(new SimpleAttributeModifier("class", item
              .getModelObject().isCurrentPage() ? "selected" : ""));
          item.add(pageLink);
        }
      });
      add(new Label("nothing_found").setVisible(false));
    } else {
      add(new Label("page_repeater").setVisible(false));
      add(new Label("pageLink").setVisible(false));
      add(new Label("pageNum").setVisible(false));
      add(new Label("nothing_found").setVisible(true));
    }

  }

  private List<PageNumDisplay> getPageNumDisplay(int startPage, int endPage,
      int currPage) {
    List<PageNumDisplay> display = new Vector<PageNumDisplay>();
    for (int i = startPage; i <= endPage; i++) {
      display.add(new PageNumDisplay(i == currPage, i));
    }

    return display;
  }

  class PageNumDisplay implements Serializable {

    private static final long serialVersionUID = -4351470774764276644L;

    private boolean currentPage;

    private int num;

    public PageNumDisplay() {
      this.currentPage = false;
      this.num = -1;
    }

    public PageNumDisplay(boolean currentPage, int num) {
      this.currentPage = currentPage;
      this.num = num;
    }

    /**
     * @return the currentPage
     */
    public boolean isCurrentPage() {
      return currentPage;
    }

    /**
     * @param currentPage
     *          the currentPage to set
     */
    public void setCurrentPage(boolean currentPage) {
      this.currentPage = currentPage;
    }

    /**
     * @return the num
     */
    public int getNum() {
      return num;
    }

    /**
     * @param num
     *          the num to set
     */
    public void setNum(int num) {
      this.num = num;
    }

  }

}

/*
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


package org.apache.oodt.cas.workflow.structs;

//JDK imports
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class WorkflowInstancePage implements Serializable {

    /* the number of this page */
    private int pageNum = -1;

    /* the total number of pages in the set */
    private int totalPages = -1;

    /* the size of the number of workflows on this page */
    private int pageSize = -1;

    /* the list of products associated with this page */
    private List pageWorkflows = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public WorkflowInstancePage() {
        pageWorkflows = new Vector();
    }

    /**
     * @param pageNum
     *            The number of this page.
     * @param totalPages
     *            The total number of pages in the set.
     * @param pageSize
     *            The size of this page.
     * @param pageWorkflows
     *            The workflows associated with this page.
     */
    public WorkflowInstancePage(int pageNum, int totalPages, int pageSize,
            List pageWorkflows) {
        this.pageNum = pageNum;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.pageWorkflows = pageWorkflows;
    }

    /**
     * @return Returns the pageNum.
     */
    public int getPageNum() {
        return pageNum;
    }

    /**
     * @param pageNum
     *            The pageNum to set.
     */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * @return Returns the pageWorkflows.
     */
    public List getPageWorkflows() {
        return pageWorkflows;
    }

    /**
     */
    public void setPageWorkflows(List pageWorkflows) {
        this.pageWorkflows = pageWorkflows;
    }

    /**
     * @return Returns the pageSize.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize
     *            The pageSize to set.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return Returns the totalPages.
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     *            The totalPages to set.
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * 
     * @return True if this is the last page in the set, false otherwise.
     */
    public boolean isLastPage() {
        return pageNum == totalPages;
    }

    /**
     * 
     * @return True if this is the fist page of the set, false otherwise.
     */
    public boolean isFirstPage() {
        return pageNum == 1;
    }

    /**
     * 
     * @return A blank, unpopulated {@link WorkflowInstancePage}.
     */
    public static WorkflowInstancePage blankPage() {
        WorkflowInstancePage blank = new WorkflowInstancePage();
        blank.setPageNum(0);
        blank.setTotalPages(0);
        blank.setPageSize(0);
        return blank;
    }

    @Override
    public String toString() {
        return String.format("(WorkflowInstancePage %s -> %d Workflows)", pageNum, pageWorkflows.size());
    }
}

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


package org.apache.oodt.cas.workflow.instrepo;

//JDK imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.commons.pagination.PaginationUtils;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public abstract class AbstractPaginatibleInstanceRepository implements
        WorkflowInstanceRepository {

    protected int pageSize = -1;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(AbstractPaginatibleInstanceRepository.class.getName());

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getFirstPage()
     */
    public WorkflowInstancePage getFirstPage() {
        WorkflowInstancePage firstPage = null;

        try {
            firstPage = getPagedWorkflows(1);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Exception getting first page: Message: "
                    + e.getMessage());
        }
        return firstPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getLastPage()
     */
    public WorkflowInstancePage getLastPage() {
        WorkflowInstancePage lastPage = null;
        WorkflowInstancePage firstPage = getFirstPage();

        try {
            lastPage = getPagedWorkflows(firstPage.getTotalPages());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception getting last page: Message: "
                    + e.getMessage());
        }

        return lastPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getNextPage(org.apache.oodt.cas.workflow.structs.WorkflowInstancePage)
     */
    public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage) {
        if (currentPage == null) {
            return getFirstPage();
        }

        if (currentPage.isLastPage()) {
            return currentPage;
        }

        WorkflowInstancePage nextPage = null;

        try {
            nextPage = getPagedWorkflows(currentPage.getPageNum() + 1);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception getting next page: Message: "
                    + e.getMessage());
        }

        return nextPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getPrevPage(org.apache.oodt.cas.workflow.structs.WorkflowInstancePage)
     */
    public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage) {
        if (currentPage == null) {
            return getFirstPage();
        }

        if (currentPage.isLastPage()) {
            return currentPage;
        }

        WorkflowInstancePage nextPage = null;

        try {
            nextPage = getPagedWorkflows(currentPage.getPageNum() - 1);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception getting next page: Message: "
                    + e.getMessage());
        }

        return nextPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getPagedWorkflows(int)
     */
    public WorkflowInstancePage getPagedWorkflows(int pageNum)
            throws InstanceRepositoryException {
        return getPagedWorkflows(pageNum, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.workflow.util.Pagination#getPagedWorkflows(int,
     *      java.lang.String)
     */
    public WorkflowInstancePage getPagedWorkflows(int pageNum, String status)
            throws InstanceRepositoryException {
        int totalPages = PaginationUtils.getTotalPage(
                status != null ? getNumWorkflowInstancesByStatus(status)
                        : getNumWorkflowInstances(), this.pageSize);

        /*
         * if there are 0 total pages in the result list size then don't bother
         * returning a valid product page instead, return blank ProductPage
         */
        if (totalPages == 0 || pageNum > totalPages || pageNum <= 0) {
            return WorkflowInstancePage.blankPage();
        }

        WorkflowInstancePage retPage = new WorkflowInstancePage();
        retPage.setPageNum(pageNum);
        retPage.setPageSize(this.pageSize);
        retPage.setTotalPages(totalPages);

        List wInstIds = paginateWorkflows(pageNum, status);

        if (wInstIds != null && wInstIds.size() > 0) {
            List workflowInstances = new Vector(wInstIds.size());

            for (Object wInstId : wInstIds) {
                String workflowInstId = (String) wInstId;
                WorkflowInstance inst = getWorkflowInstanceById(workflowInstId);
                workflowInstances.add(inst);
            }

            retPage.setPageWorkflows(workflowInstances);
        }

        return retPage;
    }

    /**
     * 
     * @param pageNum
     * @return
     * @throws InstanceRepositoryException
     */
    protected List paginateWorkflows(int pageNum)
            throws InstanceRepositoryException {
        return paginateWorkflows(pageNum, null);
    }

    /**
     * 
     * @param pageNum
     * @param status
     * @return
     * @throws InstanceRepositoryException
     */
    protected abstract List paginateWorkflows(int pageNum, String status)
            throws InstanceRepositoryException;

}

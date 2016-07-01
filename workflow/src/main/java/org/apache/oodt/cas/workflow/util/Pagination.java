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


package org.apache.oodt.cas.workflow.util;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public interface Pagination {

    /**
     * @return The first page of products for a particular {@link ProductType}.
     */
    WorkflowInstancePage getFirstPage();

    /**
     * 
     * @return The last page of products for a particular {@link ProductType}.
     */
    WorkflowInstancePage getLastPage();

    /**
     * 
     * @param currentPage
     *            The current page that tells the function what the next page to
     *            obtain is.
     * @return The next page in the ProductType product list, given the
     *         currentPage.
     */
    WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage);

    /**
     * 
     * @param currentPage
     *            The currentPage that tells the function what the previous page
     *            to obtain is.
     * @return The previous page in the ProductType product list, given the
     *         currentPage.
     */
    WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage);

    /**
     * Gets the {@link WorkflowInstancePage} identified by its
     * <code>pageNum</code> and associated <code>status</code> parameters.
     * 
     * @param pageNum
     *            The {@link WorkflowInstancePage} number to get.
     * @return The {@link WorkflowInstancePage} with the given status, and page
     *         number.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    WorkflowInstancePage getPagedWorkflows(int pageNum)
            throws InstanceRepositoryException;

    /**
     * Gets the {@link WorkflowInstancePage} identified by its
     * <code>pageNum</code> and associated <code>status</code> parameters.
     * 
     * @param pageNum
     *            The {@link WorkflowInstancePage} number to get.
     * @param status
     *            Identifies which {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance} set to paginate,
     *            e.g., only {@link org.apache.oodt.cas.workflow.structs.WorkflowInstance}s with a given status.
     * 
     * @return The {@link WorkflowInstancePage} with the given status, and page
     *         number.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    WorkflowInstancePage getPagedWorkflows(int pageNum, String status)
            throws InstanceRepositoryException;

}

//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.util;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstancePage;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;

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
    public WorkflowInstancePage getFirstPage();

    /**
     * 
     * @return The last page of products for a particular {@link ProductType}.
     */
    public WorkflowInstancePage getLastPage();

    /**
     * 
     * @param currentPage
     *            The current page that tells the function what the next page to
     *            obtain is.
     * @return The next page in the ProductType product list, given the
     *         currentPage.
     */
    public WorkflowInstancePage getNextPage(WorkflowInstancePage currentPage);

    /**
     * 
     * @param currentPage
     *            The currentPage that tells the function what the previous page
     *            to obtain is.
     * @return The previous page in the ProductType product list, given the
     *         currentPage.
     */
    public WorkflowInstancePage getPrevPage(WorkflowInstancePage currentPage);

    /**
     * Gets the {@link WorkflowInstancePage} identified by its
     * <code>pageNum</code> and associated <code>status</code> parameters.
     * 
     * @param pageNum
     *            The {@link WorkflowInstancePage} number to get.
     * @param status
     *            Identifies which {@link WorkflowInstance} set to paginate,
     *            e.g., only {@link WorkflowInstance}s with a given status.
     * 
     * @return The {@link WorkflowInstancePage} with the given status, and page
     *         number.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public WorkflowInstancePage getPagedWorkflows(int pageNum)
            throws InstanceRepositoryException;

    /**
     * Gets the {@link WorkflowInstancePage} identified by its
     * <code>pageNum</code> and associated <code>status</code> parameters.
     * 
     * @param pageNum
     *            The {@link WorkflowInstancePage} number to get.
     * @param status
     *            Identifies which {@link WorkflowInstance} set to paginate,
     *            e.g., only {@link WorkflowInstance}s with a given status.
     * 
     * @return The {@link WorkflowInstancePage} with the given status, and page
     *         number.
     * @throws InstanceRepositoryException
     *             If any error occurs.
     */
    public WorkflowInstancePage getPagedWorkflows(int pageNum, String status)
            throws InstanceRepositoryException;

}

//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.repository;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating implementations of {@link WorkflowRepository}s.
 * </p>
 * 
 */
public interface WorkflowRepositoryFactory {

    /**
     * @return A new implementation of the {@link WorkflowRepository} interface.
     */
    public WorkflowRepository createRepository();
}

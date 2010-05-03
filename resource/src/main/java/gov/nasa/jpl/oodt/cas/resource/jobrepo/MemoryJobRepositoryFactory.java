//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobrepo;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>Creates new {@link MemoryJobRepository}s.s 
 * </p>.
 */
public class MemoryJobRepositoryFactory implements JobRepositoryFactory {

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.resource.jobrepo.JobRepositoryFactory#createRepository()
   */
  public JobRepository createRepository() {
    return new MemoryJobRepository();
  }

}

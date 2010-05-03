//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.jobrepo;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory for creating {@link JobRepository}s
 * </p>.
 */
public interface JobRepositoryFactory {

  /**
   * Creates new {@link JobRepository} implementations.
   * 
   * @return A new {@link JobRepository} implementation.
   */
  public JobRepository createRepository();

}

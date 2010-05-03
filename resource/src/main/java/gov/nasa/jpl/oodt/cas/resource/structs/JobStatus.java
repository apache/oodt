//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public interface JobStatus {
  
  public static final String QUEUED = "__Queued__";
  
  public static final String EXECUTED = "__Executed__";
  
  public static final String SCHEDULED = "__Scheduled__";
  
  public static final String COMPLETE = "__Complete__";
  
  public static final String KILLED = "__Killed__";

}

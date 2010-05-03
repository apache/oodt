//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.scheduler;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A Factory interface for creating implementations of {@link Scheduler}s.
 * </p>
 * 
 */
public interface SchedulerFactory {

	/**
	 * @return A new implementation of the {@link Scheduler} interface.
	 */
	public Scheduler createScheduler();
}
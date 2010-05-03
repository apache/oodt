//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.monitor;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * Creates new {@link Monitor} implementations.
 * </p>
 * 
 */
public interface MonitorFactory {

	/**
	 * @return A new implementation of the {@link Monitor} interface.
	 */
	public Monitor createMonitor();
}
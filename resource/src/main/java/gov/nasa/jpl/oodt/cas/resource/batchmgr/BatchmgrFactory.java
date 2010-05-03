//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.batchmgr;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating implementations of the SimpleBatchmgr.
 * </p>
 * 
 */
public interface BatchmgrFactory {

	/**
	 * @return A new implementation of the {@link Batchmgr} interface.
	 */
	public Batchmgr createBatchmgr();
}
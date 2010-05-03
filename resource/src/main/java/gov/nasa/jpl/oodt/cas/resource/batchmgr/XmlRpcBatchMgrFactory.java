//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.batchmgr;

import gov.nasa.jpl.oodt.cas.resource.util.GenericResourceManagerObjectFactory;
import gov.nasa.jpl.oodt.cas.resource.monitor.Monitor;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A batchmgr factory.
 * </p>
 * 
 */

public class XmlRpcBatchMgrFactory implements BatchmgrFactory {

	private Monitor mon = null;
	
	public XmlRpcBatchMgrFactory(){
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nasa.jpl.oodt.cas.resource.batchmgr.BatchmgrFactory#createBatchmgr()
	 */
	public Batchmgr createBatchmgr() {
		return new XmlRpcBatchMgr();
	}

}
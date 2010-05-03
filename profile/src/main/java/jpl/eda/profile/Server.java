// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Server.java,v 1.1.1.1 2004/03/02 20:53:18 kelly Exp $

package jpl.eda.profile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import jpl.eda.xmlquery.XMLQuery;
import jpl.eda.profile.corba.ServerPackage.ProfileServiceException;

/**
 * Server for a single client transaction for profiles.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface Server extends Remote {
	/**
	 * Run a query.
	 *
	 * @param q The query.
	 * @return Matching {@link Profile}s.
	 * @throws ProductException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	List query(XMLQuery q) throws ProfileException, RemoteException;

	/**
	 * Retrieve a profile by its ID.
	 *
	 * @param id ID.
	 * @return a <code>Profile</code> value, or null if the <var>id</var> is unknown.
	 * @throws ProfileException if an error occurs.
	 * @throws RemoteException if an error occurs.
	 */
	Profile getProfile(String id) throws ProfileException, RemoteException;

	/**
         * Add profiles.
         *
         * @param profileStr Profile string that may contains mutiple profiles.
         * @throws ProfileException if an error occurs.
         * @throws RemoteException if an error occurs.
         */
	void add(String profileStr) throws ProfileException, RemoteException;

	/**
         * Remove a profile by ID and version.                
         * 
         * @param profId Profile identifier.
	 * @param version Version.
         * @throws ProfileException if an error occurs.
         * @throws RemoteException if an error occurs. 
         */
	boolean remove(String profId, String version) throws ProfileException, RemoteException;

	/**
         * Replace a profile.                
         * 
         * @param profileStr Profile string.
         * @throws ProfileException if an error occurs.
         * @throws RemoteException if an error occurs. 
         */
	void replace(String profileStr) throws ProfileException, RemoteException;
}


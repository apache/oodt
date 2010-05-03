// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileHandler.java,v 1.2 2006/07/07 21:56:12 kelly Exp $

package jpl.eda.profile.handlers;

import java.util.Collection;
import java.util.List;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileException;
import jpl.eda.xmlquery.XMLQuery;
import org.w3c.dom.Document;

/**
 * Interface of a profile handler.
 *
 * A profile handler can search for profiles or retrieve a single profile by its ID.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public interface ProfileHandler {
	/**
	 * Find a set of profiles that match the given <var>query</var>.
	 *
	 * This method searchs the profiles provided by this profile service provider
	 * based on criteria in the <var>query</var>.  It returns an XML document
	 * describing the profiles and profile elements that match.
	 *
	 * @param query Search criteria.
	 * @return List of matching {@link Profile} objects.
	 * @throws ProfileException If any other error occurs.
	 */
	List findProfiles(XMLQuery query) throws ProfileException;

	/**
	 * Get a profile.
	 *
	 * @param profID ID of the profile to get.
	 * @return The profile with the given <var>profID</var>, or null if it's not found.
	 * @throws ProfileException If any error occurs.
	 */
	Profile get(String profID) throws ProfileException;
	
	/**
	 * Get my ID.
	 *
	 * @return A string that identifies this profile handler in some way.
	 */
	String getID();
}

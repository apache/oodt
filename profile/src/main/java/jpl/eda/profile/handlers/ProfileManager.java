// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileManager.java,v 1.1.1.1 2004/03/02 20:53:23 kelly Exp $

package jpl.eda.profile.handlers;

import java.util.Collection;
import java.util.Iterator;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileException;
import jpl.eda.xmlquery.XMLQuery;
import org.w3c.dom.Document;

/**
 * Interface of a profile manager.
 *
 * A profile manager can manage profiles by adding, removing, and manipulating them---in
 * addition to searching them.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public interface ProfileManager extends ProfileHandler {
	/**
	 * Add a profile into the set of profiles managed by this profile server.
	 *
	 * @param profile The profile to add or replace.
	 * @throws ProfileException If any error occurs.
	 */
	void add(Profile profile) throws ProfileException;

	/**
	 * Add all of the profiles in the given collection to the set managed by this
	 * server.
	 *
	 * Any profiles that already exist (as identified by their profile IDs) are replaced.
	 *
	 * @param collection Collection of {@link Profile}s to add.
	 * @throws ProfileException If any error occurs.
	 */
	void addAll(Collection collection) throws ProfileException;

	/**
	 * Clear all profiles.
	 *
	 * This irrevocably removes all profiles from the server, leaving it with none.
	 *
	 * @throws ProfileException If any error occurs.
	 */
	void clear() throws ProfileException;

	/**
	 * Tell if the given profile is managed by this server.
	 *
	 * @param profile The profile to check.
	 * @return True if <var>profile</var> is present in the server, false otherwise.
	 * @throws ProfileException If any error occurs.
	 */
	boolean contains(Profile profile) throws ProfileException;

	/**
	 * Tell if the given collection of profiles are managed by this server.
	 *
	 * @param collection The collection of {@link Profile}s to check.
	 * @return True if every {@link Profile} in <var>collection</var> are present in
	 * the server, false otherwise.
	 * @throws ProfileException If any error occurs.
	 */
	boolean containsAll(Collection collection) throws ProfileException;

	/**
	 * Get all profiles.
	 *
	 * @return  A collection of profiles
	 * @throws ProfileException If any error occurs.
	 */
	Collection getAll() throws ProfileException;

	/**
	 * Tell if the set of profiles managed by this server is empty.
	 *
	 * @return True if there are no profiles in this server.
	 * @throws ProfileException If any error occurs.
	 */
	boolean isEmpty() throws ProfileException;

	/**
	 * Iterate over the available profiles.
	 *
	 * Each call to <code>next</code> yields a copy of the next {@link Profile} object
	 * managed by this server.  Updates to profile objects are not backed by the
	 * server; make your updates and then call {@link #add}.
	 *
	 * @return An iterator over {@link Profile}s.
	 * @throws ProfileException If any error occurs.
	 */
	Iterator iterator() throws ProfileException;

	/**
	 * Remove the profile with the given ID.
	 *
	 * This removes forever the given profile from the set of profiles managed by this
	 * server.
	 *
	 * @param profID What profile to remove.
	 * @param version What version of the the profile identified by <var>profID</var> to remove.
	 * @return True if thie profile was actually present and removed; false if it wasn't present.
	 * @throws ProfileException If any error occurs.
	 */
	boolean remove(String profID, String version) throws ProfileException;

	/**
	 * Remove the profile with the given ID.
	 *
	 * This removes forever the given profile from the set of profiles managed by this
	 * server.
	 *
	 * @param profID ID of profile to remove.
	 * @return True if thie profile was actually present and removed; false if it wasn't present.
	 * @throws ProfileException If any error occurs.
	 */
	boolean remove(String profID) throws ProfileException;

	/**
	 * Get the size of the set of profiles managed by this server.
	 *
	 * @return The number of available profiles.
	 * @throws ProfileException If any error occurs.
	 */
	int size() throws ProfileException;

	/**
	 * Replace a profile into the set of profiles managed by this profile server.
	 *
	 * @param profile The profile to add or replace.
	 * @throws ProfileException If any error occurs.
	 */
	void replace(Profile profile) throws ProfileException;
}

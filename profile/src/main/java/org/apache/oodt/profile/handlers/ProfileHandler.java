/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.profile.handlers;

import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileException;
import org.apache.oodt.xmlquery.XMLQuery;

import java.util.List;

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

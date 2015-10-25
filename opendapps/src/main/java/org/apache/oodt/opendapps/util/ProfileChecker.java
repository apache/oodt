/**
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
package org.apache.oodt.opendapps.util;

// JDK imports
import java.util.List;

// OODT imports
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileElement;

// Spring imports
import org.springframework.util.StringUtils;

/**
 * Utility class that checks an OODT Profile versus a list of required/optional elements,
 * and provides a validation summary for quick inspection by the publisher.
 * 
 * @author Luca Cinquini
 *
 */
public class ProfileChecker {
	
	// list of mandatory profile elements
	private final static String[] mandatoryProfileElements = new String[] {  };
	
	// list of optional profile elements
	private final static String[] optionalProfileElements = new String[] { "mission_name", "sensor", "institute",
		                                                                     "variable", "cf_standard_name", "variable_long_name",
		                                                                     "spatial_coverage",
		                                                                     "north_degrees", "east_degrees", "south_degrees", "west_degrees",
		                                                                     "datetime_start", "datetime_stop" };
		
	/**
	 * Main method to check an OODT profile.
	 * 
	 * @param profile : the OODT profile that needs validation.
	 * @param sb : buffer to write the output to.
	 * @return : true if the profile is valid, false otherwise.
	 */
	public static boolean check(final Profile profile, final StringBuilder sb) {
		
		// profile passes by default
		boolean ok = true;
		sb.append("\nChecking profile=").append(profile.getProfileAttributes().getID());
		
		ok = ok && checkResourceAttribute("Identifier", profile.getResourceAttributes().getIdentifier(), true, sb);
		
		ok = ok && checkResourceAttribute("Title", profile.getResourceAttributes().getTitle(), true, sb);
		
		ok = ok && checkResourceAttribute("Description", profile.getResourceAttributes().getDescription(), false, sb);
		
		ok = ok && checkResourceAttribute("Location of type "+ProfileUtils.MIME_TYPE_OPENDAP_HTML, 
				             selectResourceLocationByMimeType(profile.getResourceAttributes().getResLocations(), ProfileUtils.MIME_TYPE_OPENDAP_HTML), 
				             true, sb);
		
		ok = ok && checkResourceAttribute("Location of type "+ProfileUtils.MIME_TYPE_THREDDS, 
        selectResourceLocationByMimeType(profile.getResourceAttributes().getResLocations(), ProfileUtils.MIME_TYPE_THREDDS), 
        true, sb);
		
		ok = ok && checkResourceAttribute("Location of type "+ProfileUtils.MIME_TYPE_HTML, 
        selectResourceLocationByMimeType(profile.getResourceAttributes().getResLocations(), ProfileUtils.MIME_TYPE_HTML), 
        true, sb);
		
		ok = ok && checkResourceAttribute("Location of type "+ProfileUtils.MIME_TYPE_GIS, 
        selectResourceLocationByMimeType(profile.getResourceAttributes().getResLocations(), ProfileUtils.MIME_TYPE_GIS), 
        true, sb);
		
		for (String name : mandatoryProfileElements) {
			ok = ok && checkProfileElement(profile, name, true, sb);
		}
		for (String name : optionalProfileElements) {
			ok = ok && checkProfileElement(profile, name, false, sb);
		}
		
		return ok;
	}
	
	/**
	 * Method to check that a profile Resource Attribute has a valid value.
	 * 
	 * @param name
	 * @param value
	 * @param mandatory
	 * @param sb
	 * @return
	 */
	private static boolean checkResourceAttribute(String name, String value, boolean mandatory, StringBuilder sb) {
		sb.append("\n\tResource Attribute '").append(name).append("' = ");
		if (!StringUtils.hasText(value) || value.equalsIgnoreCase("null")) {
				if (mandatory) return false; // bad value
		} else {
			sb.append(value);
		}
		return true;
	}
	
	/**
	 * Method to check that the profile contains at least one valid value for of a specific element name.
	 * 
	 * @param profile
	 * @param name
	 * @param mandatory
	 * @param sb
	 * @return
	 */
	private static boolean checkProfileElement(Profile profile, String name, boolean mandatory, StringBuilder sb) {
		
		sb.append("\n\tProfile Element '").append(name).append("' = ");
	  // profile element is valid by default
		boolean ok = true; 
	
		if (profile.getProfileElements().containsKey(name)) {
				ProfileElement profElement = (ProfileElement)profile.getProfileElements().get(name);
			
  			// profile element found
  			List<String> values = profElement.getValues();
  			if (values.size()>0) {
  				boolean first = true;
  				for (String value : values) {
  					if (!StringUtils.hasText(value) || value.equalsIgnoreCase("null")) {
  						if (mandatory) ok = false; // invalid value for this profile element
  					} else {
  						if (!first) sb.append(", ");
  						sb.append(value);
  						first = false;
  					}
  				}
  			} else {
  				if (mandatory) ok = false; // no values found for this profile element
  			}
		
		} else {
			if (mandatory) ok = false; // no profile element found
		}
		
		return ok;
		
	}
	
	/**
	 * Method to select the resource location of a specific mime type, if found.
	 * 
	 * @param resLocations
	 * @param mimeType
	 * @return
	 */
	private static String selectResourceLocationByMimeType(List<String> resLocations, String mimeType) {
		
		for (String resLocation : resLocations) {
			String[] parts = resLocation.split("\\|"); // regular expression of ProfileUtils.CHAR
			if (parts[1].equals(mimeType)) return parts[0];
		}
		
		// resource location not found
		return null;
		
	}

}
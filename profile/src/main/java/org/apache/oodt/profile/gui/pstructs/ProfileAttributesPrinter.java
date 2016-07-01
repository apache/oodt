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

/*
 * Created on Jun 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.oodt.profile.gui.pstructs;


import org.apache.oodt.profile.ProfileAttributes;

/**
 * @author mattmann
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProfileAttributesPrinter {
	
	private ProfileAttributes myProfAttributes=null;
	/**
	 * 
	 */
	public ProfileAttributesPrinter(ProfileAttributes pa) {
		super();
		// TODO Auto-generated constructor stub
		myProfAttributes = pa;
	}
	
	public String toXMLString(){
		StringBuilder rStr= new StringBuilder();
		
		rStr.append("<profAttributes>\n");
		rStr.append("\t<profId>").append(myProfAttributes.getID()).append("</profId>\n");
		rStr.append("\t<profType>").append(myProfAttributes.getType()).append("</profType>\n");
		rStr.append("\t<profVersion>").append(myProfAttributes.getVersion()).append("</profVersion>\n");
		rStr.append("\t<profStatusId>").append(myProfAttributes.getStatusID()).append("</profStatusId>\n");
		rStr.append("\t<profSecurityType>").append(myProfAttributes.getSecurityType()).append("</profSecurityType>\n");
		rStr.append("\t<profParentId>").append(myProfAttributes.getParent()).append("</profParentId>\n");

	  for (Object o1 : myProfAttributes.getChildren()) {
		String theChild = (String) o1;
		rStr.append("\t<profChildId>").append(theChild).append("</profChildId>\n");
	  }
		
		rStr.append("\t<profRegAuthority>").append(myProfAttributes.getRegAuthority()).append("</profRegAuthority>\n");

	  for (Object o : myProfAttributes.getRevisionNotes()) {
		String theNote = (String) o;
		rStr.append("\t<profRevisionNote>").append(theNote).append("</profRevisionNote>\n");
	  }
		
		rStr.append("</profAttributes>\n\n");
		

		return rStr.toString();
	}
}

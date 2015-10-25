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


import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileElement;


/**
 * @author mattmann
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProfilePrinter {
	
	private Profile myProfile=null;
	private String dtdUrl=null;
	
	/**
	 * Pretty printing class for a profile, using lightweight, string
	 * concatenation classes and methods
	 */
	public ProfilePrinter(Profile p,String profDTDURL) {
		super();
		// TODO Auto-generated constructor stub
		myProfile = p;
		dtdUrl = profDTDURL;
	}
	
	public String toXMLString(){
		String rStr = "<?xml version=\"1.0\" ?>\n";
		rStr+="<!DOCTYPE profile SYSTEM \""+dtdUrl+"\">\n\n";
		rStr+="<profile>\n";

		
		ProfileAttributesPrinter pap = new ProfileAttributesPrinter(myProfile.getProfileAttributes());
		rStr+=pap.toXMLString();
		
		
		ResourceAttributesPrinter rap = new ResourceAttributesPrinter(myProfile.getResourceAttributes());
		rStr+=rap.toXMLString();


	  for (String profElemName : myProfile.getProfileElements().keySet()) {
		ProfileElement pe = (ProfileElement) myProfile.getProfileElements().get(profElemName);
		ProfileElementPrinter pPrinter = new ProfileElementPrinter(pe);
		rStr += pPrinter.toXMLString();
	  }
		
		rStr+="</profile>\n";
		
		return rStr;
	}
}

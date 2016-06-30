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

import org.apache.oodt.profile.ProfileElement;

/**
 * @author mattmann
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProfileElementPrinter {
	private ProfileElement myProfileElement=null;
	
	/**
	 * 
	 */
	public ProfileElementPrinter(ProfileElement pe) {
		super();
		// TODO Auto-generated constructor stub
		myProfileElement = pe;
	}
	
	public String toXMLString(){
		StringBuilder rStr= new StringBuilder();
		
		  rStr.append("<profElement>\n");
		  rStr.append("\t<elemName>").append(myProfileElement.getName()).append("</elemName>\n");
		  rStr.append("\t<elemMaxOccurrence>").append(myProfileElement.getMaxOccurrence())
			  .append("</elemMaxOccurrence>\n");
		  rStr.append("\t<elemMaxValue>").append(myProfileElement.getMaxValue()).append("</elemMaxValue>\n");
		  rStr.append("\t<elemMinValue>").append(myProfileElement.getMinValue()).append("</elemMinValue>\n");

	  for (Object o : myProfileElement.getValues()) {
		String theValue = (String) o;
		rStr.append("<elemValue>").append(theValue).append("</elemValue>\n");
	  }
		  rStr.append("\t<elemComment>").append(myProfileElement.getComments()).append("</elemComment>\n");
		  rStr.append("</profElement>\n");
		return rStr.toString();
	}
}

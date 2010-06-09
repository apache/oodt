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
package jpl.eda.profile.gui.pstructs;

import jpl.eda.profile.ProfileElement;
import java.util.Iterator;

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
		String rStr="";
		
		  rStr+="<profElement>\n";
		  rStr+="\t<elemName>"+myProfileElement.getName()+"</elemName>\n";
		  rStr+="\t<elemMaxOccurrence>"+myProfileElement.getMaxOccurrence()+"</elemMaxOccurrence>\n";
		  rStr+="\t<elemMaxValue>"+myProfileElement.getMaxValue()+"</elemMaxValue>\n";
		  rStr+="\t<elemMinValue>"+myProfileElement.getMinValue()+"</elemMinValue>\n";
		  
		  for(Iterator i = myProfileElement.getValues().iterator(); i.hasNext(); ){
		  	String theValue = (String)i.next();
		  	rStr+="<elemValue>"+theValue+"</elemValue>\n";
		  }
		  rStr+="\t<elemComment>"+myProfileElement.getComments()+"</elemComment>\n";
		  rStr+="</profElement>\n";
		return rStr;
	}
}

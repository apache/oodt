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


import jpl.eda.profile.ProfileAttributes;
import java.util.Iterator;

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
		String rStr="";
		
		rStr+="<profAttributes>\n";
		rStr+="\t<profId>"+myProfAttributes.getID()+"</profId>\n";
		rStr+="\t<profType>"+myProfAttributes.getType()+"</profType>\n";
		rStr+="\t<profVersion>"+myProfAttributes.getVersion()+"</profVersion>\n";
		rStr+="\t<profStatusId>"+myProfAttributes.getStatusID()+"</profStatusId>\n";
		rStr+="\t<profSecurityType>"+myProfAttributes.getSecurityType()+"</profSecurityType>\n";
		rStr+="\t<profParentId>"+myProfAttributes.getParent()+"</profParentId>\n";
		
		for(Iterator i = myProfAttributes.getChildren().iterator(); i.hasNext(); ){
			String theChild = (String)i.next();
			rStr+="\t<profChildId>"+theChild+"</profChildId>\n";
		}
		
		rStr+="\t<profRegAuthority>"+myProfAttributes.getRegAuthority()+"</profRegAuthority>\n";
		
		for(Iterator i = myProfAttributes.getRevisionNotes().iterator(); i.hasNext(); ){
			String theNote = (String)i.next();
			rStr+="\t<profRevisionNote>"+theNote+"</profRevisionNote>\n";
		}
		
		rStr+="</profAttributes>\n\n";
		

		return rStr;
	}
}

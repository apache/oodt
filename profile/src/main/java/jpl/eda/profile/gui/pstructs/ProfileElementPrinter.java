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

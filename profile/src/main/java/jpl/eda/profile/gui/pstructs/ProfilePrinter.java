/*
 * Created on Jun 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jpl.eda.profile.gui.pstructs;


import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileElement;
import java.util.Iterator;


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

		
		for(Iterator i = myProfile.getProfileElements().keySet().iterator(); i.hasNext(); ){
			String profElemName = (String)i.next();
			
			ProfileElement pe = (ProfileElement)myProfile.getProfileElements().get(profElemName);
			ProfileElementPrinter pPrinter = new ProfileElementPrinter(pe);
			rStr+=pPrinter.toXMLString();
		}
		
		rStr+="</profile>\n";
		
		return rStr;
	}
}

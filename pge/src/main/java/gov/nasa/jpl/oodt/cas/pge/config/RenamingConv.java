//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.config;

//JDK imports
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class RenamingConv {

	private String renamingString;
	
	private Metadata tmpReplaceMet;
	
	public RenamingConv(String renamingString) {
		this.renamingString = renamingString;
		this.tmpReplaceMet = new Metadata();
	}
	
	public String getRenamingString() {
		return this.renamingString;
	}
	
	public void addTmpReplaceMet(String key, List<String> values) {
		this.tmpReplaceMet.replaceMetadata(key, values);
	}
	
	public Metadata getTmpReplaceMet() {
		return this.tmpReplaceMet;
	}
	
}

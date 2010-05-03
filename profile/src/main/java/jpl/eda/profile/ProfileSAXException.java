// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileSAXException.java,v 1.1.1.1 2004/03/02 20:53:16 kelly Exp $

package jpl.eda.profile;

import org.xml.sax.SAXException;

/**
 * An XML-related SAX exception from a profile server.
 *
 * @author Kelly
 */
public class ProfileSAXException extends ProfileException {
	/**
	 * Create a profile SAX exception.
	 *
	 * @param cause The SAX exception that caused this profile exception.
	 */
	public ProfileSAXException(SAXException cause) {
		super(cause);
	}
}

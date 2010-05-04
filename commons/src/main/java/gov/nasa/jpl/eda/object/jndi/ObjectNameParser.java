// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ObjectNameParser.java,v 1.1 2004-03-02 19:28:59 kelly Exp $

package jpl.eda.object.jndi;

import java.util.Properties;
import javax.naming.CompoundName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

/** JNDI parser for object names.
 *
 * @author Kelly
 */
class ObjectNameParser implements NameParser {
	private static Properties syntax = new Properties(); {
		syntax.put("jndi.syntax.direction", "flat");
		syntax.put("jndi.syntax.ignorecase", "false");
	}
	public Name parse(String name) throws NamingException {
		return new CompoundName(name, syntax);
	}
}

	

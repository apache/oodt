// Copyright 2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ConfigurationEntityResolver.java,v 1.2 2004-04-01 23:34:46 kelly Exp $

package jpl.eda;

import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** XML entity resolver for the configuration file.
 *
 * This resolver attempts to use a locally accessible configuration.dtd so that we can
 * bootstrap enterprise applications without http access.  You see, The config file
 * specifies the list of entity directories, but the config file itself is an XML document
 * that refers to its doctype entity.  We therefore resolve the config DTD to a
 * classpath-acessible copy.
 *
 * @author Kelly
 */
class ConfigurationEntityResolver implements EntityResolver {
	public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
		if (Configuration.DTD_FPI.equals(publicID) || Configuration.DTD_OLD_FPI.equals(publicID))
			return new InputSource(Configuration.class.getResourceAsStream("Configuration.dtd"));
		return null;
	}
}

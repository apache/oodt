// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ObjectCtxFactory.java,v 1.1 2004-03-02 19:28:59 kelly Exp $

package jpl.eda.object.jndi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

/** JNDI context factory for object contexts.
 *
 * @author Kelly
 */
public class ObjectCtxFactory implements InitialContextFactory {
	public Context getInitialContext(Hashtable environment) {
		return new ObjectContext(environment);
	}
}

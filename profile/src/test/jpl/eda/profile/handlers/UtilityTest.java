// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: UtilityTest.java,v 1.1 2005/08/01 17:48:20 kelly Exp $

package jpl.eda.profile.handlers;

import java.util.Collection;
import jpl.eda.profile.ProfileException;
import junit.framework.TestCase;

/**
 * Unit test the <code>Utility</code> class.
 *
 * @author Kelly
 */ 
public class UtilityTest extends TestCase {
	/**
	 * Creates a new <code>ProfileTest</code> instance.
	 *
	 * @param name a <code>String</code> value.
	 */
	public UtilityTest(String name) {
		super (name);
	}

	/**
	 * Test the <code>getProfileCollection</code> method.
	 *
	 * @throws ProfileException if an error occurs.
	 */
	public void testGetProfileCollection() throws ProfileException {
		Collection profiles = Utility.getProfileCollection(PROFILES);
		assertEquals(1, profiles.size());
		
	}

	/** Test document containing perhaps multiple profiles. */
	private static final String PROFILES = "<profiles><profile><profAttributes><profId>1</profId><profType>PROFILE</profType>"
		+ "<profStatusId>ACTIVE</profStatusId></profAttributes><resAttributes><Identifier>1</Identifier>"
		+ "<resContext>TEST</resContext><resClass>TEST</resClass></resAttributes></profile></profiles>";

}

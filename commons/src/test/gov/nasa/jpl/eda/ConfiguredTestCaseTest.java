// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: ConfiguredTestCaseTest.java,v 1.1 2004-04-01 23:33:22 kelly Exp $

package jpl.eda;

import junit.framework.TestCase;
import org.xml.sax.SAXParseException;

/**
 * Unit test the ConfiguredTestCase class.
 *
 * @author Kelly
 */ 
public class ConfiguredTestCaseTest extends TestCase {
	/** Construct the test case for the ConfiguredTestCaseTest class.
	 *
	 * @param name Case name
	 */
	public ConfiguredTestCaseTest(String name) {
		super(name);
	}

	public void testIt() throws Exception {
		Case c = new Case();
		try {
 			c.setUp();
			assertNotNull(Configuration.configuration);
		} finally {
			c.tearDown();
		}			
	}

	public void testItAgain() throws Exception {
		Case c = new Case();
		try {
			c.setUp();
			assertNotNull(Configuration.configuration);
		} finally {
			c.tearDown();
		}			
	}

	private static class Case extends ConfiguredTestCase {
		Case() {
			super("test");
		}
		public void setUp() throws Exception {
			super.setUp();
		}
		public void tearDown() throws Exception {
			super.tearDown();
		}
	}		
}

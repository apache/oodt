// Copyright 2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Base64Test.java,v 1.1.1.1 2004-02-28 13:09:21 kelly Exp $

package jpl.eda.util;

import java.io.*;
import java.util.*;
import junit.framework.*;

/** Unit test the {@link Base64} class.
 *
 * @author Kelly
 */ 
public class Base64Test extends TestCase {
	/** Construct the test case for the {@link Base64} class. */
	public Base64Test(String name) {
		super(name);
	}

	/** Test encoding and decoding.
	 */
	public void testEncDec() {
		byte[] a = Base64.encode("abcde".getBytes());
		assertTrue("Base-64 encoding failed", Arrays.equals("YWJjZGU=".getBytes(), a));
		byte[] b = Base64.decode("YWJjZGU=".getBytes());
		assertTrue("Base-64 decoding failed", Arrays.equals("abcde".getBytes(), b));
	}
}

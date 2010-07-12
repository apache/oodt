// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: UtilityTest.java,v 1.1 2004-11-30 01:39:47 kelly Exp $

package jpl.eda.util;

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

public class UtilityTest extends TestCase {
	public UtilityTest(String caseName) {
		super(caseName);
	}

	public void testDelete() throws IOException {
		File top = File.createTempFile("topdir", ".dir");
		top.delete();
		top.mkdir();
		File f1 = File.createTempFile("nesteddir", ".file", top);
		File f2 = File.createTempFile("nesteddir", ".file", top);
		File d1 = File.createTempFile("nesteddir", ".dir", top);
		d1.delete();
		d1.mkdir();
		File f3 = File.createTempFile("nesteddir", ".file", d1);
		File d2 = File.createTempFile("nesteddir", ".dir", d1);
		d2.delete();
		d2.mkdir();
		File f4 = File.createTempFile("nesteddir", ".file", d2);

		assertTrue(Utility.delete(top));
		assertTrue(!top.exists());
	}
}

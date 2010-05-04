// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: EmptyByteArrayCodecTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import jpl.eda.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import jpl.eda.xmlquery.ByteArrayCodec; // Imported for javadoc

/**
 * Unit test the {@link ByteArrayCodec} class with an empty byte array.
 *
 * @author Kelly
 */ 
public class EmptyByteArrayCodecTest extends CodecTest {
	/** Construct the test case for the {@link ByteArrayCodec} class. */
	public EmptyByteArrayCodecTest(String name) {
		super(name);
	}

	public void testIt() throws Exception {
		runTest(CodecFactory.createCodec("jpl.eda.xmlquery.ByteArrayCodec"));
	}

	protected Object getTestObject() {
		return TEST_OBJECT;
	}

	protected long getTestSize() {
		return 0;
	}

	protected void checkEquality(Object encodedAndDecoded) {
		assertTrue("Empty byte array codec failed", java.util.Arrays.equals(TEST_OBJECT, (byte[]) encodedAndDecoded));
	}

	private static final byte[] TEST_OBJECT = new byte[0];
}

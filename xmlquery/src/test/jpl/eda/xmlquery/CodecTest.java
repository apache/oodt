// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: CodecTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import jpl.eda.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/** Unit test a codec.
 *
 * @author Kelly
 */ 
abstract class CodecTest extends TestCase {
	/** Construct the test case for a codec. */
	protected CodecTest(String name) {
		super(name);
	}

	/** Test the codec. */
	protected void runTest(Codec codec) throws Exception {
		// Test encoding and decoding
		Document doc = XML.createDocument();
		Node node = codec.encode(getTestObject(), doc);
		Object object = codec.decode(node);
		checkEquality(object);

		// Test size computation
		assertEquals(getTestSize(), codec.sizeOf(getTestObject()));
	}

	/** Get the test object to encode.
	 *
	 * @return The test object.
	 */
	protected Object getTestObject() {
		return TEST_OBJECT;
	}

	/**
	 * Get the size of the test object.
	 *
	 * @return Size of the test object in bytes.
	 */
	protected long getTestSize() {
		return TEST_SIZE;
	}

	/** Test the encoded and decoded object for equality with the test object.
	 *
	 * @param encodedAndDecoded The encoded and decoded object.
	 */
	protected void checkEquality(Object encodedAndDecoded) {
		assertEquals(getTestObject(), encodedAndDecoded);
	}

	/** The object we'll encode and decode with the codec. */
	private static final String TEST_OBJECT = "This is my test object.";

	/** Size of the test object in bytes. */
	private static final long TEST_SIZE = 23;
}

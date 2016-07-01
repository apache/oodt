/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.xmlquery;

import org.apache.oodt.commons.util.*;
import junit.framework.*;
import org.w3c.dom.*;

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

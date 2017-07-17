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

import java.io.InputStream;
import org.apache.oodt.commons.io.NullInputStream;
import org.apache.oodt.xmlquery.CodecFactory; // Imported solely for Javadoc
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Unit test the {@link CodecFactory} class.
 *
 * @author Kelly
 */ 
public class CodecFactoryTest extends TestCase {
	/** Construct the test case for the {@link CodecFactory} class. */
	public CodecFactoryTest(String name) {
		super(name);
	}

	public void testInvalidCodec() {
		try {
			Codec codec = CodecFactory.createCodec("unknown.class.name");
			fail("CodecFactory somehow created an object of an unknown class");
		} catch (RuntimeException ignored) {}
	}

	public void testValidCodec() {
		Codec c1 = CodecFactory.createCodec("org.apache.oodt.xmlquery.CodecFactoryTest$TestCodec");
		assertNotNull(c1);
		Codec c2 = CodecFactory.createCodec("org.apache.oodt.xmlquery.CodecFactoryTest$TestCodec");
		assertSame(c1, c2);
	}

	public static class TestCodec implements Codec {
		public TestCodec() {}
		public Node encode(Object object, Document doc) { return null; }
		public Object decode(Node node) { return null; }
		public long sizeOf(Object obj) { return 0; }
		public InputStream getInputStream(Object object) {
			return new NullInputStream();
		}
	}
}

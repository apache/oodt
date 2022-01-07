/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.metadata.util;

import junit.framework.TestCase;

public class TestMimeTypeUtils extends TestCase {

	public void testMimeTypes() {
		MimeTypeUtils mtUtils = new MimeTypeUtils();
		assertEquals("application/xml", mtUtils.getMimeType("file.xml"));
		assertEquals("text/plain", mtUtils.getMimeType("file.txt"));
		assertEquals("application/pdf", mtUtils.getMimeType("file.pdf"));
		assertEquals("application/xhtml+xml", mtUtils.getMimeType("file.xhtml"));
		assertEquals("application/xml", mtUtils.getSuperTypeForMimeType(mtUtils.getMimeType("file.xhtml")));
		assertEquals("audio/amr", mtUtils.getMimeType("file.amr"));
	}
	
}

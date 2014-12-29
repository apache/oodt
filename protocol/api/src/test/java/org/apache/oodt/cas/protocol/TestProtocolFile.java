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
package org.apache.oodt.cas.protocol;

//JDK imports
import java.io.File;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ProtocolFile}
 * 
 * @author bfoster
 */
public class TestProtocolFile extends TestCase {

	public void testInitialState() {
		String filePath = ProtocolFile.SEPARATOR + "path" + ProtocolFile.SEPARATOR + "to" + ProtocolFile.SEPARATOR + "file";
		ProtocolFile pFile = new ProtocolFile(filePath, false);
		assertEquals(filePath, pFile.getPath());
		assertEquals("file", pFile.getName());
		assertFalse(pFile.isDir());
		assertFalse(pFile.isRelative());
		
		// Test Parent file
		String parentPath = ProtocolFile.SEPARATOR + "path" + ProtocolFile.SEPARATOR + "to";
		assertEquals(parentPath, pFile.getParent().getPath());
		assertEquals("to", pFile.getParent().getName());
		assertTrue(pFile.getParent().isDir());
		assertFalse(pFile.getParent().isRelative());
	}
	
	public void testEquals() {
		assertEquals(new ProtocolFile("/test/directory", true), new ProtocolFile(
				new ProtocolFile("/test", true), "directory", true));
		assertEquals(new ProtocolFile(new ProtocolFile("/", true), "repo", true), new ProtocolFile("/repo", true));
		assertEquals(new ProtocolFile(new ProtocolFile("/", true), "", true), new ProtocolFile("/", true));
	}
}

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
		String filePath = File.separator + "path" + File.separator + "to" + File.separator + "file";
		ProtocolFile pFile = new ProtocolFile(filePath, false);
		assertEquals(filePath, pFile.getPath());
		assertEquals(pFile.getPath(), pFile.toString());
		assertEquals("file", pFile.getName());
		assertFalse(pFile.isDir());
		assertFalse(pFile.isRelative());
		
		// Test Parent file
		String parentPath = File.separator + "path" + File.separator + "to";
		assertEquals(parentPath, pFile.getParent().getPath());
		assertEquals(pFile.getParent().getPath(), pFile.getParent().toString());
		assertEquals("to", pFile.getParent().getName());
		assertTrue(pFile.getParent().isDir());
		assertFalse(pFile.getParent().isRelative());
	}
	
	public void testRoot() {
		assertEquals(File.separator, ProtocolFile.ROOT.getPath());
		assertNull(ProtocolFile.ROOT.getParent());
		assertEquals(ProtocolFile.ROOT.getPath(), ProtocolFile.ROOT.getName());
		assertEquals(ProtocolFile.ROOT.getPath(), ProtocolFile.ROOT.toString());
		assertTrue(ProtocolFile.ROOT.isDir());
		assertFalse(ProtocolFile.ROOT.isRelative());
	}
	
	public void testHome() {
		assertEquals(new File("").getAbsolutePath(), ProtocolFile.HOME.getPath());
		assertEquals(ProtocolFile.HOME.getPath(), ProtocolFile.HOME.toString());
		assertFalse(ProtocolFile.HOME.isRelative());
		assertTrue(ProtocolFile.HOME.isDir());
	}
}

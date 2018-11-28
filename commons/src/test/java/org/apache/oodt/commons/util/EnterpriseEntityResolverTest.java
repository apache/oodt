// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import java.io.File;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

/**
 * Unit test the EnterpriseEntityResolver class.
 *
 * @author Kelly
 */ 
public class EnterpriseEntityResolverTest extends TestCase {
	/** Construct the test case for the XML class.
	 *
	 * @param name Case name
	 */
	public EnterpriseEntityResolverTest(String name) {
		super(name);
	}

	/**
	 * Create a temporary directory with a temporary file as the test entity.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void setUp() throws Exception {
		super.setUp();
		testDir = File.createTempFile("eet", ".dir");
		testDir.delete();
		testDir.mkdir();
		testFile = new File(testDir, "test-entry-do-not-remove.dtd");
		if (!testFile.createNewFile())
			throw new Exception(testFile + " already exists, but shouldn't");
	}

	/**
	 * Delete the temporary file entity and test directory.
	 *
	 * @throws Exception if an error occurs.
	 */
	public void tearDown() throws Exception{
		testFile.delete();
		testDir.delete();
		super.tearDown();
	}

	/**
	 * Test if the entity parser works at initialization time.
	 */
	public void testEntityParsing() {
		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.entities.get("-//JPL//DTD TEST ENTRY DO NOT REMOVE//EN"));
	}

	/**
	 * Test if the resolver computes filenames based on public and system identifiers.
	 */
	public void testFilenameComputation() {
		assertNull(EnterpriseEntityResolver.computeFilename(null, null));
		assertNull(EnterpriseEntityResolver.computeFilename(null, "unknown"));
		assertNull(EnterpriseEntityResolver.computeFilename("unknown", null));
		assertNull(EnterpriseEntityResolver.computeFilename("unknown", "unknown"));

		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.computeFilename("-//JPL//DTD TEST ENTRY DO NOT REMOVE//EN", null));
		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.computeFilename(null, "http://oodt.jpl.nasa.gov/test-entry-do-not-remove.dtd"));
		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.computeFilename("-//JPL//DTD TEST ENTRY DO NOT REMOVE//EN",
				"illegal-url:test-entry-do-not-remove.dtd"));
		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.computeFilename("illegal-FPI",
				"http://oodt.jpl.nasa.gov/test-entry-do-not-remove.dtd"));
		assertEquals("test-entry-do-not-remove.dtd",
			EnterpriseEntityResolver.computeFilename("-//JPL//DTD TEST ENTRY DO NOT REMOVE//EN",
				"http://oodt.jpl.nasa.gov/test-entry-do-not-remove.dtd"));
	}

	/**
	 * Test if the resolver finds a file based on a list of directories and name.
	 */
	public void testFileFinding() {
		List dirs = Collections.singletonList(testDir.toString());
		assertEquals(testFile, EnterpriseEntityResolver.findFile(dirs, testFile.getName()));
	}

	/**
	 * Test if the resolver can convert a string specification of entity references
	 * into a {@link List}.
	 */
	public void testDirFinding() {
		List dirs = EnterpriseEntityResolver.getEntityRefDirs("");
		assertTrue(dirs.isEmpty());
		dirs = EnterpriseEntityResolver.getEntityRefDirs("/tmp,/usr/local/xml");
		assertEquals(2, dirs.size());
		assertEquals("/tmp", dirs.get(0));
		assertEquals("/usr/local/xml", dirs.get(1));
	}

	/** Test directory to hold <var>testFile</var>, the test entity. */
	private File testDir;

	/** The test entity. */
	private File testFile;
}

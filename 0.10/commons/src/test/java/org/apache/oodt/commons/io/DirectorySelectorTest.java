/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.commons.io;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class DirectorySelectorTest extends TestCase {
	
	File workingDir;
	String relPath = "org/apache/oodt/commons/io/";
	File thisDir;
	String thisClass = "DirectorySelectorTest.class";
	
	public DirectorySelectorTest(String name) {
		
		super(name);
		
		workingDir = new File(DirectorySelectorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		thisDir = new File(workingDir, relPath);

	}
	
	/**
	 * Tests starting from directory containing this class file,
	 * no recursion involved
	 */
	public void testPositiveSelectionWithoutRecursion() {
		
		DirectorySelector ds = new DirectorySelector(Arrays.asList( new String[] { thisClass } ));
		List<String> dirs = ds.traverseDir(thisDir);
		assertEquals(1, dirs.size());
		assertEquals("file://"+thisDir.getAbsolutePath(), dirs.get(0));
		
	}
	
	/**
	 * Tests starting from top-level tests directory,
	 * involves recursion
	 */
	public void testPositiveSelectionWithRecursion() {
		
		DirectorySelector ds = new DirectorySelector(Arrays.asList( new String[] { thisClass } ));
		List<String> dirs = ds.traverseDir(workingDir);
		assertEquals(1, dirs.size());
		assertEquals("file://"+thisDir.getAbsolutePath(), dirs.get(0));
		
	}
	
	/**
	 * Tests that no directories are selected if passing an invalid file.
	 */
	public void testNegativeSelectionForInvalidFile() {
		
		DirectorySelector ds = new DirectorySelector(Arrays.asList( new String[] { "doesNotExist.txt" } ));
		List<String> dirs = ds.traverseDir(workingDir);
		assertEquals(0, dirs.size());

	}
	
	/**
	 * Tests that no directories are returned when no files are specified.
	 */
	public void testNegativeSelectionForNoFiles() {
		
		DirectorySelector ds = new DirectorySelector(Arrays.asList( new String[] {} ));
		List<String> dirs = ds.traverseDir(workingDir);
		assertEquals(0, dirs.size());
		
	}
	
	/**
	 * Tests that no directories are returned when starting from an invalid directory.
	 */
	public void testNegativeSelectionForInvalidDirectory() {
		
		DirectorySelector ds = new DirectorySelector(Arrays.asList( new String[] { thisClass } ));
		List<String> dirs = ds.traverseDir( new File("/tmp/does/not/exist") );
		assertEquals(0, dirs.size());
		
	}

}

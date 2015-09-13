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

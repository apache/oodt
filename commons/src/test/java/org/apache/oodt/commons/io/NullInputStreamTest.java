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

package org.apache.oodt.commons.io;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * Unit test the {@link NullInputStream} class.
 *
 * @author Kelly
 */ 
public class NullInputStreamTest extends TestCase {
        /**
	 * Creates a new <code>NullInputStreamTest</code> instance.
	 *
	 * @param name Test case name.
	 */
	public NullInputStreamTest(String name) {
                super(name);
        }

        protected void setUp() throws Exception {
                in = new NullInputStream();
        }

        public void testClosing() {
                try {
                        in.close();
                } catch (IOException ex) {
                        fail("Should not throw an exception on close: " + ex.getMessage());
                }
                try {
                        in.read();
                        fail("Should not be able to read a closed stream");
                } catch (IOException ignore) {}
                try {
                        in.close();
                        fail("Should not be able to close a closed stream");
                } catch (IOException ignore) {}
        }

        public void testReading() {
                try {
                        assertEquals(-1, in.read());
                } catch (IOException ex) {
                        fail("Should not throw an exception on reading: " + ex.getMessage());
                }
        }

        /** The {@link NullInputStream} we're testing. */
        private NullInputStream in;
}

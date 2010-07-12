// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NullInputStreamTest.java,v 1.1.1.1 2004-02-28 13:09:20 kelly Exp $

package jpl.eda.io;

import java.io.IOException;
import java.io.InputStream;
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

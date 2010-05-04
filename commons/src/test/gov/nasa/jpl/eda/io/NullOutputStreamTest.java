// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: NullOutputStreamTest.java,v 1.1.1.1 2004-02-28 13:09:21 kelly Exp $

package jpl.eda.io;

import java.io.*;
import java.util.*;
import junit.framework.*;

/** Unit test the {@link NullOutputStream} class.
 *
 * @author Kelly
 */ 
public class NullOutputStreamTest extends TestCase {
	/** Construct the test case for the {@link NullOutputStream} class. */
	public NullOutputStreamTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		out = new NullOutputStream();
	}

	public void testClosing() {
		try {
			out.close();
		} catch (IOException ex) {
			fail("Should not throw an exception on close: " + ex.getMessage());
		}
		try {
			out.write(1);
			fail("Should not be able to write to a closed stream");
		} catch (IOException ignore) {}
		try {
			out.flush();
			fail("Should not be able to flush a closed stream");
		} catch (IOException ignore) {}
		try {
			out.close();
			fail("Should not be able to close a closed stream");
		} catch (IOException ignore) {}
	}

	public void testFlush() {
		try {
			out.flush();
		} catch (IOException ex) {
			fail("Should not throw an exception on flush: " + ex.getMessage());
		}
	}

	public void testWriting() {
		try {
			byte[] array = new byte[]{(byte)0, (byte)1, (byte)2};
			out.write(1);
			out.write(array);
			out.write(array, 1, 1);
		} catch (IOException ex) {
			fail("Should not throw an exception on writing: " + ex.getMessage());
		}
	}

	/** The {@link NullOutputStream} we're testing. */
	private NullOutputStream out;

}


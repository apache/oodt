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
// $Id: CompressedObjectCodecTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;
import jpl.eda.util.*;
import junit.framework.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import jpl.eda.xmlquery.CompressedObjectCodec; // Imported for javadoc

/** Unit test the {@link CompressedObjectCodec} class.
 *
 * @author Kelly
 */ 
public class CompressedObjectCodecTest extends CodecTest {
	/** Construct the test case for the {@link CompressedObjectCodec} class. */
	public CompressedObjectCodecTest(String name) {
		super(name);
	}

	public void testIt() throws Exception {
		runTest(CodecFactory.createCodec("jpl.eda.xmlquery.CompressedObjectCodec"));
	}

	public long getTestSize() {
		// Serialization overhead adds a few bytes, so we override the method here
		// with this value:
		return 30;
	}
}

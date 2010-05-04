// Copyright 2000-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CodecFactoryTest.java,v 1.1.1.1 2004-03-02 19:37:17 kelly Exp $

package jpl.eda.xmlquery;

import java.io.InputStream;
import jpl.eda.io.NullInputStream;
import jpl.eda.xmlquery.CodecFactory; // Imported solely for Javadoc
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Unit test the {@link CodecFactory} class.
 *
 * @author Kelly
 */ 
public class CodecFactoryTest extends TestCase {
	/** Construct the test case for the {@link CodecFactory} class. */
	public CodecFactoryTest(String name) {
		super(name);
	}

	public void testInvalidCodec() {
		try {
			Codec codec = CodecFactory.createCodec("unknown.class.name");
			fail("CodecFactory somehow created an object of an unknown class");
		} catch (RuntimeException good) {}
	}

	public void testValidCodec() {
		Codec c1 = CodecFactory.createCodec("jpl.eda.xmlquery.CodecFactoryTest$TestCodec");
		assertNotNull(c1);
		Codec c2 = CodecFactory.createCodec("jpl.eda.xmlquery.CodecFactoryTest$TestCodec");
		assertSame(c1, c2);
	}

	private static class TestCodec implements Codec {
		public TestCodec() {}
		public Node encode(Object object, Document doc) { return null; }
		public Object decode(Node node) { return null; }
		public long sizeOf(Object obj) { return 0; }
		public InputStream getInputStream(Object object) {
			return new NullInputStream();
		}
	}
}

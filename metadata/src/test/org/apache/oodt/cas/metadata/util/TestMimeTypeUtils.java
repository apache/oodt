package org.apache.oodt.cas.metadata.util;

import junit.framework.TestCase;

public class TestMimeTypeUtils extends TestCase {

	public void testMimeTypes() {
		MimeTypeUtils mtUtils = new MimeTypeUtils();
		assertEquals("application/xml", mtUtils.getMimeType("file.xml"));
		assertEquals("text/plain", mtUtils.getMimeType("file.txt"));
		assertEquals("application/pdf", mtUtils.getMimeType("file.pdf"));
		assertEquals("application/xhtml+xml", mtUtils.getMimeType("file.xhtml"));
		assertEquals("application/xml", mtUtils.getSuperTypeForMimeType(mtUtils.getMimeType("file.xhtml")));
	}
	
}

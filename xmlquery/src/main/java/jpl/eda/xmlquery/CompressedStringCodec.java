// Copyright 2000-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CompressedStringCodec.java,v 1.1.1.1 2004-03-02 19:37:14 kelly Exp $

package jpl.eda.xmlquery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import jpl.eda.io.Base64DecodingInputStream;
import jpl.eda.io.Base64EncodingOutputStream;
import jpl.eda.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A result encoder/decoder for compressed strings.
 *
 * This codec uses a GZIP compressed string format for objects.
 *
 * @author Kelly
 */
class CompressedStringCodec implements Codec {
	public Node encode(Object object, Document doc) throws DOMException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try {
			Base64EncodingOutputStream base64 = new Base64EncodingOutputStream(byteArray);
			GZIPOutputStream gzip = new GZIPOutputStream(base64);
			gzip.write(object.toString().getBytes());
			gzip.close();
		} catch (IOException cantHappen) {}
		Element value = doc.createElement("resultValue");
		value.appendChild(doc.createCDATASection(byteArray.toString()));
		return value;
	}

	public Object decode(Node node) {
		String encodedValue;
		if (node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)
			encodedValue = node.getFirstChild().getNodeValue();
		else
			encodedValue = XML.text(node);
		String rc = null;
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(encodedValue.getBytes());
			Base64DecodingInputStream base64 = new Base64DecodingInputStream(byteArray);
			GZIPInputStream gzip = new GZIPInputStream(base64);
			StringBuffer b = new StringBuffer();
			int numRead;
			byte[] buf = new byte[1024];
			while ((numRead = gzip.read(buf)) != -1)
				b.append(new String(buf, 0, numRead));
			gzip.close();
			rc = b.toString();
		} catch (IOException cantHappen) {}
		return rc;
	}

	public InputStream getInputStream(Object value) {
		return new ByteArrayInputStream(((String) value).getBytes());
	}

	public long sizeOf(Object obj) {
		return ((String) obj).getBytes().length;
	}
}

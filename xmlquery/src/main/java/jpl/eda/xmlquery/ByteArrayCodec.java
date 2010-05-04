// Copyright 1999-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ByteArrayCodec.java,v 1.1.1.1 2004-03-02 19:37:14 kelly Exp $

package jpl.eda.xmlquery;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import jpl.eda.util.Base64;
import jpl.eda.util.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A result encoder/decoder for byte arrays.
 *
 * This codec uses base-64 encoding for byte arrays.
 *
 * @author Kelly
 */
class ByteArrayCodec implements Codec {
	public Node encode(Object object, Document doc) throws DOMException {
		Element value = doc.createElement("resultValue");
		value.appendChild(doc.createCDATASection(new String(Base64.encode((byte[]) object))));
		return value;
	}

	public Object decode(Node node) {
		String encodedValue;
		if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)
			encodedValue = node.getFirstChild().getNodeValue();
		else
			encodedValue = XML.text(node);
		if (encodedValue.length() <= 0) return new byte[0];
		return Base64.decode(encodedValue.getBytes());
	}

	public InputStream getInputStream(Object value) {
		return new ByteArrayInputStream((byte[]) value);
	}

	public long sizeOf(Object object) {
		return ((byte[]) object).length;
	}
}

// Copyright 2000-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: CompressedObjectCodec.java,v 1.1.1.1 2004-03-02 19:37:14 kelly Exp $

package jpl.eda.xmlquery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import jpl.eda.io.Base64DecodingInputStream;
import jpl.eda.io.Base64EncodingOutputStream;
import jpl.eda.io.NullOutputStream;
import jpl.eda.util.XML;
import jpl.eda.io.CountingOutputStream;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A result encoder/decoder for compressed, serialized objects.
 *
 * This codec uses a GZIP compressed serialized object format for objects.
 *
 * @author Kelly
 */
class CompressedObjectCodec implements Codec {
	public Node encode(Object object, Document doc) throws DOMException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try {
			Base64EncodingOutputStream base64 = new Base64EncodingOutputStream(byteArray);
			GZIPOutputStream gzip = new GZIPOutputStream(base64);
			ObjectOutputStream objStream = new ObjectOutputStream(gzip);
			objStream.writeObject(object);
			objStream.close();
		} catch (IOException cantHappen) {}
		Element value = doc.createElement("resultValue");
		value.appendChild(doc.createCDATASection(byteArray.toString()));
		return value;
	}

	public Object decode(Node node) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException,
		OptionalDataException {
		String encodedValue;
		if (node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE)
			encodedValue = node.getFirstChild().getNodeValue();
		else
			encodedValue = XML.text(node);
		Object rc = null;
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(encodedValue.getBytes());
			Base64DecodingInputStream base64 = new Base64DecodingInputStream(byteArray);
			GZIPInputStream gzip = new GZIPInputStream(base64);
			ObjectInputStream objStream = new ObjectInputStream(gzip);
			rc = objStream.readObject();
			objStream.close();
		} catch (InvalidClassException ex) {
			throw ex;
		} catch (StreamCorruptedException ex) {
			throw ex;
		} catch (OptionalDataException ex) {
			throw ex;
		} catch (IOException cantHappen) {}
		return rc;
	}

	public InputStream getInputStream(Object value) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(value);
		oos.close();
		baos.close();
		return new ByteArrayInputStream(baos.toByteArray());
	}

	public long sizeOf(Object obj) {
		try {
			CountingOutputStream c = new CountingOutputStream(new NullOutputStream());
			ObjectOutputStream stream = new ObjectOutputStream(c);
			stream.writeObject(obj);
			stream.close();
			return c.getBytesWritten();
		} catch (IOException ex) {
			throw new IllegalStateException("I/O exception " + ex.getClass().getName() + " can't happen, yet did: "
				+ ex.getMessage());
		}
	}
}

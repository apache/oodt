/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.xmlquery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import org.apache.oodt.commons.io.Base64DecodingInputStream;
import org.apache.oodt.commons.io.Base64EncodingOutputStream;
import org.apache.oodt.commons.io.NullOutputStream;
import org.apache.oodt.commons.util.XML;
import org.apache.oodt.commons.io.CountingOutputStream;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** A result encoder/decoder for serialized objects.
 *
 * This codec uses a serialized object format for objects.
 *
 * @author Kelly
 */
class ObjectCodec implements Codec {
	public Node encode(Object object, Document doc) throws DOMException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try {
			Base64EncodingOutputStream base64 = new Base64EncodingOutputStream(byteArray);
			ObjectOutputStream objStream = new ObjectOutputStream(base64);
			objStream.writeObject(object);
			objStream.close();
		} catch (IOException ignored) {}
		Element value = doc.createElement("resultValue");
		value.appendChild(doc.createCDATASection(byteArray.toString()));
		return value;
	}

	public Object decode(Node node) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException,
		OptionalDataException {
		String encodedValue;
		if (node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE) {
		  encodedValue = node.getFirstChild().getNodeValue();
		} else {
		  encodedValue = XML.text(node);
		}
		Object rc = null;
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(encodedValue.getBytes());
			Base64DecodingInputStream base64 = new Base64DecodingInputStream(byteArray);
			ObjectInputStream objStream = new ObjectInputStream(base64);
			rc = objStream.readObject();
			objStream.close();
		} catch (InvalidClassException ex) {
			throw ex;
		} catch (StreamCorruptedException ex) {
			throw ex;
		} catch (OptionalDataException ex) {
			throw ex;
		} catch (IOException ignored) {}
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

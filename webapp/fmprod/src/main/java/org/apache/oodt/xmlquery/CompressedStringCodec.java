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

import org.apache.oodt.commons.io.Base64DecodingInputStream;
import org.apache.oodt.commons.io.Base64EncodingOutputStream;
import org.apache.oodt.commons.util.XML;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** A result encoder/decoder for compressed strings.
 *
 * This codec uses a GZIP compressed string format for objects.
 *
 * @author Kelly
 */
class CompressedStringCodec implements Codec {

  public static final int INT = 1024;

  public Node encode(Object object, Document doc) throws DOMException {
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		try {
			Base64EncodingOutputStream base64 = new Base64EncodingOutputStream(byteArray);
			GZIPOutputStream gzip = new GZIPOutputStream(base64);
			gzip.write(object.toString().getBytes());
			gzip.close();
		} catch (IOException ignored) {}
		Element value = doc.createElement("resultValue");
		value.appendChild(doc.createCDATASection(byteArray.toString()));
		return value;
	}

	public Object decode(Node node) {
		String encodedValue;
		if (node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE) {
		  encodedValue = node.getFirstChild().getNodeValue();
		} else {
		  encodedValue = XML.text(node);
		}
		String rc = null;
		try {
			ByteArrayInputStream byteArray = new ByteArrayInputStream(encodedValue.getBytes());
			Base64DecodingInputStream base64 = new Base64DecodingInputStream(byteArray);
			GZIPInputStream gzip = new GZIPInputStream(base64);
			StringBuilder b = new StringBuilder();
			int numRead;
			byte[] buf = new byte[INT];
			while ((numRead = gzip.read(buf)) != -1) {
			  b.append(new String(buf, 0, numRead));
			}
			gzip.close();
			rc = b.toString();
		} catch (IOException ignored) {}
		return rc;
	}

	public InputStream getInputStream(Object value) {
		return new ByteArrayInputStream(((String) value).getBytes());
	}

	public long sizeOf(Object obj) {
		return ((String) obj).getBytes().length;
	}
}

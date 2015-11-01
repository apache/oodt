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
import java.io.InputStream;
import org.apache.oodt.commons.util.Base64;
import org.apache.oodt.commons.util.XML;
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
		if (node.getFirstChild() != null && node.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE) {
		  encodedValue = node.getFirstChild().getNodeValue();
		} else {
		  encodedValue = XML.text(node);
		}
		if (encodedValue.length() <= 0) {
		  return new byte[0];
		}
		return Base64.decode(encodedValue.getBytes());
	}

	public InputStream getInputStream(Object value) {
		return new ByteArrayInputStream((byte[]) value);
	}

	public long sizeOf(Object object) {
		return ((byte[]) object).length;
	}
}

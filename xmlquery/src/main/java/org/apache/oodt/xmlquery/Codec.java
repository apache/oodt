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

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/** Result encoder/decoder.
 *
 * Classes that implement this interface encode and decode query results to and from XML
 * format.
 *
 * @author Kelly
 */
interface Codec {
	/** Encode the given object into XML format.
	 *
	 * The encoding takes the object, encodes into a DOM structure, and returns a
	 * &lt;resultValue&gt; element.
	 *
	 * @param object Object to encode.
	 * @param doc What document will own the created XML nodes.
	 * @return A &lt;resultValue&gt; element encoding the <var>object</var>.
	 * @throws DOMException If an error occurs while encoding the object.
	 */
	Node encode(Object object, Document doc) throws DOMException;

	/** Decode the given XML representation into its object.
	 *
	 * The decoding takes the &lt;resultValue&gt; node, and decodes it into the object
	 * it represents.
	 *
	 * @param node The &lt;resultValue&gt; node.
	 * @return The object that the <var>node</var> represents.
	 * @throws ClassNotFoundException If the class of the object in <var>node</var> can't be found.
	 * @throws InvalidClassException If something is wrong with the class encoded in <var>node</var>.
	 * @throws StreamCorruptedException When control information in <var>node</var> is inconsistent.
	 * @throws OptionalDataException If primitive datatypes instead of an object was found encoded in the <var>node</var>.
	 */
	Object decode(Node node) throws ClassNotFoundException, InvalidClassException, StreamCorruptedException,
		OptionalDataException;

	/**
	 * Compute the size of the given object.
	 *
	 * @param object Object.
	 * @return Size of <var>object</var> in bytes.
	 */
	long sizeOf(Object object);

	/**
	 * Yield the given object as a stream.
	 *
	 * This method takes the given object and yields a stream version of it
	 * appropriate concrete codec type.  For example, {@link ByteArrayCodec}s may
	 * yield simple {@link ByteArrayInputStream}s for their objects.
	 *
	 * @param object The object to be streamed.
	 * @return An <code>InputStream</code> of <var>object</var>.
	 * @throws IOException if an error occurs.
	 */
	InputStream getInputStream(Object object) throws IOException;
}

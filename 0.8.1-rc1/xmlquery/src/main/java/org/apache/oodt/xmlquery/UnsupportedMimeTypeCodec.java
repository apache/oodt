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

import java.io.*;
import org.w3c.dom.*;

/** A result encoder/decoder for unsupported MIME types.
 *
 * This codec throws <code>UnsupportedOperationException</code>s on any encoding or
 * decoding attempt.
 *
 * @author Kelly
 */
class UnsupportedMimeTypeCodec implements Codec {
	public Node encode(Object object, Document doc) {
		throw new UnsupportedOperationException("MIME type not supported for encoding");
	}
	public Object decode(Node node) {
		throw new UnsupportedOperationException("MIME type not supported for decoding");
	}
	public long sizeOf(Object object) {
		throw new UnsupportedOperationException("MIME type not supported for sizing");
	}
	public InputStream getInputStream(Object object) {
		throw new UnsupportedOperationException("MIME type not supported for streaming");
	}
}

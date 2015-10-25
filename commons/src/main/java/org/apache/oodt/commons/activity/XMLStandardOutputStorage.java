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

package org.apache.oodt.commons.activity;

import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Simple storage that writes XML documents describing activities to the standard output.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class XMLStandardOutputStorage extends XMLStorage {
	/**
	 * Creates a new {@link XMLStandardOutputStorage} instance.
	 */
	public XMLStandardOutputStorage() {
		super();
		factory = TransformerFactory.newInstance();
	}

	protected void saveDocument(Document doc) {
		try {
			Transformer transformer = factory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);
			System.out.println();
		} catch (TransformerException ex) {
			throw new IllegalStateException("Unexpected TransformerException: " + ex.getMessage());
		}
	}

	/** Factory for transformers that serialize XML documents into plain text. */ 
	private TransformerFactory factory;
}

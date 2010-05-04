// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: XMLStandardOutputStorage.java,v 1.1 2004-03-02 19:28:58 kelly Exp $

package jpl.eda.activity;

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

	protected void saveDocument(Document doc) throws IOException {
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

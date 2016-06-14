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

import java.util.*;
import org.apache.oodt.commons.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/** Unit test the {@link XMLQuery} class.
 *
 * @author Kelly
 */ 
public class XMLQueryTest extends org.apache.oodt.commons.ConfiguredTestCase {
	/** Construct the test case for the {@link XMLQuery} class. */
	public XMLQueryTest(String name) {
		super(name);
	}

	public void testCtor() {
		List mimes = new ArrayList();
		mimes.add("text/plain");
		mimes.add("image/jpeg");
		XMLQuery q = new XMLQuery("profStatusId = UNKNOWN OR A > 3 AND RETURN = C",
			"id", "title", "description", "dataDictID", "resultModeID", "propType", "propLevels", 45, mimes);
		assertEquals("profStatusId = UNKNOWN OR A > 3 AND RETURN = C", q.getKwdQueryString());
		assertEquals(45, q.getMaxResults());
		assertEquals(1, q.getSelectElementSet().size());
		assertEquals(new QueryElement("elemName", "C"), q.getSelectElementSet().get(0));
		assertEquals(0, q.getFromElementSet().size());
		assertEquals(6, q.getWhereElementSet().size());
		assertEquals(new QueryElement("elemName", "profStatusId"), q.getWhereElementSet().get(0));
		assertEquals(new QueryElement("LITERAL", "UNKNOWN"), q.getWhereElementSet().get(1));
		assertEquals(new QueryElement("RELOP", "EQ"), q.getWhereElementSet().get(2));
		assertEquals(new QueryElement("elemName", "A"), q.getWhereElementSet().get(3));
		assertEquals(new QueryElement("LITERAL", "3"), q.getWhereElementSet().get(4));
		assertEquals(new QueryElement("RELOP", "GT"), q.getWhereElementSet().get(5));
		// Need some testing of expressions with LOGOP's, but NOT handling seems broken.
		assertEquals(0, q.getResults().size());
		assertEquals(2, q.getMimeAccept().size());
	}		
	
	public void testObjectMethods() {
		XMLQuery q1 = new XMLQuery("Subject < Phrenology OR A > 3 AND RETURN = C",
			"id", "title", "description", "dataDictID", "resultModeID", "propType", "propLevels", 45);
		XMLQuery q2 = new XMLQuery("Subject < Phrenology OR A > 3 AND RETURN = C",
			"id", "title", "description", "dataDictID", "resultModeID", "propType", "propLevels", 45);
		XMLQuery q3 = new XMLQuery("Subject > Phrenology OR A < 3 AND RETURN = D",
			"id", "title", "description", "dataDictID", "resultModeID", "propType", "propLevels", 45);
		assertEquals(q1, q1);
		assertEquals(q1, q2);
		assertTrue(!q1.equals(q3));
		XMLQuery q4 = (XMLQuery) q3.clone();
		assertEquals(q3, q4);
		assertTrue(q3 != q4);
	}

	public void testParser() {
		XMLQuery q1 = new XMLQuery("(A < 1 AND A > 2) AND RETURN = B", "id", "title", "description", "dataDictID",
			"resultModeID", "propType", "propLevels", 45);
		List where = q1.getWhereElementSet();
		assertEquals(7, where.size());

		QueryElement qe;

		qe = (QueryElement) where.get(0);
		assertEquals("elemName", qe.getRole());
		assertEquals("A", qe.getValue());

		qe = (QueryElement) where.get(1);
		assertEquals("LITERAL", qe.getRole());
		assertEquals("1", qe.getValue());

		qe = (QueryElement) where.get(2);
		assertEquals("RELOP", qe.getRole());
		assertEquals("LT", qe.getValue());

		qe = (QueryElement) where.get(3);
		assertEquals("elemName", qe.getRole());
		assertEquals("A", qe.getValue());

		qe = (QueryElement) where.get(4);
		assertEquals("LITERAL", qe.getRole());
		assertEquals("2", qe.getValue());

		qe = (QueryElement) where.get(5);
		assertEquals("RELOP", qe.getRole());
		assertEquals("GT", qe.getValue());

		qe = (QueryElement) where.get(6);
		assertEquals("LOGOP", qe.getRole());
		assertEquals("AND", qe.getValue());

		List select = q1.getSelectElementSet();
		assertEquals(1, select.size());
		
		qe = (QueryElement) select.get(0);
		assertEquals("elemName", qe.getRole());
		assertEquals("B", qe.getValue());
	}

	public void testXML() {
		NodeList children;

		List mimes = new ArrayList();
		mimes.add("text/xml");
		mimes.add("image/gif");
		XMLQuery q1 = new XMLQuery("Subject < Phrenology OR A > 3 AND RETURN = C",
			"id", "title", "description", "dataDictID", "resultModeID", "propType", "propLevels", 45, mimes);
		Document doc = q1.getXMLDoc();
		Node root = doc.getDocumentElement();
		assertEquals("query", root.getNodeName());

		Node queryAttributes = root.getFirstChild();
		assertEquals("queryAttributes", queryAttributes.getNodeName());
		children = queryAttributes.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			String text = XML.text(child);
			if ("queryId".equals(name)) {
				assertEquals("id", text);
			} else if ("queryTitle".equals(name)) {
				assertEquals("title", text);
			} else if ("queryDesc".equals(name)) {
				assertEquals("description", text);
			} else if ("queryType".equals(name)) {
				assertEquals("QUERY", text);
			} else if ("queryStatusId".equals(name)) {
				assertEquals("ACTIVE", text);
			} else if ("querySecurityType".equals(name)) {
				assertEquals("UNKNOWN", text);
			} else if ("queryRevisionNote".equals(name)) {
				assertEquals("1999-12-12 JSH V1.0 Under Development", text);
			} else if ("queryDataDictId".equals(name)) {
				assertEquals("dataDictID", text);
			} else fail("Unknown node <" + name + "> under <queryAttributes>");
		}

		Node queryResultMode = queryAttributes.getNextSibling();
		assertEquals("queryResultModeId", queryResultMode.getNodeName());
		assertEquals("resultModeID", XML.text(queryResultMode));

		Node propogationType = queryResultMode.getNextSibling();
		assertEquals("queryPropogationType", propogationType.getNodeName());
		assertEquals("propType", XML.text(propogationType));

		Node propogationLevels = propogationType.getNextSibling();
		assertEquals("queryPropogationLevels", propogationLevels.getNodeName());
		assertEquals("propLevels", XML.text(propogationLevels));

		Node mimeNode = propogationLevels.getNextSibling();
		assertEquals("queryMimeAccept", mimeNode.getNodeName());
		assertEquals("text/xml", XML.text(mimeNode));

		mimeNode = mimeNode.getNextSibling();
		assertEquals("queryMimeAccept", mimeNode.getNodeName());
		assertEquals("image/gif", XML.text(mimeNode));		

		Node maxResults = mimeNode.getNextSibling();
		assertEquals("queryMaxResults", maxResults.getNodeName());
		assertEquals("45", XML.text(maxResults));

		Node results = maxResults.getNextSibling();
		assertEquals("queryResults", results.getNodeName());
		assertEquals("0", XML.text(results));

		Node kwqString = results.getNextSibling();
		assertEquals("queryKWQString", kwqString.getNodeName());
		assertEquals("Subject < Phrenology OR A > 3 AND RETURN = C", XML.text(kwqString));

		Node node = kwqString.getNextSibling();
		assertEquals("queryStatistics", node.getNodeName());
		node = node.getNextSibling();
		assertEquals("querySelectSet", node.getNodeName());
		node = node.getNextSibling();
		assertEquals("queryFromSet", node.getNodeName());
		node = node.getNextSibling();
		assertEquals("queryWhereSet", node.getNodeName());
		node = node.getNextSibling();
		assertEquals("queryResultSet", node.getNodeName());
		assertNull(node.getNextSibling());

		XMLQuery q2 = new XMLQuery(root);
		assertEquals(q1, q2);
	}

	/**
	 * Test if we can parse an XML query document even with an inaccessible system ID.
	 *
	 * @throws SAXException if an error occurs.
	 */
	public void testXMLEntityResolution() throws SAXException {
		new XMLQuery(BAD_HOST);
	}

	private static String BAD_HOST = "<!DOCTYPE query PUBLIC '-//JPL//DTD OODT Query 1.0//EN' "
		+ "'http://unknown-host.unk/edm-query/query.dtd'>\n<query><queryAttributes><queryId>queryServlet</queryId>"
		+ "<queryTitle>QueryfromQueryServlet</queryTitle><queryDesc>Bad host name in system ID</queryDesc><queryType>"
		+ "QUERY</queryType><queryStatusId>ACTIVE</queryStatusId><querySecurityType>UNKNOWN</querySecurityType>"
		+ "<queryRevisionNote>1999-12-12JSHV1.0UnderDevelopment</queryRevisionNote><queryDataDictId>UNKNOWN"
		+ "</queryDataDictId></queryAttributes><queryResultModeId>ATTRIBUTE</queryResultModeId><queryPropogationType>"
		+ "BROADCAST</queryPropogationType><queryPropogationLevels>N/A</queryPropogationLevels><queryMimeAccept>*/*"
		+ "</queryMimeAccept><queryMaxResults>100</queryMaxResults><queryResults>0</queryResults><queryKWQString>"
		+ "RETURN = SPECIMEN_COLLECTED_CODE</queryKWQString><queryStatistics/><querySelectSet><queryElement>"
		+ "<tokenRole>elemName</tokenRole><tokenValue>SPECIMEN_COLLECTED_CODE</tokenValue></queryElement></querySelectSet>"
		+ "<queryFromSet/><queryWhereSet/><queryResultSet/></query>";

}

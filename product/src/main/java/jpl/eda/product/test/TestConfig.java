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


package jpl.eda.product.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Stress test configuration.  To configure a stress test, create an XML file with the following structure:
 * <code>&lt;test object='<var>product-server</var>'<br/>
 *   concurrency='<var>simultaneous</var>'<br/>
 *   maxWait='<var>timeout</var>'&gt;<br/>
 *   &lt;query expr='<var>expr<sub>0</sub></var>' md5='<var>md5<sub>0</sub></var>'/&gt;<br/>
 *   &lt;query expr='<var>expr<sub>1</sub></var>' md5='<var>md5<sub>1</sub></var>'/&gt;<br/>
 *   ...<br/>
 *   &lt;query expr='<var>expr<sub>n</sub></var>' md5='<var>md5<sub>n</sub></var>'/&gt;<br/>
 * &lt;/test></code>
 * <p>where</p><dl>
 * <dt><var>product-server</var></dt>
 * <dd>Address of the product server</dd>
 * <dt><var>simultaneous</var></dt>
 * <dd>Number of concurrent queries to run</dd>
 * <dt><var>timeout</var></dt>
 * <dd>Maximum time to wait (milliseconds) for a product transaction to complete</dd>
 * <dt><var>expr_<sub>n</sub></var></dt>
 * <dd>Keyword query expression</dd>
 * <dt><var>md5_<sub>n</sub></var></dt>
 * <dd>MD5 digest of expected response to query</dd>
 * </dl>
 *
 * @author Kelly
 * @version $Revision: 1.3 $
 */
public class TestConfig {
	/**
	 * Build a test configuration.
	 *
	 * @param file Config file.
	 * @return a <code>TestConfig</code> value.
	 * @throws Throwable if an error occurs.
	 */
	public static TestConfig parseQueries(File file) throws Throwable {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		factory.setIgnoringComments(true);
		factory.setNamespaceAware(false);
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		Element root = doc.getDocumentElement();
		String object = root.getAttribute("object");
		int concurrency = Integer.parseInt(root.getAttribute("concurrency"));
		int maxWait = Integer.parseInt(root.getAttribute("maxWait"));
		NodeList children = root.getChildNodes();
		List queries = new ArrayList(children.getLength());
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "query".equals(child.getNodeName())) {
				Element e = (Element) child;
				String expr = e.getAttribute("expr");
				String md5Str = e.getAttribute("md5");
				Query query;
				if (md5Str == null || md5Str.length() == 0)
					query = new Query(expr);
				else
					query = new Query(expr, toDigest(md5Str));
				queries.add(query);
			}
		}
		return new TestConfig(object, concurrency, maxWait, queries);
	}

	/**
	 * Creates a new <code>TestConfig</code> instance.
	 *
	 * @param object Product server address to query.
	 * @param concurrency How many queries to run at a time.
	 * @param maxWait How long to wait for a product transaction.
	 * @param queries a <code>List</code> value of {@link Query} objects. 
	 */
	private TestConfig(String object, int concurrency, int maxWait, List queries) {
		this.object = object;
		this.concurrency = concurrency;
		this.maxWait = maxWait;
		this.queries = queries;
	}

	/**
	 * Get the product server address.
	 *
	 * @return product server address
	 */
	public String getObject() {
		return object;
	}

	/**
	 * Get how many queries to run at once.
	 *
	 * @return how many queries to run at once.
	 */
	public int getConcurrency() {
		return concurrency;
	}

	/**
	 * Get how long to wait for a product transaction.
	 *
	 * @return how long to wait for a product transaction in milliseconds.
	 */
	public int getMaxWait() {
		return maxWait;
	}

	/**
	 * Get the queries to attempt.
	 *
	 * @return a <code>List</code> value of {@link Query} objects.
	 */
	public List getQueries() {
		return queries;
	}

	/** Product server address. */
	private String object;

	/** How many queries to run at once. */
	private int concurrency;

	/** How long to wait for a product transaction. */
	private int maxWait;

	/** Queries to attempt, a <code>List</code> value of {@link Query} objects. */
	private List queries;

	/**
	 * Parse the given string as if it were an MD5 digest byte array.
	 *
	 * @param s a <code>String</code> value.
	 * @return a <code>byte[]</code> value.
	 */
	private static byte[] toDigest(String s) {
		byte[] array = new byte[16];
		for (int i = 0; i < 16; ++i)
			array[i] = (byte) Short.parseShort(s.substring(i * 2, (i * 2) + 2), 16);
		return array;
	}
}

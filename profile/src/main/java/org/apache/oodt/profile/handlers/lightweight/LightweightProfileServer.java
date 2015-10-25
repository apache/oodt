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


package org.apache.oodt.profile.handlers.lightweight;

import org.apache.oodt.commons.util.DOMParser;
import org.apache.oodt.commons.util.XML;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileException;
import org.apache.oodt.profile.handlers.ProfileHandler;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A lightweight profile server.
 *
 * A lightweight profile server is lightweight because it doesn't rely on any external
 * database or other search/retrieval mechanism.
 *
 * @author Kelly
 */
final public class LightweightProfileServer implements ProfileHandler {
	/**
	 * Create a lightweight profile server using defaults.
	 *
	 * @throws IOException If an I/O error occurs.
	 * @throws SAXException If an error occurs parsing the profile file.
	 */
	public LightweightProfileServer() throws IOException, SAXException, URISyntaxException {
		this(System.getProperties());
	}

	/**
	 * Create a lightweight profile server using the given properties.
	 *
	 * The property we use is
	 * <code>profiles.url</code>,
	 * which is the URL of the file containing profile definitions that this server
	 * will read and serve.
	 *
	 * @param props Properties.
	 * @throws IOException If an I/O error occurs.
	 * @throws SAXException If an error occurs parsing the profile file.
	 * @throws MalformedURLException If the URL to the profile file is malformed.
	 */
	public LightweightProfileServer(Properties props)
		throws IOException, SAXException, URISyntaxException {
		this(new URI(props.getProperty("org.apache.oodt.profile.handlers.LightweightProfileServer.profiles.url",
                        props.getProperty("org.apache.oodt.profile.webServer.baseURL", "http://eda.jpl.nasa.gov")
                        + "/profiles.xml")),
			props.getProperty("org.apache.oodt.profile.handlers.LightweightProfileServer.id", "lightweight"));
	}

	/**
	 * Create a lightweight profile server using the given URL.
	 *
	 * @param url URL of the file containing profile definitions that this server will read and serve.
	 * @param id Identifier to report for when this handler is queried by name.
	 * @throws IOException If an I/O error occurs.
	 * @throws SAXException If an error occurs parsing the profile file.
	 */
	public LightweightProfileServer(URI url, String id) throws IOException, SAXException {
		this.id = id;

		// Get the list of profiles from the cache, if it's there.
	  profiles = (List) cache.get(url);
	  if (profiles != null) return;

		// It wasn't in the cache, so create a parser to parse the file.  We only
		// deal with correct files, so turn on validation and install an error
		// handler that will throw an exception.
		profiles = new ArrayList();
		DOMParser parser = XML.createDOMParser();
		parser.setErrorHandler(new ErrorHandler() {
			public void error(SAXParseException ex) throws SAXParseException {
				System.err.println("Parse error line " + ex.getLineNumber() + " column "
					+ ex.getColumnNumber() + ": " + ex.getMessage());
				throw ex;
			}
			public void warning(SAXParseException ex) {
				System.err.println("Parse warning: " + ex.getMessage());
			}
			public void fatalError(SAXParseException ex) throws SAXParseException {
				throw ex;
			}
		});

		// Parse the file.
		InputSource inputSource = new InputSource(url.toString());
		parser.parse(inputSource);
		Document doc = parser.getDocument();

		// Normalize it and get the document element, and from that, create a list
		// of profiles, and add it to the cache.
		doc.normalize();
		Element root = doc.getDocumentElement();
		profiles = Profile.createProfiles(root, new SearchableObjectFactory());
	  cache.put(url, profiles);

	  System.err.println("LightweightProfileServer ready");
	}

	public List findProfiles(XMLQuery query) throws ProfileException {
		// Compute the where-expression based on the query string, and start off
		// with an empty set of results.
		Set matchingProfiles = new HashSet();
		WhereExpression whereExpression = createWhereExpression(query);

		// Search each profile, and add the results of the search to the set of results.
	  for (Object profile1 : profiles) {
		SearchableProfile profile = (SearchableProfile) profile1;

		// Search the profile with the where-expression.
		Result result = profile.search(whereExpression);

		// If there are any matching elements, add the profile to the set.
		if (!result.matchingElements().isEmpty()) {
		  matchingProfiles.add(profile);
		}
	  }

		// Convert from set to list.
		return new ArrayList(matchingProfiles);
	}

	/**
	 * Get a single profile matching the given ID.
	 *
	 * @param profID a {@link String} value.
	 * @return a {@link Profile} value.
	 */
	public Profile get(String profID) {
		if (profID == null) return null;
		Profile rc = null;
	  for (Object profile : profiles) {
		Profile p = (Profile) profile;
		if (p.getProfileAttributes().getID().equals(profID)) {
		  rc = p;
		  break;
		}
	  }
		return rc;
	}

	/**
	 * Convert the XML query to a where expression.
	 *
	 * @param query The query to convert
	 * @return The equivalent, simplified where expression.
	 */
	private static WhereExpression createWhereExpression(XMLQuery query) {
		Stack stack = new Stack();
		 
		// Collect together the where- and from-sets, joined with an "and".
		List allElements = new ArrayList(query.getWhereElementSet());
		List fromElements = query.getFromElementSet();
		if (!fromElements.isEmpty()) {
			allElements.addAll(fromElements);
			allElements.add(new QueryElement("LOGOP", "AND"));
		}
		
		// For each item in the where-set
	  for (Object allElement : allElements) {
		QueryElement queryElement = (QueryElement) allElement;

		// Get the keyword and its type.
		String keyword = queryElement.getValue();
		String type = queryElement.getRole();

		if (type.equals("elemName")) {
		  // It's an element name, so push the element name.
		  stack.push(keyword);
		} else if (type.equals("LITERAL")) {
		  // It's a literal value, so push the value.
		  stack.push(keyword);
		} else if (type.equals("LOGOP")) {
		  // It's a logical operator.  Pop the operands off the
		  // stack and push the appropriate operator back on.
		  if (keyword.equals("AND")) {
			stack.push(new AndExpression((WhereExpression) stack.pop(), (WhereExpression) stack.pop()));
		  } else if (keyword.equals("OR")) {
			stack.push(new OrExpression((WhereExpression) stack.pop(), (WhereExpression) stack.pop()));
		  } else if (keyword.equals("NOT")) {
			stack.push(new NotExpression((WhereExpression) stack.pop()));
		  } else {
			throw new IllegalArgumentException("Illegal operator \"" + keyword + "\" in query");
		  }
		} else if (type.equals("RELOP")) {
		  // It's a relational operator.  Pop the element name and
		  // literal value off the stack, and push the operator
		  // expression on with the given operator.
		  stack.push(new OperatorExpression((String) stack.pop(), (String) stack.pop(), keyword));
		}
	  }

		// If there's nothing on the stack, we're given nothing, so give back everything.
		if (stack.size() == 0)
			return new ConstantExpression(true);
		else if (stack.size() > 1)
			throw new IllegalStateException("Imbalanced expression in query");
		
		// Simplify/optimize the where-expression and return it.
		return ((WhereExpression) stack.pop()).simplify();
	}

	/** {@inheritDoc} */
	public String getID() {
		return id;
	}

	/** Profiles I serve. */
	private List profiles;

	/**
	 * Cache of profiles.
	 *
	 * This is a mapping from {@link java.net.URL} of the profile source to the {@link
	 * List} of profiles.  We do this so we don't have to keep rereading and reparsing
	 * the possibly huge profile file each time an object of this class is
	 * instantiated.
	 *
	 * <p><em>Question:</em> Since when are multiple LightweightProfileServers being
	 * constructed anyway?  There's just one per profile server process, and it's
	 * using just the one file, so there should be no need for this cache.  Who added
	 * this?  And if it were me, what was I smoking?
	 */
	private static Map cache = new HashMap();

	/** My ID. */
	private String id;

	/**
	 * Application execution entry point.
	 *
	 * This lets you try out a query to the Lightweight Profile server.
	 *
	 * @param argv Command-line arguments.
	 * @throws Exception Should any error occur.
	 */
	public static void main(String[] argv) throws Exception {
		if (argv.length == 0) {
			System.err.println("Usage: <query>...");
			System.exit(1);
		}

		// Create the profile
		LightweightProfileServer lp = new LightweightProfileServer();

		// Gather together the command-line arguments into a single long string.
		StringBuilder b = new StringBuilder();
	  for (String anArgv : argv) {
		b.append(anArgv).append(' ');
	  }

		// Create the query object from the expression.
		XMLQuery query = new XMLQuery(b.toString().trim(), /*id*/"cli1", /*title*/"CmdLine-1",
			/*desc*/"This is a query entered on the command-line", /*ddId*/null, /*resultModeId*/null,
			/*propType*/null, /*propLevels*/null, XMLQuery.DEFAULT_MAX_RESULTS);

		// Display the results.
		System.out.println(lp.findProfiles(query));

		// All done.
		System.exit(0);
	}
}

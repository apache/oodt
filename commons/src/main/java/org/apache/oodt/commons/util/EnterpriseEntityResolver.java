// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/** XML entity resolver for enterprise applications.
 *
 * This resolver attempts to retrieves entities from local sources before deferring to the
 * default parser's entity resolver.
 *
 * @author Kelly
 */
public class EnterpriseEntityResolver implements EntityResolver {
	/** Mapping of public identifiers to known file names. */
	static Map entities;

	/** Initialize the class by reading the entites.xml file. */
	static {
		entities = new HashMap();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(true);
			javax.xml.parsers.SAXParser p = factory.newSAXParser();
			p.parse(new InputSource(EnterpriseEntityResolver.class.getResourceAsStream("entities.xml")),
				new DefaultHandler() {
					private StringBuffer token = new StringBuffer();
					private String pi;
					public void characters(char[] ch, int start, int length) {
						token.append(ch, start, length);
					}
					public void endElement(String ns, String name, String qual) {
						if ("pi".equals(qual)) {
						  pi = token.toString().trim();
						} else if ("filename".equals(qual)) {
							entities.put(pi, token.toString().trim());
						}
						token.delete(0, token.length());
					}
				});
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		} catch (SAXParseException ex) {
			System.err.println("Error parsing entities.xml at line " + ex.getLineNumber() + ", column "
				+ ex.getColumnNumber() + "; ignoring entity lookup");
		} catch (SAXException ex) {
			System.err.println("Exception parsing entities.xml: " + ex.getMessage() + "; ignoring entity lookup");
		} catch (IOException ex) {
			System.err.println("I/O error reading entities.xml: " + ex.getMessage() + "; ignoring entity lookup");
		}
	}

	public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
		String filename = computeFilename(publicID, systemID);
		if (filename == null) {
		  return null;
		}

		// Resolve it using class loader first.  Any DTD in the toplevel directory
		// of any jar present to the application is a potential source.
		InputStream in = getClass().getResourceAsStream("/" + filename);
		if (in != null) {
		  return new InputSource(new BufferedReader(new InputStreamReader(in)));
		}

		// OK, try the filesystem next.  You can control what directories get
		// searched by setting the entity.dirs property.
		File file = findFile(getEntityRefDirs(System.getProperty("entity.dirs", "")), filename);
		if (file != null) {
		  try {
			return new InputSource(new BufferedReader(new FileReader(file)));
		  } catch (IOException ignore) {
		  }
		}

		// No luck either way.
		return null;
	}

	/** Compute the possible filename from public and system identifiers.
	 *
	 * This attempts to map the public ID to a known file based on a table (see
	 * <code>entity.xml</code>).  If that doesn't work, then it will use the
	 * file part of the system ID.  If there's no file part, it returns null.
	 *
	 * @param publicID The public identifier.
	 * @param systemID The system identifier.
	 * @return A file computed from <var>publicID</var> and <var>systemID</var>.
	 */
	static String computeFilename(String publicID, String systemID) {
		String name = (String) entities.get(publicID);
		if (name == null) {
		  try {
			URL url = new URL(systemID);
			File file = new File(url.getFile());
			name = file.getName();
		  } catch (MalformedURLException ignore) {
		  }
		}
		return name;
	}

	/**
	 * Get a list of entity directories from the given string specification.
	 *
	 * @param spec Directory names separated by commas.
	 * @return a {@link List} of those directory names.
	 */
	static List getEntityRefDirs(String spec) {
		List dirs = new ArrayList();
		for (StringTokenizer t = new StringTokenizer(spec, ",;|"); t.hasMoreTokens();) {
		  dirs.add(t.nextToken());
		}
		return dirs;
	}

	/** Find a file under a list of directories.
	 * 
	 * @param dirs List of {@link java.lang.String} directory names.
	 * @param filename Name of the file to find under one of the directories named in <var>dirs</var>.
	 * @return The first path to the file named by <var>filename</var> under a directory in <var>dirs</var>,
	 * or null if no directory in <var>dirs</var> contains a file named <var>filename</var>.
	 */
	static File findFile(List dirs, String filename) {
	  for (Object dir : dirs) {
		File potentialFile = new File((String) dir, filename);
		if (potentialFile.isFile()) {
		  return potentialFile;
		}
	  }
		return null;
	}
}

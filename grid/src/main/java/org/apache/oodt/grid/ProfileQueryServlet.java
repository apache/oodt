/**
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

package org.apache.oodt.grid;

import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.handlers.ProfileHandler;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Profile query servlet handles profile queries.  It returns every matching profile from
 * every query handler that provides matching profiles.  If no handlers are installed,
 * then it returns an empty &lt;profiles&gt; document.
 * 
 */
public class ProfileQueryServlet extends QueryServlet {
	/** {@inheritDoc} */
	protected List getServers(Configuration config) {
		return config.getProfileServers();
	}

	/** {@inheritDoc} */
	protected void handleQuery(XMLQuery query, List handlers, HttpServletRequest req, HttpServletResponse res)
		throws IOException {
		// Find if the query should be targeted to specific handlers.
		Set ids = new HashSet();
		if (query.getFromElementSet() != null) {
			for (Iterator i = query.getFromElementSet().iterator(); i.hasNext();) {
				QueryElement qe = (QueryElement) i.next();
				if ("handler".equals(qe.getRole()) && qe.getValue() != null)
					ids.add(qe.getValue());
			}
		}
		
		res.setContentType("text/xml");					       // XML, comin' at ya
		res.getWriter().println("<?xml version='1.0' encoding='UTF-8'?>");     // UTF-8 no less.  Boo-ya.
		res.getWriter().println("<!DOCTYPE profiles PUBLIC '"		       // Get a doctype in there for the highly ...
			+ Profile.PROFILES_DTD_FPI + "' '"			       // ... anal ...
			+ Profile.PROFILES_DTD_URL + "'>");		               // ... retentive.
		res.getWriter().println("<profiles>");				       // Start tag for the whole /sbin/fsck mess
		Transformer transformer = null;					       // Start out w/no transformer and no doc
		Document doc = null;						       // Don't make 'em if we don't need 'em
		boolean sentAtLeastOne = false;					       // Track if we send any profiles at all
		Exception exception = null;					       // And save any exception
		for (Iterator i = handlers.iterator(); i.hasNext();) try {	       // To iterate over each handler
			ProfileHandler handler = (ProfileHandler) i.next();            // Get the handler
			String id = handler.getID();                                   // Get the ID, and if targeting to IDs
			if (!ids.isEmpty() && !ids.contains(id)) continue;             // ... and it's not one we want, skip it.
			List results = handler.findProfiles(query);                    // Have it find profiles
			if (results == null) results = Collections.EMPTY_LIST;         // Assume nothing
			for (Iterator j = results.iterator(); j.hasNext();) {          // For each matching profile
				Profile profile = (Profile) j.next();	               // Get the profile
				if (transformer == null) {		               // No transformer/doc yet?
					transformer = createTransformer();             // Then make the transformer
					doc = Profile.createProfileDocument();         // And the doc
				}					               // And use the doc ...
				Node profileNode = profile.toXML(doc);	               // To render the profile into XML
				DOMSource source = new DOMSource(profileNode);         // And the XML becomes is source
				StreamResult result = new StreamResult(res.getWriter()); // And the response is a result
				transformer.transform(source, result);	               // And serialize into glorious text
				sentAtLeastOne = true;				       // OK, we got at least one out the doo
			}
		} catch (Exception ex) {					       // Uh oh
			exception = ex;						       // OK, just hold onto it for now
		}
		if (!sentAtLeastOne && exception != null) {			       // Got none out the door and had an error?
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,    // Then we can report it.
				exception.getMessage());
			return;
		}

		// However, if we get here, we got at least one profile out the door. In
		// that case, hide any error that might've occurred from subsequent
		// handlers.  (Or, if we get here, there were no errors.  Yay.)
		res.getWriter().println("</profiles>");				       // End tag for the whole fsck'n mess
	}

	/**
	 * Create a transformer, properly configured for XML text serialization.
	 *
	 * @return a <code>Transformer</code> value.
	 * @throws TransformerException if an error occurs.
	 */
	private static Transformer createTransformer() throws TransformerException {
		Transformer transformer;
		synchronized (TRANSFORMER_FACTORY) {
			transformer = TRANSFORMER_FACTORY.newTransformer();
		}
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		return transformer;
	}

	/** Sole transformer factory this class will ever need. */
	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
}


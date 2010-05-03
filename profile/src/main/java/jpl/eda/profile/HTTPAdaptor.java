// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: HTTPAdaptor.java,v 1.2 2005/12/01 22:45:00 kelly Exp $

package jpl.eda.profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jpl.eda.xmlquery.XMLQuery;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.BufferedInputStream;
import java.io.FilterInputStream;

/**
 * Adapt the web-grid HTTP profile service into the procedural interface expected by the
 * profile client.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
class HTTPAdaptor implements ProfileService {
	/**
	 * Creates a new <code>HTTPAdaptor</code> instance.
	 *
	 * @param url URL to web-grid HTTP profile service.
	 */
	HTTPAdaptor(URL url) {
		this.url = url;
	}

	/** {@inheritDoc} */
	public Server createServer() {
		return new HTTPAccessor();
	}
	
	/** URL to web-grid HTTP profile service. */
	private URL url;

	/**
	 * Access for a single query session.
	 */
	private class HTTPAccessor implements Server {
		public Profile getProfile(String id) {
			throw new UnsupportedOperationException("Retrieval of a single profile over HTTP not yet implemented");
		}

		public void add(String p) {
			throw new UnsupportedOperationException("Adding a profile over HTTP not yet implemented");
		}

		public boolean remove(String p, String v) {
			throw new UnsupportedOperationException("Removing a profile over HTTP not yet implemented");
		}

		public void replace(String p) {
			throw new UnsupportedOperationException("Replacing a profile over HTTP not yet implemented");
		}

		/**
		 * Handle a query for profiles.  This method sends the XMLQuery as XML to
		 * the web-grid profile service.  It parses the entire XML response into a
		 * DOM tree in memory and then converts that to a list of Profile objects.
		 * This is unfortunate if there is a large number of returned profiles.
		 * Sadly, we have no other choice because that's the interface stipulated.
		 *
		 * @param q a <code>XMLQuery</code> value.
		 * @return List of matching {@link Profile}s.
		 * @throws ProfileException if an error occurs.
		 */
		public List query(XMLQuery q) throws ProfileException {
			List profiles = Collections.EMPTY_LIST;
			InputStream in = null;
			try {
				StringBuffer b = new StringBuffer("xmlq=");
				b.append(URLEncoder.encode(q.getXMLDocString(), "UTF-8"));

				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setDoInput(true);
				c.setDoOutput(true);
				c.setUseCaches(true);
				c.setAllowUserInteraction(false);
				c.setInstanceFollowRedirects(true);
				c.setRequestMethod("POST");
				c.setRequestProperty("Content-Length", String.valueOf(b.length()));
				c.connect();
				OutputStream out = null;
				try {
					out = c.getOutputStream();
					out.write(b.toString().getBytes());
				} finally {
					if (out != null) try {
						out.close();
					} catch (IOException ignore) {}
				}
				int rc = c.getResponseCode();
				if (rc == HttpURLConnection.HTTP_OK) {
					in = c.getInputStream();
					DocumentBuilder documentBuilder;
					synchronized (DOCUMENT_BUILDER_FACTORY) {
						documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
					}
					Document doc = documentBuilder.parse(in);
					profiles = Profile.createProfiles(doc.getDocumentElement());
				} else if (rc == HttpURLConnection.HTTP_NOT_FOUND) {
					// no prob, just return the empty list
				} else {
					throw new ProfileException("Received response code " + rc + " from HTTP server");
				}
			} catch (IOException ex) {
				throw new ProfileException(ex);
			} catch (ParserConfigurationException ex) {
				throw new ProfileException(ex);
			} catch (SAXParseException ex) {
				throw new ProfileException("SAX parse error at line " + ex.getLineNumber() + ", column "
					+ ex.getColumnNumber() + ", for entity with public ID " + ex.getPublicId() + ", system ID "
					+ ex.getSystemId() + ": " + ex.getMessage());
			} catch (SAXException ex) {
				throw new ProfileException(ex);
			} finally {
				if (in != null) try {
					in.close();
				} catch (IOException ignore) {}
			}
			return profiles;
		}
	}

	/** Sole factory of document builders this class will ever need. */
	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;

	static {
		DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
		DOCUMENT_BUILDER_FACTORY.setCoalescing(true);
		DOCUMENT_BUILDER_FACTORY.setIgnoringComments(true);
		DOCUMENT_BUILDER_FACTORY.setIgnoringElementContentWhitespace(true);
		DOCUMENT_BUILDER_FACTORY.setNamespaceAware(false);
		DOCUMENT_BUILDER_FACTORY.setValidating(true);
	}
}

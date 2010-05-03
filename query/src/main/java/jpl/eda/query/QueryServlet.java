// Copyright 1999-2001 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: QueryServlet.java,v 1.1.1.1 2004-03-04 18:35:16 kelly Exp $

package jpl.eda.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jpl.eda.Configuration;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;
import jpl.eda.product.ProductClient;
import jpl.eda.profile.ProfileClient;
import org.w3c.dom.Document;
import jpl.eda.profile.Profile;
import java.util.Iterator;
import org.w3c.dom.Node;
import jpl.eda.util.XML;
import jpl.eda.EDAException;
import java.util.List;
import jpl.eda.profile.ProfileException;
import jpl.eda.product.ProductException;
import jpl.eda.activity.ActivityTracker;
import jpl.eda.activity.Activity;
import jpl.eda.xmlquery.activity.QueryConstructed;
import jpl.eda.profile.activity.ConnectedToServer;
import jpl.eda.xmlquery.activity.ResultsReceived;

// DEFN xmlQuery(x) = XMLQuery object created from XML string x
// NFED
// DEFN param(a, b) = string value of parameter a from HttpServletRequest b
// NFED
// DEFN query(type, server, name, query) =
//          type is "profile" -> query := profileSearch(server, name, query)
//          type is "product" -> query := productSearch(server, name, query)
// NFED
// DEFN isValid(req) = true if req contains parameters type, object, and query,
//      and type is either "profile" or "product"
// NFED

/** Servlet that handles OODT queries.
 *
 * This servlet takes the following parameters, either from a GET or POST request:
 *
 * <ul>
 *
 *   <li><code>type</code> which should be either <code>profile</code> or
 *   <code>product</code>, indicating what kind of query to perform.</li>
 *
 *   <li><code>object</code> which is the name of the CORBA object to which to send the
 *   query.</li>
 *
 * </ul>
 *
 * <p>Then, either the <code>query</code> parameter must be specified, and its value set
 * to the XML serialized format of an {@link XMLQuery} object, or any of the following may
 * be specified (default values are used for unspecified values):
 * <ul>
 *
 *   <li><code>keywordQuery</code> is the DIS-style keyword query string.</li>
 *
 *   <li><code>id</code> is the ID of the query.</li>
 *
 *   <li><code>title</code> is the title of the query.</li>
 *
 *   <li><code>desc</code> is the description of the query.</li>
 *
 *   <li><code>ddID</code> is the data dictionary ID.</li>
 *
 *   <li><code>resultModeID</code> is the return instance, INSTANCE, PROFILE, or CLASS.</li>
 *
 *   <li><code>propType</code> is either BROADCAST or PROPOGATE.</li>
 *
 *   <li><code>propLevels</code> is how deep to propogate.</li>
 *
 *   <li><code>maxResults</code> is how many results to retrieve.</li>
 * </ul>
 *
 *
 * <p>In response, this servlet delivers the {@link XMLQuery} with results to the
 * requestor in XML format, using the MIME type <code>text/xml</code>.
 *
 * @author Kelly
 */
public class QueryServlet extends HttpServlet {
	public void init() throws ServletException {
		try {
			jpl.eda.ExecServer.runInitializers();
		} catch (EDAException ex) {
			throw new ServletException(ex);
		}

		preferredNamespace = System.getProperty("jpl.eda.query.preferredNamespace", "urn:eda:rmi:");
	}

	/** Construct the query servlet.
	 */
	public QueryServlet() {
		super();
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doIt(req, res);
	}
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doIt(req, res);
	}

	/** Process the request for the query parameters, query, and deliver the XML query
	 * result.
	 *
	 * @param req Servlet request.
	 * @param res Servlet response.
	 * @throws ServletException If there's something wrong with the request or response.
	 * @throws IOException If there's an I/O error writing the image.
	 */
	private void doIt(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// FXN: [ isValid(req) -> res := query(param("type", req), ns(req), param("object", req),
		//                                  xmlQuery(param("query", req)))
		//      | true         -> res := error message                                                       ]

		try {
			Activity activity = ActivityTracker.createActivity();

			// [ type := param("type", req) ]
			String type = req.getParameter("type");
			if (!"profile".equals(type) && !"product".equals(type))
				throw new IllegalArgumentException("type parameter not specified or is not \"profile\" or"
					+ " \"product\"");
			
			// [ object := param("object", req) ]
			String object = req.getParameter("object");
			if (object == null)
				throw new IllegalArgumentException("required object parameter not specified");
			if (!object.startsWith("urn:"))
				object = preferredNamespace + object;

			// [ queryStr := param("query", req) ]
			String queryStr = req.getParameter("query");

			// Get individual parameters, if specified.
			String keywordQuery = req.getParameter("keywordQuery");
			String id = req.getParameter("id");
			String title = req.getParameter("title");
			String desc = req.getParameter("desc");
			String ddID = req.getParameter("ddID");
			String resultModeID = req.getParameter("resultModeID");
			String propType = req.getParameter("propType");
			String propLevels = req.getParameter("propLevels");
			String maxResultsString = req.getParameter("maxResults");
			int maxResults = XMLQuery.DEFAULT_MAX_RESULTS;
			if (maxResultsString != null)
				maxResults = Integer.parseInt(maxResultsString);
				
			// [ query := xmlQuery(queryStr) ]
			XMLQuery xmlQuery;
			if (queryStr != null)
				xmlQuery = new XMLQuery(queryStr);
			else
				xmlQuery = new XMLQuery(keywordQuery, id, title, desc, ddID, resultModeID, propType, propLevels,
					maxResults);

			// [ res := query(type, ns, object, xmlQuery) ]

			res.setContentType("text/xml; encoding=UTF-8");

			if ("profile".equals(type)) {
				xmlQuery.getQueryHeader().setID(activity.getID());
				activity.log(new QueryConstructed());

				ProfileClient pc = new ProfileClient(object);
				activity.log(new ConnectedToServer());

				List results = pc.query(xmlQuery);
				activity.log(new ResultsReceived());

				Document doc = Profile.createProfilesDocument();
				for (Iterator i = results.iterator(); i.hasNext();) {
					Profile profile = (Profile) i.next();
					Node node = profile.toXML(doc);
					doc.getDocumentElement().appendChild(node);
				}
				//activity.log(new ProfileDocumentCreated());

				XML.serialize(doc, res.getWriter());
				activity.stop();
			} else {
				ProductClient pc = new ProductClient(object);
				XMLQuery response = pc.query(xmlQuery);
				res.getOutputStream().write(response.getXMLDocString().getBytes());
			}
		} catch (ProfileException ex) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Profile error: " + ex.getMessage());
		} catch (ProductException ex) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Product error: " + ex.getMessage());
		} catch (SAXException ex) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "XMLQuery malformed, parse error: "
				+ ex.getClass().getName() + ": " + ex.getMessage());
		} catch (IllegalArgumentException ex) {
			res.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		}
	}

	/** Preferred object namespace prefix. */
	private String preferredNamespace;

	/** Test driver.
	 *
	 * This connects to a specified QueryServlet and exercises it.  To run it, pass
	 * the following arguments:
	 * <ol>
	 *   <li>URL to QueryServlet, such as <code>http://oodt.jpl.nasa.gov/servlet/jpl.oodt.servlets.QueryServlet</code></li>
	 *   <li><code>profile</code> or <code>product</code></li>
	 *   <li>object name</li>
	 *   <li>query expression</li>
	 * </ol>
	 *
	 * @param argv Command-line arguments.
	 * @throws Exception If an error occurs.
	 */
	public static void main(String[] argv) throws Exception {
		if (argv.length != 4) {
			System.err.println("Usage: <queryServletURL> profile|product <objectName> <expression>");
			System.exit(1);
		}

		// First, gather the data.
		URL queryServletURL = new URL(argv[0]);
		String type = argv[1];
		String objectName = argv[2];
		XMLQuery query = new XMLQuery(argv[3], /*id*/"my ID", /*title*/"Test Query", /*desc*/"Query from command-line",
			/*data dictionary*/null, /*result mode*/"DATA", /*propType*/"BROADCAST", /*propLevels*/"N/A",
			/*max results*/XMLQuery.DEFAULT_MAX_RESULTS);
		
		// Get the URL connection and encode the data.
		HttpURLConnection conn = (HttpURLConnection) queryServletURL.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Encode the request
		StringBuffer req = new StringBuffer();
		req.append("type=").append(URLEncoder.encode(type, "UTF-8")).append('&');
		req.append("object=").append(URLEncoder.encode(objectName, "UTF-8")).append('&');
		req.append("query=").append(URLEncoder.encode(query.getXMLDocString(), "UTF-8"));

		// Connect and send the request.
		conn.connect();
		conn.getOutputStream().write(req.toString().getBytes());

		// Display the result.  We could also gather the result into a string and
		// construct an XMLQuery here, but this is just an example.
		InputStream in = conn.getInputStream();
		byte[] buf = new byte[512];	
		int num;
		while ((num = in.read(buf)) != -1)
			System.out.write(buf, 0,num);

		// Done.
		conn.disconnect();
		System.exit(0);
	}
}

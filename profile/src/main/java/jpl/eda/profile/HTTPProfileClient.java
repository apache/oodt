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

package jpl.eda.profile;

import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpClient;

import jpl.eda.profile.handlers.Utility;
import jpl.eda.profile.ProfileException;

import java.util.List;
import java.util.Collection;
import java.util.Arrays;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * 
 * <p>
 * This class connects to an HTTP-based QueryServlet and passes the appropriate
 * parameters and so forth needed to return a list of profiles.
 * 
 * </p>
 * 
 * @author mattmann
 * @version 1.0
 *  
 */
public class HTTPProfileClient {

	private String profileServletUrl = null;
	private String profileServerUrn = null;

	/**
	 * <p>
	 * Public constructor
	 * </p>
	 * 
	 * @param profServletUrl
	 *            The string url representation (e.g.
	 *            http://someserver/servlet/ProfileServlet) pointing to the
	 *            query servlet.
	 * @param profServerUrn
	 *            The urn of the profile server which you would like to contact
	 *            via the QueryServlet.
	 *  
	 */
	public HTTPProfileClient(String profServletUrl, String profServerUrn) {
		profileServletUrl = profServletUrl;
		profileServerUrn = profServerUrn;
	}

	/**
	 * <p>
	 * Accessor method for profile servlet url
	 * </p>
	 * 
	 * @return Profile Servlet Url
	 */
	public String getProfileServletUrl() {
		return profileServletUrl;
	}

	/**
	 * <p>
	 * Accessor method for profile server urn
	 * </p>
	 * 
	 * @return Profile Server Urn
	 */
	public String getProfileServerUrn() {
		return profileServerUrn;
	}

	/**
	 * <p>
	 * Setter method for profile servlet url
	 * </p>
	 * 
	 * @param profServletUrl
	 *            The new profile servlet url.
	 * @return void.
	 */
	public void setProfileServletUrl(String profServletUrl) {
		profileServletUrl = profServletUrl;
	}

	/**
	 * <p>
	 * Setter method for profile servlet url
	 * </p>
	 * 
	 * @param profServerUrn
	 *            The new profile server urn.
	 * @return void.
	 */
	public void setProfileServerUrn(String profServerUrn) {
		profileServerUrn = profServerUrn;
	}

	/**
	 * <p>
	 * The query method. You give it a string keyword query (e.g. a DIS-style
	 * query) and it gives you back a list of profiles after it contacts the
	 * http query servlet.
	 * </p>
	 * 
	 * @param query
	 *            A string DIS-style query.
	 * @return a {@link List}of {@link Profile}s.
	 */
	public List query(String query)
		throws ProfileException, UnsupportedEncodingException {

		//construct the url string for a jpl.eda.servlet.QueryServlet style
		// query

		String targetUrl = null;

		try {
			targetUrl =
				profileServletUrl
					+ "?type=profile&object="
					+ URLEncoder.encode(profileServerUrn, "UTF-8")
					+ "&keywordQuery="
					+ URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw e;
		}

		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(
			5000);

		GetMethod theGet = new GetMethod(targetUrl);
		String respData = null; //the response data

		theGet.addRequestHeader(new Header("Accept", "*/*, text/xml"));
		theGet.addRequestHeader(new Header("Accept-Language", "*"));

		int state = -1;

		try {
			state = httpClient.executeMethod(theGet);

			boolean errorFlag = true;

			if (state != HttpStatus.SC_NO_CONTENT
				&& state != HttpStatus.SC_OK) {
				errorFlag = true;
			}

			for (int i = 0; i < theGet.getResponseHeaders().length; i++) {
				Header h = (Header) theGet.getResponseHeaders()[i];

				if (errorFlag) {
					System.err.println(h);
				}
			}

			respData = theGet.getResponseBodyAsString();
		} catch (Exception ioe1) {
			ioe1.printStackTrace();
			System.err.println(ioe1.getMessage());
			try{
				System.err.println("Error: here was the response body: "+theGet.getResponseBodyAsString());
			}
			catch(Exception ignore){}

		} finally {
			theGet.releaseConnection();
		}

		return Arrays.asList(
			jpl
				.eda
				.profile
				.handlers
				.Utility
				.getProfileCollection(respData)
				.toArray());

	}

	/**
	 * <p>
	 * Main method accepts two parameters: <br>
	 * 
	 * <ul>
	 * <li><code>--servletUrl The Url pointing to the query servlet.</code>
	 * </li>
	 * <li><code>--serverUrn The Urn of the Profile server to contact.</code>
	 * </li>
	 * <li><code>--query The DIS-style query to pass.</code></li>
	 * </ul>
	 * 
	 * @param args
	 *            Method of passing command line parameters.
	 * @return void.
	 */
	public static void main(String[] args)
		throws ProfileException, UnsupportedEncodingException {

		String pUrn = null, pUrl = null, kwdQuery = null;

		String usage =
			"java jpl.eda.profile.HTTPProfileClient --servletUrl [http:// url to profile servlet] --serverUrn [profile server urn] --query [keyword query]\n";

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("--servletUrl")) {
				pUrl = args[++i];
			} else if (args[i].equals("--serverUrn")) {
				pUrn = args[++i];
			} else if (args[i].equals("--query")) {
				kwdQuery = args[++i];
			}
		}

		if (pUrn == null || pUrl == null || kwdQuery == null) {
			System.err.println(usage);
			System.exit(1);
		}

		HTTPProfileClient profClient = new HTTPProfileClient(pUrl, pUrn);

		System.out.println(profClient.query(kwdQuery));

	}

}

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
package org.apache.oodt.cas.filemgr.catalog.solr;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.solr.client.solrj.util.ClientUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class containing client-side functionality for interacting with a Solr server.
 * This class uses an {@link HttpClient} for all HTTP communication.
 *
 * @author Luca Cinquini
 *
 */
public class SolrClient {

	// base URL of Solr server
	private String solrUrl;

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	/**
	 * Constructor initializes the Solr URL
	 * @param url
	 */
	public SolrClient(final String url) {

		solrUrl = url;

	}

	/**
	 * Method to send one or more documents to be indexed to Solr.
	 *
	 * @param docs
	 * @param commit
	 * @param mimeType : the mime-type format of the documents
	 * @return
	 * @throws CatalogException
	 */
	public String index(List<String> docs, boolean commit, String mimeType) throws CatalogException {

		try {

			final String url = this.buildUpdateUrl();

			// build message
			StringBuilder message = new StringBuilder("<add>");
			for (String doc : docs) {
				message.append(doc);
			}
			message.append("</add>");

			// send POST request
			LOG.info("Posting message:"+message+" to URL:"+url);
			String response = doPost(url, message.toString(), mimeType);
			LOG.info(response);

			// commit changes ?
			if (commit) {
				this.commit();
			}

			LOG.info(response);
			return response;

		} catch(Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new CatalogException(e.getMessage());
		}

	}

	/**
	 * Method to send a message containing a 'delete' instruction to Solr.
	 * @param id
	 * @param commit
	 * @return
	 * @throws CatalogException
	 */
	public String delete(String id, boolean commit) throws CatalogException {

		try {

			// build POST request
			String url = this.buildUpdateUrl();
			if (commit) {
				url += "?commit=true";
			}
			String message = "<delete><query>id:"+id+"</query></delete>";

			// send POST request
			LOG.info("Posting message:"+message+" to URL:"+url);

			return doPost(url, message, Parameters.MIME_TYPE_XML);

		} catch(Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new CatalogException(e.getMessage());
		}

	}

	/**
	 * Method to query the Solr index for a product with the specified id.
	 * @param id
	 * @return
	 */
	public String queryProductById(String id, String mimeType) throws CatalogException {

		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[]{Parameters.PRODUCT_ID+":"+id} );
		return query(params, mimeType);

	}

	/**
	 * Method to query the Solr index for a product with the specified name.
	 * @param name
	 * @param mimeType
	 * @return
	 */
	public String queryProductByName(String name, String mimeType) throws CatalogException {

		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[]{Parameters.PRODUCT_NAME+":"+ ClientUtils.escapeQueryChars(name)} );
		return query(params, mimeType);

	}

	/**
	 * Method to query Solr for the most recent 'n' products.
	 * @param n
	 * @return
	 * @throws CatalogException
	 */
	public String queryProductsByDate(int n, String mimeType) throws CatalogException {

		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[]{ "*:*"} );
		params.put("rows", new String[]{ ""+n} );
		params.put("sort", new String[]{ Parameters.PRODUCT_RECEIVED_TIME+" desc"} );
		return query(params, mimeType);

	}

	/**
	 * Method to query Solr for the most recent 'n' products of a specified type.
	 * @param n
	 * @return
	 * @throws CatalogException
	 */
	public String queryProductsByDateAndType(int n, ProductType type, String mimeType) throws CatalogException {

		ConcurrentHashMap<String, String[]> params = new ConcurrentHashMap<String, String[]>();
		params.put("q", new String[]{ Parameters.PRODUCT_TYPE_NAME+type.getName() } );
		params.put("rows", new String[]{ ""+n} );
		params.put("sort", new String[]{ Parameters.PRODUCT_RECEIVED_TIME+" desc"} );
		return query(params, mimeType);

	}

	/**
	 * Method to commit the current changes to the Solr index.
	 * @throws Exception
	 */
	public void commit() throws IOException, CatalogException {

		String message = "<commit waitSearcher=\"true\"/>";
		String url =  this.buildUpdateUrl();
		doPost(url, message, Parameters.MIME_TYPE_XML);

	}

	/**
	 * Method to send a generic query to the Solr server.
	 *
	 * @param parameters
	 * @param mimeType : the desired mime type for the results (XML, JSON, ...)
	 * @return
	 */
	public String query(Map<String, String[]> parameters, String mimeType) throws CatalogException {

		try {

			// build HTTP request
			String url = this.buildSelectUrl();

			// execute request
			return this.doGet(url, parameters, mimeType);

		} catch(Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
			throw new CatalogException(e.getMessage());
		}

	}

	/**
	 * Method to execute a GET request to the given URL with the given parameters.
	 * @param url
	 * @param parameters
	 * @return
	 */
	private String doGet(String url, Map<String, String[]> parameters, String mimeType)
			throws IOException, CatalogException {

		// build HTTP/GET request


		List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
		for (Map.Entry<String, String[]> key : parameters.entrySet()) {
			for (String value : key.getValue()) {
				nvps.add(new BasicNameValuePair(key.getKey(), value));
			}
		}
		// request results in JSON format
		if (mimeType.equals(Parameters.MIME_TYPE_JSON)) {
			nvps.add(new BasicNameValuePair("wt", "json"));
		}

		String paramString = URLEncodedUtils.format(nvps, "utf-8");

		HttpRequestBase method = new HttpGet(url+"?"+paramString);
		LOG.info("GET url: "+url+" query string: "+method.getURI());

		// send HTTP/GET request, return response
		return doHttp(method);

	}

	/**
	 * Method to execute a POST request to the given URL.
	 * @param url
	 * @param document
	 * @return
	 */
	private String doPost(String url, String document, String mimeType) throws IOException, CatalogException {

		// build HTTP/POST request
		HttpPost method = new HttpPost(url);
		HttpEntity requestEntity = null;
		try {
			requestEntity = new StringEntity(document, mimeType, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		method.setEntity(requestEntity);
		// send HTTP/POST request, return response
		return doHttp(method);

	}

	/**
	 * Common functionality for HTTP GET and POST requests.
	 * @param method
	 * @return
	 * @throws Exception
	 */
	private String doHttp(HttpRequestBase method) throws IOException, CatalogException {

		String response = null;
		BufferedReader br = null;
		try {

			// send request
			HttpClient httpClient = new DefaultHttpClient();
			// OODT-719 Prevent httpclient from spawning closewait tcp connections
			method.setHeader("Connection", "close");

			HttpResponse statusCode = httpClient.execute(method);

			// read response
			if (statusCode.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

				// still consume the response
				ResponseHandler<String> handler = new BasicResponseHandler();

				handler.handleResponse(statusCode);
				throw new CatalogException("HTTP method failed: " + statusCode.getStatusLine().toString());

			} else {
				ResponseHandler<String> handler = new BasicResponseHandler();

				String resp = handler.handleResponse(statusCode);

				response=resp;

			}

		} finally {
			// must release the connection even if an exception occurred
			method.releaseConnection();
			if (br!=null) {
				try {
					br.close();
				} catch (Exception ignored) {
				}
			}
		}

		return response;

	}

	/**
	 * Builds the URL used to update the Solr index.
	 *
	 * Example: http://localhost:8983/solr/update?
	 * @return
	 */
	private String buildUpdateUrl() {

		return solrUrl + (solrUrl.endsWith("/") ? "" : "/") + "update";

	}

	/**
	 * Builds the URL used to query the Solr index.
	 *
	 * Example: http://localhost:8983/solr/select?
	 * @return
	 */
	private String buildSelectUrl() {

		return solrUrl + (solrUrl.endsWith("/") ? "" : "/") + "select";

	}

}

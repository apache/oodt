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
package org.apache.oodt.cas.protocol.http.util;

//JDK imports
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//APACHE imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.metadata.util.MimeTypeUtils;
import org.apache.oodt.cas.protocol.http.HttpFile;

/**
 * Utility methods for HTTP Protocol related tasks.
 *
 * @author bfoster
 */
public class HttpUtils {

  static final MimeTypeUtils MIME_TYPES = new MimeTypeUtils();
  
	// Pattern looking for <a href="(group-2)"/>(group-3)</a> . . . group-1 is for either " or '
	static final Pattern XHTML_LINK_PATTERN = Pattern.compile("<\\s*a\\s+href\\s*=\\s*(['\"])(.+?)\\1\\s*>(.+?)<\\s*/\\s*a\\s*>"); 
	static final Pattern LAZY_LINK_PATTERN = Pattern.compile("<\\s*a\\s+href\\s*=\\s*(['\"])(.+?)\\1\\s*/\\s*>"); 

	private HttpUtils() {}
	
	/**
	 * Resolves a path against given {@link URI} and creates the resolved {@link URI}.
	 * (i.e. base = "http://localhost" ; path = "/path/to/file" ; resolved = "http://localhost/path/to/file")
	 * Handles all cases: if base already has a path, if path is relative, if path is absolute.
	 * 
	 * @param base The base {@link URI} which the given path will be resolved against.
	 * @param path The path to be resolved against the given {@link URI}
	 * @return resolved {@link URI}. 
	 * @throws URISyntaxException
	 */
	public static URI resolveUri(URI base, String path) throws URISyntaxException {
		Validate.notNull(base, "base URI must not be NULL");
		Validate.notNull(path, "resolve path must not be NULL");
		if (path.startsWith("http://")) {
			return new URI(path);
		} else if (path.startsWith("/")) {
			return new URI(base.getScheme() + "://" + base.getHost() + path);
		} else {
			if (base.toString().endsWith("/")) {
				return new URI(base.toString() + path);
			} else {
				return new URI(base.toString() + "/" + path);				
			}
		}
	}

	public static HttpURLConnection connect(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.connect();
		conn.getResponseMessage();
		return conn;
	}

  public static boolean checkForRedirection(URL beforeConnUrl, URL afterConnUrl) {
    return !beforeConnUrl.toString().equals(afterConnUrl.toString());
  }

  public static String readUrl(HttpURLConnection conn) throws IOException {
    // create URL source reader
    Scanner scanner = new Scanner(conn.getInputStream());

    // Read in link
    StringBuffer sb = new StringBuffer("");
    while (scanner.hasNext())
      sb.append(scanner.nextLine());
    
    return sb.toString();
  }

	public static List<HttpFile> findLinks(HttpFile file) throws IOException, URISyntaxException {
		Matcher matcher = XHTML_LINK_PATTERN.matcher(HttpUtils.readUrl(connect(file.getLink())));
		List<HttpFile> httpFiles = new ArrayList<HttpFile>();
		while (matcher.find()) {
			String link = matcher.group(2).trim();
			String virtualPath = matcher.group(3).trim();
			URL url = resolveUri(file.getLink().toURI(), link).toURL();
			httpFiles.add(new HttpFile(file, link, isDirectory(url, virtualPath), url));
		}
		matcher = LAZY_LINK_PATTERN.matcher(HttpUtils.readUrl(connect(file.getLink())));
		while (matcher.find()) {
			String link = matcher.group(2).trim();
			URL url = resolveUri(file.getLink().toURI(), link).toURL();
			httpFiles.add(new HttpFile(file, link, isDirectory(url, link), url));
		}
		return httpFiles;
	}
		
	public static boolean isDirectory(URL url, String virtualPath) throws IOException {
		try {
			String mime = MIME_TYPES.autoResolveContentType(url.toString(),
					MimeTypeUtils.readMagicHeader(url.openStream()));
			return (mime.equals("text/html") && !virtualPath.endsWith(".html"));
		} catch (Exception e) {
			throw new IOException("URL does not exist '" + url + "'", e);
		}
	}
}

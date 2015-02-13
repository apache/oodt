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
package org.apache.oodt.cas.protocol.http;

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.http.util.HttpUtils;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

//JDK imports
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * Http Protocol communication class
 * 
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class HttpProtocol implements Protocol {

  private static Map<String, List<HttpFile>> linkChildren = new HashMap<String, List<HttpFile>>();

  private HttpFile parentFile;
  private HttpFile currentFile;
  private boolean isConnected;
  
  public HttpProtocol() {
    isConnected = false;
  }

  public void cd(ProtocolFile file) throws ProtocolException {
    try {
    	HttpFile httpFile = null;
    	if (!(file instanceof HttpFile)) {
    		URL link = HttpUtils.resolveUri(currentFile.getLink().toURI(), file.getPath()).toURL();
  			httpFile = new HttpFile(link.getPath(), file.isDir(), link);
      } else {
        httpFile = (HttpFile) file;
      }
    	
      if (!HttpUtils
          .isDirectory(httpFile.getLink(), file.getPath()))
        throw new ProtocolException(file
            + " is not a directory (mime type must be text/html)");
      this.currentFile = httpFile;
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to " + file + " : "
          + e.getMessage(), e);
    }
  }
  
  public void cdRoot() {
  	currentFile = parentFile;
  }
  
  public void cdHome() {
  	cdRoot();
  }

  public void connect(String host, Authentication auth)
      throws ProtocolException {
    try {
      URL url = new URL("http://" + host + "/");
      url.openStream().close();
      currentFile = parentFile = new HttpFile("/", true, url);
      isConnected = true;
    } catch (Exception e) {
      throw new ProtocolException("Failed to connect to http://" + host + " : "
          + e.getMessage());
    }
  }

  public void close() throws ProtocolException {
    currentFile = parentFile = null;
  }

  public void get(ProtocolFile fromFile, File toFile)
      throws ProtocolException {

    OutputStream out = null;
    InputStream in = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(toFile));
      if (fromFile instanceof HttpFile) {
    	  in = ((HttpFile) fromFile).getLink().openStream();
      } else {
    	  in = HttpUtils.resolveUri(currentFile.getLink().toURI(), fromFile.getPath()).toURL().openStream();
      }

      byte[] buffer = new byte[1024];
      int numRead;
      long numWritten = 0;
      while ((numRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, numRead);
        numWritten += numRead;
      }
      in.close();
      out.close();
    } catch (Exception e) {
      throw new ProtocolException("Failed to get file '" + fromFile + "' : "
          + e.getMessage(), e);
    } finally {
      if (in != null)
        try {
          in.close();
        } catch (Exception e) {
          // log failure
        }
      if (out != null)
        try {
          out.close();
        } catch (Exception e) {
          // log failure
        }
    }
  }
  
  public void put(File fromFile, ProtocolFile toFile) {
	  //do nothing
  }

  public List<ProtocolFile> ls() throws ProtocolException {
  	List<ProtocolFile> lsResults = new ArrayList<ProtocolFile>();
  	for (HttpFile file : parseLink(currentFile)) {
  		lsResults.add(file);
  	}
    return lsResults;
  }

	public List<ProtocolFile> ls(ProtocolFileFilter filter)
			throws ProtocolException {
  	List<ProtocolFile> lsResults = new ArrayList<ProtocolFile>();
  	for (HttpFile file : parseLink(currentFile)) {
  		if (filter.accept(file)) {
  			lsResults.add(file);
  		}
  	}
    return lsResults;
	}
	
  public ProtocolFile pwd() throws ProtocolException {
    try {
      return currentFile;
    } catch (Exception e) {
      throw new ProtocolException("Failed to get current working directory : "
          + e.getMessage());
    }
  }

  public boolean connected() {
    return this.isConnected;
  }

  public List<HttpFile> parseLink(HttpFile file)
      throws ProtocolException {
    List<HttpFile> children = linkChildren.get(file.getLink()
        .toString());
    if (file.isDir() && children == null) {
      try {

        // Open link.
        HttpURLConnection conn = HttpUtils.connect(file.getLink());

        // If redirection took place, then change the ProtocolFile's URL.
        if (HttpUtils.checkForRedirection(file.getLink(), conn.getURL())) {
          file = new HttpFile(file, file.getPath(), file.isDir(), conn.getURL());
        }

        // Find links in URL.
        children = new LinkedList<HttpFile>();
        children.addAll(HttpUtils.findLinks(file));
        
        // Save children links found.
        linkChildren.put(file.getLink().toString(), children);

      } catch (Exception e) {
        throw new ProtocolException("Failed to get children links for " + file
            + " : " + e.getMessage(), e);
      }
    }
    return children;
  }

//  public static String findLinkInATag(String aTag) {
//    // find 'href' attribute
//    String find = aTag.substring(aTag.indexOf("href") + 4);
//    // USE STRICT FINDING FIRST
//    // (['\"])\s*?[(http)(./)(..)/#].+?\\1
//    // finds link between ' or ", which starts with one of
//    // the following: http, ./, .., /, #
//    // these starting possibilities can then be followed any
//    // number of characters until the corresponding
//    // ' or " is reached.
//    String patternRegExp = "(['\"])\\s*?[\\(http\\)\\(\\./\\)\\(\\.\\.\\)/#].+?\\1";
//    Pattern linkPattern = Pattern.compile(patternRegExp);
//    Matcher linkMatch = linkPattern.matcher(find);
//    if (linkMatch.find())
//      find = find.substring(linkMatch.start() + 1, linkMatch.end() - 1);
//    else {
//      // RELAX FINDING SOME
//      patternRegExp = "(['\"])\\s*?[^./].+?\\1";
//      linkPattern = Pattern.compile(patternRegExp);
//      linkMatch = linkPattern.matcher(find);
//      if (linkMatch.find())
//        find = find.substring(linkMatch.start() + 1, linkMatch.end() - 1);
//      else {
//        // EXTREMELY RELAX FINDING
//        patternRegExp = "[^\"='/>\\s]+?[^\\s>\"']*?";
//        linkPattern = Pattern.compile(patternRegExp);
//        linkMatch = linkPattern.matcher(find);
//        if (linkMatch.find())
//          find = find.substring(linkMatch.start(), linkMatch.end());
//        else {
//          return null;
//        }
//      }
//    }
//    return find;
//  }
//
//  public static String createLinkFromHref(HttpFile parent, String href) {
//    if (!href.startsWith("http")) {
//      String link = parent.getLink().toExternalForm();
//      if (href.startsWith("..")) {
//        int index = link.substring(0, link.lastIndexOf("/")).lastIndexOf("/");
//        href = (index < 7) ? link + href.substring(2) : link.substring(0, link
//            .substring(0, link.lastIndexOf("/")).lastIndexOf("/"))
//            + href.substring(2);
//      } else if (href.startsWith("./")) {
//        int index = link.lastIndexOf("/");
//        href = (index < 7) ? link + href.substring(1) : link
//            .substring(0, index)
//            + href.substring(1);
//      } else if (href.startsWith("/")) {
//        URL url = parent.getLink();
//        href = url.getProtocol() + "://" + url.getHost() + href;
//      } else {
//        // find the last / in current link
//        int index = link.lastIndexOf("/");
//        // (index < 7) checks if in the current link, "/" only exists
//        // in the protocol section of link (i.e. http://jpl.nasa.gov)
//        href = (index < 7) ? link + "/" + href : link.substring(0, index) + "/"
//            + href;
//      }
//    }
//
//    // remove "/" at end of link
//    if (href.endsWith("/"))
//      href = href.substring(0, href.length() - 1);
//    href = href.trim();
//
//    return href;
//  }
//
//  public ProtocolFile getProtocolFileFor(String path, boolean isDir)
//      throws ProtocolException {
//    try {
//      StringTokenizer st = new StringTokenizer(path, "/ ");
//      HttpFile curPath = this.parentFile;
//      // System.out.println(parentPath);
//      if (st.hasMoreTokens()) {
//        do {
//          String token = st.nextToken();
//          List<HttpFile> children = this.parseLink(curPath);
//          for (HttpFile pFile : children) {
//            if (pFile.getName().equals(token)) {
//              // System.out.println("token " + token + " " +
//              // pFile);
//              curPath = pFile;
//              continue;
//            }
//          }
//        } while (st.hasMoreTokens());
//        if (curPath.equals(this.parentFile))
//          return new HttpFile(path, isDir, new URL("http://"
//                  + this.getSite().getHost() + path), curPath);
//      }
//      return curPath;
//    } catch (Exception e) {
//      throw new ProtocolException("Failed to get ProtocolPath for " + path);
//    }
//  }

  public void delete(ProtocolFile file) {}

//  private URL getSite() {
//	return currentURL;  
//  }
  
  public static void main(String[] args) throws Exception {
    String urlString = null, downloadToDir = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--url"))
        urlString = args[++i];
      else if (args[i].equals("--downloadToDir"))
        downloadToDir = args[++i];
    }

    if (urlString == null)
      throw new Exception("Must specify a url to download: --url <url>");

    URL url = new URL(urlString);
    ProtocolFile urlFile = new HttpFile(url.getPath(), false, url);
    File toFile = new File(downloadToDir, urlFile.getName());
    toFile = toFile.getAbsoluteFile();
    toFile.createNewFile();
    new HttpProtocol().get(urlFile, toFile);
  }

}

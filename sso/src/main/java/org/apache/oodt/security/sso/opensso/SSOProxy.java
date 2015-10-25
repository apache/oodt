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

package org.apache.oodt.security.sso.opensso;

//JDK imports
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

//APACHE imports

/**
 * 
 * A client class to the services provided by the <a
 * href="https://opensso.dev.java.net/">OpenSSO</a> project. The descriptions of
 * these services are <a
 * href="http://developers.sun.com/identity/reference/techart/id-svcs.html"
 * >here</a>.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class SSOProxy implements SSOMetKeys {

  private static final Logger LOG = Logger.getLogger(SSOProxy.class.getName());
  private static final String AUTH_ENDPOINT;
  private static final String AUTH_ENDPOINT_KEY = "AUTH_ENDPOINT";
  private static final String IDENT_READ_ENDPOINT;
  private static final String IDENT_READ_ENDPOINT_KEY = "IDENT_READ_ENDPOINT";
  private static final String IDENT_ATTR_ENDPOINT;
  private static final String IDENT_ATTR_ENDPOINT_KEY = "IDENT_ATTR_ENDPOINT";
  private static final String LOG_ENDPOINT;
  private static final String LOG_ENDPOINT_KEY = "LOG_ENDPOINT";
  
  static {
	  if (System.getProperty(AUTH_ENDPOINT_KEY) != null) {
		  AUTH_ENDPOINT = System.getProperty(AUTH_ENDPOINT_KEY);
	  } else {
		  AUTH_ENDPOINT = AUTHENTICATE_ENDPOINT;
	  }
	  if (System.getProperty(IDENT_READ_ENDPOINT_KEY) != null) {
		  IDENT_READ_ENDPOINT = System.getProperty(IDENT_READ_ENDPOINT_KEY);
	  } else {
		  IDENT_READ_ENDPOINT = IDENTITY_READ_ENDPOINT;
	  }
	  if (System.getProperty(IDENT_ATTR_ENDPOINT_KEY) != null) {
		  IDENT_ATTR_ENDPOINT = System.getProperty(IDENT_ATTR_ENDPOINT_KEY);
	  } else {
		  IDENT_ATTR_ENDPOINT = IDENTITY_ATTRIBUTES_ENDPOINT;
	  }
	  if (System.getProperty(LOG_ENDPOINT_KEY) != null) {
		  LOG_ENDPOINT = System.getProperty(LOG_ENDPOINT_KEY);
	  } else {
		  LOG_ENDPOINT = LOGOUT_ENDPOINT;
	  }

	  LOG.log(Level.INFO, AUTH_ENDPOINT_KEY + " set to " + AUTH_ENDPOINT);
	  LOG.log(Level.INFO, IDENT_READ_ENDPOINT_KEY + " set to " + IDENT_READ_ENDPOINT);
	  LOG.log(Level.INFO, IDENT_ATTR_ENDPOINT_KEY + " set to " + IDENT_ATTR_ENDPOINT);
	  LOG.log(Level.INFO, LOG_ENDPOINT_KEY + " set to " + LOG_ENDPOINT);
  }

  public String authenticate(String username, String password) {
    HttpClient httpClient = new HttpClient();
    PostMethod post = new PostMethod(AUTH_ENDPOINT);
    String response;
    String ssoToken = null;

    NameValuePair[] data = { new NameValuePair("username", username),
        new NameValuePair("password", password),
        new NameValuePair("uri", "realm/lmmp") };

    post.setRequestBody(data);

    try {
      httpClient.executeMethod(post);
      if (post.getStatusCode() != HttpStatus.SC_OK) {
        throw new HttpException(post.getStatusLine().toString());
      }
      response = post.getResponseBodyAsString().trim();
      ssoToken = response.substring(9);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }

    return ssoToken;
  }

  public IdentityDetails readIdentity(String username, String token)
      throws Exception {
    HttpClient httpClient = new HttpClient();
    PostMethod post = new PostMethod(IDENT_READ_ENDPOINT);
    LOG.log(Level.INFO, "Obtaining identity: username: [" + username
        + "]: token: [" + token + "]: REST url: [" + IDENT_READ_ENDPOINT
        + "]");
    NameValuePair[] data = { new NameValuePair("name", username),
        new NameValuePair("admin", token) };

    post.setRequestBody(data);

    httpClient.executeMethod(post);
    if (post.getStatusCode() != HttpStatus.SC_OK) {
      throw new Exception(post.getStatusLine().toString());
    }

    return parseIdentityDetails(post.getResponseBodyAsString().trim());

  }

  public UserDetails getUserAttributes(String token) throws Exception {
    HttpClient httpClient = new HttpClient();
    PostMethod post = new PostMethod(IDENT_ATTR_ENDPOINT);
    LOG.log(Level.INFO, "Obtaining user attributes: token: [" + token
        + "]: REST url: [" + IDENT_ATTR_ENDPOINT + "]");
    NameValuePair[] data = { new NameValuePair("subjectid", token) };

    post.setRequestBody(data);

    httpClient.executeMethod(post);
    if (post.getStatusCode() != HttpStatus.SC_OK) {
      throw new Exception(post.getStatusLine().toString());
    }

    return parseUserDetails(post.getResponseBodyAsString().trim());

  }

  public void logout(String token) {
    HttpClient httpClient = new HttpClient();
    PostMethod post = new PostMethod(LOG_ENDPOINT);
    LOG.log(Level.INFO, "Logging out: token: [" + token + "]: REST url: ["
        + LOG_ENDPOINT + "]");
    NameValuePair[] data = { new NameValuePair("subjectid", token) };
    post.setRequestBody(data);

    try {
      httpClient.executeMethod(post);
      if (post.getStatusCode() != HttpStatus.SC_OK) {
        throw new HttpException(post.getStatusLine().toString());
      }
    } catch (HttpException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }
  }

  private IdentityDetails parseIdentityDetails(String serviceResponse) {
    ByteArrayInputStream is = new ByteArrayInputStream(serviceResponse
        .getBytes());
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    IdentityDetails details = new IdentityDetails();
    String line = null, lastAttrKeyRead = null;

    try {
      while ((line = br.readLine()) != null) {
        if (line.equals(IDENTITY_DETAILS_ATTR_SKIP_LINE))
          continue;
        String key, val;
        if (line.startsWith(IDENTITY_DETAILS_REALM)) {
          // can't parse it the same way
          key = line.substring(0, IDENTITY_DETAILS_REALM.length());
          val = line.substring(IDENTITY_DETAILS_REALM.length() + 1);
        } else {
          String[] lineToks = line.split("=");
          key = lineToks[0];
          val = lineToks[1];
        }

        if (key.equals(IDENTITY_DETAILS_NAME)) {
          details.setName(val);
        } else if (key.equals(IDENTITY_DETAILS_TYPE)) {
          details.setType(val);
        } else if (key.equals(IDENTITY_DETAILS_REALM)) {
          details.setRealm(val);
        } else if (key.equals(IDENTITY_DETAILS_GROUP)) {
          details.getGroups().add(val);
        } else if (key.equals(IDENTITY_DETAILS_ATTR_NAME)) {
          lastAttrKeyRead = val;
        } else if (key.equals(IDENTITY_DETAILS_ATTR_VALUE)) {
          details.getAttributes().addMetadata(lastAttrKeyRead, val);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error reading service response line: [" + line
          + "]: Message: " + e.getMessage());
    } finally {
      try {
        is.close();
      } catch (Exception ignore) {
      }

      try {
        br.close();
      } catch (Exception ignore) {
      }

    }

    return details;
  }

  private UserDetails parseUserDetails(String serviceResponse) {
    ByteArrayInputStream is = new ByteArrayInputStream(serviceResponse
        .getBytes());
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    UserDetails details = new UserDetails();
    String line = null, lastAttrKeyRead = null;

    try {
      while ((line = br.readLine()) != null) {
        String key, val;
        if (line.startsWith(USER_DETAILS_ROLE)) {
          // can't parse by splitting, parse by using substring
          key = line.substring(0, USER_DETAILS_ROLE.length());
          val = line.substring(USER_DETAILS_ROLE.length() + 1);
        } else {
          String[] lineToks = line.split("=");
          key = lineToks[0];
          val = lineToks[1];
        }

        if (key.equals(USER_DETAILS_TOKEN)) {
          details.setToken(val);
        } else if (key.equals(USER_DETAILS_ROLE)) {
          details.getRoles().add(val);
        } else if (key.equals(USER_DETAILS_ATTR_NAME)) {
          lastAttrKeyRead = val;
        } else if (key.equals(USER_DETAILS_ATTR_VALUE)) {
          details.getAttributes().addMetadata(lastAttrKeyRead, val);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Error reading service response line: [" + line
          + "]: Message: " + e.getMessage());
    } finally {
      try {
        is.close();
      } catch (Exception ignore) {
      }

      try {
        br.close();
      } catch (Exception ignore) {
      }

    }

    return details;
  }

  public static void main(String[] args) throws Exception {
    String usage = "SSOProxy <cmd> [args]\n\n" + "Where cmd is one of:\n"
        + "authenticate <user> <pass>\n" + "identity <user> <token>\n"
        + "attributes <token>\nlogout <token>\n";

    if (args.length < 2 || args.length > 3) {
      System.err.println(usage);
      System.exit(1);
    }

    String cmd = args[0];
    SSOProxy sso = new SSOProxy();
    if (cmd.equals(AUTH_COMMAND)) {
      System.out.println(sso.authenticate(args[1], args[2]));
    } else if (cmd.equals(IDENTITY_COMMAND)) {
      System.out.println(sso.readIdentity(args[1], args[2]));
    } else if (cmd.equals(ATTRIBUTES_COMMAND)) {
      System.out.println(sso.getUserAttributes(args[1]));
    } else if (cmd.equals(LOGOUT_COMMAND)) {
      sso.logout(args[1]);
    }

  }

}

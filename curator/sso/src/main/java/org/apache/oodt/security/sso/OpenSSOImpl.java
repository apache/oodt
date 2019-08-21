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

package org.apache.oodt.security.sso;

import org.apache.commons.codec.binary.Base64;
import org.apache.oodt.security.sso.opensso.SSOMetKeys;
import org.apache.oodt.security.sso.opensso.SSOProxy;
import org.apache.oodt.security.sso.opensso.SingleSignOnException;
import org.apache.oodt.security.sso.opensso.UserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

/**
 * 
 * Connects to OpenSSO's authorization endpoint and authenticates a user,
 * implementing the CAS {@link AbstractWebBasedSingleSignOn} interface. This
 * class can be used in e.g., CAS curator to link into Open SSO.
 */
public class OpenSSOImpl extends AbstractWebBasedSingleSignOn implements
    SSOMetKeys {

  private static final Logger LOG = Logger.getLogger(OpenSSOImpl.class
      .getName());

  private transient SSOProxy ssoProxy;

  /**
   * Default constructor.
   */
  public OpenSSOImpl() {
    this.ssoProxy = new SSOProxy();
  }

  public String getCurrentUsername() {
    String cookieVal = this.getCookieVal(USER_COOKIE_KEY);
    if (cookieVal == null) {
      // let's try and get the SSO token
      // and pull the username from there
      String ssoToken = this.getSSOToken();
      if (ssoToken != null) {
        UserDetails details;
        try {
          details = this.ssoProxy.getUserAttributes(ssoToken);
        } catch (Exception e) {
          LOG.log(Level.SEVERE, e.getMessage());
          return UNKNOWN_USER;
        }
        return details.getAttributes().getMetadata(UID_ATTRIBUTE_NAME) != null ? details
            .getAttributes().getMetadata(UID_ATTRIBUTE_NAME) : UNKNOWN_USER;
      } else {
        return UNKNOWN_USER;
      }
    } else {
      return new String(Base64.decodeBase64(cookieVal.getBytes()));
    }
  }

  public boolean getLastConnectionStatus() {
    return this.isLoggedIn();
  }

  public boolean isLoggedIn() {
    // TODO: make sure the token is valid?
    return (this.getSSOToken() != null);
  }

  public boolean login(String username, String password) {

    String ssoToken;
    try {
      ssoToken = this.ssoProxy.authenticate(username, password);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, e.getMessage());
      return false;
    }

    this.addCookie(SSO_COOKIE_KEY, "\"" + ssoToken + "\"");

    this.addCookie(USER_COOKIE_KEY,
        "\"" + new String(Base64.encodeBase64(username.getBytes())) + "\"");

    return true;
  }

  public void logout() {
    this.ssoProxy.logout(this.getSSOToken());
    this.clearCookie(SSO_COOKIE_KEY);
    this.clearCookie(USER_COOKIE_KEY);
  }

  /**
   * Gets the SSO groups for the LMMP user, identified by her
   * <code>ssoAuth</code>, where her User ID is provided by
   * {@link OpenSSOImpl#getCurrentUsername()} and her Token is provided by
   * {@link OpenSSOImpl#getSSOToken()}.
   * 
   * @return A {@link List} of String LMMP groups for the User.
   * @throws SingleSignOnException
   *           If any error (e.g., HTTP REST error) occurs.
   * @throws IOException If the SSO token cannot be read.
   */
  public List<String> getGroupsForUser() throws IOException, SingleSignOnException {
    String token = this.getSSOToken();
    if (token == null) {
      return Collections.EMPTY_LIST;
    } else {
      UserDetails details = this.ssoProxy.getUserAttributes(token);
      // groups are formatted in this response to include whole
      // principals, like lmmp-infra,...principal
      // so split on "," and take the first token to get the group name
      List<String> groups = new Vector<String>();
      for (String rawGroup : details.getRoles()) {
        groups.add(rawGroup.split(",")[0]);
      }

      return groups;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.security.sso.SingleSignOn#retrieveGroupsForUser(java.lang
   * .String)
   */
  @Override
  public List<String> retrieveGroupsForUser(String username) {
    // FIXME: not implemented yet
    return Collections.EMPTY_LIST;
  }

  protected String getSSOToken() {
    String cookieVal = this.getCookieVal(SSO_COOKIE_KEY);
    if (cookieVal != null) {
      return cookieVal;
    } else {
      return null;
    }
  }

  private String getCookieVal(String name) {
    Cookie[] cookies = this.req.getCookies();
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(name)) {
        return cookie.getValue().startsWith("\"")
            && cookie.getValue().endsWith("\"") ? cookie.getValue().substring(
            1, cookie.getValue().length() - 1) : cookie.getValue();
      }
    }

    return null;
  }

  private void addCookie(String name, String val) {
    Cookie userCookie = new Cookie(name, val);
    userCookie.setPath("/");
    userCookie.setMaxAge((int) (System.currentTimeMillis() + (60 * 15)));
    this.res.addCookie(userCookie);
  }

  private void clearCookie(String name) {
    Cookie userCookie = new Cookie(name, "blank");
    userCookie.setPath("/");
    userCookie.setMaxAge(0);
    this.res.addCookie(userCookie);
  }

}

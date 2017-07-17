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


package org.apache.oodt.security.sso;

//JDK imports
import java.util.List;

/**
 * 
 * The CAS java-based single sign on API.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface SingleSignOn {

  /**
   * Should return the current logged in Single Sign On username returned from
   * the implementation-specific authentication API.
   * 
   * @return A string representation of the current SSO username.
   */
  String getCurrentUsername();

  /**
   * Returns <code>true</code> when the user is logged in, or false otherwise.
   * 
   * @return True if the user is logged in, false otherwise.
   */
  boolean isLoggedIn();

  /**
   * Logs the user with the provided <code>username</code> and
   * <code>password</code> in to the SSO authentication mechanism.
   * 
   * @param username
   *          The username credentials.
   * @param password
   *          The password credentials.
   * @return True if the login was successful, false otherwise.
   */
  boolean login(String username, String password);

  /**
   * Logs the current SSO user out of her session.
   */
  void logout();

  /**
   * Should provide information (true or false) as to whether the last
   * connection to the SSO authentication service was successful.
   * 
   * @return True if the last authentication was successful, false otherwise.
   */
  boolean getLastConnectionStatus();

  /**
   * Obtains a user's groups from the security principal that this SSO object
   * talks to.
   * 
   * @param username
   *          The username to obtain the groups for.
   * @return A {@link List} of string group names obtained from the security
   *         principal.
   **/
  List<String> retrieveGroupsForUser(String username);

}

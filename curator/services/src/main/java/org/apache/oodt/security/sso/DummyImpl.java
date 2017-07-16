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
import java.util.Collections;
import java.util.List;

/**
 * 
 * Dummy implementation of SSO auth -- if you're logged in, it logs you out. If
 * you're logged out, it logs you in. Both are independent of the actual
 * username/password combination you enter. On top of that, your username will
 * always be <code>guest</code>.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class DummyImpl extends AbstractWebBasedSingleSignOn {

  private static final String DEFAULT_USERNAME = "guest";
  
  private static final String DEFAULT_GROUP = "guest";

  private boolean connected = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.security.sso.SingleSignOn#getCurrentUsername()
   */
  public String getCurrentUsername() {
    return DEFAULT_USERNAME;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.security.sso.SingleSignOn#getLastConnectionStatus()
   */
  public boolean getLastConnectionStatus() {
    // TODO Auto-generated method stub
    return this.connected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.security.sso.SingleSignOn#isLoggedIn()
   */
  public boolean isLoggedIn() {
    // TODO Auto-generated method stub
    return this.connected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.security.sso.SingleSignOn#login(java.lang.String,
   * java.lang.String)
   */
  public boolean login(String username, String password) {
    this.connected = true;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.security.sso.SingleSignOn#logout()
   */
  public void logout() {
    this.connected = false;
  }

  /* (non-Javadoc)
   * @see org.apache.oodt.cas.security.sso.SingleSignOn#retrieveGroupsForUser(java.lang.String)
   */
  public List<String> retrieveGroupsForUser(String username) {
    return Collections.singletonList(DEFAULT_GROUP);
  }

}

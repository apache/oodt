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

package org.apache.oodt.cas.curation;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

public class CurationSession extends WebSession {

  private static final long serialVersionUID = 3000383124161748477L;

  private boolean loggedIn = false;

  private String loginUsername = null;

  public CurationSession(Request request) {
    super(request);
  }

  public static CurationSession get() {
    return (CurationSession) Session.get();
  }

  /**
   * @return the loggedIn
   */
  public boolean isLoggedIn() {
    return loggedIn;
  }

  /**
   * @param loggedIn
   *          the loggedIn to set
   */
  public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
  }

  /**
   * @return the loginUsername
   */
  public String getLoginUsername() {
    return loginUsername;
  }

  /**
   * @param loginUsername
   *          the loginUsername to set
   */
  public void setLoginUsername(String loginUsername) {
    this.loginUsername = loginUsername;
  }

}

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


package org.apache.oodt.cas.curation.servlet;

//OODT imports
import org.apache.oodt.cas.curation.util.SSOUtils;
import org.apache.oodt.cas.security.sso.SingleSignOn;

//JDK imports
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * An {@link HttpServlet} web service configured with the CAS
 * {@link SingleSignOn} interface.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class SSOConfiguredCuratorWebService extends HttpServlet implements
    CuratorConfMetKeys {

  private static final long serialVersionUID = 5907425130388872634L;

  protected SingleSignOn sso;

  /**
   * Configures the web context persistence layer for the CAS SSO so that all
   * services ({@link HttpServlet}s) that extend this implementation get the
   * ability to configure an SSO object for free, essentially.
   * 
   * @param req
   *          The HTTP request object.
   * @param res
   *          The HTTP response object.
   */
  protected void configureSingleSignOn(HttpServletRequest req,
      HttpServletResponse res) {
    this.sso = SSOUtils.getWebSingleSignOn(getServletContext(), req, res);
  }

}

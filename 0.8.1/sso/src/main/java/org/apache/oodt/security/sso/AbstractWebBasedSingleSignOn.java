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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Abstract class providing an HTTP request and response interface pair to allow
 * for persistence and management of state information related to SingleSignOn.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class AbstractWebBasedSingleSignOn implements SingleSignOn {

  protected HttpServletResponse res;

  protected HttpServletRequest req;

  public AbstractWebBasedSingleSignOn() {
    this.req = null;
    this.res = null;
  }

  /**
   * Constructs a new {@link AbstractWebBasedSingleSignOn} with the given HTTP
   * request and response.
   * 
   * @param res
   *          The {@link HttpServletRequest}.
   * @param req
   *          The {@link HttpServletResponse}.
   */
  public AbstractWebBasedSingleSignOn(HttpServletResponse res,
      HttpServletRequest req) {
    this.res = res;
    this.req = req;
  }

  /**
   * @return the res
   */
  public HttpServletResponse getRes() {
    return res;
  }

  /**
   * @param res
   *          the res to set
   */
  public void setRes(HttpServletResponse res) {
    this.res = res;
  }

  /**
   * @return the req
   */
  public HttpServletRequest getReq() {
    return req;
  }

  /**
   * @param req
   *          the req to set
   */
  public void setReq(HttpServletRequest req) {
    this.req = req;
  }

}

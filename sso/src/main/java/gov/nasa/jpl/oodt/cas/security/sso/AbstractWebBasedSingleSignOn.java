//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.security.sso;

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

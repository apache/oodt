//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.security.sso;

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

  private static boolean connected = false;

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#getCurrentUsername()
   */
  public String getCurrentUsername() {
    return DEFAULT_USERNAME;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#getLastConnectionStatus()
   */
  public boolean getLastConnectionStatus() {
    // TODO Auto-generated method stub
    return DummyImpl.connected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#isLoggedIn()
   */
  public boolean isLoggedIn() {
    // TODO Auto-generated method stub
    return DummyImpl.connected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#login(java.lang.String,
   * java.lang.String)
   */
  public boolean login(String username, String password) {
    DummyImpl.connected = true;
    return DummyImpl.connected;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#logout()
   */
  public void logout() {
    DummyImpl.connected = false;
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpl.oodt.cas.security.sso.SingleSignOn#retrieveGroupsForUser(java.lang.String)
   */
  public List<String> retrieveGroupsForUser(String username) {
    return Collections.singletonList(DEFAULT_GROUP);
  }

}

//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.security.sso;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Object factory for creating {@link SingleSignOn}s from class name Strings.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class SingleSignOnFactory {

  private static final Logger LOG = Logger.getLogger(SingleSignOnFactory.class
      .getName());

  @SuppressWarnings("unchecked")
  public static AbstractWebBasedSingleSignOn getWebBasedSingleSignOn(String className) {
    AbstractWebBasedSingleSignOn sso = null;
    Class<AbstractWebBasedSingleSignOn> clazz = null;

    try {
      clazz = (Class<AbstractWebBasedSingleSignOn>) Class.forName(className);
      sso = clazz.newInstance();
      return sso;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "InstantiationException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "IllegalAccessException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    }

    return null;
  }

}

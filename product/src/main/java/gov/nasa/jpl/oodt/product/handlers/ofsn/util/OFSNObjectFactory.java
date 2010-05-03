//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn.util;

//OODT imports
import gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNGetHandler;
import gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * The Object factory to use in the OFSN product server.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public final class OFSNObjectFactory {

  private static final Logger LOG = Logger.getLogger(OFSNObjectFactory.class
      .getName());

  /**
   * <p>
   * Constructs a new {@link OFSNListHandler} from the specified
   * <code>className</code>.
   * </p>
   * 
   * @param className
   *          The class name of the OFSNListHandler object to create.
   * @return A newly constructed {@link OFSNListHandler} object.
   */
  public static OFSNListHandler getListHandler(String className) {
    try {
      Class<OFSNListHandler> listHandler = (Class<OFSNListHandler>) Class
          .forName(className);
      return listHandler.newInstance();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading list handler class " + className
              + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "InstantiationException when loading list handler class " + className
              + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "IllegalAccessException when loading list handler class " + className
              + " Message: " + e.getMessage());
    }

    return null;
  }

  /**
   * <p>
   * Constructs a new {@link OFSNGetHandler} from the specified
   * <code>className</code>.
   * </p>
   * 
   * @param className
   *          The class name of the OFSNGetHandler object to create.
   * @return A newly constructed {@link OFSNGetHandler} object.
   */
  public static OFSNGetHandler getGetHandler(String className) {
    try {
      Class<OFSNGetHandler> getHandler = (Class<OFSNGetHandler>) Class
          .forName(className);
      return getHandler.newInstance();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading get handler class " + className
              + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "InstantiationException when loading get handler class " + className
              + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      LOG.log(Level.WARNING,
          "IllegalAccessException when loading get handler class " + className
              + " Message: " + e.getMessage());
    }

    return null;
  }

}

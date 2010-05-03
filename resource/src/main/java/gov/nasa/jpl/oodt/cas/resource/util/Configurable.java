//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

//JDK imports
import java.util.Properties;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Classes defining this interface define how to configure themselves by
 * accepting a {@link Properties} object
 * </p>.
 */
public interface Configurable {

  /**
   * Configure the object with the given {@link Properties}.
   * 
   * @param props
   *          Properties to use to configure the Object.
   */
  public void configure(Properties props);

}

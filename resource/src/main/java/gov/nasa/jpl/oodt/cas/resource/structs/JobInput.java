//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.structs;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.util.Configurable;
import gov.nasa.jpl.oodt.cas.resource.util.XmlRpcWriteable;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Input to a job that should be writeable over the XML-RPC wire.
 * </p>.
 */
public interface JobInput extends XmlRpcWriteable, Configurable {

  /**
   * Gets the ID of this JobInput
   * 
   * @return The string identifier of this JobInput object.
   */
  public String getId();

}

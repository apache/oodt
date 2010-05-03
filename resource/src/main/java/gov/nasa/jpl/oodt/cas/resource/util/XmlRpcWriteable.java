//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An interface requiring implementing classes to define how that can be
 * serialized to and from the XML-RPC wire.
 * </p>.
 */
public interface XmlRpcWriteable {

  /**
   * This method should define how to take an XML-RPC serializable
   * {@link Object} and load the internal data members of the implementing class
   * from the given input {@link Object}.
   * 
   * @param in
   *          The {@link Object} to read in and instantiate the implementation
   *          of this class with.
   */
  public void read(Object in);

  /**
   * 
   * @return An XML-RPC safe serialization {@link Object} of the implementing
   *         class for this interface.
   */
  public Object write();

}

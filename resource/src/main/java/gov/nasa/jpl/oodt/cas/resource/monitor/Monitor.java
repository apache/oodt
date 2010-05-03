//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.monitor;

//JDK imports
import java.util.List;
import java.net.URL;

//OODT imports
import gov.nasa.jpl.oodt.cas.resource.structs.ResourceNode;
import gov.nasa.jpl.oodt.cas.resource.structs.exceptions.MonitorException;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * The Job Monitor interface.
 * </p>
 */
public interface Monitor {

 /**
   * Gets the load on a resource node available to the resource manager.
   * 
   * @param node
   *          The {@link ResourceNode} to obtain the load for.
   * @return An integer respresentation of the load on a {@link ResourceNode}.
   * @throws MonitorException
   *           If there is any error obtaining the load.
   */
  public int getLoad(ResourceNode node) throws MonitorException;

  /**
   * 
   * @return A {@link List} of the {@link ResourceNode}s known to the resource
   *         manager.
   * @throws MonitorException
   *           If any error occurs getting the {@link List} of
   *           {@link ResourceNode}s.
   */
  public List getNodes() throws MonitorException;
  
  
  /**
   * Gets the {@link ResourceNode} with the given <code>nodeId</code>.
   * 
   * @return The {@link ResourceNode} for the corresponding <code>nodeId</code>.
   * @throws MonitorException
   *           If any error occurs.
   */
  public ResourceNode getNodeById(String nodeId) throws MonitorException;

  
  /**
   * Returns the {@link ResourceNode} with the given <code>ipAddr</code>.
   * @param ipAddr The URL of the ResourceNode to return.
   * @return The {@link ResourceNode} with the given ipAddr.
   * @throws MonitorException If any error occurs.
   */
  public ResourceNode getNodeByURL(URL ipAddr) throws MonitorException;
  
  /**
   * Reduces the load on a particular {@link ResourceNode} by the given
   * <code>loadValue</code>.
   * 
   * @param node The {@link ResourceNode} to reduce the load on.
   * @param loadValue The amount of reduction.
   * @return True if successfully reduced, false otherwise.
   * @throws MonitorException If any error occurs.
   */
  public boolean reduceLoad(ResourceNode node, int loadValue)
  throws MonitorException;
  
  
  /**
   * 
   * @param node
   *          The {@link ResourceNode} to assign load to.
   * @param loadValue
   *          The integer load to assign to the given {@link ResourceNode}.
   *          
   * @return True if the Monitor was able to assign the load, false
   * otherwise.
   * @throws MonitorException
   *           If any error occurs assigning the load.
   */
  public boolean assignLoad(ResourceNode node, int loadValue)
      throws MonitorException;
}

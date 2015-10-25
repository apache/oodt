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


package org.apache.oodt.cas.resource.monitor;

//JDK imports
import java.util.List;
import java.net.URL;

//OODT imports
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;

/**
 * 
 * @author woollard
 * @author bfoster
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
 int getLoad(ResourceNode node) throws MonitorException;

  /**
   * 
   * @return A {@link List} of the {@link ResourceNode}s known to the resource
   *         manager.
   * @throws MonitorException
   *           If any error occurs getting the {@link List} of
   *           {@link ResourceNode}s.
   */
  List getNodes() throws MonitorException;
  
  
  /**
   * Gets the {@link ResourceNode} with the given <code>nodeId</code>.
   * 
   * @return The {@link ResourceNode} for the corresponding <code>nodeId</code>.
   * @throws MonitorException
   *           If any error occurs.
   */
  ResourceNode getNodeById(String nodeId) throws MonitorException;

  
  /**
   * Returns the {@link ResourceNode} with the given <code>ipAddr</code>.
   * @param ipAddr The URL of the ResourceNode to return.
   * @return The {@link ResourceNode} with the given ipAddr.
   * @throws MonitorException If any error occurs.
   */
  ResourceNode getNodeByURL(URL ipAddr) throws MonitorException;
  
  /**
   * Reduces the load on a particular {@link ResourceNode} by the given
   * <code>loadValue</code>.
   * 
   * @param node The {@link ResourceNode} to reduce the load on.
   * @param loadValue The amount of reduction.
   * @return True if successfully reduced, false otherwise.
   * @throws MonitorException If any error occurs.
   */
  boolean reduceLoad(ResourceNode node, int loadValue)
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
  boolean assignLoad(ResourceNode node, int loadValue)
      throws MonitorException;
  
	/**
     * Adds a new {@link ResourceNode} for this {@link Monitor} to manage (if
     * node already exist, then should perform update)
     * 
     * @param node
     *            The new {@link ResourceNode} to manage
     */
    void addNode(ResourceNode node) throws MonitorException;

    /**
     * Remove {@link ResourceNode} from this {@link Monitor}
     * 
     * @param nodeId
     *            The id of the {@link ResourceNode} to remove
     */
    void removeNodeById(String nodeId) throws MonitorException;
	
}

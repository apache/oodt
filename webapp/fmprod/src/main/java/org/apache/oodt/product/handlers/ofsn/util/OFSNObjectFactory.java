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


package org.apache.oodt.product.handlers.ofsn.util;

//OODT imports
import org.apache.oodt.product.handlers.ofsn.OFSNGetHandler;
import org.apache.oodt.product.handlers.ofsn.OFSNListHandler;

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
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading list handler class " + className
              + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "InstantiationException when loading list handler class " + className
              + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage());
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
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading get handler class " + className
              + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "InstantiationException when loading get handler class " + className
              + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "IllegalAccessException when loading get handler class " + className
              + " Message: " + e.getMessage());
    }

    return null;
  }

}

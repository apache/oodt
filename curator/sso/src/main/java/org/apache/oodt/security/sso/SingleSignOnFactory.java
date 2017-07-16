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


package org.apache.oodt.security.sso;


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
    AbstractWebBasedSingleSignOn sso;
    Class<AbstractWebBasedSingleSignOn> clazz;

    try {
      clazz = (Class<AbstractWebBasedSingleSignOn>) Class.forName(className);
      sso = clazz.newInstance();
      return sso;
    } catch (ClassNotFoundException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "ClassNotFoundException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    } catch (InstantiationException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "InstantiationException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    } catch (IllegalAccessException e) {
      LOG.log(Level.SEVERE, e.getMessage());
      LOG.log(Level.WARNING,
          "IllegalAccessException when loading web based sso class "
              + className + " Message: " + e.getMessage());
    }

    return null;
  }

}

/**
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

package org.apache.oodt.grid;

import org.apache.oodt.profile.handlers.ProfileHandler;

/**
 * A profile server.
 * 
 */
public class ProfileServer extends Server {
  /**
   * Creates a new <code>ProfileServer</code> instance.
   * 
   * @param configuration
   *          System configuration.
   * @param className
   *          Class name of profile handler.
   */
  public ProfileServer(Configuration configuration, String className) {
    super(configuration, className);
  }

  /** {@inheritDoc} */
  protected String getType() {
    return "profile";
  }

  public int hashCode() {
    return super.hashCode() ^ 0x55555555;
  }

  public boolean equals(Object obj) {
    return super.equals(obj) && obj instanceof ProfileServer;
  }

  public String toString() {
    return "ProfileServer[" + super.toString() + "]";
  }

  /**
   * Create a query handler from this server.
   * 
   * @return a <code>ProfileHandler</code> value.
   * @throws ClassNotFoundException
   *           if the class can't be found.
   * @throws InstantiationException
   *           if the handler can't be instantiated.
   * @throws IllegalAccessException
   *           if the handler has no public constructor.
   */
  public ProfileHandler createProfileHandler() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    return (ProfileHandler) createHandler();
  }
}

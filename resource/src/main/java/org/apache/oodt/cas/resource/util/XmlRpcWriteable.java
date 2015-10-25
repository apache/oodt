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


package org.apache.oodt.cas.resource.util;

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
  void read(Object in);

  /**
   * 
   * @return An XML-RPC safe serialization {@link Object} of the implementing
   *         class for this interface.
   */
  Object write();

}

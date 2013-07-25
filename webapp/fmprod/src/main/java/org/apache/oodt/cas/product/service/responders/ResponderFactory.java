/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.oodt.cas.product.service.responders;

/**
 * Factory to generate {@link Responder} objects.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ResponderFactory
{
  /**
   * Creates a new {@link Responder} subtype.
   * @param type the type of responder to create
   * @return a new instance of a responder subtype
   */
  public static Responder createResponder(String type)
  {
    if (type == null)
    {
      return new NullResponder();
    }
    else if (type.equals("file"))
    {
      return new FileResponder();
    }
    else if (type.equals("zip"))
    {
      return new ZipResponder();
    }

    return new UnrecognizedFormatResponder();
  }
}

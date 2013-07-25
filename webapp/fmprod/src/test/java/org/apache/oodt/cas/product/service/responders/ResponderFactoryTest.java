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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Implements tests for methods in the {@link ResponderFactory} class.
 * @author rlaidlaw
 * @version $Revision$
 */
public class ResponderFactoryTest
{
  /**
   * Tests that the createResponder method returns a {@link NullResponder} when
   * null is supplied as the argument.
   */
  @Test
  public void testCreateNullResponder()
  {
    assertEquals("ResponderFactory didn't return the correct type.",
      NullResponder.class, ResponderFactory.createResponder(null).getClass());
  }



  /**
   * Tests that the createResponder method returns an
   * {@link UnrecognizedFormatResponder} when an unrecognized string is supplied
   * as the argument.
   */
  @Test
  public void testCreateUnrecognizedFormatResponder()
  {
    assertEquals("ResponderFactory didn't return the correct type.",
      UnrecognizedFormatResponder.class,
      ResponderFactory.createResponder("xyz").getClass());
  }



  /**
   * Tests that the createResponder method returns a {@link FileResponder} when
   * "file" is supplied as the argument.
   */
  @Test
  public void testCreateFileResponder()
  {
    assertEquals("ResponderFactory didn't return the correct type.",
      FileResponder.class, ResponderFactory.createResponder("file").getClass());
  }



  /**
   * Tests that the createResponder method returns a {@link ZipResponder} when
   * "zip" is supplied as the argument.
   */
  @Test
  public void testCreateZipResponder()
  {
    assertEquals("ResponderFactory didn't return the correct type.",
      ZipResponder.class, ResponderFactory.createResponder("zip").getClass());
  }
}

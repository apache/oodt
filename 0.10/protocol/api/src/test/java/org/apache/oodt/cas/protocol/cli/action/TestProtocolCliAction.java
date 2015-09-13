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
package org.apache.oodt.cas.protocol.cli.action;

//JDK imports
import java.net.URISyntaxException;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ProtocolCliAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestProtocolCliAction extends TestCase {

   public void testInitialState() throws URISyntaxException {
      MockProtocolCliAction action = new MockProtocolCliAction();
      action.setUser("user");
      action.setPass("pass");
      action.setSite("http://some-site");
      assertEquals("user", action.getAuthentication().getUser());
      assertEquals("pass", action.getAuthentication().getPass());
      assertEquals("http://some-site", action.getSite().toString());
   }
}

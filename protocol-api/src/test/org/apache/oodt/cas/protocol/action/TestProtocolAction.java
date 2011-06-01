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
package org.apache.oodt.cas.protocol.action;

//JUnit imports
import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * Test class for {@link ProtocolAction}.
 * 
 * @author bfoster
 */
public class TestProtocolAction extends TestCase {

	public void testInitialState() throws URISyntaxException {
		MockProtocolAction action = new MockProtocolAction();
		action.setId("id");
		action.setUser("user");
		action.setPass("pass");
		action.setSite("http://some-site");
		assertEquals("id", action.getId());
		assertEquals("user", action.getAuthentication().getUser());
		assertEquals("pass", action.getAuthentication().getPass());
		assertEquals("http://some-site", action.getSite().toString());
	}
}

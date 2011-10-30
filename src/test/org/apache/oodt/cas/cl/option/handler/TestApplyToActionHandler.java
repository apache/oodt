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
package org.apache.oodt.cas.cl.option.handler;

//OODT static imports
import static org.apache.oodt.cas.cl.test.util.TestUtils.createAdvancedOption;
import static org.apache.oodt.cas.cl.test.util.TestUtils.createApplyToActionHandler;
import static org.apache.oodt.cas.cl.test.util.TestUtils.createOptionInstance;

//OODT imports
import org.apache.oodt.cas.cl.action.PrintMessageAction;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ApplyToActionHandler}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestApplyToActionHandler extends TestCase {

	public void testWithoutApplyToActionMappingSet() {
		ApplyToActionHandler handler = new ApplyToActionHandler();
		assertNull(handler.getApplyToActions());
		PrintMessageAction action = new PrintMessageAction();
		action.setName("PrintMessageAction");

		assertNull(action.getMessage());

		AdvancedCmdLineOption option = new AdvancedCmdLineOption();
		option.setLongOption("message");
		option.setHandler(handler);
		option.getHandler().handleOption(action,
				createOptionInstance(option, "Howdy"));

		assertEquals("Howdy", action.getMessage());
	}

	public void testApplyToActionsMapping() {
		PrintMessageAction action = new PrintMessageAction();
		action.setName("PrintMessageAction");
		AdvancedCmdLineOption option = createAdvancedOption("printMessage",
				createApplyToActionHandler(action.getName(), "setMessage"));
		option.getHandler().handleOption(action,
				createOptionInstance(option, "Howdy"));

		assertEquals("Howdy", action.getMessage());
	}
}

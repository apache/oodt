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
package org.apache.oodt.cas.cl.store.spring;

//OODT static imports
import static org.apache.oodt.cas.cl.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.findAction;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.getOptionByName;

//JDK imports
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.action.PrintMessageAction;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.handler.CmdLineOptionBeanHandler;
import org.apache.oodt.cas.cl.option.require.ActionDependencyRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.springframework.context.ApplicationContext;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link SpringCmdLineStore}.
 *
 * @author bfoster (Brian Foster)
 */
public class TestSpringCmdLineStore extends TestCase {

	private static final String SPRING_CONFIG = "src/testdata/cmd-line-config.xml";

	public void testActionNamesAutoSet() {
		SpringCmdLineStore store = new SpringCmdLineStore(SPRING_CONFIG);
		ApplicationContext appContext = store.getApplicationContext();
		@SuppressWarnings("unchecked")
		Map<String, CmdLineAction> actionsMap = appContext.getBeansOfType(CmdLineAction.class);
		for (Entry<String, CmdLineAction> entry : actionsMap.entrySet()) {
			assertEquals(entry.getKey(), entry.getValue().getName());
		}
	}

	public void testApplicationContextAutoSet() {
		SpringCmdLineStore store = new SpringCmdLineStore(SPRING_CONFIG);
		AdvancedCmdLineOption option = (AdvancedCmdLineOption) getOptionByName(
				"outputStream", store.loadSupportedOptions());
		assertEquals(((CmdLineOptionBeanHandler) option.getHandler()).getContext(),
				store.getApplicationContext());
	}

	public void testLoadSupportedActions() {
		SpringCmdLineStore store = new SpringCmdLineStore(SPRING_CONFIG);
		Set<CmdLineAction> actions = store.loadSupportedActions();

		// Check that all actions were loaded.
		assertEquals(2, actions.size());

		// Load and verify PrintMessageAction was loaded correctly.
		CmdLineAction action = findAction("PrintMessageAction", actions);
		assertTrue(action instanceof PrintMessageAction);
		PrintMessageAction pma = (PrintMessageAction) action;
		assertEquals("Prints out a given message", pma.getDescription());
		assertNull(pma.getMessage());
		assertEquals(System.out, pma.getOutputStream());

		// Load and verify PrintHelloWorldAction was loaded correctly.
		action = findAction("PrintHelloWorldAction", actions);
		assertTrue(action instanceof PrintMessageAction);
		pma = (PrintMessageAction) action;
		assertEquals("Prints out 'Hello World'", pma.getDescription());
		assertEquals("Hello World", pma.getMessage());
		assertEquals(System.out, pma.getOutputStream());
	}

	public void testLoadSupportedOptions() {
		SpringCmdLineStore store = new SpringCmdLineStore(SPRING_CONFIG);
		Set<CmdLineOption> options = store.loadSupportedOptions();

		// Check that all options were loaded.
		assertEquals(2, options.size());

		// Load and verify useTestOutputStream was loaded correctly.
		CmdLineOption option = getOptionByName("outputStream", options);
		assertTrue(option instanceof AdvancedCmdLineOption);
		AdvancedCmdLineOption advancedOption = (AdvancedCmdLineOption) option;
		assertEquals("utos", advancedOption.getShortOption());
		assertEquals("outputStream", advancedOption.getLongOption());
		assertEquals("Specify OutputStream", advancedOption.getDescription());
		assertTrue(advancedOption.hasArgs());
		assertEquals(1, advancedOption.getDefaultArgs().size());
		assertEquals("org.apache.oodt.cas.cl.store.spring.TestOutputStream",
				advancedOption.getDefaultArgs().get(0));
		assertEquals("OutputStream classpath", advancedOption.getArgsDescription());
		assertEquals(1, advancedOption.getRequirementRules().size());
		assertEquals("PrintMessageAction", ((ActionDependencyRule) advancedOption
				.getRequirementRules().get(0)).getActionName());
		assertEquals(Relation.OPTIONAL, ((ActionDependencyRule) advancedOption
				.getRequirementRules().get(0)).getRelation());

		// Load and verify printMessage was loaded correctly.
		option = getOptionByName("printMessage", options);
		assertTrue(option instanceof AdvancedCmdLineOption);
		advancedOption = (AdvancedCmdLineOption) option;
		assertEquals("pm", advancedOption.getShortOption());
		assertEquals("printMessage", advancedOption.getLongOption());
		assertEquals("Message to print out", advancedOption.getDescription());
		assertEquals(true, advancedOption.hasArgs());
		assertEquals("message", advancedOption.getArgsDescription());
		assertEquals(1, advancedOption.getRequirementRules().size());
		assertEquals("PrintMessageAction", ((ActionDependencyRule) advancedOption
				.getRequirementRules().get(0)).getActionName());
		assertEquals(Relation.REQUIRED, ((ActionDependencyRule) advancedOption
				.getRequirementRules().get(0)).getRelation());
		assertNotNull(advancedOption.getHandler());
		assertTrue(advancedOption.getHandler() instanceof CmdLineOptionBeanHandler);
		assertEquals(1, ((CmdLineOptionBeanHandler) advancedOption.getHandler())
				.getApplyToBeans().size());
		assertEquals(
				findAction("PrintMessageAction", store.loadSupportedActions()),
				((CmdLineOptionBeanHandler) advancedOption.getHandler())
						.getApplyToBeans().get(0).getBean());
		assertEquals("setMessage",
				((CmdLineOptionBeanHandler) advancedOption.getHandler())
						.getApplyToBeans().get(0).getMethodName());
	}

	public void testHandlers() {
		SpringCmdLineStore store = new SpringCmdLineStore(SPRING_CONFIG);
		Set<CmdLineOption> options = store.loadSupportedOptions();
		Set<CmdLineAction> actions = store.loadSupportedActions();

		// Load PrintHelloWorldAction
		PrintMessageAction printHelloWorldAction = (PrintMessageAction) findAction(
				"PrintHelloWorldAction", actions);
		AdvancedCmdLineOption outputStreamOption = (AdvancedCmdLineOption) getOptionByName(
				"outputStream", options);
		outputStreamOption.getHandler().handleOption(printHelloWorldAction,
				createOptionInstance(outputStreamOption));
		assertTrue(printHelloWorldAction.getOutputStream() instanceof TestOutputStream);
		printHelloWorldAction.execute();
		assertEquals("Hello World\n",
				((TestOutputStream) printHelloWorldAction.getOutputStream()).getText());
	}
}

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
package org.apache.oodt.cas.cli.option.store.spring;

//OODT static imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findAction;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getOptionByName;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.action.PrintMessageAction;
import org.apache.oodt.cas.cli.action.store.spring.SpringCmdLineActionStore;
import org.apache.oodt.cas.cli.exception.CmdLineActionStoreException;
import org.apache.oodt.cas.cli.exception.CmdLineOptionStoreException;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler;
import org.apache.oodt.cas.cli.option.require.ActionDependencyRule;
import org.apache.oodt.cas.cli.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cli.option.store.spring.SpringCmdLineOptionStore;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link SpringCmdLineOptionStore}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestSpringCmdLineOptionStore extends TestCase {

   private static final String SPRING_OPTION_CONFIG = "src/test/resources/cmd-line-options.xml";
   private static final String SPRING_ACTION_CONFIG = "src/test/resources/cmd-line-actions.xml";

   public void testLoadSupportedOptions() throws CmdLineActionStoreException,
         CmdLineOptionStoreException {
      SpringCmdLineOptionStore optionStore = new SpringCmdLineOptionStore(
            SPRING_OPTION_CONFIG);
      Set<CmdLineOption> options = optionStore.loadSupportedOptions();
      SpringCmdLineActionStore actionStore = new SpringCmdLineActionStore(
            SPRING_ACTION_CONFIG);
      Set<CmdLineAction> actions = actionStore.loadSupportedActions();

      // Check that all options were loaded.
      assertEquals(2, options.size());

      // Load and verify printHelloWorld was loaded correctly.
      CmdLineOption option = getOptionByName("printHelloWorld", options);
      assertTrue(option instanceof AdvancedCmdLineOption);
      AdvancedCmdLineOption advancedOption = (AdvancedCmdLineOption) option;
      assertEquals("phw", advancedOption.getShortOption());
      assertEquals("printHelloWorld", advancedOption.getLongOption());
      assertEquals("Print Hello World", advancedOption.getDescription());
      assertFalse(advancedOption.hasArgs());
      assertEquals(1, advancedOption.getStaticArgs().size());
      assertEquals("Hello World!", advancedOption.getStaticArgs().get(0));
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
      assertTrue(advancedOption.getHandler() instanceof ApplyToActionHandler);
      assertEquals(1, ((ApplyToActionHandler) advancedOption.getHandler())
            .getApplyToActions().size());
      assertEquals(findAction("PrintMessageAction", actions).getName(),
            ((ApplyToActionHandler) advancedOption.getHandler())
                  .getApplyToActions().get(0).getActionName());
      assertEquals("setMessage",
            ((ApplyToActionHandler) advancedOption.getHandler())
                  .getApplyToActions().get(0).getMethodName());
   }

   public void testHandlers() throws CmdLineActionStoreException,
         CmdLineOptionStoreException {
      SpringCmdLineOptionStore store = new SpringCmdLineOptionStore(
            SPRING_OPTION_CONFIG);
      Set<CmdLineOption> options = store.loadSupportedOptions();
      SpringCmdLineActionStore actionStore = new SpringCmdLineActionStore(
            SPRING_ACTION_CONFIG);
      Set<CmdLineAction> actions = actionStore.loadSupportedActions();

      // Load PrintHelloWorldAction
      PrintMessageAction printHelloWorldAction = (PrintMessageAction) findAction(
            "PrintMessageAction", actions);
      AdvancedCmdLineOption printHelloWorldOption = (AdvancedCmdLineOption) getOptionByName(
            "printHelloWorld", options);
      printHelloWorldOption.getHandler().handleOption(printHelloWorldAction,
            createOptionInstance(printHelloWorldOption));
      ActionMessagePrinter printer = new ActionMessagePrinter();
      printHelloWorldAction.execute(printer);
      assertEquals(1, printer.getPrintedMessages().size());
      assertEquals("Hello World!", printer.getPrintedMessages().get(0));
   }
}

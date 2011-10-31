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
package org.apache.oodt.cas.cl.option.store.spring;

//OODT static imports
import static org.apache.oodt.cas.cl.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.findAction;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.getOptionByName;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.action.PrintMessageAction;
import org.apache.oodt.cas.cl.action.store.spring.SpringCmdLineActionStore;
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.handler.ApplyToActionHandler;
import org.apache.oodt.cas.cl.option.require.ActionDependencyRule;
import org.apache.oodt.cas.cl.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cl.test.util.TestOutputStream;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link SpringCmdLineOptionStore}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestSpringCmdLineOptionStore extends TestCase {

   private static final String SPRING_OPTION_CONFIG = "src/testdata/cmd-line-options.xml";
   private static final String SPRING_ACTION_CONFIG = "src/testdata/cmd-line-actions.xml";

   public void testLoadSupportedOptions() {
      SpringCmdLineOptionStore optionStore = new SpringCmdLineOptionStore(
            SPRING_OPTION_CONFIG);
      Set<CmdLineOption> options = optionStore.loadSupportedOptions();
      SpringCmdLineActionStore actionStore = new SpringCmdLineActionStore(
            SPRING_ACTION_CONFIG);
      Set<CmdLineAction> actions = actionStore.loadSupportedActions();

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
      assertEquals("org.apache.oodt.cas.cl.test.util.TestOutputStream",
            advancedOption.getDefaultArgs().get(0));
      assertEquals("OutputStream classpath",
            advancedOption.getArgsDescription());
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

   public void testHandlers() {
      SpringCmdLineOptionStore store = new SpringCmdLineOptionStore(
            SPRING_OPTION_CONFIG);
      Set<CmdLineOption> options = store.loadSupportedOptions();
      SpringCmdLineActionStore actionStore = new SpringCmdLineActionStore(
            SPRING_ACTION_CONFIG);
      Set<CmdLineAction> actions = actionStore.loadSupportedActions();

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
            ((TestOutputStream) printHelloWorldAction.getOutputStream())
                  .getText());
   }
}

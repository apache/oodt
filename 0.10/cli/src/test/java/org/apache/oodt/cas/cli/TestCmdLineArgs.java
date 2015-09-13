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
package org.apache.oodt.cas.cli;

//Google static imports
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

//OODT static imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAction;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createActionOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.CmdLineArgs;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link CmdLineArgs}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestCmdLineArgs extends TestCase {

   private final static CmdLineAction TEST_ACTION_1 = createAction("TestAction1");
   private final static CmdLineAction TEST_ACTION_2 = createAction("TestAction2");
   private final static CmdLineAction TEST_ACTION_3 = createAction("TestAction3");

   private final static ActionCmdLineOption ACTION_OPTION = createActionOption("operation");
   private final static HelpCmdLineOption HELP_OPTION = new HelpCmdLineOption();
   private final static PrintSupportedActionsCmdLineOption PSA_ACTION = new PrintSupportedActionsCmdLineOption();

   private final static Set<CmdLineAction> SUPPORTED_ACTIONS = newHashSet(
         TEST_ACTION_1, TEST_ACTION_2, TEST_ACTION_3);
   private final static Set<CmdLineOption> SUPPORTED_OPTIONS = newHashSet(
         (CmdLineOption) ACTION_OPTION, (CmdLineOption) HELP_OPTION,
         (CmdLineOption) PSA_ACTION);

   public void testBaseCase() {
      CmdLineOptionInstance specifiedAction = createOptionInstance(
            ACTION_OPTION, TEST_ACTION_1.getName());
      Set<CmdLineOptionInstance> specifiedOptions = newHashSet(specifiedAction);
      CmdLineArgs args = new CmdLineArgs(SUPPORTED_ACTIONS, SUPPORTED_OPTIONS,
            specifiedOptions);
      assertEquals(TEST_ACTION_1, args.getSpecifiedAction());
      assertEquals(args.getActionOptionInst(), specifiedAction);
      assertEquals(ACTION_OPTION, args.getActionOptionInst().getOption());
      assertNull(args.getHelpOptionInst());
      assertNull(args.getPrintSupportedActionsOptionInst());
   }

   public void testCaseActionNotSupported() {
      CmdLineOptionInstance specifiedAction = createOptionInstance(
            ACTION_OPTION, "NotSupportedActionName");
      Set<CmdLineOptionInstance> specifiedOptions = newHashSet(specifiedAction);
      CmdLineArgs args = new CmdLineArgs(SUPPORTED_ACTIONS, SUPPORTED_OPTIONS,
            specifiedOptions);

      // Verify that CmdLineAction is null since it was not able to be located
      // in set of supported actions.
      assertNull(args.getSpecifiedAction());
      // Verify that if did find the action option.
      assertEquals(specifiedAction.getOption(), args.getActionOptionInst()
            .getOption());
      // Verify that if found the specified action even though it is not
      // supported.
      assertEquals(newArrayList("NotSupportedActionName"), args
            .getActionOptionInst().getValues());
   }
}

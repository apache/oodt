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

//OODT static imports
import static org.apache.oodt.cas.cli.CmdLineUtility.validate;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createActionOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAdvancedOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createApplyToActionHandler;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineFailedValidation;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getOptionByName;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.CmdLineArgs;
import org.apache.oodt.cas.cli.CmdLineUtility;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.action.PrintMessageAction;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cli.option.validator.ArgRegExpCmdLineOptionValidator;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator.Result;
import org.apache.oodt.cas.cli.test.util.TestUtils;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link CmdLineUtility}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestCmdLineUtility extends TestCase {

   public void testCheck() {
      CmdLineArgs args = getArgs();

      // Expect pass.
      assertEquals(0, CmdLineUtility.check(args).size());

      // Expect fail.
      args.getSupportedOptions().add(
            TestUtils.createSimpleOption("ReqTestAction", true));
      assertEquals(1, CmdLineUtility.check(args).size());
   }

   public void testValidate() {
      CmdLineArgs args = getArgs();

      // Expect pass.
      assertEquals(1, CmdLineUtility.validate(args).size());
      assertEquals(Result.Grade.PASS, CmdLineUtility.validate(args).get(0)
            .getGrade());

      // Add validator which will cause fail.
      AdvancedCmdLineOption option = (AdvancedCmdLineOption) getOptionByName("message", args.getSupportedOptions());
      ArgRegExpCmdLineOptionValidator validator = new ArgRegExpCmdLineOptionValidator();
      validator.setAllowedArgs(Lists.newArrayList("\\d{1,2}"));
      option.addValidator(validator);

      // Expect fail.
      assertFalse(determineFailedValidation(validate(args)).isEmpty());
   }

   public void testHandle() {
      CmdLineArgs args = getArgs();

      // Verify handling works.
      PrintMessageAction action = (PrintMessageAction) args
            .getSpecifiedAction();
      assertNull(action.getMessage());
      CmdLineUtility.handle(args);
      assertEquals("Test Message", action.getMessage());
   }

   private CmdLineArgs getArgs() {
      // Setup Supported Actions.
      String actionName = "TestAction";
      PrintMessageAction action = new PrintMessageAction();
      action.setName(actionName);
      Set<CmdLineAction> actions = Sets.newHashSet((CmdLineAction) action);

      // Setup Supported Options.
      Set<CmdLineOption> options = Sets.newHashSet();
      options.add(new HelpCmdLineOption());
      options.add(new PrintSupportedActionsCmdLineOption());
      options.add(createActionOption("action"));
      AdvancedCmdLineOption option = createAdvancedOption("message",
            createApplyToActionHandler(actionName, "setMessage"));
      ArgRegExpCmdLineOptionValidator validator = new ArgRegExpCmdLineOptionValidator();
      validator.setAllowedArgs(Lists.newArrayList(".*"));
      option.addValidator(validator);
      options.add(option);

      // Setup Specified Options.
      Set<CmdLineOptionInstance> specifiedOptions = Sets.newHashSet();
      specifiedOptions.add(createOptionInstance(option, "Test Message"));
      specifiedOptions.add(createOptionInstance(createActionOption("action"),
            "TestAction"));

      // Setup CmdLineArgs.
      return new CmdLineArgs(actions, options, specifiedOptions);
   }
}

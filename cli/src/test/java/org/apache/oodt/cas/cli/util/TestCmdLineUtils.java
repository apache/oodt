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
package org.apache.oodt.cas.cli.util;

//OODT static imports
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAction;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createActionOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createAdvancedOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createGroupOption;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionInstance;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createOptionalRequirementRule;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createRequiredRequirementRule;
import static org.apache.oodt.cas.cli.test.util.TestUtils.createSimpleOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineFailedValidation;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findFirstActionOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findSpecifiedActionOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findSpecifiedHelpOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findSpecifiedPrintSupportedActionsOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.validate;

//JDK imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//JUnit imports
import junit.framework.TestCase;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupSubOption;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cli.option.handler.CmdLineOptionHandler;
import org.apache.oodt.cas.cli.option.validator.AllowedArgsCmdLineOptionValidator;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cli.util.CmdLineUtils;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Test class for {@link CmdLineUtils}.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestCmdLineUtils extends TestCase {

   public void testDetermineRequired() {
      CmdLineAction action = createAction("TestAction");
      CmdLineOption urlOption, passOption, actionOption;

      HashSet<CmdLineOption> options = Sets.newHashSet(
            urlOption = createSimpleOption("url",
                  createRequiredRequirementRule(action)),
            passOption = createSimpleOption("pass", false),
            createSimpleOption("user", false),
            actionOption = createActionOption("operation"));

      Set<CmdLineOption> requiredOptions = CmdLineUtils.determineRequired(
            action, options);
      assertEquals(Sets.newHashSet(urlOption, actionOption), requiredOptions);

      options = Sets.newHashSet(
            urlOption = createSimpleOption("url",
                  createRequiredRequirementRule(action)),
            passOption = createSimpleOption("pass", true),
            createSimpleOption("user", false),
            actionOption = createActionOption("operation"));

      requiredOptions = CmdLineUtils.determineRequired(action, options);
      assertEquals(Sets.newHashSet(urlOption, passOption, actionOption),
            requiredOptions);
   }

   public void testIsRequired() {
      CmdLineAction action = createAction("TestAction");
      assertTrue(CmdLineUtils.isRequired(action,
            createSimpleOption("url", createRequiredRequirementRule(action))));
      assertTrue(CmdLineUtils.isRequired(action,
            createSimpleOption("url", true)));
      assertFalse(CmdLineUtils.isRequired(action,
            createSimpleOption("url", false)));
   }

   public void testDetermineOptional() {
      CmdLineAction action = createAction("TestAction");
      CmdLineOption actionOption = new ActionCmdLineOption();
      CmdLineOption passOption, userOption;

      HashSet<CmdLineOption> options = Sets.newHashSet(
            createSimpleOption("url", createRequiredRequirementRule(action)),
            passOption = createSimpleOption("pass", false),
            userOption = createSimpleOption("user", false), actionOption);

      Set<CmdLineOption> optionalOptions = CmdLineUtils.determineOptional(
            action, options);
      assertEquals(2, optionalOptions.size());
      assertEquals(Sets.newHashSet(passOption, userOption), optionalOptions);

      options = Sets.newHashSet(createSimpleOption("pass", true),
            createSimpleOption("user", true), actionOption);

      optionalOptions = CmdLineUtils.determineOptional(action, options);
      assertTrue(optionalOptions.isEmpty());

      options = Sets.newHashSet(
            passOption = createSimpleOption("pass",
                  createOptionalRequirementRule(action)),
            userOption = createSimpleOption("user",
                  createOptionalRequirementRule(action)), actionOption);

      optionalOptions = CmdLineUtils.determineOptional(action, options);
      assertEquals(Sets.newHashSet(passOption, userOption), optionalOptions);
   }

   public void testIsOptional() {
      CmdLineAction action = createAction("TestAction");
      assertTrue(CmdLineUtils.isOptional(action,
            createSimpleOption("url", createOptionalRequirementRule(action))));
      assertFalse(CmdLineUtils.isOptional(action,
            createSimpleOption("url", true)));
      assertTrue(CmdLineUtils.isOptional(action,
            createSimpleOption("url", false)));
   }

   public void testGetRequiredOptions() {
      CmdLineOption urlOption = createSimpleOption("url", true);
      HashSet<CmdLineOption> options = Sets.newHashSet(urlOption,
            createActionOption("action"), createSimpleOption("user", false),
            createSimpleOption("pass", false));

      assertEquals(Sets.newHashSet(urlOption),
            CmdLineUtils.getRequiredOptions(options));
   }

   public void testGetRequiredOptionsDoNotIgnoreActionOptions() {
      CmdLineOption actionOption, urlOption;
      HashSet<CmdLineOption> options = Sets.newHashSet(
            actionOption = createActionOption("action"),
            urlOption = createSimpleOption("url", true),
            createSimpleOption("user", false),
            createSimpleOption("pass", false));

      actionOption.setRequired(true);
      assertEquals(Sets.newHashSet(actionOption, urlOption),
            CmdLineUtils.getRequiredOptions(options, false));
   }

   public void testSortOptionsByRequiredStatus() {
      CmdLineAction action = createAction("action");
      CmdLineOption userOption, urlOption, passOption, actionOption;
      HashSet<CmdLineOption> options = Sets.newHashSet(
            userOption = createSimpleOption("user", false),
            urlOption = createSimpleOption("url",
                  createRequiredRequirementRule(action)),
            passOption = createSimpleOption("pass", false),
            actionOption = createActionOption("action"));

      actionOption.setRequired(true);
      List<CmdLineOption> sortedOptions = CmdLineUtils
            .sortOptionsByRequiredStatus(options);
      assertEquals(options.size(), sortedOptions.size());
      assertEquals(actionOption, sortedOptions.get(0));
      assertEquals(urlOption, sortedOptions.get(1));
      assertEquals(Sets.newHashSet(userOption, passOption),
            Sets.newHashSet(sortedOptions.get(2), sortedOptions.get(3)));
   }

   public void testSortActions() {
      Set<CmdLineAction> actions = Sets.newHashSet(
            createAction("Tom"),
            createAction("Bill"),
            createAction("Young"),
            createAction("Andy"));
      List<CmdLineAction> sortedActions = CmdLineUtils.sortActions(actions);
      assertEquals("Andy", sortedActions.get(0).getName());
      assertEquals("Bill", sortedActions.get(1).getName());
      assertEquals("Tom", sortedActions.get(2).getName());
      assertEquals("Young", sortedActions.get(3).getName());
   }

   public void testGetOptionByName() {
      CmdLineAction action = createAction("action");
      CmdLineOption userOption, urlOption, passOption, actionOption;
      HashSet<CmdLineOption> options = Sets.newHashSet(
            userOption = createSimpleOption("user", "username", false),
            urlOption = createSimpleOption("u", "url",
                  createRequiredRequirementRule(action)),
            passOption = createSimpleOption("pass", "password", false),
            actionOption = createActionOption("action"));

      assertEquals(userOption,
            CmdLineUtils.getOptionByName(userOption.getShortOption(), options));
      assertEquals(userOption,
            CmdLineUtils.getOptionByName(userOption.getLongOption(), options));
      assertEquals(urlOption,
            CmdLineUtils.getOptionByName(urlOption.getShortOption(), options));
      assertEquals(urlOption,
            CmdLineUtils.getOptionByName(urlOption.getLongOption(), options));
      assertEquals(passOption,
            CmdLineUtils.getOptionByName(passOption.getShortOption(), options));
      assertEquals(passOption,
            CmdLineUtils.getOptionByName(passOption.getLongOption(), options));
      assertEquals(actionOption, CmdLineUtils.getOptionByName(
            actionOption.getShortOption(), options));
      assertEquals(actionOption,
            CmdLineUtils.getOptionByName(actionOption.getLongOption(), options));
   }

   public void testGetOptionInstanceByName() {
      CmdLineAction action = createAction("action");
      CmdLineOptionInstance userOptionInst, urlOptionInst, passOptionInst, actionOptionInst;
      HashSet<CmdLineOptionInstance> optionInsts = Sets.newHashSet(
            userOptionInst = new CmdLineOptionInstance(createSimpleOption(
                  "user", "username", false), new ArrayList<String>()),
            urlOptionInst = new CmdLineOptionInstance(createSimpleOption("u",
                  "url", createRequiredRequirementRule(action)),
                  new ArrayList<String>()),
            passOptionInst = new CmdLineOptionInstance(createSimpleOption(
                  "pass", "password", false), new ArrayList<String>()),
            actionOptionInst = new CmdLineOptionInstance(
                  createActionOption("action"), new ArrayList<String>()));

      assertEquals(userOptionInst, CmdLineUtils.getOptionInstanceByName(
            userOptionInst.getOption().getShortOption(), optionInsts));
      assertEquals(userOptionInst, CmdLineUtils.getOptionInstanceByName(
            userOptionInst.getOption().getLongOption(), optionInsts));
      assertEquals(urlOptionInst, CmdLineUtils.getOptionInstanceByName(
            urlOptionInst.getOption().getShortOption(), optionInsts));
      assertEquals(urlOptionInst, CmdLineUtils.getOptionInstanceByName(
            urlOptionInst.getOption().getLongOption(), optionInsts));
      assertEquals(passOptionInst, CmdLineUtils.getOptionInstanceByName(
            passOptionInst.getOption().getShortOption(), optionInsts));
      assertEquals(passOptionInst, CmdLineUtils.getOptionInstanceByName(
            passOptionInst.getOption().getLongOption(), optionInsts));
      assertEquals(actionOptionInst, CmdLineUtils.getOptionInstanceByName(
            actionOptionInst.getOption().getShortOption(), optionInsts));
      assertEquals(actionOptionInst, CmdLineUtils.getOptionInstanceByName(
            actionOptionInst.getOption().getLongOption(), optionInsts));
   }

   public void testIsSubOption() {
      CmdLineOption subOption = createSimpleOption("test", false);
      GroupCmdLineOption groupOption = createGroupOption("group", false);
      assertFalse(CmdLineUtils.isSubOption(groupOption, subOption));

      groupOption.addSubOption(new GroupSubOption(subOption, false));
      assertTrue(CmdLineUtils.isSubOption(groupOption, subOption));
   }

   public void testFindPerformAndQuitOptions() {
      AdvancedCmdLineOption performAndQuitOption = createAdvancedOption("help",
            null);
      performAndQuitOption.setPerformAndQuit(true);
      AdvancedCmdLineOption otherOption = createAdvancedOption("help", null);
      otherOption.setPerformAndQuit(false);

      CmdLineOptionInstance performAndQuitOptionInstance = new CmdLineOptionInstance(
            performAndQuitOption, new ArrayList<String>());
      CmdLineOptionInstance otherInstance = new CmdLineOptionInstance(
            otherOption, new ArrayList<String>());

      assertEquals(Sets.newHashSet(performAndQuitOptionInstance),
            CmdLineUtils.findPerformAndQuitOptions(Sets.newHashSet(
                  performAndQuitOptionInstance, otherInstance)));
   }

   public void testIsPerformAndQuitOption() {
      AdvancedCmdLineOption performAndQuitOption = createAdvancedOption("help",
            null);
      performAndQuitOption.setPerformAndQuit(true);
      AdvancedCmdLineOption otherOption = createAdvancedOption("help", null);
      otherOption.setPerformAndQuit(false);

      assertTrue(CmdLineUtils.isPerformAndQuitOption(performAndQuitOption));
      assertFalse(CmdLineUtils.isPerformAndQuitOption(otherOption));
   }

   public void testFindSpecifiedOption() {
      CmdLineOption findOption = createSimpleOption("test", false);
      Set<CmdLineOptionInstance> options = Sets.newHashSet(
            createOptionInstance(createSimpleOption("test1", false)),
            createOptionInstance(createSimpleOption("test2", false)));

      assertNull(CmdLineUtils.findSpecifiedOption(findOption, options));
      options.add(createOptionInstance(findOption));
      assertEquals(createOptionInstance(findOption),
            CmdLineUtils.findSpecifiedOption(findOption, options));
   }

   public void testFindAllOfSpecifiedOption() {
      CmdLineOption findOption = createSimpleOption("test", false);
      Set<CmdLineOptionInstance> options = Sets.newHashSet(
            createOptionInstance(createSimpleOption("test1", false)),
            createOptionInstance(createSimpleOption("test2", false)));

      assertTrue(CmdLineUtils.findAllOfSpecifiedOption(findOption, options)
            .isEmpty());
      CmdLineOptionInstance firstOption = createOptionInstance(findOption,
            "first");
      CmdLineOptionInstance secondOption = createOptionInstance(findOption,
            "second");
      options.add(firstOption);
      assertEquals(Sets.newHashSet(firstOption),
            CmdLineUtils.findAllOfSpecifiedOption(findOption, options));
      options.add(secondOption);
      assertEquals(Sets.newHashSet(firstOption, secondOption),
            CmdLineUtils.findAllOfSpecifiedOption(findOption, options));
   }

   public void testIsPrintSupportedActionsOption() {
      assertFalse(CmdLineUtils
            .isPrintSupportedActionsOption(createSimpleOption("test", false)));
      assertTrue(CmdLineUtils
            .isPrintSupportedActionsOption(new PrintSupportedActionsCmdLineOption()));
   }

   public void testFindPrintSupportedActionsOption() {
      CmdLineOption psaAction = new PrintSupportedActionsCmdLineOption();
      Set<CmdLineOption> options = Sets.newHashSet(
            (CmdLineOption) createSimpleOption("test", false),
            createSimpleOption("test2", false));

      assertNull(CmdLineUtils.findPrintSupportedActionsOption(options));
      options.add(psaAction);
      assertEquals(psaAction,
            CmdLineUtils.findPrintSupportedActionsOption(options));
   }

   public void testFindSpecifiedPrintSupportedActionsOption() {
      CmdLineOptionInstance psaAction = createOptionInstance(
            new PrintSupportedActionsCmdLineOption());
      Set<CmdLineOptionInstance> options = Sets.newHashSet(
            createOptionInstance(createSimpleOption("test", false)),
            createOptionInstance(createSimpleOption("test2", false)));

      assertNull(findSpecifiedPrintSupportedActionsOption(options));
      options.add(psaAction);
      assertEquals(psaAction,
            findSpecifiedPrintSupportedActionsOption(options));
      options.add(createOptionInstance(new PrintSupportedActionsCmdLineOption(
            "psa2", "PrintSupportedActions2", "Print Actions 2", false)));
      try {
         findSpecifiedPrintSupportedActionsOption(options);
         fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException ignore) { /* expect throw */ }
   }

   public void testIsSimpleOption() {
      assertTrue(CmdLineUtils
            .isSimpleOption(new PrintSupportedActionsCmdLineOption()));
      assertTrue(CmdLineUtils.isSimpleOption(new HelpCmdLineOption()));
      assertTrue(CmdLineUtils.isSimpleOption(createSimpleOption("Test", true)));
   }

   public void testIsActionOption() {
      assertFalse(CmdLineUtils.isActionOption(new HelpCmdLineOption()));
      assertTrue(CmdLineUtils.isActionOption(createActionOption("action")));
   }

   public void testFindActionOption() {
      ActionCmdLineOption actionOption = createActionOption("action");
      Set<CmdLineOption> options = Sets.newHashSet(
            (CmdLineOption) createSimpleOption("test", false),
            createSimpleOption("test", false));

      assertNull(findFirstActionOption(options));
      options.add(actionOption);
      assertEquals(actionOption, findFirstActionOption(options));
   }

   public void testFindSpecifiedActionOption() {
      CmdLineOptionInstance actionOption = createOptionInstance(createActionOption("action"));
      Set<CmdLineOptionInstance> options = Sets.newHashSet(
            createOptionInstance(createSimpleOption("test", false)),
            createOptionInstance(createSimpleOption("test", false)));

      assertNull(findSpecifiedActionOption(options));
      options.add(actionOption);
      assertEquals(actionOption,
            findSpecifiedActionOption(options));
      options.add(createOptionInstance(createActionOption("action2")));
      try {
         findSpecifiedActionOption(options);
         fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException ignore) { /* expect throw */ }
      
   }

   public void testIsGroupOption() {
      assertFalse(CmdLineUtils.isGroupOption(new HelpCmdLineOption()));
      assertTrue(CmdLineUtils.isGroupOption(createGroupOption("test", false)));
   }

   public void testAsGroupOption() {
      try {
         CmdLineUtils.asGroupOption(createSimpleOption("test", false));
         fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException ignore) { /* expect throw */
      }
      CmdLineUtils.asGroupOption(createGroupOption("test", false));
   }

   public void testIsHelpOption() {
      assertFalse(CmdLineUtils.isHelpOption(createSimpleOption("test", false)));
      assertTrue(CmdLineUtils.isHelpOption(new HelpCmdLineOption()));
   }

   public void testAsHelpOption() {
      try {
         CmdLineUtils.asHelpOption(createSimpleOption("test", false));
         fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException ignore) { /* expect throw */ }
      CmdLineUtils.asHelpOption(new HelpCmdLineOption());
   }

   public void testFindHelpOption() {
      HelpCmdLineOption helpOption = new HelpCmdLineOption();
      Set<CmdLineOption> options = Sets.newHashSet(
            (CmdLineOption) createSimpleOption("test", false),
            createSimpleOption("test", false));

      assertNull(CmdLineUtils.findHelpOption(options));
      options.add(helpOption);
      assertEquals(helpOption, CmdLineUtils.findHelpOption(options));
   }

   public void testFindSpecifiedHelpOption() {
      CmdLineOptionInstance helpOption = createOptionInstance(new HelpCmdLineOption());
      Set<CmdLineOptionInstance> options = Sets.newHashSet(
            createOptionInstance(createSimpleOption("test", false)),
            createOptionInstance(createSimpleOption("test", false)));

      assertNull(findSpecifiedHelpOption(options));
      options.add(helpOption);
      assertEquals(helpOption, findSpecifiedHelpOption(options));
      options.add(createOptionInstance(new HelpCmdLineOption("h2", "help2", "Second Help Option", true)));
      try {
         findSpecifiedHelpOption(options);
         fail("Should have thrown IllegalArgumentException");
      } catch (IllegalArgumentException ignore) { /* expect throw */ }
   }

   public void testValidate() {
      AdvancedCmdLineOption option = new AdvancedCmdLineOption("t", "test", "",
            true);
      AllowedArgsCmdLineOptionValidator validator = new AllowedArgsCmdLineOptionValidator();
      validator.setAllowedArgs(Lists.newArrayList("value"));
      option.setValidators(Lists
            .newArrayList((CmdLineOptionValidator) validator));

      // Test case fail.
      assertFalse(determineFailedValidation(validate(createOptionInstance(option, "value1"))).isEmpty());
      // Test case pass.
      assertTrue(determineFailedValidation(validate(createOptionInstance(option, "value"))).isEmpty());
   }

   public void testHandle() {
      CmdLineAction action = createAction("testAction");
      action.setDescription("test description");
      AdvancedCmdLineOption option = new AdvancedCmdLineOption("t", "test", "",
            true);
      // Insure runs with no errors when action doesn't have a handler.
      CmdLineUtils.handle(action, createOptionInstance(option));

      // Test case when option has a handler.
      option.setHandler(new CmdLineOptionHandler() {
         @Override
         public void initialize(CmdLineOption option) {}

         @Override
         public void handleOption(CmdLineAction selectedAction,
               CmdLineOptionInstance optionInstance) {
            selectedAction.setDescription("handler modified description");
         }

         @Override
         public String getHelp(CmdLineOption option) {
            return null;
         }

         @Override
         public String getArgDescription(CmdLineAction action,
               CmdLineOption option) {
            return null;
         }
      });
      CmdLineUtils.handle(action, createOptionInstance(option));
      assertEquals("handler modified description", action.getDescription());
   }

   public void testFindAction() {
      CmdLineAction action = createAction("TestAction1");
      Set<CmdLineAction> actions = Sets.newHashSet(action,
            createAction("TestAction2"), createAction("TestAction3"));
      assertNull(CmdLineUtils.findAction(
            createOptionInstance(createActionOption("action"), "TestAction"),
            actions));
      assertEquals(action, CmdLineUtils.findAction(
            createOptionInstance(createActionOption("action"), "TestAction1"),
            actions));
   }
}

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
package org.apache.oodt.cas.cli.test.util;

//JDK imports
import java.util.Collections;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cli.option.handler.ApplyToAction;
import org.apache.oodt.cas.cli.option.handler.ApplyToActionHandler;
import org.apache.oodt.cas.cli.option.handler.CmdLineOptionHandler;
import org.apache.oodt.cas.cli.option.require.ActionDependencyRule;
import org.apache.oodt.cas.cli.option.require.RequirementRule;
import org.apache.oodt.cas.cli.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator;

//Google imports
import com.google.common.collect.Lists;

/**
 * Base Test case for CAS-CL unit tests.
 * 
 * @author bfoster (Brian Foster)
 */
public class TestUtils {

   public static CmdLineAction createAction(String name) {
      return new CmdLineAction(name, "This is an action description") {

         @Override
         public void execute(ActionMessagePrinter printer) {
            // do nothing
         }

      };
   }

   public static GroupCmdLineOption createGroupOption(String longName,
         boolean required) {
      GroupCmdLineOption option = new GroupCmdLineOption();
      option.setLongOption(longName);
      option.setShortOption(longName);
      option.setRequired(required);
      return option;
   }

   public static SimpleCmdLineOption createSimpleOption(String longName,
         boolean required) {
      return createSimpleOption(longName, longName, required);
   }

   public static SimpleCmdLineOption createSimpleOption(String shortName,
         String longName, boolean required) {
      SimpleCmdLineOption option = new SimpleCmdLineOption();
      option.setShortOption(shortName);
      option.setLongOption(longName);
      option.setRequired(required);
      return option;
   }

   public static SimpleCmdLineOption createSimpleOption(String longName,
         RequirementRule rule) {
      return createSimpleOption(longName, longName, rule);
   }

   public static SimpleCmdLineOption createSimpleOption(String shortName,
         String longName, RequirementRule rule) {
      SimpleCmdLineOption option = new SimpleCmdLineOption();
      option.setShortOption(shortName);
      option.setLongOption(longName);
      option.setRequirementRules(Collections.singletonList(rule));
      return option;
   }

   public static AdvancedCmdLineOption createAdvancedOption(String longName,
         CmdLineOptionHandler handler) {
      return createAdvancedOption(longName, longName, handler);
   }

   public static AdvancedCmdLineOption createAdvancedOption(String shortName,
         String longName, CmdLineOptionHandler handler) {
      AdvancedCmdLineOption option = new AdvancedCmdLineOption();
      option.setShortOption(shortName);
      option.setLongOption(longName);
      option.setHandler(handler);
      return option;
   }

   public static AdvancedCmdLineOption createValidationOption(String longName,
         CmdLineOptionValidator... validators) {
      AdvancedCmdLineOption option = new AdvancedCmdLineOption();
      option.setLongOption(longName);
      option.setShortOption(longName);
      option.setValidators(Lists.newArrayList(validators));
      return option;
   }

   public static ActionCmdLineOption createActionOption(String longName) {
      ActionCmdLineOption option = new ActionCmdLineOption();
      option.setLongOption(longName);
      option.setShortOption(longName);
      return option;
   }

   public static CmdLineOptionInstance createOptionInstance(
         CmdLineOption option, String... values) {
      return new CmdLineOptionInstance(option, Lists.newArrayList(values));
   }

   public static RequirementRule createRequiredRequirementRule(
         CmdLineAction action) {
      ActionDependencyRule rule = new ActionDependencyRule();
      rule.setActionName(action.getName());
      rule.setRelation(Relation.REQUIRED);
      return rule;
   }

   public static RequirementRule createOptionalRequirementRule(
         CmdLineAction action) {
      ActionDependencyRule rule = new ActionDependencyRule();
      rule.setActionName(action.getName());
      rule.setRelation(Relation.OPTIONAL);
      return rule;
   }

   public static ApplyToActionHandler createApplyToActionHandler(
         String actionName, String methodName) {
      ApplyToActionHandler handler = new ApplyToActionHandler();
      handler.setApplyToActions(Lists.newArrayList(new ApplyToAction(
            actionName, methodName)));
      return handler;
   }
}

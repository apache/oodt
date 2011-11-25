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

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupSubOption;
import org.apache.oodt.cas.cli.option.HandleableCmdLineOption;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cli.option.SimpleCmdLineOption;
import org.apache.oodt.cas.cli.option.ValidatableCmdLineOption;
import org.apache.oodt.cas.cli.option.require.RequirementRule;
import org.apache.oodt.cas.cli.option.require.RequirementRule.Relation;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator;

//Google imports
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Collection of common helper methods.
 * 
 * @author bfoster (Brian Foster)
 */
public class CmdLineUtils {

   private CmdLineUtils() {
   }

   /**
    * Determines which of the given {@link CmdLineOption}s are required because
    * the given {@link CmdLineAction} was specified.
    * 
    * @param action
    *           The {@link CmdLineAction} which was specified.
    * @param options
    *           The {@link CmdLineOption}s in question of being required or not.
    * @return The {@link Set} of {@link CmdLineOption}s where are required
    *         because the given {@link CmdLineAction} was specified.
    */
   public static Set<CmdLineOption> determineRequired(CmdLineAction action,
         Set<CmdLineOption> options) {
      Validate.notNull(action);
      Validate.notNull(options);

      Set<CmdLineOption> requiredOptions = getRequiredOptions(options);
      for (CmdLineOption option : options) {
         if (isRequired(action, option)) {
            requiredOptions.add(option);
         }
      }
      return requiredOptions;
   }

   /**
    * Determines the given {@link GroupCmdLineOption}'s sub-options which are
    * affect the given {@link CmdLineAction}.
    * 
    * @param action
    *           The {@link CmdLineAction} for which given
    *           {@link GroupCmdLineOption}'s sub-options will be determined
    *           relevant or not
    * @param option
    *           The {@link GroupCmdLineOption} whose sub-options are in question
    *           of being relevant to the given {@link CmdLineAction}
    * @return The {@link GroupCmdLineOption}'s sub-options who are relevant to
    *         given {@link CmdLineAction}
    */
   public static Set<CmdLineOption> determineRelevantSubOptions(
         CmdLineAction action, GroupCmdLineOption option) {
      Set<CmdLineOption> relevantOptions = Sets.newHashSet();
      for (GroupSubOption subOption : option.getSubOptions()) {
         if (subOption.isRequired()
               || isRequired(action, subOption.getOption())
               || isOptional(action, subOption.getOption())) {
            relevantOptions.add(subOption.getOption());
         }
      }
      return relevantOptions;
   }

   /**
    * Determines the sub-options of given {@link GroupCmdLineOption} who are
    * always required.
    * 
    * @param option
    *           The {@link GroupCmdLineOption} whose sub-options are check where
    *           they are required
    * @return The required sub-options of given {@link GroupCmdLineOption}
    */
   public static Set<CmdLineOption> determineRequiredSubOptions(
         GroupCmdLineOption option) {
      Validate.notNull(option);

      Set<CmdLineOption> requiredOptions = Sets.newHashSet();
      for (GroupSubOption subOption : option.getSubOptions()) {
         if (subOption.isRequired()) {
            requiredOptions.add(subOption.getOption());
         }
      }
      return requiredOptions;
   }

   /**
    * Determines the sub-options of given {@link GroupCmdLineOption} who are
    * either always required for become requried when given
    * {@link CmdLineAction} is specified.
    * 
    * @param action
    *           The specified {@link CmdLineAction}
    * @param option
    *           The {@link GroupCmdLineOption} whose sub-options are checked if
    *           they are required
    * @return The given {@link GroupCmdLineOption}'s required sub-options
    */
   public static Set<CmdLineOption> determineRequiredSubOptions(
         CmdLineAction action, GroupCmdLineOption option) {
      Validate.notNull(action);
      Validate.notNull(option);

      Set<CmdLineOption> requiredOptions = Sets.newHashSet();
      for (GroupSubOption subOption : option.getSubOptions()) {
         if (subOption.isRequired()
               || isRequired(action, subOption.getOption())) {
            requiredOptions.add(subOption.getOption());
         }
      }
      return requiredOptions;
   }

   /**
    * Determines if the given {@link CmdLineOption} is required because the
    * given {@link CmdLineAction} was specified.
    * 
    * @param action
    *           The {@link CmdLineAction} which was specified.
    * @param option
    *           The {@link CmdLineOption} in question of being required or not.
    * @return True is option is required, false otherwise.
    */
   public static boolean isRequired(CmdLineAction action, CmdLineOption option) {
      Validate.notNull(option);
      Validate.notNull(action);

      for (RequirementRule requirementRule : option.getRequirementRules()) {
         if (requirementRule.getRelation(action) == Relation.REQUIRED) {
            return true;
         }
      }
      return false;
   }

   /**
    * Determines which of the given {@link CmdLineOption}s are optional because
    * the given {@link CmdLineAction} was specified.
    * 
    * @param action
    *           The {@link CmdLineAction} which was specified.
    * @param options
    *           The {@link CmdLineOption} in question of being optional or not.
    * @return The {@link Set} of {@link CmdLineOption}s where are optional
    *         because the given {@link CmdLineAction} was specified.
    */
   public static Set<CmdLineOption> determineOptional(CmdLineAction action,
         Set<CmdLineOption> options) {
      Validate.notNull(action);
      Validate.notNull(options);

      Set<CmdLineOption> optionalOptions = Sets.newHashSet();
      for (CmdLineOption option : options) {
         if (isOptional(action, option)) {
            optionalOptions.add(option);
         }
      }
      return optionalOptions;
   }

   /**
    * Determines if the given {@link CmdLineOption} is optional because the
    * given {@link CmdLineAction} was specified.
    * 
    * @param action
    *           The {@link CmdLineAction} which was specified.
    * @param option
    *           The {@link CmdLineOption} in question of being optional or not.
    * @return True is option is optional, false otherwise.
    */
   public static boolean isOptional(CmdLineAction action, CmdLineOption option) {
      Validate.notNull(action);
      Validate.notNull(option);

      for (RequirementRule requirementRule : option.getRequirementRules()) {
         if (requirementRule.getRelation(action) == Relation.OPTIONAL) {
            return true;
         }
      }
      return false;
   }

   /**
    * Get {@link CmdLineOption}s which are always required regardless of
    * {@link CmdLineAction} specified. NOTE: Ignores {@link CmdLineOption}s of
    * type {@link ActionCmdLineOption}.
    * 
    * @param options
    *           The {@link CmdLineOption}S to check for required
    *           {@link CmdLineOption}s
    * @return The {@link CmdLineOption}s which will be check for always required
    *         {@link CmdLineOption}s
    */
   public static Set<CmdLineOption> getRequiredOptions(
         Set<CmdLineOption> options) {
      Validate.notNull(options);

      return getRequiredOptions(options, true);
   }

   /**
    * Get {@link CmdLineOption}s which are always required regardless of
    * {@link CmdLineAction} specified.
    * 
    * @param options
    *           The {@link CmdLineOption}S to check for required
    *           {@link CmdLineOption}s
    * @param ignoreActionOption
    *           Where or not to ignore {@link CmdLineOption}s of type
    *           {@link ActionCmdLineOption}
    * @return The {@link CmdLineOption}s which will be check for always required
    *         {@link CmdLineOption}s
    */
   public static Set<CmdLineOption> getRequiredOptions(
         Set<CmdLineOption> options, boolean ignoreActionOption) {
      Validate.notNull(options);

      Set<CmdLineOption> requiredOptions = Sets.newHashSet();
      for (CmdLineOption option : options) {
         if (option.isRequired()
               && !(isActionOption(option) && ignoreActionOption)) {
            requiredOptions.add(option);
         }
      }
      return requiredOptions;
   }

   /**
    * Sorts {@link CmdLineOption}s by requirement levels. {@link CmdLineOption}s
    * which are always required have highest sort score, followed by
    * {@link CmdLineOption}s which have {@link RequirementRule}s, followed by
    * all others.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOption}s to sort.
    * @return The {@link CmdLineOption}s sorted by requirement.
    */
   public static List<CmdLineOption> sortOptionsByRequiredStatus(
         Set<CmdLineOption> options) {
      Validate.notNull(options);

      List<CmdLineOption> optionsList = Lists.newArrayList(options);
      Collections.sort(optionsList, new Comparator<CmdLineOption>() {
         public int compare(CmdLineOption option1, CmdLineOption option2) {
            int thisScore = (option1.isRequired() ? 2 : 0)
                  + (!option1.getRequirementRules().isEmpty() ? 1 : 0);
            int compareScore = (option2.isRequired() ? 2 : 0)
                  + (!option2.getRequirementRules().isEmpty() ? 1 : 0);
            return new Integer(thisScore).compareTo(compareScore);
         }
      });
      Collections.reverse(optionsList);
      return optionsList;
   }

   /**
    * Finds {@link CmdLineOption} whose short name or long name equals given
    * option name.
    * 
    * @param optionName
    *           The short or long name of the {@link CmdLineOption} to find
    * @param options
    *           The {@link CmdLineOption}s to search in
    * @return The {@link CmdLineOption} found or null if not found.
    */
   public static CmdLineOption getOptionByName(String optionName,
         Set<CmdLineOption> options) {
      Validate.notNull(optionName);
      Validate.notNull(options);

      for (CmdLineOption option : options) {
         if (option.getLongOption().equals(optionName)
               || option.getShortOption().equals(optionName)) {
            return option;
         } else if (isGroupOption(option)) {
            Set<CmdLineOption> subOptions = Sets.newHashSet(); 
            for (GroupSubOption subOption : asGroupOption(option).getSubOptions()) {
               subOptions.add(subOption.getOption());
            }
            CmdLineOption foundOption = getOptionByName(optionName, subOptions);
            if (foundOption != null) {
               return foundOption;
            }
         }
      }
      return null;
   }

   /**
    * Finds {@link CmdLineOptionInstance} whose {@link CmdLineOption}'s short
    * name or long name equals given option name.
    * 
    * @param optionName
    *           The short or long name of the {@link CmdLineOptionInstance}'s
    *           {@link CmdLineOption} to find
    * @param optionInsts
    *           The {@link CmdLineOptionIntance}s to search in
    * @return The {@link CmdLineOptionInstance} found or null if not found.
    */
   public static CmdLineOptionInstance getOptionInstanceByName(
         String optionName, Set<CmdLineOptionInstance> optionInsts) {
      Validate.notNull(optionName);
      Validate.notNull(optionInsts);

      for (CmdLineOptionInstance optionInst : optionInsts)
         if (optionInst.getOption().getLongOption().equals(optionName)
               || optionInst.getOption().getShortOption().equals(optionName))
            return optionInst;
      return null;
   }

   /**
    * Checks if given {@link CmdLineOption} is a sub-option of other given
    * {@link CmdLineOption}.
    * 
    * @param option
    *           {@link CmdLineOption} which is being checked if the other given
    *           {@link CmdLineOption} is one of its sub-options
    * @param subOption
    *           The {@link CmdLineOption} which is being check if it is a
    *           sub-option of the other given {@link CmdLineOption}
    * @return True if subOption is a sub-option of option
    */
   public static boolean isSubOption(CmdLineOption option,
         CmdLineOption subOption) {
      Validate.notNull(option);
      Validate.notNull(subOption);

      if (isGroupOption(option)) {
         for (GroupSubOption curSubOption : asGroupOption(option)
               .getSubOptions()) {
            if (curSubOption.getOption().equals(subOption)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Finds all the perform-and-quit type {@link CmdLineOptionInstance}s from
    * the given {@link Set} of {@link CmdLineOptionInstance}s.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOptionInstance} who will be
    *           check for perform-and-quit {@link CmdLineOptionInstance}s
    * @return The perform-and-quit {@link CmdLineOptionInstance}s found
    */
   public static Set<CmdLineOptionInstance> findPerformAndQuitOptions(
         Set<CmdLineOptionInstance> options) {
      Validate.notNull(options);

      Set<CmdLineOptionInstance> performAndQuitOptions = Sets.newHashSet();
      for (CmdLineOptionInstance option : options) {
         if (isPerformAndQuitOption(option.getOption())) {
            performAndQuitOptions.add(option);
         }
      }
      return performAndQuitOptions;
   }

   /**
    * Checks if the given {@link CmdLineOption} is a perform-and-quit type
    * {@link CmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} being checked if it is
    *           perform-and-quit type.
    * @return True if {@link CmdLineOption} is a perform-and-quit type, false
    *         otherwise.
    */
   public static boolean isPerformAndQuitOption(CmdLineOption option) {
      Validate.notNull(option);

      if (isSimpleOption(option)) {
         return asSimpleOption(option).isPerformAndQuit();
      }
      return false;
   }

   /**
    * Finds the first {@link CmdLineOptionInstance} whose {@link CmdLineOption}
    * is the given {@link CmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to find.
    * @param specifiedOptions
    *           The {@link CmdLineOptionInstance} whose {@link CmdLineOption} is
    *           the given {@link CmdLineOption}
    * @return The first {@link CmdLineOptionInstance} whose
    *         {@link CmdLineOption} is the given {@link CmdLineOption}, or null
    *         if not found
    */
   public static CmdLineOptionInstance findSpecifiedOption(
         CmdLineOption option, Set<CmdLineOptionInstance> specifiedOptions) {
      Validate.notNull(option);
      Validate.notNull(specifiedOptions);

      for (CmdLineOptionInstance specifiedOption : specifiedOptions) {
         if (specifiedOption.getOption().equals(option)) {
            return specifiedOption;
         }
      }
      return null;
   }

   /**
    * Finds the {@link CmdLineOptionInstance}s whose {@link CmdLineOption} is
    * the given {@link CmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to find.
    * @param specifiedOptions
    *           The {@link CmdLineOptionInstance} whose {@link CmdLineOption} is
    *           the given {@link CmdLineOption}
    * @return The {@link CmdLineOptionInstance}s whose {@link CmdLineOption} is
    *         the given {@link CmdLineOption}
    */
   public static Set<CmdLineOptionInstance> findAllOfSpecifiedOption(
         CmdLineOption option, Set<CmdLineOptionInstance> specifiedOptions) {
      Validate.notNull(option);
      Validate.notNull(specifiedOptions);

      Set<CmdLineOptionInstance> options = Sets.newHashSet();
      for (CmdLineOptionInstance specifiedOption : specifiedOptions) {
         if (specifiedOption.getOption().equals(option)) {
            options.add(specifiedOption);
         }
      }
      return options;
   }

   /**
    * Checks if {@link CmdLineOption} is a {@link PrintSupportedActionsOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} in question
    * @return True is {@link CmdLineOption} is a
    *         {@link PrintSupportedActionsOption}, false otherwise
    */
   public static boolean isPrintSupportedActionsOption(CmdLineOption option) {
      Validate.notNull(option);

      return option instanceof PrintSupportedActionsCmdLineOption;
   }

   /**
    * Finds {@link CmdLineOption} of type
    * {@link PrintSupportedActionsCmdLineOption}.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOption} to find a
    *           {@link PrintSupportedActionsCmdLineOption} in
    * @return The found {@link PrintSupportedActionsCmdLineOption}, or null if
    *         not found.
    */
   public static PrintSupportedActionsCmdLineOption findPrintSupportedActionsOption(
         Set<CmdLineOption> options) {
      Validate.notNull(options);

      for (CmdLineOption option : options) {
         if (isPrintSupportedActionsOption(option)) {
            return (PrintSupportedActionsCmdLineOption) option;
         }
      }
      return null;
   }

   /**
    * Finds {@link CmdLineOptionInstance} whose {@link CmdLineOption} of type
    * {@link PrintSupportedActionsCmdLineOption}.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOptionInstance} to find the
    *           {@link CmdLineOptionInstance} whose {@link CmdLineOption} is of
    *           type {@link PrintSupportedActionsCmdLineOption} in
    * @throws IllegalArgumentException
    *            If more than one print supported actions option is specified
    * @return The found {@link CmdLineOptionInstance} whose
    *         {@link CmdLineOption} is of type
    *         {@link PrintSupportedActionsCmdLineOption}, or null if not found.
    */
   public static CmdLineOptionInstance findSpecifiedPrintSupportedActionsOption(
         Set<CmdLineOptionInstance> options) {
      Validate.notNull(options);

      CmdLineOptionInstance specifiedPsa = null;
      for (CmdLineOptionInstance option : options) {
         if (isPrintSupportedActionsOption(option.getOption())) {
            if (specifiedPsa != null) {
               throw new IllegalArgumentException(
                     "Only on print supported actions option can be specified!");
            }
            specifiedPsa = option;
         }
      }
      return specifiedPsa;
   }

   /**
    * Checks if {@link CmdLineOption} is a {@link SimpleCmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} checked if it is a
    *           {@link SimpleCmdLineOption}
    * @return True if {@link CmdLineOption} is a {@link SimpleCmdLineOption},
    *         false otherwise
    */
   public static boolean isSimpleOption(CmdLineOption option) {
      Validate.notNull(option);

      return option instanceof SimpleCmdLineOption;
   }

   /**
    * Casts the {@link CmdLineOption} to a {@link SimpleCmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to cast as a
    *           {@link SimpleCmdLineOption}
    * @return The casted {@link CmdLineOption}
    */
   public static SimpleCmdLineOption asSimpleOption(CmdLineOption option) {
      Validate.isTrue(isSimpleOption(option));

      return (SimpleCmdLineOption) option;
   }

   /**
    * Checks if {@link CmdLineOption} is a {@link ActionCmdLineOption}.
    * 
    * @param option
    *           {@link CmdLineOption} check if a {@link ActionCmdLineOption}
    * @return True if {@link CmdLineOption} is a {@link ActionCmdLineOption},
    *         false otherwise
    */
   public static boolean isActionOption(CmdLineOption option) {
      Validate.notNull(option);

      return option instanceof ActionCmdLineOption;
   }

   /**
    * Find {@link ActionCmdLineOption} in {@link Set} of {@link CmdLineOption}s.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOption} to search through for a
    *           {@link ActionCmdLineOption}
    * @return The found {@link ActionCmdLineOption}, or null if not found
    */
   public static ActionCmdLineOption findFirstActionOption(
         Set<CmdLineOption> options) {
      Validate.notNull(options);

      for (CmdLineOption option : options) {
         if (isActionOption(option)) {
            return (ActionCmdLineOption) option;
         } else if (isGroupOption(option)) {
            GroupCmdLineOption groupOption = (GroupCmdLineOption) option;
            Set<CmdLineOption> subOptions = Sets.newHashSet();
            for (GroupSubOption subOption : groupOption.getSubOptions()) {
               subOptions.add(subOption.getOption());
            }
            ActionCmdLineOption foundOption = findFirstActionOption(subOptions);
            if (foundOption != null) {
               return foundOption;
            }
         }
      }
      return null;
   }

   /**
    * Finds all {@link CmdLineOption}s that are {@link ActionCmdLineOption}s.
    * 
    * @param options
    *           The {@link CmdLineOption}s to look through for
    *           {@link ActionCmdLineOption}s
    * @return All {@link ActionCmdLineOption}s found
    */
   public static List<ActionCmdLineOption> findActionOptions(
         Set<CmdLineOption> options) {
      Validate.notNull(options);

      List<ActionCmdLineOption> actionOptions = Lists.newArrayList();
      for (CmdLineOption option : options) {
         if (isActionOption(option)) {
            actionOptions.add((ActionCmdLineOption) option);
         } else if (isGroupOption(option)) {
            GroupCmdLineOption groupOption = (GroupCmdLineOption) option;
            Set<CmdLineOption> subOptions = Sets.newHashSet();
            for (GroupSubOption subOption : groupOption.getSubOptions()) {
               subOptions.add(subOption.getOption());
            }
            actionOptions.addAll(findActionOptions(subOptions));
         }
      }
      return actionOptions;
   }

   /**
    * Find {@link ActionCmdLineOption} in {@link Set} of {@link CmdLineOption}s.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOption} to search through for a
    *           {@link ActionCmdLineOption}
    * @throws IllegalArgumentException
    *            If more than one action option has be specified
    * @return The found {@link ActionCmdLineOption}, or null if not found
    */
   public static CmdLineOptionInstance findSpecifiedActionOption(
         Set<CmdLineOptionInstance> options) {
      Validate.notNull(options);

      CmdLineOptionInstance specifiedAction = null;
      for (CmdLineOptionInstance option : options) {
         if (option.isAction()) {
            if (specifiedAction != null) {
               throw new IllegalArgumentException(
                     "Only one action may be specified!");
            }
            specifiedAction = option;
         } else if (option.isGroup()) {
            CmdLineOptionInstance foundAction = findSpecifiedActionOption(option
                  .getSubOptions());
            if (specifiedAction != null && foundAction != null) {
               throw new IllegalArgumentException(
                  "Only one action may be specified!");
            } else if (foundAction != null) {
               specifiedAction = foundAction;
            }
         }
      }
      return specifiedAction;
   }

   /**
    * Checks if {@link CmdLineOption} is a {@link GroupCmdLineOption}.
    * 
    * @param option
    *           {@link CmdLineOption} in question
    * @return True if {@link CmdLineOption} is a {@link GroupCmdLineOption},
    *         false otherwise
    */
   public static boolean isGroupOption(CmdLineOption option) {
      Validate.notNull(option);

      return option instanceof GroupCmdLineOption;
   }

   /**
    * Cast {@link CmdLineOption} to a {@link GroupCmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to be cast as a
    *           {@link GroupCmdLineOption}
    * @return A {@link GroupCmdLineOption}
    */
   public static GroupCmdLineOption asGroupOption(CmdLineOption option) {
      Validate.isTrue(isGroupOption(option));

      return (GroupCmdLineOption) option;
   }

   /**
    * Checks if {@link CmdLineOption} is a {@link HelpCmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} that is checked if it is a
    *           {@link HelpCmdLineOption}
    * @return True if {@link CmdLineOption} is a {@link HelpCmdLineOption},
    *         otherwise false
    */
   public static boolean isHelpOption(CmdLineOption option) {
      Validate.notNull(option);

      return option instanceof HelpCmdLineOption;
   }

   /**
    * Casts {@link CmdLineOption} to a {@link HelpCmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to be cast as a
    *           {@link HelpCmdLineOption}
    * @return A {@link HelpCmdLineOption}
    */
   public static HelpCmdLineOption asHelpOption(CmdLineOption option) {
      Validate.isTrue(isHelpOption(option));

      return (HelpCmdLineOption) option;
   }

   /**
    * Finds the {@link HelpCmdLineOption} in the {@link Set} of
    * {@link CmdLineOption}s.
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOption}s to search through for
    *           {@link HelpCmdLineOption}
    * @return The found {@link HelpCmdLineOption}, null if not found
    */
   public static HelpCmdLineOption findHelpOption(Set<CmdLineOption> options) {
      Validate.notNull(options);

      for (CmdLineOption option : options) {
         if (isHelpOption(option)) {
            return (HelpCmdLineOption) option;
         }
      }
      return null;
   }

   /**
    * Finds the {@link CmdLineOptionInstance} whose {@link CmdLineOption} is of
    * type {@link HelpCmdLineOption}
    * 
    * @param options
    *           The {@link Set} of {@link CmdLineOptionInstance}s to search
    *           through for the {@link CmdLineOptionInstance} whose
    *           {@link CmdLineOption} is of type {@link HelpCmdLineOption}
    * @throws IllegalArgumentException
    *            If more than one help option is specified
    * @return The found {@link CmdLineOptionInstance}, null if not found
    */
   public static CmdLineOptionInstance findSpecifiedHelpOption(
         Set<CmdLineOptionInstance> options) {
      Validate.notNull(options);

      CmdLineOptionInstance specifiedHelp = null;
      for (CmdLineOptionInstance option : options) {
         if (isHelpOption(option.getOption())) {
            if (specifiedHelp != null) {
               throw new IllegalArgumentException(
                     "Help can only be specified once!");
            }
            specifiedHelp = option;
         }
      }
      return specifiedHelp;
   }

   /**
    * Finds the {@link CmdLineAction} specified by the given
    * {@link CmdLineOptionInstance}.
    * 
    * @param actionOption
    *           The {@link CmdLineOptionInstance} which specifies an
    *           {@link ActionCmdLineOption}
    * @param supportedActions
    *           {@link Set} of supported {@link CmdLineAction}s
    * @return The found {@link CmdLineAction}, null if not found
    */
   public static CmdLineAction findAction(CmdLineOptionInstance actionOption,
         Set<CmdLineAction> supportedActions) {
      Validate.isTrue(actionOption.isAction());
      Validate.notEmpty(actionOption.getValues());
      Validate.notNull(supportedActions);

      return findAction(actionOption.getValues().get(0), supportedActions);
   }

   /**
    * Finds a {@link CmdLineAction} by its name.
    * 
    * @param actionName
    *           Name of {@link CmdLineAction} to find
    * @param supportedActions
    *           {@link Set} of supported {@link CmdLineAction}s
    * @return Found {@link CmdLineAction} or null if not found
    */
   public static CmdLineAction findAction(String actionName,
         Set<CmdLineAction> supportedActions) {
      Validate.notNull(actionName);
      Validate.notNull(supportedActions);

      for (CmdLineAction action : supportedActions) {
         if (action.getName().equals(actionName)) {
            return action;
         }
      }
      return null;
   }

   /**
    * Validates the given {@link CmdLineOptionInstance}.
    * 
    * @param option
    *           The {@link CmdLineOptionInstance} to be validated
    * @return {@link CmdLineOptionValidator.Result}s from running
    *         {@link CmdLineOptionValidator}s.
    */
   public static List<CmdLineOptionValidator.Result> validate(
         CmdLineOptionInstance option) {
      Validate.notNull(option);

      List<CmdLineOptionValidator.Result> results = Lists.newArrayList();
      if (option.isValidatable()) {
         for (CmdLineOptionValidator validator : ((ValidatableCmdLineOption) option
               .getOption()).getValidators()) {
            results.add(validator.validate(option));
         }
      }

      if (option.isGroup()) {
         for (CmdLineOptionInstance subOption : option.getSubOptions()) {
            results.addAll(validate(subOption));
         }
      }
      return results;
   }

   /**
    * Finds the {@link CmdLineOptionValidator.Result}s with grade of FAIL.
    * 
    * @param results
    *           The {@link CmdLineOptionValidator.Result}s which are checked if
    *           they have a failing grade.
    * @return {@link CmdLineOptionValidator.Result}s with grade of FAIL.
    */
   public static List<CmdLineOptionValidator.Result> determineFailedValidation(
         List<CmdLineOptionValidator.Result> results) {
      List<CmdLineOptionValidator.Result> failedResults = Lists.newArrayList();
      for (CmdLineOptionValidator.Result result : results) {
         if (result.getGrade().equals(CmdLineOptionValidator.Result.Grade.FAIL)) {
            failedResults.add(result);
         }
      }
      return failedResults;
   }

   /**
    * Runs the {@link CmdLineOptionInstance}'s handler against the given
    * {@link CmdLineAction}.
    * 
    * @param action
    *           The {@link CmdLineAction} which the given
    *           {@link CmdLineOptionInstance}'s handler will be run against
    * @param option
    *           The {@link CmdLineOptionInstance} whose handler will be run
    *           against the given {@link CmdLineAction}
    */
   public static void handle(CmdLineAction action, CmdLineOptionInstance option) {
      Validate.notNull(action);
      Validate.notNull(option);

      if (option.isHandleable()
            && ((HandleableCmdLineOption) option.getOption()).getHandler() != null) {
         ((HandleableCmdLineOption) option.getOption()).getHandler()
               .handleOption(action, option);
      }

      if (option.isGroup()) {
         for (CmdLineOptionInstance subOption : option.getSubOptions()) {
            handle(action, subOption);
         }
      }     
   }

   /**
    * Formats given string to a string where txt starts at startIndex and wraps
    * around at endIndex, all other indexes are filled with empty space.
    */
   public static String getFormattedString(String string, int startIndex,
         int endIndex) {
      StringBuffer outputString = new StringBuffer("");
      String[] splitStrings = StringUtils.split(string, " ");
      StringBuffer curLine = null;
      for (int i = 0; i < splitStrings.length; i++) {
         curLine = new StringBuffer("");
         curLine.append(splitStrings[i] + " ");

         for (; i + 1 < splitStrings.length
               && curLine.length() + splitStrings[i + 1].length() <= (endIndex - startIndex); i++)
            curLine.append(splitStrings[i + 1] + " ");

         outputString.append(StringUtils.repeat(" ", startIndex)
               + curLine.toString() + "\n");
      }
      return outputString.toString();
   }

   public static List<?> convertToType(List<String> values, Class<?> type)
         throws MalformedURLException, ClassNotFoundException,
         InstantiationException, IllegalAccessException {
      if (type.equals(File.class)) {
         List<Object> files = new LinkedList<Object>();
         for (String value : values)
            files.add(new File(value));
         return files;
      } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
         List<Object> booleans = new LinkedList<Object>();
         for (String value : values)
            booleans.add(value.toLowerCase().trim().equals("true"));
         return booleans;
      } else if (type.equals(URL.class)) {
         List<Object> urls = new LinkedList<Object>();
         for (String value : values)
            urls.add(new URL(value));
         return urls;
      } else if (type.equals(Class.class)) {
         List<Object> classes = new LinkedList<Object>();
         for (String value : values)
            classes.add(Class.forName(value));
         return classes;
      } else if (type.equals(List.class)) {
         List<Object> lists = new LinkedList<Object>();
         lists.add(values);
         return lists;
      } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
         List<Object> ints = new LinkedList<Object>();
         for (String value : values)
            ints.add(new Integer(value));
         return ints;
      } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
         List<Object> longs = new LinkedList<Object>();
         for (String value : values)
            longs.add(new Long(value));
         return longs;
      } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
         List<Object> doubles = new LinkedList<Object>();
         for (String value : values)
            doubles.add(new Double(value));
         return doubles;
      } else if (type.equals(String.class)) {
         StringBuffer combinedString = new StringBuffer("");
         for (String value : values)
            combinedString.append(value + " ");
         return Lists.newArrayList(combinedString.toString().trim());
      } else {
         List<Object> objects = new LinkedList<Object>();
         for (String value : values) {
            Object object = Class.forName(value).newInstance();
            if (!type.isAssignableFrom(object.getClass())) {
               throw new RuntimeException(object.getClass() + " is not a valid"
                     + " type or sub-type of " + type);
            }
            objects.add(object);
         }
         return objects;
      }
   }
}

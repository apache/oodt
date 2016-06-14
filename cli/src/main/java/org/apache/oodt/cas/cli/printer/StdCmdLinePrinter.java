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
package org.apache.oodt.cas.cli.printer;


import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.*;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator.Result;

import java.util.List;
import java.util.Set;

import static org.apache.oodt.cas.cli.util.CmdLineUtils.*;


/**
 * Standard {@link CmdLinePrinter}.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLinePrinter implements CmdLinePrinter {

   @Override
   public String printActionHelp(CmdLineAction action,
         Set<CmdLineOption> options) {
      return "" + getHeader(action) + "\n" + getDescription(action) + "\n" + getUsage(action, options) + "\n"
             + getExamples(action) + "\n" + getFooter(action) + "\n";
   }

   protected String getHeader(CmdLineAction action) {
      return "** Action Help for '" + action.getName() + "' **";
   }

   protected String getDescription(CmdLineAction action) {
      StringBuilder sb = new StringBuilder("> DESCRIPTION:\n");
      if (action.getDetailedDescription() != null) {
         sb.append(" ").append(action.getDetailedDescription()
               .replaceAll("^\\s*", "").replaceAll("\\s*$", ""));
      } else if (action.getDescription() != null) {
         sb.append(" ").append(
               action.getDescription().replaceAll("^\\s*", "")
                     .replaceAll("\\s*$", ""));
      } else {
         sb.append(" - N/A");
      }
      return sb.append("\n").toString();
   }

   protected String getUsage(CmdLineAction action, Set<CmdLineOption> options) {
      StringBuilder sb = new StringBuilder("> USAGE:\n");
      sb.append(getRequiredSubHeader()).append("\n");
      Set<CmdLineOption> requiredOptions = determineRequired(action, options);
      List<CmdLineOption> sortedRequiredOptions = sortOptions(requiredOptions);
      for (CmdLineOption option : sortedRequiredOptions) {
         sb.append(getRequiredOptionHelp(action, option)).append("\n");
      }

      sb.append(getOptionalSubHeader()).append("\n");
      Set<CmdLineOption> optionalOptions = determineOptional(action, options);
      List<CmdLineOption> sortedOptionalOptions = sortOptions(optionalOptions);
      for (CmdLineOption option : sortedOptionalOptions) {
         sb.append(getOptionalOptionHelp(action, option)).append("\n");
      }
      return sb.toString();
   }

   protected String getRequiredSubHeader() {
      return " Required:";
   }

   protected String getRequiredOptionHelp(CmdLineAction action,
         CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "   ");
      } else {
         return getOptionHelp(action, option, "   ");
      }
   }

   protected String getOptionalSubHeader() {
      return " Optional:";
   }

   protected String getOptionalOptionHelp(CmdLineAction action,
         CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "   ");
      } else {
         return getOptionHelp(action, option, "   ");
      }
   }

   protected String getExamples(CmdLineAction action) {
      StringBuilder sb = new StringBuilder("> EXAMPLES:\n");
      if (action.getExamples() != null) {
         sb.append(" ").append(action.getExamples().replaceAll("^\\s*", "")
               .replaceAll("\\s*$", ""));
      } else {
         sb.append(" - N/A");
      }
      return sb.toString();
   }

   protected String getFooter(CmdLineAction action) {
      return "";
   }

   protected String getOptionHelp(CmdLineAction action, CmdLineOption option,
         String indent) {
      String argDescription = null;
      if (option instanceof AdvancedCmdLineOption) {
         argDescription = ((AdvancedCmdLineOption) option).getHandler()
               .getArgDescription(action, option);
      }

      String argHelp;
      if (option instanceof ActionCmdLineOption && option.hasArgs()) {
         argHelp = " " + action.getName();
      } else {
         argHelp = (option.hasArgs() ? " <"
               + (argDescription != null ? argDescription : option
                     .getArgsDescription()) + ">" : "");
      }
      return indent + "-" + option.getShortOption() + " [--"
            + option.getLongOption() + "]" + argHelp;
   }

   protected String getGroupHelp(CmdLineAction action,
         GroupCmdLineOption option, String indent) {
      StringBuilder helpString = new StringBuilder(getOptionHelp(action, option, indent));
      Set<CmdLineOption> subOptions = determineRequiredSubOptions(action,
          option);
      if (subOptions.isEmpty()) {
         if (!option.getSubOptions().isEmpty()) {
            helpString.append("\n").append(indent).append("  One of:");
            for (GroupSubOption subOption : option.getSubOptions()) {
               helpString.append("\n").append(getOptionHelp(action, subOption.getOption(), "   "
                                                                                           + indent));
            }
         }
      } else {
         for (CmdLineOption subOption : determineRelevantSubOptions(action,
               option)) {
            helpString.append("\n");
            if (subOption instanceof GroupCmdLineOption) {
               helpString.append(getGroupHelp(action,
                     (GroupCmdLineOption) subOption, "  " + indent));
            } else {
               helpString.append(getOptionHelp(action, subOption, "  " + indent));
            }
            helpString.append(" ").append(subOptions.contains(subOption) ? "(required)"
                                                                         : "(optional)");
         }
      }
      return helpString.toString();
   }

   @Override
   public String printActionsHelp(Set<CmdLineAction> actions) {
      StringBuilder sb = new StringBuilder("");
      sb.append("-----------------------------------------------------------------------------------------------------------------\n");
      sb.append("|").append(StringUtils.rightPad(" Action", 35)).append("|").append(" Description\n");
      sb.append("-----------------------------------------------------------------------------------------------------------------\n");
      for (CmdLineAction action : sortActions(actions)) {
         sb.append("  ").append(StringUtils.rightPad(action.getName(), 35));
         sb.append(" ").append(action.getDescription()).append("\n\n");
      }
      sb.append("-----------------------------------------------------------------------------------------------------------------\n");
      return sb.toString();
   }

   @Override
   public String printOptionsHelp(Set<CmdLineOption> options) {
      StringBuilder sb = new StringBuilder("");
      List<CmdLineOption> sortedOptions = sortOptionsByRequiredStatus(options);
      sb.append(getHeader()).append("\n");
      for (CmdLineOption option : sortedOptions) {
         sb.append(getOptionHelp(option, "")).append("\n");
      }
      sb.append(getFooter()).append("\n");
      return sb.toString();
   }

   protected String getHeader() {
      return ""
             + "-----------------------------------------------------------------------------------------------------------------\n"
             + "|" + StringUtils.rightPad(" Short", 7) + "|"
             + StringUtils.rightPad(" Long", 50) + "| Description\n"
             + "-----------------------------------------------------------------------------------------------------------------\n";
   }

   protected String getOptionHelp(CmdLineOption option, String indent) {
      String argName = option.hasArgs() ? " <" + option.getArgsDescription()
            + ">" : "";
      String optionUsage = indent
            + "-"
            + StringUtils.rightPad(option.getShortOption() + ",", 7)
            + "--"
            + StringUtils.rightPad((option.getLongOption() + argName),
                  49 - indent.length()) + option.getDescription();

      optionUsage = " " + optionUsage;

      if (!option.getRequirementRules().isEmpty()) {
         optionUsage += "\n"
               + getFormattedString("Requirement Rules:", 62, 113)
               + getFormattedString(option.getRequirementRules().toString(),
                     63, 113);
      }

      if (option instanceof AdvancedCmdLineOption) {
         if (((AdvancedCmdLineOption) option).hasHandler()) {
            String handlerHelp = ((AdvancedCmdLineOption) option).getHandler()
                  .getHelp(option);
            if (handlerHelp != null) {
               optionUsage += "\n"
                     + getFormattedString("Handler:", 62, 113)
                     + getFormattedString(handlerHelp, 63, 113);
            }
         }
      } else if (isGroupOption(option)) {
         GroupCmdLineOption groupOption = asGroupOption(option);
         optionUsage += "\n";
         optionUsage += "   SubOptions:\n";
         optionUsage += "   > Required:\n";

         List<CmdLineOption> optionalOptions = Lists.newArrayList();
         for (GroupSubOption subOption : groupOption.getSubOptions()) {
            if (subOption.isRequired()) {
               optionUsage += getOptionHelp(subOption.getOption(), "     ");
            } else {
               optionalOptions.add(subOption.getOption());
            }
         }
         optionUsage += "   > Optional:\n";
         for (CmdLineOption optionalOption : optionalOptions) {
            optionUsage += getOptionHelp(optionalOption, "     ");
         }
      }

      return optionUsage;
   }

   protected String getFooter() {
      return "-----------------------------------------------------------------------------------------------------------------";
   }

   @Override
   public String printOptionValidationErrors(List<Result> results) {
      StringBuilder sb = new StringBuilder("Validation Failures:");
      for (Result result : results) {
         sb.append(" - ").append(result.getMessage()).append("\n");
      }
      return sb.toString();
   }

   @Override
   public String printRequiredOptionsMissingError(
         Set<CmdLineOption> missingOptions) {
      StringBuilder sb = new StringBuilder("Missing required options:\n");
      for (CmdLineOption option : missingOptions) {
         sb.append(" - ").append(option.toString()).append("\n");
      }
      return sb.toString();
   }

   @Override
   public String printActionMessages(List<String> messages) {
      StringBuilder sb = new StringBuilder("");
      for (String message : messages) {
         sb.append(message);
      }
      return sb.toString();
   }
}

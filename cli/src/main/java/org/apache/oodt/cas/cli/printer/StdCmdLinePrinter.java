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

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.asGroupOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineOptional;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineRequiredSubOptions;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getFormattedString;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.isGroupOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.List;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupSubOption;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator.Result;
import org.apache.oodt.cas.cli.util.CmdLineUtils;

import com.google.common.collect.Lists;

/**
 * Standard {@link CmdLinePrinter}.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLinePrinter implements CmdLinePrinter {

   @Override
   public String printActionHelp(CmdLineAction action,
         Set<CmdLineOption> options) {
      StringBuffer sb = new StringBuffer("");
      sb.append(getHeader(action)).append("\n");

      sb.append(getRequiredSubHeader()).append("\n");
      Set<CmdLineOption> requiredOptions = determineRequired(action, options);
      List<CmdLineOption> sortedRequiredOptions = sortOptionsByRequiredStatus(requiredOptions);
      for (CmdLineOption option : sortedRequiredOptions) {
         sb.append(getRequiredOptionHelp(action, option)).append("\n");
      }

      sb.append(getOptionalSubHeader()).append("\n");
      Set<CmdLineOption> optionalOptions = determineOptional(action, options);
      List<CmdLineOption> sortedOptionalOptions = sortOptionsByRequiredStatus(optionalOptions);
      for (CmdLineOption option : sortedOptionalOptions) {
         sb.append(getOptionalOptionHelp(action, option)).append("\n");
      }

      sb.append(getFooter(action)).append("\n");
      return sb.toString();
   }

   protected String getHeader(CmdLineAction action) {
      return "Action Help for '" + action.getName() + "'";
   }

   protected String getRequiredSubHeader() {
      return " - Required:";
   }

   protected String getRequiredOptionHelp(CmdLineAction action,
         CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "    ");
      } else {
         return getOptionHelp(action, option, "    ");
      }
   }

   protected String getOptionalSubHeader() {
      return " - Optional:";
   }

   protected String getOptionalOptionHelp(CmdLineAction action,
         CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "    ");
      } else {
         return getOptionHelp(action, option, "    ");
      }
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

      String argHelp = null;
      if (option instanceof ActionCmdLineOption && option.hasArgs()) {
         argHelp = action.getName();
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
      String helpString = getOptionHelp(action, option, indent) + "\n";
      Set<CmdLineOption> subOptions = determineRequiredSubOptions(action,
            (GroupCmdLineOption) option);
      if (subOptions.isEmpty()) {
         if (!option.getSubOptions().isEmpty()) {
            helpString += indent + "  One of:";
            for (GroupSubOption subOption : option.getSubOptions()) {
               helpString += "\n"
                     + getOptionHelp(action, subOption.getOption(), "   "
                           + indent);
            }
         }
      } else {
         for (CmdLineOption subOption : subOptions) {
            if (subOption instanceof GroupCmdLineOption) {
               helpString += getGroupHelp(action,
                     (GroupCmdLineOption) subOption, "  " + indent);
            } else {
               helpString += getOptionHelp(action, subOption, "  " + indent);
            }
         }
      }
      return helpString;
   }

   @Override
   public String printActionsHelp(Set<CmdLineAction> actions) {
      StringBuffer sb = new StringBuffer("");
      sb.append("Actions:").append("\n");
      for (CmdLineAction action : actions) {
         sb.append("  Action:").append("\n");
         sb.append("    Name: ").append(action.getName()).append("\n");
         sb.append("    Description: ").append(action.getDescription())
               .append("\n").append("\n");
      }
      return sb.toString();
   }

   @Override
   public String printOptionsHelp(Set<CmdLineOption> options) {
      StringBuffer sb = new StringBuffer("");
      List<CmdLineOption> sortedOptions = sortOptionsByRequiredStatus(options);
      sb.append(getHeader()).append("\n");
      for (CmdLineOption option : sortedOptions) {
         sb.append(getOptionHelp(option, "")).append("\n");
      }
      sb.append(getFooter()).append("\n");
      return sb.toString();
   }

   protected String getHeader() {
      StringBuffer sb = new StringBuffer("");
      sb.append("-----------------------------------------------------------------------------------------------------------------\n");
      sb.append("|" + StringUtils.rightPad(" Short", 7) + "|"
            + StringUtils.rightPad(" Long", 50) + "| Description\n");
      sb.append("-----------------------------------------------------------------------------------------------------------------\n");
      return sb.toString();
   }

   protected String getOptionHelp(CmdLineOption option, String indent) {
      String argName = option.hasArgs() ? " <" + option.getArgsDescription()
            + ">" : "";
      String optionUsage = indent + "-"
            + StringUtils.rightPad(option.getShortOption() + ",", 7) + "--"
            + StringUtils.rightPad((option.getLongOption() + argName), 49 - indent.length())
            + option.getDescription();

      optionUsage = " " + optionUsage;

      if (!option.getRequirementRules().isEmpty()) {
         optionUsage += "\n"
               + getFormattedString("Requirement Rules:", 62, 113)
               + getFormattedString(option.getRequirementRules().toString(),
                     63, 113);
      }

      if (option instanceof AdvancedCmdLineOption) {
         if (((AdvancedCmdLineOption) option).hasHandler()) {
            optionUsage += "\n"
                  + getFormattedString("Handler:", 62, 113)
                  + getFormattedString(((AdvancedCmdLineOption) option)
                        .getHandler().getHelp(option), 63, 113);
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
      StringBuffer sb = new StringBuffer("Validation Failures:");
      for (Result result : results) {
         sb.append(" - ").append(result.getMessage()).append("\n");
      }
      return sb.toString();
   }

   @Override
   public String printRequiredOptionsMissingError(
         Set<CmdLineOption> missingOptions) {
      StringBuffer sb = new StringBuffer("Missing required options:\n");
      for (CmdLineOption option : missingOptions) {
         sb.append(" - ").append(option.toString()).append("\n");
      }
      return sb.toString();
   }
}

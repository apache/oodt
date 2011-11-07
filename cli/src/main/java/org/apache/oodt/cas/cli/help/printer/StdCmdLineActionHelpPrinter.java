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
package org.apache.oodt.cas.cli.help.printer;

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineOptional;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineRequiredSubOptions;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.List;
import java.util.Set;
import java.util.Stack;

//OODT imports
import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupSubOption;
import org.apache.oodt.cas.cli.util.CmdLineUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Standard help printer for printing help for a {@link CmdLineAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineActionHelpPrinter implements CmdLineActionHelpPrinter {

   /**
    * {@inheritDoc}
    */
   public String printHelp(CmdLineAction action, Set<CmdLineOption> options) {
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

   protected String getRequiredOptionHelp(CmdLineAction action, CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "    ");
      } else {
         return getOptionHelp(action, option, "    ");
      }
   }

   protected String getOptionalSubHeader() {
      return " - Optional:";
   }

   protected String getOptionalOptionHelp(CmdLineAction action, CmdLineOption option) {
      if (option instanceof GroupCmdLineOption) {
         return getGroupHelp(action, (GroupCmdLineOption) option, "    ");
      } else {
         return getOptionHelp(action, option, "    ");
      }
   }

   protected String getFooter(CmdLineAction action) {
      return "";
   }

   protected String getOptionHelp(CmdLineAction action, CmdLineOption option, String indent) {
      String argDescription = null;
      if (option instanceof AdvancedCmdLineOption) {
         argDescription = ((AdvancedCmdLineOption) option).getHandler().getArgDescription(action, option);
      }

      String argHelp = null;
      if (option instanceof ActionCmdLineOption && option.hasArgs()) {
         argHelp = action.getName();
      } else {
         argHelp = (option.hasArgs() ? " <" + (argDescription != null ?
               argDescription : option.getArgsDescription()) + ">" : "");
      }
      return indent + "-" + option.getShortOption() + " [--"
            + option.getLongOption() + "]"
            + argHelp;
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
}

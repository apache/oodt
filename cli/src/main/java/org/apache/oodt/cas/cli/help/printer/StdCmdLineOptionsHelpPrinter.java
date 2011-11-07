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
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getFormattedString;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.List;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;

/**
 * Standard help printer for {@link CmdLineOption}s.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineOptionsHelpPrinter implements CmdLineOptionsHelpPrinter {

   /**
    * {@inheritDoc}
    */
   public String printHelp(Set<CmdLineOption> options) {
      StringBuffer sb = new StringBuffer("");
      List<CmdLineOption> sortedOptions = sortOptionsByRequiredStatus(options);
      sb.append(getHeader()).append("\n");
      for (CmdLineOption option : sortedOptions) {
         sb.append(getOptionHelp(option)).append("\n");
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

   protected String getOptionHelp(CmdLineOption option) {
      String argName = option.hasArgs() ? " <" + option.getArgsDescription()
            + ">" : "";
      String optionUsage = "-"
            + StringUtils.rightPad(option.getShortOption() + ",", 7) + "--"
            + StringUtils.rightPad((option.getLongOption() + argName), 49)
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
      }

      return optionUsage;
   }

   protected String getFooter() {
      return "-----------------------------------------------------------------------------------------------------------------";
   }
}

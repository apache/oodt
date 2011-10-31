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
package org.apache.oodt.cas.cl.help.printer;

//OODT static imports
import static org.apache.oodt.cas.cl.util.CmdLineUtils.getFormattedString;
import static org.apache.oodt.cas.cl.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.List;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.cl.option.AdvancedCmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOption;

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
      if (option instanceof AdvancedCmdLineOption) {
         if (((AdvancedCmdLineOption) option).hasHandler()) {
            optionUsage += getFormattedString(((AdvancedCmdLineOption) option)
                  .getHandler().getHelp(option), 62, 113);
         }
      }

      if (option.isRequired()) {
         optionUsage = " " + optionUsage;
      } else if (!option.getRequirementRules().isEmpty()) {
         optionUsage = "{" + optionUsage + "}";
         optionUsage += "\n"
               + getFormattedString(
                     "RequiredOptions: " + option.getRequirementRules(), 62,
                     113);
      } else {
         optionUsage = "[" + optionUsage + "]";
      }

      return optionUsage;
   }

   protected String getFooter() {
      return "-----------------------------------------------------------------------------------------------------------------";
   }
}

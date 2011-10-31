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
package org.apache.oodt.cas.cli.parser;

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findHelpOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getOptionByName;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.isSubOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.help.OptionHelpException;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption.SubOption;
import org.apache.oodt.cas.cli.util.Args;

//Google imports
import com.google.common.annotations.VisibleForTesting;

/**
 * Standard Command-line parser which parser command line options of the form
 * --longOption -shortOption. Supports group options, options with values, and
 * without values.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineOptionParser implements CmdLineOptionParser {

   public Set<CmdLineOptionInstance> parse(Args args,
         Set<CmdLineOption> validOptions) throws IOException {
      HashSet<CmdLineOptionInstance> optionInstances = new HashSet<CmdLineOptionInstance>();

      CmdLineOption helpOption = findHelpOption(validOptions);
      if (helpOption == null) {
         throw new OptionHelpException(
               "Must specify a help option in set of valid options");
      }

      if (args.numArgs() < 1) {
         throw new OptionHelpException("Must specify options : type -"
               + helpOption.getShortOption() + " or --"
               + helpOption.getLongOption() + " for info");
      }

      Stack<CmdLineOptionInstance> groupOptions = new Stack<CmdLineOptionInstance>();
      for (String arg : args) {

         if (isOption(arg)) {

            // check if option is a valid one
            CmdLineOption option = getOptionByName(getOptionName(arg),
                  validOptions);
            if (option == null) {
               throw new IOException("Invalid option: '" + arg + "'");
            }

            // read found option
            CmdLineOptionInstance specifiedOption = getOption(args, option);

            // Check if we are currently loading subOptions.
            if (!groupOptions.isEmpty()) {

               CmdLineOptionInstance currentGroup = groupOptions.peek();

               // Check if option is a subOption for current group.
               if (!isSubOption(currentGroup.getOption(), option)) {

                  // Check if current group was expecting more subOptions.
                  Set<CmdLineOption> requiredSubOptions = verifyGroupHasRequiredSubOptions(currentGroup);
                  if (!requiredSubOptions.isEmpty()) {
                     throw new IOException(
                           "Missing the following required subOptions for '"
                                 + currentGroup.getOption()
                                 + "': "
                                 + sortOptionsByRequiredStatus(requiredSubOptions));

                  } else {

                     // pop group and add to list of specified options.
                     optionInstances.add(groupOptions.pop());
                  }
               } else {

                  // Add option to current group subOptions.
                  currentGroup.addSubOption(specifiedOption);
                  continue;

               }
            }

            if (option instanceof GroupCmdLineOption) {

               // Push group as current group.
               groupOptions.push(specifiedOption);

            } else {

               // Option good to go.
               optionInstances.add(specifiedOption);
            }
         } else {
            throw new IOException("Invalid argument: '" + arg + "'");
         }
      }
      while (!groupOptions.isEmpty()) {
         CmdLineOptionInstance currentGroup = groupOptions.pop();
         Set<CmdLineOption> requiredSubOptions = verifyGroupHasRequiredSubOptions(currentGroup);
         if (!requiredSubOptions.isEmpty()) {
            throw new IOException(
                  "Missing the following required subOptions for '"
                        + currentGroup.getOption() + "': "
                        + sortOptionsByRequiredStatus(requiredSubOptions));

         } else {
            optionInstances.add(currentGroup);
         }
      }
      return optionInstances;
   }

   @VisibleForTesting
   /* package */static Set<CmdLineOption> verifyGroupHasRequiredSubOptions(
         CmdLineOptionInstance group) {
      Validate.isTrue(group.isGroup());

      Set<CmdLineOption> missingSubOptions = new HashSet<CmdLineOption>();
      TOP: for (SubOption subOption : ((GroupCmdLineOption) group.getOption())
            .getSubOptions()) {
         if (subOption.isRequired()) {
            for (CmdLineOptionInstance specifiedSubOption : group
                  .getSubOptions()) {
               if (specifiedSubOption.getOption().equals(subOption.getOption())) {
                  continue TOP;
               }
            }
            missingSubOptions.add(subOption.getOption());
         }
      }
      return missingSubOptions;
   }

   @VisibleForTesting
   /* package */static CmdLineOptionInstance getOption(Args args,
         CmdLineOption option) throws IOException {
      CmdLineOptionInstance specifiedOption = new CmdLineOptionInstance();
      specifiedOption.setOption(option);
      List<String> values = getValues(args);
      if (option.hasArgs()) {
         if (!values.isEmpty()) {
            specifiedOption.setValues(values);
         } else if (!option.hasDefaultArgs()) {
            throw new IOException("Option " + option + " requires args");
         }
      } else if (!option.hasArgs() && !values.isEmpty()) {
         throw new IOException("Option " + option + " does not support args");
      }
      return specifiedOption;
   }

   @VisibleForTesting
   /* package */static List<String> getValues(Args args) {
      List<String> values = new ArrayList<String>();
      String nextValue = args.getCurrentArg();
      while (nextValue != null && !isOption(nextValue)) {
         values.add(nextValue);
         nextValue = args.incrementAndGet();
      }
      return values;
   }

   @VisibleForTesting
   /* package */static boolean isOption(String arg) {
      return (arg.startsWith("-"));
   }

   @VisibleForTesting
   /* package */static String getOptionName(String arg) {
      if (arg.startsWith("--")) {
         return arg.substring(2);
      } else if (arg.startsWith("-")) {
         return arg.substring(1);
      } else {
         return null;
      }
   }
}

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
package org.apache.oodt.cas.cli.construct;

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.getOptionByName;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.isHelpOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.isSubOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.sortOptionsByRequiredStatus;

//JDK imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineConstructionException;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.GroupCmdLineOption;
import org.apache.oodt.cas.cli.option.GroupSubOption;
import org.apache.oodt.cas.cli.util.CmdLineIterable;
import org.apache.oodt.cas.cli.util.ParsedArg;

//Google imports
import com.google.common.annotations.VisibleForTesting;

/**
 * Standard {@link CmdLineConstructor} which support options and option
 * groups.
 *
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineConstructor implements CmdLineConstructor {

   public Set<CmdLineOptionInstance> construct(CmdLineIterable<ParsedArg> parsedArgs,
         Set<CmdLineOption> validOptions) throws CmdLineConstructionException {
      HashSet<CmdLineOptionInstance> optionInstances = new HashSet<CmdLineOptionInstance>();

      Stack<CmdLineOptionInstance> groupOptions = new Stack<CmdLineOptionInstance>();
      for (ParsedArg arg : parsedArgs) {

         if (arg.getType().equals(ParsedArg.Type.OPTION)) {

            // check if option is a valid one
            CmdLineOption option = getOptionByName(arg.getName(),
                  validOptions);
            if (option == null) {
               throw new CmdLineConstructionException("Invalid option: '" + arg.getName() + "'");
            }

            // read found option
            CmdLineOptionInstance specifiedOption = getOption(parsedArgs, option);

            // Check if we are currently loading subOptions.
            if (!groupOptions.isEmpty()) {

               CmdLineOptionInstance currentGroup = groupOptions.peek();

               // Check if option is NOT a subOption for current group.
               if (!isSubOption(currentGroup.getOption(), option)) {

                  // Check if current group was expecting more subOptions.
                  Set<CmdLineOption> requiredSubOptions = verifyGroupHasRequiredSubOptions(currentGroup);
                  if (!requiredSubOptions.isEmpty()) {
                     throw new CmdLineConstructionException(
                           "Missing the following required subOptions for '"
                                 + currentGroup.getOption()
                                 + "': "
                                 + sortOptionsByRequiredStatus(requiredSubOptions));

                  } else if (currentGroup.getSubOptions().isEmpty()) {
                     throw new CmdLineConstructionException(
                           "Must specify a subOption for group option '"
                                 + currentGroup.getOption() + "'");

                  } else {

                     // pop group and add to list of specified options.
                     optionInstances.add(groupOptions.pop());
                  }
               // It is a sub-option...
               } else {

                  // Add option to current group subOptions.
                  currentGroup.addSubOption(specifiedOption);
                  continue;

               }
            }

            if (option instanceof GroupCmdLineOption) {

               // Push group as current group.
               groupOptions.push(specifiedOption);
               
               if (!parsedArgs.hasNext()) {
                  throw new CmdLineConstructionException(
                        "Must specify a subOption for group option '"
                              + specifiedOption.getOption() + "'");
               }
            } else if (option.isSubOption()) {
               throw new CmdLineConstructionException("Option '" + option
                     + "' is a subOption, but was used at top level Option");

            } else {

               // Option good to go.
               optionInstances.add(specifiedOption);
            }
         } else {
            throw new CmdLineConstructionException("Invalid argument: '" + arg + "'");
         }
      }
      while (!groupOptions.isEmpty()) {
         CmdLineOptionInstance currentGroup = groupOptions.pop();
         Set<CmdLineOption> requiredSubOptions = verifyGroupHasRequiredSubOptions(currentGroup);
         if (!requiredSubOptions.isEmpty()) {
            throw new CmdLineConstructionException(
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
   /* package */static CmdLineOptionInstance getOption(CmdLineIterable<ParsedArg> args,
         CmdLineOption option) throws CmdLineConstructionException {
      CmdLineOptionInstance specifiedOption = new CmdLineOptionInstance();
      specifiedOption.setOption(option);
      List<String> values = getValues(args);
      if (isHelpOption(option)) {
         specifiedOption.setValues(values);         
      } else if (option.hasArgs()) {
         if (!values.isEmpty()) {
            specifiedOption.setValues(values);
         } else if (!option.hasStaticArgs()) {
            throw new CmdLineConstructionException("Option " + option + " requires args");
         }
      } else if (!option.hasArgs() && !values.isEmpty()) {
         throw new CmdLineConstructionException("Option " + option + " does not support args");
      }
      return specifiedOption;
   }


   @VisibleForTesting
   /* package */static Set<CmdLineOption> verifyGroupHasRequiredSubOptions(
         CmdLineOptionInstance group) {
      Validate.isTrue(group.isGroup());

      Set<CmdLineOption> missingSubOptions = new HashSet<CmdLineOption>();
      TOP: for (GroupSubOption subOption : ((GroupCmdLineOption) group.getOption())
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
   /* package */static List<String> getValues(CmdLineIterable<ParsedArg> args) {
      List<String> values = new ArrayList<String>();
      ParsedArg nextValue = args.incrementAndGet();
      while (nextValue != null && nextValue.getType().equals(ParsedArg.Type.VALUE)) {
         values.add(nextValue.getName());
         nextValue = args.incrementAndGet();
      }
      args.descrementIndex();
      return values;
   }
}

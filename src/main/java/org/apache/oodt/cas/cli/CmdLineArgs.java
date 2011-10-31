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
package org.apache.oodt.cas.cli;

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findAction;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findActionOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findHelpOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findPrintSupportedActionsOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findSpecifiedOption;

//JDK imports
import java.util.HashSet;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;

/**
 * A convenience class for {@link CmdLineUtility} for helping use already parsed
 * Command-Line arguments.
 * 
 * @author bfoster (Brian Foster)
 */
public class CmdLineArgs {

   private CmdLineAction specifiedAction;
   private Set<CmdLineAction> supportedActions;

   private HelpCmdLineOption helpOption;
   private CmdLineOptionInstance helpOptionInst;
   private ActionCmdLineOption actionOption;
   private CmdLineOptionInstance actionOptionInst;
   private PrintSupportedActionsCmdLineOption psaOption;
   private CmdLineOptionInstance psaOptionInst;
   private Set<CmdLineOption> supportedOptions;
   private Set<CmdLineOption> customSupportedOptions;
   private Set<CmdLineOptionInstance> specifiedOptions;
   private Set<CmdLineOptionInstance> customSpecifiedOptions;

   /* package */CmdLineArgs(Set<CmdLineAction> supportedActions,
         Set<CmdLineOption> supportedOptions,
         Set<CmdLineOptionInstance> specifiedOptions) {
      Validate.notNull(supportedActions);
      Validate.notNull(supportedOptions);
      Validate.notNull(specifiedOptions);

      helpOption = findHelpOption(supportedOptions);
      helpOptionInst = findSpecifiedOption(helpOption, specifiedOptions);
      actionOption = findActionOption(supportedOptions);
      actionOptionInst = findSpecifiedOption(actionOption, specifiedOptions);
      psaOption = findPrintSupportedActionsOption(supportedOptions);
      psaOptionInst = findSpecifiedOption(psaOption, specifiedOptions);

      this.supportedOptions = new HashSet<CmdLineOption>(supportedOptions);

      customSupportedOptions = new HashSet<CmdLineOption>(supportedOptions);
      customSupportedOptions.remove(helpOption);
      customSupportedOptions.remove(actionOption);
      customSupportedOptions.remove(psaOption);

      this.specifiedOptions = new HashSet<CmdLineOptionInstance>(
            specifiedOptions);

      customSpecifiedOptions = new HashSet<CmdLineOptionInstance>(
            specifiedOptions);
      if (helpOptionInst != null) {
         customSpecifiedOptions.remove(helpOptionInst);
      }
      if (actionOptionInst != null) {
         customSpecifiedOptions.remove(actionOptionInst);
      }
      if (psaOptionInst != null) {
         customSpecifiedOptions.remove(psaOptionInst);
      }

      this.supportedActions = supportedActions;
      if (actionOptionInst != null) {
         specifiedAction = findAction(actionOptionInst, supportedActions);
      }
   }

   /**
    * @return The {@link HelpCmdLineOption}
    */
   public HelpCmdLineOption getHelpOption() {
      return helpOption;
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link HelpCmdLineOption}, or null if it was not specified
    */
   public CmdLineOptionInstance getHelpOptionInst() {
      return helpOptionInst;
   }

   /**
    * @return The {@link ActionCmdLineOption}
    */
   public ActionCmdLineOption getActionOption() {
      return actionOption;
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link ActionCmdLineOption}, or null if it was not specified
    */
   public CmdLineOptionInstance getActionOptionInst() {
      return actionOptionInst;
   }

   /**
    * @return The {@link PrintSupportedActionsCmdLineOption}
    */
   public PrintSupportedActionsCmdLineOption getPrintSupportedActionsOption() {
      return psaOption;
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link PrintSupportedActionsCmdLineOption}, or null if it was not
    *         specified
    */
   public CmdLineOptionInstance getPrintSupportedActionsOptionInst() {
      return psaOptionInst;
   }

   /**
    * @return All supported {@link CmdLineOption}s
    */
   public Set<CmdLineOption> getSupportedOptions() {
      return supportedOptions;
   }

   /**
    * @return Supported {@link CmdLineOption}s less Help, Action,
    *         PrintSupportActions options
    */
   public Set<CmdLineOption> getCustomSupportedOptions() {
      return customSupportedOptions;
   }

   /**
    * @return All specified {@link CmdLineOptionInstance}s
    */
   public Set<CmdLineOptionInstance> getSpecifiedOptions() {
      return specifiedOptions;
   }

   /**
    * @return Specified {@link CmdLineOptionInstance}s less Help, Action,
    *         PrintSupportedActions option instances
    */
   public Set<CmdLineOptionInstance> getCustomSpecifiedOptions() {
      return customSpecifiedOptions;
   }

   /**
    * @return All supported {@link CmdLineAction}s
    */
   public Set<CmdLineAction> getSupportedActions() {
      return supportedActions;
   }

   /**
    * @return The {@link CmdLineAction} which was specified
    */
   public CmdLineAction getSpecifiedAction() {
      return specifiedAction;
   }
}

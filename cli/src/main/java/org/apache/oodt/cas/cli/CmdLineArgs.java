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

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;

import java.util.Set;

//OODT static imports
import static org.apache.oodt.cas.cli.util.CmdLineUtils.*;

/**
 * A convenience class for {@link CmdLineUtility} for helping use already parsed
 * Command-Line arguments.
 *
 * @author bfoster (Brian Foster)
 */
public class CmdLineArgs {

   private CmdLineAction specifiedAction;
   private Set<CmdLineAction> supportedActions;

   private CmdLineOptionInstance helpOptionInst;
   private CmdLineOptionInstance actionOptionInst;
   private CmdLineOptionInstance psaOptionInst;
   private Set<CmdLineOption> supportedOptions;
   private Set<CmdLineOptionInstance> specifiedOptions;

   /* package */CmdLineArgs(Set<CmdLineAction> supportedActions,
         Set<CmdLineOption> supportedOptions,
         Set<CmdLineOptionInstance> specifiedOptions) {
      Validate.notNull(supportedActions);
      Validate.notNull(supportedOptions);
      Validate.notNull(specifiedOptions);

      helpOptionInst = findSpecifiedHelpOption(specifiedOptions);
      psaOptionInst = findSpecifiedPrintSupportedActionsOption(specifiedOptions);
      actionOptionInst = findSpecifiedActionOption(specifiedOptions);

      this.supportedOptions = supportedOptions;
      this.specifiedOptions = specifiedOptions;
      this.supportedActions = supportedActions;

      if (actionOptionInst != null) {
         specifiedAction = findAction(actionOptionInst, supportedActions);
      }
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link org.apache.oodt.cas.cli.option.HelpCmdLineOption}, or null if it was not specified
    */
   public CmdLineOptionInstance getHelpOptionInst() {
      return helpOptionInst;
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link org.apache.oodt.cas.cli.option.ActionCmdLineOption}, or null if it was not specified
    */
   public CmdLineOptionInstance getActionOptionInst() {
      return actionOptionInst;
   }

   /**
    * @return The {@link CmdLineOptionInstance} which is the specified
    *         {@link org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption}, or null if it was not
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
    * @return All specified {@link CmdLineOptionInstance}s
    */
   public Set<CmdLineOptionInstance> getSpecifiedOptions() {
      return specifiedOptions;
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

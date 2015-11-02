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
import static org.apache.oodt.cas.cli.util.CmdLineUtils.asAdvancedOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineFailedValidation;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.determineRequired;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findFirstActionOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findHelpOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findPerformAndQuitOptions;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.findPrintSupportedActionsOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.handlePerformAndQuitOption;
import static org.apache.oodt.cas.cli.util.CmdLineUtils.isAdvancedOption;

//JDK imports
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction.ActionMessagePrinter;
import org.apache.oodt.cas.cli.action.store.CmdLineActionStore;
import org.apache.oodt.cas.cli.action.store.spring.SpringCmdLineActionStoreFactory;
import org.apache.oodt.cas.cli.construct.CmdLineConstructor;
import org.apache.oodt.cas.cli.construct.StdCmdLineConstructor;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.cli.exception.CmdLineActionExecutionException;
import org.apache.oodt.cas.cli.exception.CmdLineActionStoreException;
import org.apache.oodt.cas.cli.exception.CmdLineConstructionException;
import org.apache.oodt.cas.cli.exception.CmdLineOptionStoreException;
import org.apache.oodt.cas.cli.exception.CmdLineParserException;
import org.apache.oodt.cas.cli.option.ActionCmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.HelpCmdLineOption;
import org.apache.oodt.cas.cli.option.PrintSupportedActionsCmdLineOption;
import org.apache.oodt.cas.cli.option.store.CmdLineOptionStore;
import org.apache.oodt.cas.cli.option.store.spring.SpringCmdLineOptionStoreFactory;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator;
import org.apache.oodt.cas.cli.parser.CmdLineParser;
import org.apache.oodt.cas.cli.parser.StdCmdLineParser;
import org.apache.oodt.cas.cli.presenter.CmdLinePresenter;
import org.apache.oodt.cas.cli.presenter.StdCmdLinePresenter;
import org.apache.oodt.cas.cli.printer.CmdLinePrinter;
import org.apache.oodt.cas.cli.printer.StdCmdLinePrinter;
import org.apache.oodt.cas.cli.util.CmdLineIterable;
import org.apache.oodt.cas.cli.util.CmdLineUtils;
import org.apache.oodt.cas.cli.util.ParsedArg;

//Google imports
import com.google.common.collect.Lists;

/**
 * A highly configurable utility class which supports parsing and handling of
 * command line arguments via its action driven design. After parsing the
 * command line arguments it will check for required arguments not specified,
 * validate the arguments, run the arguments handlers and then invoke the
 * specified action. It also supports print out help messages and printing
 * supported actions.
 * 
 * @author bfoster (Brian Foster)
 */
public class CmdLineUtility {
   private static Logger LOG = Logger.getLogger(CmdLineUtility.class.getName());
   private boolean debugMode;
   private CmdLineParser parser;
   private CmdLineConstructor constructor;
   private CmdLineOptionStore optionStore;
   private CmdLineActionStore actionStore;
   private CmdLinePrinter printer;
   private CmdLinePresenter presenter;

   public CmdLineUtility() {
      parser = new StdCmdLineParser();
      constructor = new StdCmdLineConstructor();
      optionStore = new SpringCmdLineOptionStoreFactory().createStore();
      actionStore = new SpringCmdLineActionStoreFactory().createStore();
      printer = new StdCmdLinePrinter();
      presenter = new StdCmdLinePresenter();

      debugMode = Boolean.getBoolean("org.apache.oodt.cas.cli.debug");
   }

   public CmdLineOptionStore getOptionStore() {
      return optionStore;
   }

   public void setOptionStore(CmdLineOptionStore optionStore) {
      this.optionStore = optionStore;
   }

   public CmdLineActionStore getActionStore() {
      return actionStore;
   }

   public void setActionStore(CmdLineActionStore actionStore) {
      this.actionStore = actionStore;
   }

   public CmdLinePrinter getPrinter() {
      return printer;
   }

   public void setPrinter(CmdLinePrinter printer) {
      this.printer = printer;
   }

   public CmdLinePresenter getPresenter() {
      return presenter;
   }

   public void setPresenter(CmdLinePresenter presenter) {
      this.presenter = presenter;
   }

   public void printOptionHelp(CmdLineArgs cmdLineArgs) {
      presenter.presentOptionHelp(printer.printOptionsHelp(cmdLineArgs
            .getSupportedOptions()));
   }

   public void printActionHelp(CmdLineArgs cmdLineArgs) {
      Validate.notEmpty(cmdLineArgs.getHelpOptionInst().getValues());

      presenter.presentActionHelp(printer.printActionHelp(
            CmdLineUtils.findAction(cmdLineArgs.getHelpOptionInst().getValues()
                  .get(0), cmdLineArgs.getSupportedActions()),
            cmdLineArgs.getSupportedOptions()));
   }

   public void printActionsHelp(CmdLineArgs cmdLineArgs) {
      presenter.presentActionsHelp(printer.printActionsHelp(cmdLineArgs
            .getSupportedActions()));
   }

   public void printActionMessages(List<String> messages) {
      presenter.presentActionMessage(printer.printActionMessages(messages));
   }

   public void printValidationErrors(List<CmdLineOptionValidator.Result> results) {
      presenter.presentErrorMessage(printer
            .printOptionValidationErrors(results));
   }

   public void printMissingRequiredOptionsError(
         Set<CmdLineOption> missingOptions) {
      presenter.presentErrorMessage(printer
            .printRequiredOptionsMissingError(missingOptions));
   }

   public void printErrorMessage(String errorMessage) {
      presenter.presentErrorMessage(errorMessage);
   }

   /**
    * Parses given command line arguments, then checks for help and print
    * supported actions options, prints them out if found, otherwise performs
    * execution on the arguments - see execute(CmdLineArgs).
    * 
    * @param args
    *           The who will be parsed and executed.
    */
   public void run(String[] args) {
      try {
         CmdLineArgs cmdLineArgs = parse(args);
         if (cmdLineArgs.getSpecifiedOptions().isEmpty()) {
            printOptionHelp(cmdLineArgs);
         } else if (!handleHelp(cmdLineArgs)
               && !handlePrintSupportedActions(cmdLineArgs)) {
            execute(cmdLineArgs);
         }
      } catch (Exception e) {
         if (debugMode) {
            throw new RuntimeException(e);
         }
         printErrorMessage(e.getMessage());
      }
   }

   /**
    * Parses the given command line arguments and converts it to
    * {@link CmdLineArgs}.
    * 
    * @param args
    *           The command line arguments to parse.
    * @return The parsed command line arguments in {@link CmdLineArgs} form.
    * @throws IOException
    *            On error parsing command line arguments.
    * @throws CmdLineConstructionException
    *            On error constructing command line arguments.
    * @throws CmdLineOptionStoreException
    */
   public CmdLineArgs parse(String[] args) throws
       CmdLineActionStoreException, CmdLineConstructionException {
      Validate.notNull(parser);
      Validate.notNull(optionStore);

      // Load supported options.
      Set<CmdLineOption> validOptions = optionStore.loadSupportedOptions();
      initializeHandlers(validOptions);

      // Insure help options is present if required.
      if (findHelpOption(validOptions) == null) {
         validOptions.add(new HelpCmdLineOption());
      }

      // Insure action options are present if required.
      if (findFirstActionOption(validOptions) == null) {
         validOptions.add(new ActionCmdLineOption());
      }

      // Insure print supported actions option is present if required.
      if (findPrintSupportedActionsOption(validOptions) == null) {
         validOptions.add(new PrintSupportedActionsCmdLineOption());
      }

      // Parse command line arguments.
      return new CmdLineArgs(actionStore.loadSupportedActions(), validOptions,
            constructor.construct(
                  new CmdLineIterable<ParsedArg>(parser.parse(args)),
                  validOptions));
   }

   /**
    * Initializes each {@link CmdLineOptionHandler} with their assigned
    * {@link CmdLineOption}.
    * 
    * @param options
    */
   public void initializeHandlers(Set<CmdLineOption> options) {
      for (CmdLineOption option : options) {
         if (isAdvancedOption(option)
               && asAdvancedOption(option).getHandler() != null) {
            asAdvancedOption(option).getHandler().initialize(option);
         }
      }
   }

   /**
    * Checks if help option was specified and if so prints out help.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} which will be checked for help option
    * @return True if help was printed, false otherwise
    */
   public boolean handleHelp(CmdLineArgs cmdLineArgs) {
      if (cmdLineArgs.getHelpOptionInst() != null) {
         if (cmdLineArgs.getHelpOptionInst().getValues().isEmpty()) {
            printOptionHelp(cmdLineArgs);
         } else {
            printActionHelp(cmdLineArgs);
         }
         return true;
      }
      return false;
   }

   /**
    * Checks if print supported actions option was specified and if so prints
    * out supported actions.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} which will be checked for print
    *           supported action options
    * @return True if supported actions was printed, false otherwise
    */
   public boolean handlePrintSupportedActions(CmdLineArgs cmdLineArgs) {
      if (cmdLineArgs.getPrintSupportedActionsOptionInst() != null) {
         printActionsHelp(cmdLineArgs);
         return true;
      }
      return false;
   }

   /**
    * Checks if required options are set and validation passes, then runs
    * handlers and executes its action.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} for which execution processing will be
    *           run.
    */
   public void execute(CmdLineArgs cmdLineArgs)
         throws CmdLineActionExecutionException, CmdLineActionException {
      Set<CmdLineOptionInstance> performAndQuitOptions = findPerformAndQuitOptions(cmdLineArgs
            .getSpecifiedOptions());
      if (!performAndQuitOptions.isEmpty()) {
         for (CmdLineOptionInstance option : performAndQuitOptions) {
            handlePerformAndQuitOption(option);
         }
         return;
      }

      if (cmdLineArgs.getActionOptionInst() == null) {
         throw new CmdLineActionExecutionException(
               "Must specify an action option!");
      }
      Set<CmdLineOption> requiredOptionsNotSet = check(cmdLineArgs);
      if (!requiredOptionsNotSet.isEmpty()) {
         printMissingRequiredOptionsError(requiredOptionsNotSet);
         return;
      }

      List<CmdLineOptionValidator.Result> failedValidationResults = determineFailedValidation(validate(cmdLineArgs));
      if (!failedValidationResults.isEmpty()) {
         printValidationErrors(failedValidationResults);
         return;
      }

      handle(cmdLineArgs);

      ActionMessagePrinter printer = new ActionMessagePrinter();
      cmdLineArgs.getSpecifiedAction().execute(printer);
      printActionMessages(printer.getPrintedMessages());
   }

   /**
    * Checks for required options which are not set and returns the ones it
    * finds.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} which will be check for required
    *           options.
    * @return The required {@link CmdLineOption}s not specified.
    */
   public static Set<CmdLineOption> check(CmdLineArgs cmdLineArgs) {
      Set<CmdLineOption> requiredOptions = determineRequired(
            cmdLineArgs.getSpecifiedAction(), cmdLineArgs.getSupportedOptions());
      HashSet<CmdLineOption> requiredOptionsNotSet = new HashSet<CmdLineOption>(
            requiredOptions);
      for (CmdLineOptionInstance specifiedOption : cmdLineArgs
            .getSpecifiedOptions()) {
         requiredOptionsNotSet.remove(specifiedOption.getOption());
      }
      return requiredOptionsNotSet;
   }

   /**
    * Runs validation on {@link CmdLineArgs} and returns the validation results.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} which will be validated.
    * @return The {@link CmdLineOptionValidator.Result}s generated when running
    *         {@link CmdLineOptionValidator}s.
    */
   public static List<CmdLineOptionValidator.Result> validate(
         CmdLineArgs cmdLineArgs) {
      Validate.notNull(cmdLineArgs);

      List<CmdLineOptionValidator.Result> results = Lists.newArrayList();
      for (CmdLineOptionInstance optionInst : cmdLineArgs.getSpecifiedOptions()) {
         results.addAll(CmdLineUtils.validate(optionInst));
      }
      return results;
   }

   /**
    * Runs the {@link CmdLineOptionHandler}s for {@link CmdLineArgs} given.
    * 
    * @param cmdLineArgs
    *           The {@link CmdLineArgs} whose option handlers will be run.
    */
   public static void handle(CmdLineArgs cmdLineArgs) {
      for (CmdLineOptionInstance option : cmdLineArgs.getSpecifiedOptions()) {
         CmdLineUtils.handle(cmdLineArgs.getSpecifiedAction(), option);
      }
   }
}

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

//JDK imports
import java.util.List;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator.Result;

/**
 * Printer which is responsible for generating a {@link String} representation
 * of help, validation errors, and missing required options errors.
 *
 * @author bfoster (Brian Foster)
 */
public interface CmdLinePrinter {

   /**
    * Should generate help message for action specified by
    * .
    * 
    * @param action
    *           {@link CmdLineAction} for which help will be generate
    * @param options
    *           Supported {@link CmdLineOption}s
    * @return Help message for specified action
    */
   String printActionHelp(CmdLineAction action, Set<CmdLineOption> options);

   /**
    * Generates help messages given {@link CmdLineAction}s.
    * 
    * @param actions
    *           {@link CmdLineAction}s to print help for
    * @return Help message for given {@link CmdLineAction}s
    */
   String printActionsHelp(Set<CmdLineAction> actions);

   /**
    * Generates help message for given {@link CmdLineOption}s.
    * 
    * @param options
    *           {@link CmdLineOption}s for which help message will be generated
    * @return Help message for given {@link CmdLineOption}s
    */
   String printOptionsHelp(Set<CmdLineOption> options);

   /**
    * Generate validation error message for results of failed validations.
    * 
    * @param results
    *           {@link List} of FAILed validations
    * @return Generated validation error message
    */
   String printOptionValidationErrors(List<Result> results);

   /**
    * Generates missing required options error message from given
    * {@link CmdLineOption}s which where required and not specified.
    * 
    * @param missingOptions
    *           {@link Set} of {@link CmdLineOption}s which where required and
    *           not set.
    * @return Generated missing required options error message.
    */
   String printRequiredOptionsMissingError(
       Set<CmdLineOption> missingOptions);

   /**
    * Generates {@link CmdLineAction} message from list of messages.
    *
    * @param messages The messages from a {@link CmdLineAction}
    * @return printed message from list of messages.
    */
   String printActionMessages(List<String> messages);
}

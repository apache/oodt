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
package org.apache.oodt.cas.cli.option;

//JDK imports
import java.util.ArrayList;
import java.util.List;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.cli.option.handler.CmdLineOptionHandler;
import org.apache.oodt.cas.cli.option.validator.CmdLineOptionValidator;

/**
 * Advanced version of a {@link CmdLineOption} which supports validation and
 * option handling.
 * 
 * @author bfoster (Brian Foster)
 */
public class AdvancedCmdLineOption extends SimpleCmdLineOption implements
      ValidatableCmdLineOption, HandleableCmdLineOption {

   private CmdLineOptionHandler handler;
   private List<CmdLineOptionValidator> validators;
   private boolean performAndQuit;

   public AdvancedCmdLineOption() {
      super();
      validators = new ArrayList<CmdLineOptionValidator>();
   }

   public AdvancedCmdLineOption(String shortOption, String longOption,
         String description, boolean hasArgs) {
      super(shortOption, longOption, description, hasArgs);
   }

   public void setHandler(CmdLineOptionHandler handler) {
      this.handler = handler;
   }

   public CmdLineOptionHandler getHandler() {
      return handler;
   }

   public boolean hasHandler() {
      return handler != null;
   }
   
   public List<CmdLineOptionValidator> getValidators() {
      return this.validators;
   }

   public void addValidator(CmdLineOptionValidator validator) {
      Validate.notNull(validator);

      validators.add(validator);
   }

   public void setValidators(List<CmdLineOptionValidator> validators) {
      Validate.notNull(validators);

      this.validators = validators;
   }

   public boolean isPerformAndQuit() {
      return performAndQuit;
   }

   public void setPerformAndQuit(boolean performAndQuit) {
      this.performAndQuit = performAndQuit;
   }
}

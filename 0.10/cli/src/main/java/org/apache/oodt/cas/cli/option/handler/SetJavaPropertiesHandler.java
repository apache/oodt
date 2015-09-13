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
package org.apache.oodt.cas.cli.option.handler;

//JDK imports
import java.util.List;

//Apache imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.util.OptionPropertyRegister;

/**
 * {@link CmdLineOptionHandler} which sets Java Properties equals to the
 * values specified by the {@link CmdLineOption} this handler is attached
 * to.
 *
 * @author bfoster (Brian Foster)
 */
public class SetJavaPropertiesHandler implements CmdLineOptionHandler {

   private List<String> propertyNames;

   public void initialize(CmdLineOption option) {
      for (String property : propertyNames) {
         OptionPropertyRegister.registerOption(property, option);
      }
   }

   public void handleOption(CmdLineAction selectedAction,
         CmdLineOptionInstance optionInstance) {
      Validate.notNull(propertyNames);

      for (String propertyName : propertyNames) {
         System.setProperty(propertyName,
               StringUtils.join(optionInstance.getValues(),
                     optionInstance.getOption().getType().equals(List.class) ?
                           "," :  " "));
      }
   }

   public void setPropertyNames(List<String> propertyNames) {
      this.propertyNames = propertyNames;
   }

   public List<String> getPropertyNames() {
      return propertyNames;
   }

   public String getHelp(CmdLineOption option) {
      return "Sets the following Java Properties: " + this.propertyNames;
   }

   public String getArgDescription(CmdLineAction action, CmdLineOption option) {
      return null;
   }
}

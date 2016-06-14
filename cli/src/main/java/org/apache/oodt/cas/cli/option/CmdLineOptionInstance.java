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
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.oodt.cas.cli.util.CmdLineUtils.*;

//Apache imports

/**
 * A specified {@link CmdLineOption} with its specified argument values.
 * 
 * @author bfoster (Brian Foster)
 */
public class CmdLineOptionInstance {

   private CmdLineOption option;
   private List<String> values;
   private Set<CmdLineOptionInstance> subOptions;

   public CmdLineOptionInstance() {
      values = new ArrayList<String>();
      subOptions = new HashSet<CmdLineOptionInstance>();
   }

   public CmdLineOptionInstance(CmdLineOption option, List<String> values) {
      Validate.notNull(option);
      Validate.notNull(values);

      this.option = option;
      this.values = values;
      subOptions = new HashSet<CmdLineOptionInstance>();
   }

   public void setOption(CmdLineOption option) {
      this.option = option;
   }

   public CmdLineOption getOption() {
      return option;
   }

   public boolean isGroup() {
      return isGroupOption(option);
   }

   public boolean isAction() {
      return isActionOption(option);
   }

   public boolean isHelp() {
      return isHelpOption(option);
   }

   public boolean isPrintSupportedActions() {
      return isPrintSupportedActionsOption(option);
   }

   public boolean isValidatable() {
      return option instanceof ValidatableCmdLineOption;
   }

   public boolean isHandleable() {
      return option instanceof HandleableCmdLineOption;
   }

   public void setValues(List<String> values) {
      Validate.notNull(values);

      this.values = new ArrayList<String>(values);
   }

   public void addValue(String value) {
      values.add(value);
   }

   public List<String> getValues() {
      if (values.isEmpty() && option.hasStaticArgs()) {
         return option.getStaticArgs();
      } else {
         return values;
      }
   }

   public void setSubOptions(List<CmdLineOptionInstance> subOptions) {
      Validate.isTrue(isGroup(), "Must be group option to have subOptions");
      Validate.notNull(subOptions, "Cannot set subOptions to NULL");

      this.subOptions = new HashSet<CmdLineOptionInstance>(subOptions);
   }

   public void addSubOption(CmdLineOptionInstance subOption) {
      Validate.isTrue(isGroup(), "Must be group option to have subOptions");
      Validate.notNull(subOption, "Cannot add NULL subOption");

      this.subOptions.add(subOption);
   }

   public Set<CmdLineOptionInstance> getSubOptions() {
      return subOptions;
   }

   public boolean equals(Object obj) {
      if (obj instanceof CmdLineOptionInstance) {
         CmdLineOptionInstance compareObj = (CmdLineOptionInstance) obj;
         return compareObj.option.equals(this.option)
               && compareObj.values.equals(this.values);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = option != null ? option.hashCode() : 0;
      result = 31 * result + (values != null ? values.hashCode() : 0);
      result = 31 * result + (subOptions != null ? subOptions.hashCode() : 0);
      return result;
   }

   public String toString() {
      return "[option= " + option + ",values=" + values + ",subOptions="
            + subOptions + "]";
   }
}

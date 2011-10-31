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
import java.util.HashSet;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

/**
 * {@link CmdLineOption} which is a group option (i.e. supports sub-
 * {@link CmdLineOption}s.
 * 
 * @author bfoster (Brian Foster)
 */
public class GroupCmdLineOption extends SimpleCmdLineOption {

   private Set<SubOption> subOptions;
   private boolean allowAnySubOption;

   public GroupCmdLineOption() {
      super();
      this.setHasArgs(false);
      this.setAllowAnySubOptions(false);
      subOptions = new HashSet<SubOption>();
   }

   public GroupCmdLineOption(String shortOption, String longOption,
         String description, boolean hasArgs) {
      super(shortOption, longOption, description, hasArgs);
   }

   public void setAllowAnySubOptions(boolean allowAnySubOption) {
      this.allowAnySubOption = allowAnySubOption;
   }

   public boolean isAllowAnySubOptions() {
      return subOptions.isEmpty() && allowAnySubOption;
   }

   public void setSubOptions(Set<SubOption> subOptions) {
      Validate.notNull(subOptions, "Cannot set subOptions to NULL");

      this.subOptions = new HashSet<SubOption>(subOptions);
   }

   public void addSubOption(SubOption subOption) {
      Validate.notNull(subOption, "Cannot add NULL subOption");

      subOptions.add(subOption);
   }

   public Set<SubOption> getSubOptions() {
      return subOptions;
   }

   public boolean hasSubOptions() {
      return subOptions != null && !subOptions.isEmpty();
   }

   public static class SubOption {

      private CmdLineOption option;
      private boolean required;

      public SubOption() {
      }

      public SubOption(CmdLineOption option, boolean required) {
         this.option = option;
         this.required = required;
      }

      public void setOption(CmdLineOption option) {
         this.option = option;
      }

      public CmdLineOption getOption() {
         return option;
      }

      public void setRequired(boolean required) {
         this.required = required;
      }

      public boolean isRequired() {
         return required;
      }
   }
}

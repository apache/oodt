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
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.option.require.RequirementRule;

/**
 * Command Line option interface spec.
 * 
 * @author bfoster (Brian Foster)
 */
public interface CmdLineOption {

   public void setLongOption(String longOption);

   public String getLongOption();

   public void setShortOption(String shortOption);

   public String getShortOption();

   public void setDescription(String description);

   public String getDescription();

   public void setType(Class<?> type);

   public Class<?> getType();

   public void setRepeating(boolean repeating);

   public boolean isRepeating();

   public void setHasArgs(boolean hasArgs);

   public boolean hasArgs();

   public void setArgsDescription(String argDescription);

   public String getArgsDescription();

   public void setDefaultArgs(List<String> values);

   public List<String> getDefaultArgs();

   public boolean hasDefaultArgs();

   public void setRequired(boolean required);

   public boolean isRequired();

   public void setRequirementRules(List<RequirementRule> requirementRules);

   public List<RequirementRule> getRequirementRules();

   public void setIsSubOption(boolean isSubOption);

   public boolean isSubOption();

   public boolean equals(Object obj);

   public int hashCode();
}

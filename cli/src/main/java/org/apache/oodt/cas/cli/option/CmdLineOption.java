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

   void setLongOption(String longOption);

   String getLongOption();

   void setShortOption(String shortOption);

   String getShortOption();

   void setDescription(String description);

   String getDescription();

   void setType(Class<?> type);

   Class<?> getType();

   void setRepeating(boolean repeating);

   boolean isRepeating();

   void setHasArgs(boolean hasArgs);

   boolean hasArgs();

   void setArgsDescription(String argDescription);

   String getArgsDescription();

   void setStaticArgs(List<String> values);

   List<String> getStaticArgs();

   boolean hasStaticArgs();

   void setRequired(boolean required);

   boolean isRequired();

   void setRequirementRules(List<RequirementRule> requirementRules);

   List<RequirementRule> getRequirementRules();

   void setIsSubOption(boolean isSubOption);

   boolean isSubOption();

   boolean equals(Object obj);

   int hashCode();
}

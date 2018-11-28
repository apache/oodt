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
package org.apache.oodt.cas.cli.util;

//JDK imports
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOption;

//Google imports
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Register for determining if a Java Property is required or optional and
 * thus determining which options become required or optional because
 * said property has been registered.
 *
 * @author bfoster (Brian Foster)
 */
public class OptionPropertyRegister {

   private static Map<String, CmdLineOption> optionRegistrations = Maps.newHashMap();

   private OptionPropertyRegister() {
      throw new RuntimeException("Do not instantiate OptionPropertyRegister");
   }

   public static void registerOption(String property, CmdLineOption option) {
      Validate.isTrue(!optionRegistrations.containsKey(property),
            "Property '" + property + "' is already registered to: "
            + optionRegistrations.get(property));

      optionRegistrations.put(property, option);
   }

   public static CmdLineOption getRegisteredOption(String property) {
      for (Entry<String, CmdLineOption> optionRegistration : optionRegistrations.entrySet()) {
         if (optionRegistration.getKey().equals(property)) {
            return optionRegistration.getValue();
         }
      }
      return null;
   }
   
   public static Set<String> getProperties(CmdLineOption specifiedOption) {
      Set<String> properties = Sets.newHashSet();
      for (Entry<String, CmdLineOption> optionRegistration : optionRegistrations.entrySet()) {
         if (optionRegistration.getValue().equals(specifiedOption)) {
            properties.add(optionRegistration.getKey());
         }
      }
      return properties;
   }

   public static void clearRegister() {
      optionRegistrations.clear();
   }
}

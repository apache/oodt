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
package org.apache.oodt.cas.cli.option.store.spring;

//JDK imports
import java.util.Map;
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.store.CmdLineOptionStore;

//Spring imports
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Google imports
import com.google.common.collect.Sets;

/**
 * Spring Framework based {@link CmdLineOptionStore}.
 * 
 * @author bfoster (Brian Foster)
 */
public class SpringCmdLineOptionStore implements CmdLineOptionStore {

   private ApplicationContext appContext;

   public SpringCmdLineOptionStore(String springConfig) {
      appContext = new FileSystemXmlApplicationContext(springConfig);
   }

   @Override
   public Set<CmdLineOption> loadSupportedOptions() {
      @SuppressWarnings("unchecked")
      Map<String, CmdLineOption> optionsMap = appContext
            .getBeansOfType(CmdLineOption.class);
      Set<CmdLineOption> supportedOptions = Sets.newHashSet();
      for (CmdLineOption option : optionsMap.values()) {
         if (!option.isSubOption()) {
            supportedOptions.add(option);
         }
      }
      return supportedOptions;
   }

   protected ApplicationContext getApplicationContext() {
      return appContext;
   }
}

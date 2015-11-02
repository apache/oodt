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
package org.apache.oodt.cas.cli.action.store.spring;

import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.action.store.CmdLineActionStore;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * A Spring Framework based {@link CmdLineActionStore}.
 * 
 * @author bfoster (Brian Foster)
 */
public class SpringCmdLineActionStore implements CmdLineActionStore {

   private ApplicationContext appContext;

   public SpringCmdLineActionStore(String springConfig) {
      appContext = new FileSystemXmlApplicationContext(springConfig);
      handleSpringSetContextInjectionType();
      handleSettingNameForCmdLineActions();
   }

   @Override
   public Set<CmdLineAction> loadSupportedActions() {
      @SuppressWarnings("unchecked")
      Map<String, CmdLineAction> actionsMap = appContext
            .getBeansOfType(CmdLineAction.class);
      return new HashSet<CmdLineAction>(actionsMap.values());
   }

   protected ApplicationContext getApplicationContext() {
      return appContext;
   }

   private void handleSpringSetContextInjectionType() {
      @SuppressWarnings("unchecked")
      Map<String, SpringSetContextInjectionType> beans = appContext
            .getBeansOfType(SpringSetContextInjectionType.class);
      for (SpringSetContextInjectionType bean : beans.values()) {
         bean.setContext(appContext);
      }
   }

   private void handleSettingNameForCmdLineActions() {
      @SuppressWarnings("unchecked")
      Map<String, CmdLineAction> beans = appContext
            .getBeansOfType(CmdLineAction.class);
      for (Entry<String, CmdLineAction> entry : beans.entrySet()) {
         entry.getValue().setName(entry.getKey());
      }
   }
}

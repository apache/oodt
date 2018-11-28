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
package org.apache.oodt.cas.crawl.cli.option.handler;

//JDK imports 
import java.util.List;

//OODT imports 
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cli.option.handler.CmdLineOptionHandler;
import org.apache.oodt.cas.crawl.util.ActionBeanProperties;

/** 
 * A {@link CmdLineOptionHandler} which adds bean properties to
 * {@link ActionBeanProperties} so that they can then be injected into 
 * {@link ProductCrawler}s and {@link CrawlerAction}s.
 *
 * @author bfoster (Brian Foster) 
 */

public class CrawlerBeansPropHandler implements CmdLineOptionHandler {

   private List<String> properties;

   @Override
   public void initialize(CmdLineOption option) {
      // Do nothing.
   }

   @Override
   public void handleOption(CmdLineAction selectedAction,
         CmdLineOptionInstance optionInstance) {
      for (String beanProperty : properties) {
         if (optionInstance.getValues().size() > 1) {
            for (int i = 0; i < optionInstance.getValues().size(); i++) {
               ActionBeanProperties.setProperty(beanProperty + "[" + i + "]",
                     optionInstance.getValues().get(i));
            }
         } else if (!optionInstance.getValues().isEmpty()) {
            ActionBeanProperties.setProperty(beanProperty,
                  optionInstance.getValues().get(0));
         } else {
            throw new RuntimeException(
                  CrawlerBeansPropHandler.class.getCanonicalName()
                        + " can't apply option '" + optionInstance.getOption()
                        + "' since it has no value");
         }
      }
   }

   @Override
   public String getHelp(CmdLineOption option) {
      return "Set the following bean properties: " + properties;
   }

   @Override
   public String getArgDescription(CmdLineAction action, CmdLineOption option) {
      return null;
   }

   public void setProperties(List<String> properties) {
      this.properties = properties;
   }
}

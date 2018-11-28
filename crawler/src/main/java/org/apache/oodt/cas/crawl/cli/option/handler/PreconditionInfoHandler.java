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
import java.io.PrintStream;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;
import org.apache.oodt.cas.metadata.preconditions.PreConditionComparator;

/**
 * 
 * @author bfoster (Brian Foster)
 */
public class PreconditionInfoHandler extends BeanInfoHandler {

   @Override
   public void initialize(CmdLineOption option) {
      // Do nothing.
   }

   @Override
   public void handleOption(CmdLineAction selectedAction,
         CmdLineOptionInstance optionInstance) {
      String[] preCondIds = this.getApplicationContext().getBeanNamesForType(
            PreConditionComparator.class);
      PrintStream ps = new PrintStream(this.getOutStream());
      ps.println("PreConditionComparators:");
      for (String preCondId : preCondIds) {
         PreConditionComparator<?> preCond = (PreConditionComparator<?>) this
               .getApplicationContext().getBean(preCondId);
         ps.println("  PreCondComparator:");
         ps.println("    Id: " + preCondId);
         ps.println("    Description: " + preCond.getDescription());
         ps.println();
      }
      ps.close();
   }

   @Override
   public String getHelp(CmdLineOption option) {
      return null;
   }

   @Override
   public String getArgDescription(CmdLineAction action, CmdLineOption option) {
      return null;
   }
}

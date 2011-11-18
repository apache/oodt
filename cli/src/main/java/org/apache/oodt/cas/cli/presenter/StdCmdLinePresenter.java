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
package org.apache.oodt.cas.cli.presenter;

//JDK imports
import java.io.OutputStream;
import java.io.PrintStream;

//Apache imports
import org.apache.commons.lang.Validate;

/**
 * Standard help presenter which sends help messages to {@link System.out}.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLinePresenter implements
      CmdLinePresenter {

   private PrintStream ps;

   public StdCmdLinePresenter() {
      this(System.out);
   }

   public StdCmdLinePresenter(OutputStream os) {
      Validate.notNull(os);

      ps = new PrintStream(os);
   }

   @Override
   public void presentOptionHelp(String optionHelpMessage) {
      ps.println(optionHelpMessage);
   }

   @Override
   public void presentActionHelp(String actionHelpMessage) {
      ps.println(actionHelpMessage);
   }

   @Override
   public void presentActionsHelp(String actionsHelpMessage) {
      ps.println(actionsHelpMessage);
   }

   @Override
   public void presentErrorMessage(String errorMessage) {
      ps.println("ERROR: " + errorMessage);
   }

   @Override
   public void presentActionMessage(String actionMessage) {
      ps.println(actionMessage);
   }
}

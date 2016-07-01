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
package org.apache.oodt.cas.cli.option.handler;

//OODT imports
import org.apache.oodt.cas.cli.action.CmdLineAction;
import org.apache.oodt.cas.cli.option.CmdLineOption;
import org.apache.oodt.cas.cli.option.CmdLineOptionInstance;

/**
 * Handles a {@link CmdLineOption}'s values in relation to given
 * {@link CmdLineAction}s.
 * 
 * @author bfoster (Brian Foster)
 */
public interface CmdLineOptionHandler {

   /**
    * Called after handler construction to allow handler to setup
    * state before it is required to handle the option later. This
    * is also called when help is run so allows registration to take
    * place if necessary for help analysis.
    */
   void initialize(CmdLineOption option);

   void handleOption(CmdLineAction selectedAction,
                     CmdLineOptionInstance optionInstance);

   /**
    * Gets the {@link CmdLineOptionHandler}s help message when associated with
    * given {@link CmdLineOption}.
    * 
    * @param option
    *           The {@link CmdLineOption} to which this
    *           {@link CmdLineOptionHandler} was associated with
    * @return The help message for this {@link CmdLineOptionHandler}
    */
   String getHelp(CmdLineOption option);

   /**
    * If this handler causes the argument descriptor to be different for
    * certain {@link CmdLineAction}s, then should return the arg
    * description here.
    */
   String getArgDescription(CmdLineAction action,
                            CmdLineOption option);
}

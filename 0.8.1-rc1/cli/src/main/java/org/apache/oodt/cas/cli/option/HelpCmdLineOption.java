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

/**
 * The Help {@link CmdLineOption}.
 * 
 * @author bfoster (Brian Foster)
 */
public class HelpCmdLineOption extends SimpleCmdLineOption {

   public HelpCmdLineOption() {
      this("h", "help", "Prints help menu", false);
   }

   public HelpCmdLineOption(String shortOption, String longOption,
         String description, boolean hasArgs) {
      super(shortOption, longOption, description, hasArgs);
   }
}

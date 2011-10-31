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
package org.apache.oodt.cas.cl.help.printer;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;

/**
 * Standard help printer which prints supported actions help in the format of:
 * 
 * <pre>
 * Actions:
 *   Action:
 *     Name: ActionName
 *     Description: Action Description
 * 
 *   Action:
 *     Name: ActionName
 *     Description: Action Description
 * 
 *   ...
 *   ...
 *   ...
 * </pre>
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineActionsHelpPrinter implements CmdLineActionsHelpPrinter {

   /**
    * {@inheritDoc}
    */
   public String printHelp(Set<CmdLineAction> actions) {
      StringBuffer sb = new StringBuffer("");
      for (CmdLineAction action : actions) {
         sb.append("Actions:").append("\n");
         sb.append("  Action:").append("\n");
         sb.append("    Name: ").append(action.getName()).append("\n");
         sb.append("    Description: ").append(action.getDescription())
               .append("\n").append("\n");
      }
      return sb.toString();
   }
}

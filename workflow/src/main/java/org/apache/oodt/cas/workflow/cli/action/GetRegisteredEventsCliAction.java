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
package org.apache.oodt.cas.workflow.cli.action;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link CmdLineAction} which gets a list of registered events.
 * 
 * @author bfoster (Brian Foster)
 */
public class GetRegisteredEventsCliAction extends WorkflowCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         @SuppressWarnings("unchecked")
         List<String> events = getClient().getRegisteredEvents();

         if (events == null) {
            throw new Exception("WorkflowManager returned null event list");
         }
         for (String event : events) {
            printer.println("Event: [name=" + event + "]");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get registered events : "
               + e.getMessage(), e);
      }
   }
}

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
package org.apache.oodt.cas.resource.cli.action;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link org.apache.oodt.cas.cli.action.CmdLineAction} which list all jobs executing and shows
 * what nodes are executing those jobs in addition to queue name and
 * load value.
 * 
 */
public class GetExecReportCliAction extends ResourceCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
	  String report = getClient().getExecReport();
	  printer.println("Exec Report: ");
	  printer.println(report);
	  printer.println();
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to get exec report: "
               + e.getMessage(), e);
      }
   }
}

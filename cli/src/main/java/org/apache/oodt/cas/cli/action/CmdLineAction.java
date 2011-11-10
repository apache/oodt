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
package org.apache.oodt.cas.cli.action;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * Action which is specified and configured via {@link CmdLineOption}s and
 * then executed.
 * 
 * @author bfoster (Brian Foster)
 */
public abstract class CmdLineAction {

   private String name;
   private String description;

   public CmdLineAction() {
   }

   public CmdLineAction(String name, String description) {
      this.name = name;
      this.description = description;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public abstract void execute() throws CmdLineActionException;

}

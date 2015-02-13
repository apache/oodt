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

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

//Google imports
import com.google.common.collect.Lists;

/**
 * Action which is specified and configured via {@link CmdLineOption}s and
 * then executed.
 * 
 * @author bfoster (Brian Foster)
 */
public abstract class CmdLineAction {

   private String name;
   private String description;
   private String detailedDescription;
   private String examples;

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

   public void setDetailedDescription(String detailedDescription) {
      this.detailedDescription = detailedDescription;
   }

   public String getDetailedDescription() {
      return detailedDescription;
   }

   public void setExamples(String examples) {
      this.examples = examples;
   }

   public String getExamples() {
      return examples;
   }

   public abstract void execute(ActionMessagePrinter printer)
         throws CmdLineActionException;

   public static class ActionMessagePrinter {
      private List<String> messages;

      public ActionMessagePrinter() {
         messages = Lists.newArrayList();
      }

      public void print(String message) {
         messages.add(message);
      }

      public void println(String message) {
         print(message);
         println();
      }

      public void println() {
         messages.add("\n");
      }

      public List<String> getPrintedMessages() {
         return messages;
      }
   }
}

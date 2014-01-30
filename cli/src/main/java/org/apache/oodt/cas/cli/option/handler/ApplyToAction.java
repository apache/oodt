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

/**
 * Mapping of {@link CmdLineAction} name to its method which should be called by
 * {@link ApplyToActionHandler} when injecting argument value.
 * 
 * @author bfoster (Brian Foster)
 */
public class ApplyToAction {

   private String actionName;
   private String methodName;
   private String description;
   private String argDescription;

   public ApplyToAction() {
   }

   public ApplyToAction(String actionName, String methodName) {
      this.actionName = actionName;
      this.methodName = methodName;
   }

   public String getActionName() {
      return actionName;
   }

   public void setActionName(String actionName) {
      this.actionName = actionName;
   }

   public String getMethodName() {
      return methodName;
   }

   public void setMethodName(String methodName) {
      this.methodName = methodName;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getArgDescription() {
      return argDescription;
   }

   public void setArgDescription(String argDescription) {
      this.argDescription = argDescription;
   }

   public String toString() {
      return actionName + " : " + methodName;
   }
}

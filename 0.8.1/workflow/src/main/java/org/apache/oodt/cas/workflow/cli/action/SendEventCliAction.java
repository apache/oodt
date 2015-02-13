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

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A {@link CmdLineAction} which triggers a workflow event.
 *
 * @author bfoster (Brian Foster)
 */
public class SendEventCliAction extends WorkflowCliAction {

   private String eventName;
   private Metadata metadata;

   public SendEventCliAction() {
      metadata = new Metadata();
   }

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      Validate.notNull(eventName, "Must specify eventName");

      try {
         printer.print("Sending event '" + eventName + "'... ");
         printer.println(getClient().sendEvent(eventName, metadata) ?
               "SUCCESS" : "FAILURE");
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to send event '" + eventName
               + "' to url '" + getUrl() + "' with metadata '"
               + metadata.getHashtable() + "' : " + e.getMessage(), e);
      }
   }

   public void setEventName(String eventName) {
      this.eventName = eventName;
   }

   public String getEventName() {
      return eventName;
   }

   public void addMetadata(List<String> metadata) {
      Validate.isTrue(metadata.size() > 1);

      this.metadata.addMetadata(metadata.get(0),
            metadata.subList(1, metadata.size()));
   }
}

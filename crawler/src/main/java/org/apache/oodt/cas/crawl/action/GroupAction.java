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
package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Support of a GroupAction could go a ways to simplify command line
 * specification of actions to run. Instead of having to list actions to run one
 * would simply end up referencing the group. In addition, this may end up
 * opening up the possibility to customize the behavior of GroupActions such as
 * not continuing to run actions if one failed. Currently, the crawler continues
 * executing actions even if an action fails this could be a way to tailor that
 * behavior.
 * 
 * @author pramirez (Paul Ramirez)
 */
public class GroupAction extends CrawlerAction {
   private List<CrawlerAction> actionsToCall;

   @Override
   public boolean performAction(File product, Metadata metadata)
         throws CrawlerActionException {
      boolean allSucceeded = true;
      for (CrawlerAction action : actionsToCall) {
         try {
            LOG.info("Performing action (id = " + action.getId()
                  + " : description = " + action.getDescription() + ")");
            if (!action.performAction(product, metadata)) {
               throw new Exception("Action (id = " + action.getId()
                                   + " : description = " + action.getDescription()
                                   + ") returned false");
            }
         } catch (Exception e) {
            allSucceeded = false;
            LOG.warning("Failed to perform crawler action : " + e.getMessage());
         }

      }
      return allSucceeded;
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.notNull(actionsToCall, "Must specify actionsToCall");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public void setActionsToCall(List<CrawlerAction> actionsToCall) {
      this.actionsToCall = actionsToCall;
   }
}

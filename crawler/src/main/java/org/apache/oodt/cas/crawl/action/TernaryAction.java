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

//OODT imports
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * While branching seems more along the lines of a workflow deal having support
 * for decisions based on success or failure of an action can help support
 * slightly more complex ingest scenarios. Since an action returns a true or
 * false this action could be as simple as taking one action as a condition
 * action and then running either a success or failure action. This action
 * should allow the success or failure action to remain unspecified.
 * 
 * @since OODT-36
 * @author pramirez (Paul Ramirez)
 */
public class TernaryAction extends CrawlerAction {
   private CrawlerAction conditionAction;
   private CrawlerAction successAction;
   private CrawlerAction failureAction;

   @Override
   public boolean performAction(File product, Metadata metadata)
         throws CrawlerActionException {
      LOG.info("Performing action (id = " + conditionAction.getId()
            + " : description = " + conditionAction.getDescription() + ")");
      boolean passedCondition = conditionAction
            .performAction(product, metadata);
      if (passedCondition) {
         LOG.info("Performing action (id = " + successAction.getId()
               + " : description = " + successAction.getDescription() + ")");
         return (successAction == null) || successAction.performAction(
             product, metadata);
      } else {
         LOG.info("Performing action (id = " + failureAction.getId()
               + " : description = " + failureAction.getDescription() + ")");
         return (failureAction == null) || failureAction.performAction(
             product, metadata);
      }
   }

   public void setConditionAction(CrawlerAction conditionAction) {
      this.conditionAction = conditionAction;
   }

   public void setSuccessAction(CrawlerAction successAction) {
      this.successAction = successAction;
   }

   public void setFailureAction(CrawlerAction failureAction) {
      this.failureAction = failureAction;
   }
}

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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//Spring imports
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Sets;

/**
 * A repository and reader for {@link CrawlerAction}s associated with Crawler
 * lifecycle phases: preIngest, postIngestSuccess and postIngestFail.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class CrawlerActionRepo {

   private LinkedList<CrawlerAction> preIngestActions;
   private LinkedList<CrawlerAction> postIngestOnFailActions;
   private LinkedList<CrawlerAction> postIngestOnSuccessActions;

   public CrawlerActionRepo() {
      this.preIngestActions = new LinkedList<CrawlerAction>();
      this.postIngestOnFailActions = new LinkedList<CrawlerAction>();
      this.postIngestOnSuccessActions = new LinkedList<CrawlerAction>();
   }

   public Set<CrawlerAction> getActions() {
      Set<CrawlerAction> actions = Sets.newHashSet();
      actions.addAll(preIngestActions);
      actions.addAll(postIngestOnFailActions);
      actions.addAll(postIngestOnSuccessActions);
      return actions;
   }

   public List<CrawlerAction> getPreIngestActions() {
      return this.preIngestActions;
   }

   public List<CrawlerAction> getPostIngestOnFailActions() {
      return this.postIngestOnFailActions;
   }

   public List<CrawlerAction> getPostIngestOnSuccessActions() {
      return this.postIngestOnSuccessActions;
   }

   public void loadActionsFromBeanFactory(ApplicationContext context,
         List<String> actionIds) {
      for (String actionId : actionIds) {
         CrawlerAction action = ((CrawlerAction) context.getBean(actionId,
               CrawlerAction.class));
         List<String> phases = action.getPhases();
         for (String phase : phases) {
            switch (CrawlerActionPhases.getPhaseByName(phase)) {
               case PRE_INGEST:
                  preIngestActions.add(action);
                  break;
               case POST_INGEST_SUCCESS:
                  postIngestOnSuccessActions.add(action);
                  break;
               case POST_INGEST_FAILURE:
                  postIngestOnFailActions.add(action);
                  break;
               default:
                  throw new RuntimeException("Phase '" + phase
                        + "' is not supported");
            }
         }
      }
   }
}

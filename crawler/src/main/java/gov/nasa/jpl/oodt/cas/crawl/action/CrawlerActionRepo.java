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


package gov.nasa.jpl.oodt.cas.crawl.action;

//JDK imports
import java.util.LinkedList;
import java.util.List;

//Spring imports
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A repository and reader for {@link CrawlerAction}s associated with Crawler
 * lifecycle phases: preIngest, postIngestSuccess and postIngestFail
 * </p>.
 */
public class CrawlerActionRepo implements CrawlerActionPhases {

    LinkedList<CrawlerAction> preIngestActions;

    LinkedList<CrawlerAction> postIngestOnFailActions;

    LinkedList<CrawlerAction> postIngestOnSuccessActions;

    public CrawlerActionRepo() {
        this.preIngestActions = new LinkedList<CrawlerAction>();
        this.postIngestOnFailActions = new LinkedList<CrawlerAction>();
        this.postIngestOnSuccessActions = new LinkedList<CrawlerAction>();
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
                if (phase.equals(PRE_INGEST))
                    this.preIngestActions.add(action);
                else if (phase.equals(POST_INGEST_SUCCESS))
                    this.postIngestOnSuccessActions.add(action);
                else if (phase.equals(POST_INGEST_FAILURE))
                    this.postIngestOnFailActions.add(action);
            }
        }
    }

}

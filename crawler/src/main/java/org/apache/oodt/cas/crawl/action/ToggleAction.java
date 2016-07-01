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
import java.util.logging.Level;

//OODT imports
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Allows for cases where there are one of two (or more) actions which needs to
 * run. For example: sometimes the same file types have different checksum files
 * associated with it, this allows for either case.
 * 
 * @author pramirez (Paul Ramirez)
 */
public class ToggleAction extends CrawlerAction {

   private List<Toggle> toggles;
   private boolean shortCircuit;

   @Override
   public boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException {
      if (this.toggles != null && this.toggles.size() > 0) {
         boolean globalSuccess = false;
         for (Toggle toggle : this.toggles) {
            CrawlerAction currentAction = null;
            try {
               if (toggle.isOn(product, productMetadata)
                     && (currentAction = toggle.getCrawlerAction())
                           .performAction(product, productMetadata)) {
                  globalSuccess = true;
                  if (this.shortCircuit) {
                     return true;
                  }
               }
            } catch (Exception e) {
               LOG.log(Level.WARNING, "Failed to run toggle action '"
                     + (currentAction != null ? currentAction.getId() : null)
                     + "' : " + e.getMessage());
            }
         }
         return globalSuccess;
      } else {
         return true;
      }
   }

   public void setToggles(List<Toggle> toggles) {
      this.toggles = toggles;
   }

   public void setShortCircuit(boolean shortCircuit) {
      this.shortCircuit = shortCircuit;
   }

   public abstract class Toggle {

      private CrawlerAction action;

      public void setCrawlerAction(CrawlerAction action) {
         this.action = action;
      }

      public CrawlerAction getCrawlerAction() {
         return this.action;
      }

      public abstract boolean isOn(File product, Metadata productMetadata);

   }
}

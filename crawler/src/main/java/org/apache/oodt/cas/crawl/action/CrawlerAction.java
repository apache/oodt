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
import java.util.logging.Logger;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.commons.spring.SpringSetIdInjectionType;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * An action taken by the crawler during success or fail of one of its lifecycle
 * states: preIngest, postIngestSuccess, or postIngestFail .
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public abstract class CrawlerAction implements SpringSetIdInjectionType {

   public List<String> phases;

   public static Logger LOG = Logger.getLogger(CrawlerAction.class.getName());

   private String description;

   private String id;

   public CrawlerAction() {
   }

   @Override
   public String getId() {
      return id;
   }

   @Override
   public void setId(String id) {
      this.id = id;
   }

   @Required
   public void setPhases(List<String> phases) {
      this.phases = phases;
   }

   public List<String> getPhases() {
      return this.phases;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getDescription() {
      return this.description;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof CrawlerAction) {
         return ((CrawlerAction) obj).id.equals(id);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return id.hashCode();
   }

   public void validate() throws CrawlerActionException {
      try {
         Validate.notNull(id, "Must specify id");
         Validate.notNull(phases, "Must specify phases");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
   }

   public abstract boolean performAction(File product, Metadata productMetadata)
         throws CrawlerActionException;

}

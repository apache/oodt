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
package org.apache.oodt.cas.crawl;

//JDK imports
import java.io.File;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

/**
 * {@link ProductCrawler} used for testing the appropriated states are
 * entered for each part of the crawler's workflow.
 *
 * @author bfoster (Brian Foster)
 */
public class StateAwareProductCrawler extends ProductCrawler{

   private boolean passPreconditions = true;
   private boolean passExtraction = true;
   private boolean passRenaming = true;
   private boolean passRequiredMetadata = true;
   private boolean passPreIngestActions = true;
   private boolean passIngest = true;

   private boolean ranPreconditions = false;
   private boolean ranExtraction = false;
   private boolean ranRenaming = false;
   private boolean ranRequiredMetadata = false;
   private boolean ranPreIngestActions = false;
   private boolean ranIngest = false;
   private boolean ranPostIngestSuccessActions = false;
   private boolean ranPostIngestFailActions = false;

   public void markFailPreconditions() {
      passPreconditions = false;
   }
   
   public void markFailExtraction() {
      passExtraction = false;
   }

   public void markFailRenaming() {
      passRenaming = false;
   }

   public void markFailRequiredMetadata() {
      passRequiredMetadata = false;
   }

   public void markFailPreIngestActions() {
      passPreIngestActions = false;
   }

   public void markSkipIngest() {
      this.setSkipIngest(true);
   }

   public void markFailIngest() {
      passIngest = false;
   }

   public boolean ranPreconditions() {
      return ranPreconditions;
   }

   public boolean ranExtraction() {
      return ranExtraction;
   }

   public boolean ranRenaming() {
      return ranRenaming;
   }

   public boolean ranRequiredMetadata() {
      return ranRequiredMetadata;
   }

   public boolean ranPreIngestActions() {
      return ranPreIngestActions;
   }

   public boolean ranIngest() {
      return ranIngest;
   }

   public boolean ranPostIngestSuccessActions() {
      return ranPostIngestSuccessActions;
   }

   public boolean ranPostIngestFailActions() {
      return ranPostIngestFailActions;
   }

   @Override
   protected boolean passesPreconditions(File p) {
      ranPreconditions = true;
      return passPreconditions;
   }

   @Override
   protected Metadata getMetadataForProduct(File p)
      throws Exception {
      ranExtraction = true;
      if (passExtraction) {
         return new Metadata();
      } else {
         throw new Exception("Failed Extraction");
      }
   }

   @Override
   protected File renameProduct(File p, Metadata m)
         throws Exception {
      ranRenaming = true;
      if (passRenaming) {
         return p;
      } else {
         throw new Exception("Failed Renaming");
      }
   }
   
   @Override
   boolean containsRequiredMetadata(Metadata m) {
      ranRequiredMetadata = true;
      return passRequiredMetadata;
   }

   @Override
   boolean performPreIngestActions(File p, Metadata m) {
      ranPreIngestActions = true;
      return passPreIngestActions;
   }

   @Override
   boolean ingest(File p, Metadata m) {
      ranIngest = true;
      return passIngest;
   }

   @Override
   boolean performPostIngestOnSuccessActions(File p, Metadata m) {
      ranPostIngestSuccessActions = true;
      return true;
   }

   @Override
   boolean performPostIngestOnFailActions(File p, Metadata m) {
      ranPostIngestFailActions = true;
      return true;
   }
}

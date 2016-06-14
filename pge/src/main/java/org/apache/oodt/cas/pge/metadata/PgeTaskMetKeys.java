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
package org.apache.oodt.cas.pge.metadata;

//Google imports
import com.google.common.annotations.VisibleForTesting;

/**
 * PGETaskInstance Reserved Metadata keys.
 *
 * @author bfoster (Brian Foster)
 */
public enum PgeTaskMetKeys {

   /**
    * PGE Name used to create the execution script file name and name the Java Logger.
    */
   NAME(
         "PGETask/Name",
         "PGETask_Name"),
   /**
    * Path to CAS-PGE's configuration file.
    */
   CONFIG_FILE_PATH(
         "PGETask/ConfigFilePath",
         "PGETask_ConfigFilePath"),
   /**
    * CAS-PGE's ConfigBuilder classpath.
    */
   PGE_CONFIG_BUILDER(
         "PGETask/PgeConfigBuilder",
         "PGETask_PgeConfigBuilder"),
   /**
    * The java logger {@link FileHandler} pattern (only for filename though).
    */
   LOG_FILENAME_PATTERN(
         "PGETask/LogFilenamePattern",
         "PGETask_LogFilenamePattern"),
   /**
    * List of {@link ConfigFilePropertyAdder}s classpaths to be run.
    */
   PROPERTY_ADDERS(
         "PGETask/PropertyAdders",
         "PGETask_PropertyAdderClasspath",
         true),
   /**
    * {@link FileStager}s classpath.
    */
   FILE_STAGER(
         "PGETask/FileStager",
         "PGETask_FileStager"),
   /**
    * List of {@link ConfigFilePropertyAdder}s classpaths to be run.
    */
   DUMP_METADATA(
         "PGETask/DumpMetadata",
         "PGETask_DumpMetadata"),
   /**
    * Set by CAS-PGE to the number of milliseconds it took CAS-PGE to run.
    */
   PGE_RUNTIME(
         "PGETask/Runtime",
         "PGETask_Runtime"),
   /**
    * CAS Workflow Manager URL to which CAS-PGE should update it's status
    * and metadata.
    */
   WORKFLOW_MANAGER_URL(
         "PGETask/WorkflowManagerUrl",
         "PCS_WorkflowManagerUrl"),
   /**
    * CAS File Manager URL used for queries.
    */
   QUERY_FILE_MANAGER_URL(
         "PGETask/Query/FileManagerUrl",
         "PCS_FileManagerUrl"),
   /**
    * CAS File Manager URL used for product ingestion.
    */
   INGEST_FILE_MANAGER_URL(
         "PGETask/Ingest/FileManagerUrl",
         "PCS_FileManagerUrl"),
   /**
    * The {@link DataTransferFactory} used for product staging.
    */
   QUERY_CLIENT_TRANSFER_SERVICE_FACTORY(
         "PGETask/Query/ClientTransferServiceFactory",
         "PCS_ClientTransferServiceFactory"),
   /**
    * The {@link DataTransferFactory} used for product ingestion.
    */
   INGEST_CLIENT_TRANSFER_SERVICE_FACTORY(
         "PGETask/Ingest/ClientTransferServiceFactory",
         "PCS_ClientTransferServiceFactory"),
   /**
    * Path to Crawler Spring XML config file.
    */
   CRAWLER_CONFIG_FILE(
         "PGETask/Ingest/CrawlerConfigFile",
         "PCS_ActionRepoFile"),
   /**
    * The IDs of the {@link CrawlerAction}s in the  to run.
    */
   ACTION_IDS(
         "PGETask/Ingest/ActionsIds",
         "PCS_ActionsIds",
         true),
   /**
    * If set to true the crawler will crawl for directories instead of files.
    */
   CRAWLER_CRAWL_FOR_DIRS(
         "PGETask/Ingest/CrawlerCrawlForDirs",
         "PCS_CrawlerCrawlForDirs"),
   /**
    * If set to true the crawler will perform a deep crawl for files.
    */
   CRAWLER_RECUR(
         "PGETask/Ingest/CrawlerRecur",
         "PCS_CrawlerRecur"),
   /**
    * Path to AutoDetectProductCrawler's MimeExtractorRepo XML config.
    */
   MIME_EXTRACTOR_REPO(
         "PGETask/Ingest/MimeExtractorRepo",
         "PGETask_MimeExtractorRepo"),
   /**
    * List of metadata keys required for Product ingest.
    */
   REQUIRED_METADATA(
         "PGETask/Ingest/RequiredMetadata",
         "PCS_RequiredMetadata",
         true),
   /**
    * If set to true then will attempt to ingest all Product's even if one
    * fails. If false, will bail ingest after first failed Product ingest.
    */
   ATTEMPT_INGEST_ALL(
         "PGETask/Ingest/AttemptIngestAll",
         "PGETask_AttemptIngestAll"),
   
   /**
    * Identifies the metadata file name extension to use when CAS-PGE 
    * is running in legacy mode and generating metadata files for the 
    * StdProductCrawler. If not set, will default in CAS-PGE to .met.
    * Note, there is no new version of this property, it only exists in
    * legacy mode, but an attempt is made to provide a new path style/grouped
    * version of the key for forward compat.
    */
   MET_FILE_EXT("PGETask/Ingest/MetFileExtension", 
                "PCS_MetFileExtension");


   public static final String USE_LEGACY_PROPERTY = "org.apache.oodt.cas.pge.task.metkeys.legacyMode";

   @VisibleForTesting String name;
   @VisibleForTesting String legacyName;
   private boolean isVector;

   PgeTaskMetKeys(String name, String legacyName) {
      this(name, legacyName, false);
   }

   PgeTaskMetKeys(String name, String legacyName, boolean isVector) {
      this.name = name;
      this.legacyName = legacyName;
      this.isVector = isVector;
   }

   public String getName() {
      return Boolean.getBoolean(USE_LEGACY_PROPERTY) ? legacyName : name;
   }

   public boolean isVector() {
      return isVector;
   }

   public static PgeTaskMetKeys getByName(String name) {
      for (PgeTaskMetKeys key : values()) {
         if (key.getName().equals(name)) {
            return key;
         }
      }
      return null;
   }

   @Override
   public String toString() {
      return getName();
   }
}

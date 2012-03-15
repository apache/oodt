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

   NAME("PGETask/Name", "PGETask_Name"),
   CONFIG_FILE_PATH("PGETask/ConfigFilePath", "PGETask_ConfigFilePath"),
   LOG_FILE_PATTERN("PGETask/LogFilePattern", "PGETask_LogFilePattern"),
   PROPERTY_ADDERS("PGETask/PropertyAdders", "PGETask_PropertyAdderClasspath"),
   PGE_RUNTIME("PGETask/Runtime", "PGETask_Runtime"),
   ATTEMPT_INGEST_ALL("PGETask/AttemptIngestAll", "PGETask_AttemptIngestAll");

   public static final String USE_LEGACY_PROPERTY = "org.apache.oodt.cas.pge.legacyMode";

   @VisibleForTesting String name;
   @VisibleForTesting String legacyName;

   PgeTaskMetKeys(String name, String legacyName) {
      this.name = name;
      this.legacyName = legacyName;
   }

   public String getName() {
      return Boolean.getBoolean(USE_LEGACY_PROPERTY) ? legacyName : name;
   }
}

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
package org.apache.oodt.cas.pge.logging;

//JDK imports
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

/**
 * Log Handler which only writes logs for a given CAS-PGE name.  This allows for
 * multiple CAS-PGE instances to exist in the same JVM but still create their
 * own log files.
 *
 * @author bfoster (Brian Foster)
 */
public class PgeLogHandler extends StreamHandler {

   private String workflowInstId;

   public PgeLogHandler(String workflowInstId, OutputStream os)
         throws SecurityException, FileNotFoundException {
      super(os, new SimpleFormatter());
      this.workflowInstId = workflowInstId;
   }

   @Override
   public void publish(LogRecord record) {
      if (record instanceof PgeLogRecord
            && workflowInstId.equals(((PgeLogRecord) record).getWorkflowInstId())) {
         super.publish(record);
      }
   }
}

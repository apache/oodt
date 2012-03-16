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
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * CAS-PGE's {@link LogRecord}.
 *
 * @author bfoster (Brian Foster)
 */
public class PgeLogRecord extends LogRecord {

   private static final long serialVersionUID = 2334166761035931387L;

   private String pgeName;

   public PgeLogRecord(String pgeName, Level level, String msg) {
      super(level, msg);
      this.pgeName = pgeName;
   }

   public PgeLogRecord(String pgeName, Level level, String msg, Throwable t) {
      this(pgeName, level, msg);
      setThrown(t);
   }

   public String getPgeName() {
      return pgeName;
   }
}

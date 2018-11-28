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


package org.apache.oodt.cas.curation.structs;

/**
 *
 * Met keys for the {@link IngestionTask#getStatus()} field.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public interface IngestionTaskStatus {

  public static final String FINISHED = "Finished";
  
  public static final String STARTED = "Started";
  
  public static final String NOT_STARTED = "Not Started";
}

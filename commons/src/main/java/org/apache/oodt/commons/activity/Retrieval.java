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

package org.apache.oodt.commons.activity;

import java.util.List;

/**
   Retrieval is a vehicle for getting activities and their associated incidents from {@link Storage}.

   @author S. Hardman
   @version $Revision: 1.1 $
*/
public interface Retrieval {

   /**
      Retrieve the list of activities.

      @return A list of {@link StoredActivity} classes.
      @throws ActivityException If an error occurs accessing the Storage.
   */
   List retrieve() throws ActivityException;
}

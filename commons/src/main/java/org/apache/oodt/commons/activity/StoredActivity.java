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

import java.util.ArrayList;
import java.util.List;


/**
   This class holds an activity retrieved from storage.

   @author S. Hardman
   @version $Revision: 1.1 $
   @see StoredIncident
*/
public class StoredActivity {

   /** The identifier for the activity.
   */
   private String activityID;

   /** The list of incidents associated with the activity.
   */
   private List incidents;


   /**
      This contructor initializes the activity.

      @param activityID The identifier for the activity.
   */
   public StoredActivity(String activityID) {
      this.activityID = activityID;
      incidents = new ArrayList();
   }


   /**
      Add an incident to the list of incidents.

      @param incident The StoredIncident class to add to the list.
   */
   public void addIncident(StoredIncident incident) {
      incidents.add(incident);
   }

   /**
      Return the activity identifier.

      @return The activity identifier.
   */
   public final String getActivityID() {
      return (activityID);
   }


   /**
      Return the list of associated incidents.

      @return The list of associated incidents.
   */
   public final List getIncidents() {
      return (incidents);
   }
}

// Copyright (c) 2005, California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
// 
// $Id: StoredActivity.java,v 1.1 2005-01-05 18:22:26 shardman Exp $

package jpl.eda.activity;

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

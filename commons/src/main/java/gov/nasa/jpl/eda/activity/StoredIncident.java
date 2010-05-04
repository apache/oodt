// Copyright (c) 2005, California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
// 
// $Id: StoredIncident.java,v 1.1 2005-01-05 18:22:26 shardman Exp $

package jpl.eda.activity;


/**
   This class holds an incident retrieved from storage.

   @author S. Hardman
   @version $Revision: 1.1 $
   @see StoredActivity
*/
public class StoredIncident {

   /** The name of the incident class.
   */
   private String className;

   /** The time the incident occurred.
   */
   private long occurTime;

   /** Detailed information regarding the incident.
   */
   private String detail;


   /**
      This contructor initializes the incident.

      @param className The name of the incident class.
      @param occurTime The time the incident occurred.
      @param detail Detailed information regarding the incident.
   */
   public StoredIncident(String className, long occurTime, String detail) {
      this.className = className;
      this.occurTime = occurTime;
      this.detail = detail;
   }


   /**
      Return the name of the incident class.

      @return The name of the incident class.
   */
   public final String getClassName() {
      return (className);
   }


   /**
      Return the time the incident occurred.

      @return The time the incident occurred.
   */
   public final long getOccurTime() {
      return (occurTime);
   }


   /**
      Return the detailed information regarding the incident.

      @return The detailed information regarding the incident.
   */
   public final String getDetail() {
      return (detail);
   }
}

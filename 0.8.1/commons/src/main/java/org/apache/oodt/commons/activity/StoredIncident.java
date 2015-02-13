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

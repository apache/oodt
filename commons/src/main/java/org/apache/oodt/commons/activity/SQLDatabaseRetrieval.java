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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
   <p>This class enables retrieval of activity incidents from just about
   any database management system. It should support MySQL, PostgreSQL,
   Oracle and Sybase.
   </p>

   <p>This class uses the following properties:
   <ul>
      <li><code>org.apache.oodt.commons.activity.SQLDatabaseRetrieval.driver</code><br>
      Must contain the name of the JDBC driver class. See the following
      examples:
      <ul>
         <li>com.mysql.jdbc.Driver</li>
         <li>org.postgresql.Driver</li>
         <li>oracle.jdbc.driver.OracleDriver</li>
         <li>com.sybase.jdbc2.jdbc.SybDriver</li>
      </ul>
      </li>

      <li><code>org.apache.oodt.commons.activity.SQLDatabaseRetrieval.url</code><br>
      Must contain the URL specification for the target database. See the
      following examples:
      <ul>
         <li>jdbc:mysql://host:port/database</li>
         <li>jdbc:postgresql://host:port/database</li>
         <li>jdbc:oracle:thin:@host:port:database</li>
         <li>jdbc:sybase::Tds:host:port/database</li>
      </ul>
      </li>

      <li><code>org.apache.oodt.commons.activity.SQLDatabaseRetrieval.user</code><br>
      Must contain the user name for the target database.</li>

      <li><code>org.apache.oodt.commons.activity.SQLDatabaseRetrieval.password</code><br>
      Must contain the password for the target database.</li>

   </ul>
   </p>

   <p>This class expects the following table to exist in the target
   database (data types will vary depending on the vendor):<br>
      <pre>
      create table incidents (
         activityID varchar(32) not null,
         className varchar(255) not null,
         occurTime bigint not null default 0,
         detail text null,
         primary key (activityID, className, occurTime))
      </pre>
   </p>

   @author S. Hardman
   @version $Revision: 1.1 $
*/
public class SQLDatabaseRetrieval implements Retrieval {

   /** The database driver.
   */
   private String driver;

   /** The URL for the database.
   */
   private String url;

   /** The user name for the database.
   */
   private String user;

   /** The password associated with the user name.
   */
   private String password;

   /** The database connection.
   */
   private Connection connection;


   /**
      This constructor grabs the necessary system properties for the database connection.
   */
   public SQLDatabaseRetrieval() {

      // Grab the properties and make sure they are all there.
      driver = System.getProperty("org.apache.oodt.commons.activity.SQLDatabaseRetrieval.driver");
      url = System.getProperty("org.apache.oodt.commons.activity.SQLDatabaseRetrieval.url");
      user = System.getProperty("org.apache.oodt.commons.activity.SQLDatabaseRetrieval.user");
      password = System.getProperty("org.apache.oodt.commons.activity.SQLDatabaseRetrieval.password");

      if ((driver == null) || (url == null) || (user == null) || (password == null)) {
         throw new IllegalStateException("SQLDatabaseRetrieval(): Required system properties `org.apache.oodt.commons.activity.SQLDatabaseRetrieval.[driver,url,user,password]' are not completely defined.");
      }
   }


   /**
      Retrieve the list of activities.

      @return A list of {@link StoredActivity} classes.
      @throws ActivityException If an error occurs opening or accessing the database.
   */
   public List retrieve() throws ActivityException {

      Connection conn = null;
      Statement stmt = null;
      List activities = new ArrayList();

      try {
         conn = openConnection();
         stmt = conn.createStatement();
         String sqlCmd = "SELECT activityID, className, occurTime, detail FROM incidents ORDER BY activityID, occurTime";
         String activityID = "";
         StoredActivity activity = null;
         ResultSet rs = stmt.executeQuery(sqlCmd);
         while (rs.next()) {
             String currActivityID = rs.getString(1);
             if (!activityID.equals(currActivityID)) {
                activity = new StoredActivity(currActivityID);
                activities.add(activity);
                activityID = currActivityID;
             }
             activity.addIncident(new StoredIncident(rs.getString(2), rs.getLong(3), rs.getString(4)));
         }

         return (activities);
      }
      catch (ClassNotFoundException e) {
         throw new ActivityException("SQLDatabaseRetrieval.retrieve(): An exception occurred locating the JDBC driver class.", e);
      }
      catch (SQLException e) {
         throw new ActivityException("SQLDatabaseRetrieval.retrieve(): An exception occurred opening or accessing the database.", e);
      }
      finally {
         try {
            if (stmt != null) {
               stmt.close();
            }
            if (conn != null) {
               conn.close();
            }
         }
         catch (SQLException ignored) {}
      }
   }


   /**
      This method opens the database connection.

      @returns The Connection object.
      @throws ClassNotFoundException If the JDBC driver class can't be located.
      @throws SQLException If an error occurs opening the database connection.
   */
	private Connection openConnection() throws ClassNotFoundException, SQLException {
      Class.forName(driver);
      return (DriverManager.getConnection(url, user, password));
   }


   /**
      Execute the SQLDatabaseRetrieval class via the command-line.

      The program exits with status 0 on success, 1 on failure.

      @param argv The command-line arguments.
   */
   public static void main(String[] argv) {

      try {
         SQLDatabaseRetrieval retrieval = new SQLDatabaseRetrieval();
         List activities = retrieval.retrieve();
         for (Object activity1 : activities) {
            StoredActivity activity = (StoredActivity) activity1;
            System.out.println("Activity: " + activity.getActivityID());
            List incidents = activity.getIncidents();
            for (Object incident1 : incidents) {
               StoredIncident incident = (StoredIncident) incident1;
               System.out.println(
                   "   Incident: " + incident.getClassName() + ", " + incident.getOccurTime() + ", " + incident
                       .getDetail());
            }
         }
         System.exit(0);
      }
      catch (Exception e) {
         Throwable cause = e.getCause();
         String message = "";
         if (cause != null) {
            System.err.println(e.getMessage() + " Additional information: " + cause.getMessage());
         }
         else {
            System.err.println(e.getMessage());
         }
         System.exit(1);
      }
   }
}

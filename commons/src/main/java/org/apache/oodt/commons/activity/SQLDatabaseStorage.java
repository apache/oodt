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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

/**
 * <p>
 * This class enables storage of activity incidents in just about any database
 * management system. It should support MySQL, PostgreSQL, Oracle and Sybase.
 * </p>
 * 
 * <p>
 * This class uses the following properties:
 * <ul>
 * <li><code>org.apache.oodt.commons.activity.SQLDatabaseStorage.driver</code><br>
 * Must contain the name of the JDBC driver class. See the following examples:
 * <ul>
 * <li>com.mysql.jdbc.Driver</li>
 * <li>org.postgresql.Driver</li>
 * <li>oracle.jdbc.driver.OracleDriver</li>
 * <li>com.sybase.jdbc2.jdbc.SybDriver</li>
 * </ul>
 * </li>
 * 
 * <li><code>org.apache.oodt.commons.activity.SQLDatabaseStorage.url</code><br>
 * Must contain the URL specification for the target database. See the following
 * examples:
 * <ul>
 * <li>jdbc:mysql://host:port/database</li>
 * <li>jdbc:postgresql://host:port/database</li>
 * <li>jdbc:oracle:thin:@host:port:database</li>
 * <li>jdbc:sybase::Tds:host:port/database</li>
 * </ul>
 * </li>
 * 
 * <li><code>org.apache.oodt.commons.activity.SQLDatabaseStorage.user</code><br>
 * Must contain the user name for the target database.</li>
 * 
 * <li><code>org.apache.oodt.commons.activity.SQLDatabaseStorage.password</code>
 * <br>
 * Must contain the password for the target database.</li>
 * 
 * </ul>
 * </p>
 * 
 * <p>
 * This class expects the following table to exist in the target database (data
 * types will vary depending on the vendor):<br>
 * 
 * <pre>
 * create table incidents (
 *          activityID varchar(32) not null,
 *          className varchar(255) not null,
 *          occurTime bigint not null default 0,
 *          detail text null,
 *          primary key (activityID, className, occurTime))
 * </pre>
 * 
 * </p>
 * 
 * @author S. Hardman
 * @version $Revision: 1.2 $
 */
public class SQLDatabaseStorage implements Storage {

  /**
   * The data source;
   */
  private DataSource ds;

  /**
   * Constructor given no arguments.
   * 
   * This constructor grabs the necessary system properties and opens the
   * database connection based on the property values.
   */
  public SQLDatabaseStorage() {

    // Grab the properties and make sure they are all there.
    String driver = System
        .getProperty("org.apache.oodt.commons.activity.SQLDatabaseStorage.driver");
    String url = System
        .getProperty("org.apache.oodt.commons.activity.SQLDatabaseStorage.url");
    String user = System
        .getProperty("org.apache.oodt.commons.activity.SQLDatabaseStorage.user");
    String password = System
        .getProperty("org.apache.oodt.commons.activity.SQLDatabaseStorage.password");

    if ((driver == null) || (url == null) || (user == null)
        || (password == null)) {
      throw new IllegalStateException(
          "SQLDatabaseStorage(): Required system properties `org.apache.oodt.commons.activity.SQLDatabaseStorage.[driver,url,user,password]' are not completely defined.");
    }

    this.ds = DatabaseConnectionBuilder.buildDataSource(user, password, driver,
        url);
  }

  /**
   * This method stores the list of incidents for the activity in the database
   * table named "incidents".
   * 
   * @param id
   *          The activity identifier.
   * @param incidents
   *          A list of {@link Incident}.
   */
  public void store(String id, List incidents) {
    Statement statement = null;
    Connection conn = null;

    try {
      conn = this.ds.getConnection();
      statement = conn.createStatement();
      for (Object incident1 : incidents) {
        Incident incident = (Incident) incident1;
        statement
            .executeUpdate("insert into incidents (activityID, className, occurTime, detail) values ('"
                           + id
                           + "', '"
                           + incident.getClass().getName()
                           + "', "
                           + incident.getTime().getTime()
                           + ", '"
                           + escapeSingleQuote(incident.toString()) + "')");
      }
    } catch (SQLException e) {
      System.err
          .println("SQLDatabaseStorage.store(): Ignoring an exception that occurred while inserting a row into the database. Specifically, exception '"
              + e.getClass().getName()
              + "' occurred with message '"
              + e.getMessage() + "'");
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException ignore) {
        }
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException ignore) {
        }
      }
    }
  }

  /**
   * This method closes the database connection.
   * 
   * @throws Throwable
   *           If something goes wrong.
   */
  public void finalize() throws Throwable {
    this.ds = null;
    super.finalize();
  }

  /**
   * This method will escape any single quotes found in the input string and
   * return the escaped string. This will ready the string for insertion into a
   * database. The single quote is escaped by inserting an additional single
   * quote in front of it in the string. If some considerate developer has
   * already escaped the single quotes in the input string, this method will
   * essentially do nothing.
   * 
   * @param inputString
   *          The string to be escaped.
   * @return The escaped string.
   */
  public static String escapeSingleQuote(String inputString) {
    int index = inputString.indexOf('\'');
    if (index == -1) {
      return (inputString);
    }

    String outputString = inputString;
    while (index != -1) {

      // If the single quote is the last character in the string or
      // the next character is not another single quote, insert a
      // single quote in front of the current single quote.
      if ((index == (outputString.length() - 1))
          || (outputString.charAt(index + 1) != '\'')) {
        outputString = outputString.substring(0, index) + "'"
            + outputString.substring(index);
      }

      // If we are not at the end of the string, check for another
      // single quote.
      if ((index + 2) <= (outputString.length() - 1)) {
        index = outputString.indexOf('\'', index + 2);
      } else {
        index = -1;
      }
    }
    return (outputString);
  }
}

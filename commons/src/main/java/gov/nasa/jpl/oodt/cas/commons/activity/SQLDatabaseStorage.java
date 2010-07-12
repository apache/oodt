// Copyright (c) 2004 California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: SQLDatabaseStorage.java,v 1.2 2005-01-04 00:49:14 shardman Exp $

package jpl.eda.activity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import jpl.eda.util.Utility;

/**
	<p>This class enables storage of activity incidents in just about
	any database management system. It should support MySQL, PostgreSQL,
	Oracle and Sybase.</p>

	<p>This class uses the following properties:
	<ul>
		<li><code>jpl.eda.activity.SQLDatabaseStorage.driver</code><br>
		Must contain the name of the JDBC driver class. See the following
		examples:
		<ul>
			<li>com.mysql.jdbc.Driver</li>
			<li>org.postgresql.Driver</li>
			<li>oracle.jdbc.driver.OracleDriver</li>
			<li>com.sybase.jdbc2.jdbc.SybDriver</li>
		</ul>
		</li>

		<li><code>jpl.eda.activity.SQLDatabaseStorage.url</code><br>
		Must contain the URL specification for the target database. See the
		following examples:
		<ul>
			<li>jdbc:mysql://host:port/database</li>
			<li>jdbc:postgresql://host:port/database</li>
			<li>jdbc:oracle:thin:@host:port:database</li>
			<li>jdbc:sybase::Tds:host:port/database</li>
		</ul>
		</li>

		<li><code>jpl.eda.activity.SQLDatabaseStorage.user</code><br>
		Must contain the user name for the target database.</li>

		<li><code>jpl.eda.activity.SQLDatabaseStorage.password</code><br>
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
	@version $Revision: 1.2 $
*/
public class SQLDatabaseStorage implements Storage {

	/**
		The database connection.
	*/
	private Connection connection;


	/**
		Constructor given no arguments.

		This constructor grabs the necessary system properties and opens
		the database connection based on the property values.
	*/
	public SQLDatabaseStorage() {

		// Grab the properties and make sure they are all there.
		String driver = System.getProperty("jpl.eda.activity.SQLDatabaseStorage.driver");
		String url = System.getProperty("jpl.eda.activity.SQLDatabaseStorage.url");
		String user = System.getProperty("jpl.eda.activity.SQLDatabaseStorage.user");
		String password = System.getProperty("jpl.eda.activity.SQLDatabaseStorage.password");

		if ((driver == null) || (url == null) || (user == null) || (password == null)) {
			throw new IllegalStateException("SQLDatabaseStorage(): Required system properties `jpl.eda.activity.SQLDatabaseStorage.[driver,url,user,password]' are not completely defined.");
		}

		try {
			// Open the database connection.
         Class.forName(driver);
         connection = DriverManager.getConnection(url, user, password);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("SQLDatabaseStorage(): An exception occurred locating the JDBC driver class. Specifically, exception '" + e.getClass().getName() + "' occurred with message '" + e.getMessage() + "'"); 
		}
		catch (SQLException e) {
			throw new IllegalStateException("SQLDatabaseStorage(): An exception occurred connecting to the database. Specifically, exception '" + e.getClass().getName() + "' occurred with message '" + e.getMessage() + "'");
		}
	}


	/**
		This method stores the list of incidents for the activity in the
		database table named "incidents".

		@param id The activity identifier.
		@param incidents A list of {@link Incident}.
	*/
	public void store(String id, List incidents) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			for (Iterator i = incidents.iterator(); i.hasNext();) {
				Incident incident = (Incident) i.next();
				statement.executeUpdate("insert into incidents (activityID, className, occurTime, detail) values ('" + id + "', '" + incident.getClass().getName() + "', " + incident.getTime().getTime() + ", '" + Utility.escapeSingleQuote(incident.toString()) + "')");
			}
		} catch (SQLException e) {
			System.err.println("SQLDatabaseStorage.store(): Ignoring an exception that occurred while inserting a row into the database. Specifically, exception '" + e.getClass().getName() + "' occurred with message '" + e.getMessage() + "'");
		} finally {
			if (statement != null) try {
				statement.close();
			} catch (SQLException ignore) {}
		}
	}


	/**
		This method closes the database connection.

		@throws Throwable If something goes wrong.
	*/
	public void finalize() throws Throwable {
		connection.close();
		super.finalize();
	}
}


// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
	This class is a wrapper for JDBC.

	@author D. Crichton
	@version $Revision: 1.3 $
*/
public class JDBC_DB
{
  private static Logger LOG = Logger.getLogger(JDBC_DB.class.getName());
	Properties serverProps;
	Connection connect;
	String sql_command;
	Statement stmt;
	ResultSet rs;
	ResultSetMetaData rs_meta;
	int affected;
  	boolean keep_connect_open;
	private boolean autoCommitMode = false;

  /******************************************************************
	**
	** JDBC_DB
	**
	** Constructor method will create class instance and load the
	** Oracle DB driver
	**
	*******************************************************************/

	public  JDBC_DB(
			java.util.Properties sys_props) {

	 		serverProps = sys_props;
			keep_connect_open = true;

	}

	public  JDBC_DB(
			java.util.Properties sys_props,
			Connection srv_connect) {

	  		serverProps = sys_props;
			connect = srv_connect;

	  		keep_connect_open = srv_connect != null;
	}

	public void setAutoCommitMode(boolean autoCommitMode) {
		this.autoCommitMode = autoCommitMode;
	}

	/******************************************************************
	**
	** openConnection
	**
	** Open a connection to the database.
	**
	*******************************************************************/
	public void openConnection() throws SQLException

	{
		openConnection(serverProps.getProperty("org.apache.oodt.commons.util.JDBC_DB.user", "unknown"),
			serverProps.getProperty("org.apache.oodt.commons.util.JDBC_DB.password"),
			serverProps.getProperty("org.apache.oodt.commons.util.JDBC_DB.database"));
	}


	public void openConnection(
		String username,
		String password,
		String database) throws SQLException
	{
		String url, classname;

		if (stmt != null) {
		  stmt.close();
		}

		if (rs != null) {
		  rs.close();
		}

		if (keep_connect_open) {
		  return;
		}

		if (connect != null) {
		  connect.close();
		}


		rs_meta = null;
		connect = null;
		stmt = null;
		rs = null;

		Properties props = new Properties();
		props.put("user", username);

		if (password != null) {
		  props.put("password", password);
		}


		classname = serverProps.getProperty("org.apache.oodt.commons.util.JDBC_DB.driver", "oracle.jdbc.driver.OracleDriver");
		try {
			System.err.println("Attempting to load class " + classname);
			Class.forName(classname);
			System.err.println("Loaded " + classname);
		} catch (ClassNotFoundException e) {
			System.err.println("Can't load JDBC driver \"" + classname + "\": " + e.getMessage());
			LOG.log(Level.SEVERE, e.getMessage());
		}
		url = serverProps.getProperty("org.apache.oodt.commons.util.JDBC_DB.url", "jdbc:oracle:@");
		try {
			if (database != null) {
				System.err.println("Connecting to url+database combo: " + url + database);
				connect = DriverManager.getConnection(url+database, props);
			} else {
				System.err.println("Connecting to full url: " + url);
				connect = DriverManager.getConnection(url, props);
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception during connection creation: " + e.getMessage());
			LOG.log(Level.SEVERE, e.getMessage());
			while (e != null) {
				System.err.println(e.getMessage());
				e = e.getNextException();
			}
		}

		connect.setAutoCommit(autoCommitMode);

	}


	/******************************************************************
	**
	** closeConnection
	**
	** Close a connection to the database.
	**
	*******************************************************************/

	public void closeConnection() {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (keep_connect_open) {
				return;
			}
			if (connect != null) {
				connect.close();
			}
			connect = null;
			rs = null;
			stmt = null;
		} catch (SQLException e) {
			System.err.println("Ignoring database close connection exception");
		}
	}

	/******************************************************************
	**
	** executeSQLCommand
	**
	** Send an SQL command to the DBMS to be executed.
	**
	*******************************************************************/

	public void executeSQLCommand(String cmd) throws SQLException
	{


		/*
		** Get the string, and create the statement
		*/
		sql_command = cmd;

		if (stmt!=null) {
		  stmt.close();
		}

		if (connect == null) {
		  openConnection();
		}
		if (connect == null) {
			keep_connect_open = false;
			openConnection();
		}
		if (connect == null) {
		  throw new IllegalStateException("Connection is null!!!");
		}
		
		if (connect.isClosed()) {
			connect = null;
			keep_connect_open = false;
			openConnection();
		}
		if (connect == null) {
		  throw new IllegalStateException("Connection is still null!!!");
		}
		if (connect.isClosed()) {
		  throw new IllegalStateException("Connection got closed!");
		}

		stmt = connect.createStatement();
		affected = stmt.executeUpdate(sql_command);
	}

	/******************************************************************
	**
	** executeQuery
	**
	** Send an SQL query to the DBMS to be executed.
	**
	*******************************************************************/
	public ResultSet executeQuery(String cmd) throws SQLException
	{
		sql_command = cmd;


		if (stmt!=null) {
		  stmt.close();
		}

		if (connect == null) {
		  openConnection();
		}
		if (connect == null) {
			keep_connect_open = false;
			openConnection();
		}
		if (connect == null) {
		  throw new IllegalStateException("Connection is null!!!");
		}
		
		if (connect.isClosed()) {
			connect = null;
			keep_connect_open = false;
			openConnection();
		}
		if (connect == null) {
		  throw new IllegalStateException("Connection is still null!!!");
		}
		if (connect.isClosed()) {
		  throw new IllegalStateException("Connection got closed!");
		}

		stmt = connect.createStatement();

		if (rs!=null) {
		  rs.close();
		}

		rs = stmt.executeQuery(sql_command);

		return(rs);


	}

	/******************************************************************
	**
	** getCount
	**
	** Will return a count when user passes sting select count(*) from ...
	**
	*******************************************************************/
	public int getCount(String cmd) throws SQLException
	{
		sql_command = cmd;
		int count;


		if (stmt!=null) {
		  stmt.close();
		}

		stmt = connect.createStatement();

		if (rs!=null) {
		  rs.close();
		}

		rs = stmt.executeQuery(sql_command);

		count = 0;

		while (rs.next())
		{
			count = rs.getInt(1);
		}

		stmt.close();
		rs.close();


		return(count);


	}
	/******************************************************************
	**
	** commit
	**
	**
	**
	*******************************************************************/
	public void commit() throws SQLException
	{
		connect.commit();
	}

	/******************************************************************
	**
	** rollback
	**
	**
	**
	*******************************************************************/
	public void rollback()
	{
		try
		{
			if (connect != null) {
			  connect.rollback();
			}
		}

		catch (SQLException ignored)
		{

		}
	}

	 protected void finalize() throws Throwable
	{
		try
		{
			if ((connect != null) && (!keep_connect_open))
			{
				connect.close();

			}

			if (rs != null)
			{
				rs.close();
			}

			if (stmt != null)
			{
				stmt.close();
			}

		}
		catch (SQLException ignored)
		{
		}
	}

	/******************************************************************
	**
	** Convert Date to string in format for
	** Oracle TO_DATE processing - DD-MM-YY HH24:MI:SS
	**
	**
	*******************************************************************/


	public String toDateStr(java.util.Date inDate)
	{

		SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	  return fmt.format(inDate);


	}

	public Connection getConnection() throws SQLException
	{
		if (connect == null) {
		  openConnection();
		}
		if (connect == null) {
			keep_connect_open = false;
			openConnection();
		}
		if (connect == null) {
		  throw new IllegalStateException("getConnection can't get a connection pointer");
		}
		return(connect);
	}
}

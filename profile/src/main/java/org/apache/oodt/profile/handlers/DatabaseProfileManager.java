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

package org.apache.oodt.profile.handlers;

import org.apache.oodt.commons.util.XML;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileException;
import org.apache.oodt.profile.ProfileSQLException;
import org.apache.oodt.xmlquery.XMLQuery;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**********************************************************************************
**
** DatabaseProfileManager.java
**
** @author Dan Crichton
** 
** date: 11/10/2000
**
** description: Map a profile into/out of a relational database
**
***********************************************************************************/

public abstract class DatabaseProfileManager implements ProfileManager
{
	/** Add an element to the given node in an XML document.
	 *
	 * @param node To what node to add a new element.
	 * @param name What the new element should be named.
	 * @param value What string value the element should contain; if null, then don't add the element.
	 * @throws DOMException If an error occurs.
	 */
	private static void add(Node node, String name, String value) throws DOMException {
		if (value == null) return;
		XML.add(node, name, value.trim());
	}

	/** Dump profiles in the database.
	 *
	 * This returns a list containing the contents of the profiles.
	 *
	 * @return The profiles in the database, as a list.
	 * @throws ProfileException If an error occurs.
	 */
	public abstract List getProfiles(Connection conn) throws ProfileException;

    	Connection conn;
   	Properties props;


    	/**********************************************************************
    	 **
    	 ** DatabaseProfileManager
     	 **
    	 **
    	***********************************************************************/	

    	public DatabaseProfileManager(Properties props) throws Exception {
		this(props, openConnection(props));
    	}

    	public DatabaseProfileManager (Properties props, Connection conn) 
    	{
		this.conn = conn;
		this.props = props;
    	}


    	/**********************************************************************
    	**
    	** findProfiles
    	**
    	**  Returns a list of matching profiles
    	**
    	***********************************************************************/	
    	public List findProfiles(XMLQuery query) throws DOMException, ProfileException 
    	{
		try
		{
			return(findProfiles(conn, query));
		}
		catch (Exception e)
		{
			throw new ProfileException (e.getMessage());
		}
    	}

    	public abstract List findProfiles(Connection conn, XMLQuery query) throws DOMException, ProfileException;


	public void add(Profile profile) throws ProfileException {

		try
		{
			add(conn, profile);
			conn.commit();
		}
		catch (SQLException e)
		{
			throw new ProfileSQLException (e);
		}
	}
	public abstract void add(Connection conn, Profile profile) throws ProfileException ;
	

	public void addAll(Collection collection) throws ProfileException
	{
		try
                {
                        addAll(conn, collection);
                        conn.commit();
                }
		catch (Exception e)
                {	
			try {
				conn.rollback();
			}catch (SQLException se) {
				throw new ProfileSQLException(se);
			}			
                        throw new ProfileException(e.getMessage());
                }
	}

	public abstract void addAll(Connection conn,Collection collection) throws ProfileException;

	public abstract void clear(Connection conn) throws ProfileException ;

	public void clear() throws ProfileException 
	{
		// Create database connection

		try
		{
			clear(conn);
			conn.commit();
		}
		catch (SQLException e)
		{
			throw new ProfileSQLException (e);
		}
	}
	public boolean contains(Profile profile) throws ProfileException {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	public boolean containsAll(Collection collection) throws ProfileException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public abstract Profile get(Connection conn, String profID) throws ProfileException;

	public Profile get(String profId) throws ProfileException {
		// Create database connection

		try
		{ 
			return(get(conn, profId));
		}
		catch (ProfileException e)
		{
			throw e;
		}
	}

	public Collection getAll() throws ProfileException {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	public boolean isEmpty() throws ProfileException {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	public Iterator iterator() throws ProfileException {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public abstract boolean remove(Connection conn, String profId, String version)
		 throws ProfileException;

	public abstract boolean remove(Connection conn, String profId) throws ProfileException;


	public boolean remove(String profId, String version) throws ProfileException 
	{
		// Create database connection

		try
		{
			boolean status = remove(conn, profId, version);
			conn.commit();
			return(status);
		}
		catch (SQLException e)
		{
			throw new ProfileSQLException (e);
		}
	}

	public boolean remove(String profId) throws ProfileException {
		// Create database connection

		try
		{
			boolean status = remove(conn, profId);
			conn.commit();
			return(status);
		}
		catch (SQLException e)
		{
			throw new ProfileSQLException (e);
		}
	}

	public int size() throws ProfileException {
		try
		{
			return(size(conn));
		}
		catch (ProfileException e)
		{
			throw e;
		}

	}

	public abstract int size(Connection conn) throws ProfileException ;

	public void replace(Profile profile) throws ProfileException {
		// Create database connection

		try
		{
			replace(conn, profile);
			conn.commit();
		}
		catch (Exception e)
		{
			throw new ProfileException(e.getMessage());
		}
	}
	public abstract void replace(Connection conn, Profile profile) throws ProfileException ;

	protected  static Connection openConnection(Properties props) throws SQLException, ProfileException
	{
		// load JDBC driver
		String driver = props.getProperty("org.apache.oodt.util.JDBC_DB.driver","oracle.jdbc.driver.OracleDriver");
		try {
                        System.err.println("Attempting to load class " + driver);
                        Class.forName(driver);
                        System.err.println("Loaded " + driver);
                } catch (ClassNotFoundException e) {
                        throw new ProfileException("Can't load JDBC driver \"" + driver + "\": " +e.getMessage());
                }       

		// get connection
		String url = props.getProperty("org.apache.oodt.util.JDBC_DB.url", "jdbc:oracle:@");
		String database = props.getProperty("org.apache.oodt.util.JDBC_DB.database");
		if(database != null) url += database;

                Connection conn = DriverManager.getConnection(url,
                        		props.getProperty("org.apache.oodt.util.JDBC_DB.user"),
                        		props.getProperty("org.apache.oodt.util.JDBC_DB.password"));

		conn.setAutoCommit(false);
		return conn;
       }
}


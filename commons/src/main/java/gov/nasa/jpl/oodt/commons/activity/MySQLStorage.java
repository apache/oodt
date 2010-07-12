// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: MySQLStorage.java,v 1.1 2004-03-02 19:28:57 kelly Exp $

package jpl.eda.activity;

import javax.sql.DataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

public class MySQLStorage implements Storage {
	public MySQLStorage() {
		String url = System.getProperty("jpl.eda.activity.MySQLStorage.url", System.getProperty("activity.url", ""));
		if (url.length() == 0)
			throw new IllegalStateException("Required system property `jpl.eda.activity.MySQLStorage.url'"
				+ " (or just `activity.url') not defined or is empty");
		try {
			DataSource ds = new MysqlDataSource();
			//ds.setUrl(url);
			connection = ds.getConnection();
		} catch (SQLException ex) {
			throw new IllegalStateException("SQLException connection to `" + url + "': " + ex.getMessage());
		}
	}

	public void store(String id, List incidents) {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			for (Iterator i = incidents.iterator(); i.hasNext();) {
				Incident incident = (Incident) i.next();
				statement.executeUpdate("insert into incidents values ('" + id + "', '"
					+ incident.getClass().getName() + "', " + incident.getTime().getTime() + ", '"
					+ incident.toString() + "')");
			}
		} catch (SQLException ex) {
			System.err.println("Ignoring SQLException: " + ex.getMessage());
		} finally {
			if (statement != null) try {
				statement.close();
			} catch (SQLException ignore) {}
		}
	}

	public void finalize() throws Throwable {
		connection.close();
		super.finalize();
	}

	private Connection connection;
}

/*
create table incidents (
        activityID varchar(32) not null,
	className varchar(255) not null,
	occurTime bigint not null default 0,
	detail text null,
	primary key (activityID, className, occurTime)
);
*/

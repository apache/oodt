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

import javax.sql.DataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

public class MySQLStorage implements Storage {
	public MySQLStorage() {
		String url = System.getProperty("org.apache.oodt.commons.activity.MySQLStorage.url", System.getProperty("activity.url", ""));
		if (url.length() == 0)
			throw new IllegalStateException("Required system property `org.apache.oodt.commons.activity.MySQLStorage.url'"
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

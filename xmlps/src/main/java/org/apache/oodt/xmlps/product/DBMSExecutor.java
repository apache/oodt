/**
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

package org.apache.oodt.xmlps.product;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.xmlps.structs.CDEResult;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 *
 * <p>
 * Executes CDE Queries against an underlying JDBC database, backed by Apache
 * commons-pool and commons-dbcp.
 * </p>
 * .
 */
public class DBMSExecutor {

  private final DataSource dataSource;

  public DBMSExecutor() {
    String jdbcUrl = System.getProperty("xmlps.datasource.jdbc.url");
    String user = System.getProperty("xmlps.datasource.jdbc.user");
    String pass = System.getProperty("xmlps.datasource.jdbc.pass");
    String driver = System.getProperty("xmlps.datasource.jdbc.driver");
    dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass, driver, jdbcUrl);
  }

  public CDEResult executeLocalQuery(String sql) throws SQLException {
    try {
      Connection conn = dataSource.getConnection();
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(sql);
      CDEResult result = new CDEResult(rs, conn);
      return result;
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
    // do not close the Statement or Connection here
    // call CDEResult#close() to close ResultSet and Connection
  }
  
}

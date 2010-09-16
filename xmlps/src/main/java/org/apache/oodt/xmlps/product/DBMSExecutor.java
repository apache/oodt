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
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.FieldType;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlps.structs.CDEResult;
import org.apache.oodt.xmlps.structs.CDERow;
import org.apache.oodt.xmlps.structs.CDEValue;

//JDK imports
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  private DataSource dataSource;

  private static final Logger LOG = Logger.getLogger(DBMSExecutor.class
      .getName());

  public DBMSExecutor() {
    String jdbcUrl = System.getProperty("xmlps.datasource.jdbc.url");
    String user = System.getProperty("xmlps.datasource.jdbc.user");
    String pass = System.getProperty("xmlps.datasource.jdbc.pass");
    String driver = System.getProperty("xmlps.datasource.jdbc.driver");
    dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass, driver,
        jdbcUrl);
  }

  public CDEResult executeLocalQuery(Mapping map, String sql,
      List<String> returnNames) throws SQLException {
    Connection conn = null;
    Statement statement = null;

    CDEResult result = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(sql);

      result = new CDEResult();

      while (rs.next()) {
        CDERow row = toCDERow(rs, map, returnNames);
        result.getRows().add(row);
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (Exception ignore) {
        }

        statement = null;
      }

      if (conn != null) {
        try {
          conn.close();
        } catch (Exception ignore) {
        }

        conn = null;
      }
    }

    return result;

  }

  private CDERow toCDERow(ResultSet rs, Mapping map, List<String> returnNames) {
    CDERow row = new CDERow();
    if (returnNames != null && returnNames.size() > 0) {
      for (Iterator<String> i = returnNames.iterator(); i.hasNext();) {
        String retName = i.next();
        MappingField fld = map.getFieldByLocalName(retName);
        // only handle dynamic fields here
        // if it was a constant field, then it will be dealt with
        // later
        if (fld.getType().equals(FieldType.DYNAMIC)) {
          // go ahead and add it in
          try {
            String elemDbVal = rs.getString(retName);
            for (Iterator<MappingFunc> j = fld.getFuncs().iterator(); j
                .hasNext();) {
              MappingFunc func = j.next();
              CDEValue origVal = new CDEValue(fld.getName(), elemDbVal);
              CDEValue newVal = func.inverseTranslate(origVal);
              elemDbVal = newVal.getVal();
            }

            row.getVals().add(new CDEValue(fld.getName(), elemDbVal));
          } catch (SQLException e) {
            LOG.log(Level.WARNING, "Unable to obtain field: [" + retName
                + "] from result set: message: " + e.getMessage());
          }
        }
      }
    }

    return row;

  }

}

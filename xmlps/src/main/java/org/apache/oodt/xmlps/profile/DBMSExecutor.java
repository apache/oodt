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

package org.apache.oodt.xmlps.profile;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;
import org.apache.oodt.profile.EnumeratedProfileElement;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileAttributes;
import org.apache.oodt.profile.ProfileElement;
import org.apache.oodt.profile.ResourceAttributes;
import org.apache.oodt.xmlps.mapping.FieldType;
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlps.structs.CDEValue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 *
 * <p>
 * Executes Profile Queries against an underlying JDBC database, backed by
 * Apache commons-pool and commons-dbcp.
 * </p>
 * .
 */
public class DBMSExecutor {

  private final DataSource dataSource;

  private static final Logger LOG = Logger.getLogger(DBMSExecutor.class
      .getName());

  public DBMSExecutor() {
    String jdbcUrl = System.getProperty("xmlps.datasource.jdbc.url");
    String user = System.getProperty("xmlps.datasource.jdbc.user");
    String pass = System.getProperty("xmlps.datasource.jdbc.pass");
    String driver = System.getProperty("xmlps.datasource.jdbc.driver");
    try {
      Class.forName(driver);
    } catch (ClassNotFoundException ignore) {
    }

    this.dataSource = DatabaseConnectionBuilder.buildDataSource(user, pass,
        driver, jdbcUrl);

  }

  public List<Profile> executeLocalQuery(Mapping map, String sql,
      String resLocationSpec) throws SQLException {
    Connection conn = null;
    Statement statement = null;

    List<Profile> profiles = null;

    try {
      conn = dataSource.getConnection();
      statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(sql);

      profiles = new Vector<Profile>();

      while (rs.next()) {
        Profile prof = toProfile(rs, map, resLocationSpec);
        profiles.add(prof);
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

    return profiles;

  }

  private Profile toProfile(ResultSet rs, Mapping map, String resLocationSpec) {
    Profile profile = new Profile();
    ResourceAttributes resAttr = profile.getResourceAttributes();
    ProfileAttributes profAttr = profile.getProfileAttributes();
    resAttr.setResClass("system.profile");
    profAttr.setStatusID("active");
    profAttr.setType("profile");

    Metadata met = new Metadata();

    for (Iterator<String> i = map.getFieldNames().iterator(); i.hasNext();) {
      String fldName = i.next();
      MappingField fld = map.getFieldByName(fldName);
      ProfileElement elem = new EnumeratedProfileElement(profile);
      elem.setName(fld.getName());

      try {
        if (fld.getType().equals(FieldType.CONSTANT)) {
          elem.getValues().add(fld.getConstantValue());
        } else {
          String elemDbVal = rs.getString(fld.getDbName());
          for (Iterator<MappingFunc> j = fld.getFuncs().iterator(); j.hasNext();) {
            MappingFunc func = j.next();
            CDEValue origVal = new CDEValue(fld.getName(), elemDbVal);
            CDEValue newVal = func.inverseTranslate(origVal);
            elemDbVal = newVal.getVal();
          }

          elem.getValues().add(elemDbVal);
        }
      } catch (SQLException e) {
        e.printStackTrace();
        LOG.log(Level.WARNING, "Unable to obtain field: ["
            + fld.getLocalName() + "] from result set: message: "
            + e.getMessage());
      }

      met.addMetadata(elem.getName(), (String) elem.getValues().get(0));

      profile.getProfileElements().put(fld.getName(), elem);
    }

    if (resLocationSpec != null) {
      resAttr.getResLocations().add(
          PathUtils.replaceEnvVariables(resLocationSpec, met));
    }

    return profile;
  }

}

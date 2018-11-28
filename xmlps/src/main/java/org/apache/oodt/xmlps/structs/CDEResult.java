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

package org.apache.oodt.xmlps.structs;

//JDK imports
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlquery.Result;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * A {@link Result} that wraps a {@link ResultSet} and returns rows as Strings,
 * applying {@link MappingFuncs} if a {@link Mapping} is provided, and appending
 * constant fields if a {@link List} of {@link CDEValue}s is provided.
 */
public class CDEResult extends Result {

  private static final long serialVersionUID = 1L;

  private static final String ROW_TERMINATOR = "$";

  private final ResultSet rs;
  private final Connection con;
  private Mapping mapping;
  private List<CDEValue> constValues;

  public CDEResult(ResultSet rs, Connection con) {
    this.rs = rs;
    this.con = con;
    setMimeType("text/plain");
  }

  public void setMapping(Mapping mapping) {
    this.mapping = mapping;
  }

  public void setConstValues(List<CDEValue> constValues) {
    this.constValues = constValues;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (rs == null || con == null)
      throw new IOException("InputStream not ready, ResultSet or Connection is null!");
    return new CDEResultInputStream(this);
  }

  @Override
  public long getSize() {
    return -1;
  }

 public void close() throws SQLException {
    if (rs != null)
      rs.close();
    if (con != null)
      con.close();
  }

  public String getNextRowAsString() throws SQLException {
    if (rs.next()) {
      CDERow row = createCDERow();
      if (mapping != null)
        applyMappingFuncs(row);
      if (constValues != null)
        addConstValues(row);
      // if there is some kind of configurable response writer,
      // here would be a nice place to put it...
      return row.toString() + ROW_TERMINATOR;
    }
    return null;
  }

  private CDERow createCDERow() throws SQLException {
    CDERow row = new CDERow();
    ResultSetMetaData met = rs.getMetaData();
    int count = met.getColumnCount();
    for (int i = 1; i <= count; i++) {
      // since the SQL query was built with "SELECT ${fieldlocalname} as ${fieldname}"
      // we know that ResultSet column names equal CDE field names
      // and appear in the correct order as well
      String colName = met.getColumnName(i);
      String colValue = rs.getString(i);
      CDEValue val = new CDEValue(colName, colValue);
      row.getVals().add(val);
    }
    return row;
  }

  private void applyMappingFuncs(CDERow row) {
    for (CDEValue value : row.getVals()) {
      MappingField fld = mapping.getFieldByName(value.getCdeName());
      if (fld != null) {
        for (MappingFunc func : fld.getFuncs()) {
          CDEValue newValue = func.inverseTranslate(value);
          value.setVal(newValue.getVal());
        }
      }
    }
  }

  private void addConstValues(CDERow row) {
    row.getVals().addAll(constValues);
  }

}

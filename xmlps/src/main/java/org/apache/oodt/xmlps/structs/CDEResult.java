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

//OODT imports
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.Result;

//JDK imports
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
  private List<QueryElement> orderedFields;

  public CDEResult(ResultSet rs, Connection con) {
    this.rs = rs;
    this.con = con;
    setMimeType("text/plain");
  }
  
  public void setOrderedFields(List<QueryElement> orderedFields){
    this.orderedFields = orderedFields;    
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
      if (this.constValues != null && ((this.orderedFields == null) || (this.orderedFields.size() == 0))){
        addConstValues(row);
      }
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
    Map<String, CDEValue> dbValMap = new HashMap<String, CDEValue>();
    Map<String, CDEValue> constValMap = cdeListToMap(this.constValues);
    List<CDEValue> orderedDbVals = new Vector<CDEValue>();
    
    for (int i = 1; i <= count; i++) {
      // since the SQL query was built with "SELECT ${fieldlocalname} as ${fieldname}"
      // we know that ResultSet column names equal CDE field names
      // and appear in the correct order as well
      String fieldLocalColName = met.getColumnLabel(i);
      String colValue = rs.getString(i);
      CDEValue val = new CDEValue(fieldLocalColName, colValue);
      dbValMap.put(fieldLocalColName, val);
      orderedDbVals.add(val);
    }
    
    // now have the constant values, and the db values, order the row
    if(this.orderedFields != null){
      for(QueryElement qe: this.orderedFields){
        String qeCdeVal = this.mapping.getFieldByLocalName(qe.getValue()).getName();
        if(dbValMap.containsKey(qeCdeVal)){
          row.getVals().add(dbValMap.get(qeCdeVal));
        }
        else if(constValMap.containsKey(qeCdeVal)){
          row.getVals().add(constValMap.get(qeCdeVal));
        }
      }
    }
    else row.getVals().addAll(orderedDbVals);
    
    
    return row;
  }

  private void applyMappingFuncs(CDERow row) {
    for (CDEValue value : row.getVals()) {
      MappingField fld = mapping.getFieldByName(value.getCdeName());
      if (fld != null) {
        for (MappingFunc func : fld.getFuncs()) {
          CDEValue newValue = func.translate(value);
          value.setVal(newValue.getVal());
        }
      }
    }
  }
  
  private void addConstValues(CDERow row) {
    row.getVals().addAll(constValues);
  }  
  
  private Map<String, CDEValue> cdeListToMap(List<CDEValue> vals){
    Map<String, CDEValue> map = new HashMap<String, CDEValue>();
    if(vals != null){
      for(CDEValue val: vals){
        map.put(val.getCdeName(), val);
      }
    }
    
    return map;
  }

}

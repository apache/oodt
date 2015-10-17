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

package org.apache.oodt.xmlps.mapping;

//JDK imports
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * <p>
 * An Mapping is a {@link List} of {@link MappingField}s that define the
 * translation of common ontological queries into queries against a local site's
 * DBMS
 * </p>
 * .
 */
public class Mapping {

  private final Map<String, MappingField> fields;

  private final DatabaseTableGroup tables;

  private String id;

  private String name;

  /**
     *
     */
  public Mapping() {
    this.fields = new TreeMap<String, MappingField>();
    this.tables = new DatabaseTableGroup();
  }

  public Mapping(Map<String, MappingField> fields, DatabaseTableGroup tables,
      String id, String name) {
    super();
    this.fields = fields;
    this.id = id;
    this.name = name;
    this.tables = tables;
  }

  public void addField(String fldName, MappingField field) {
    this.fields.put(fldName, field);
  }

  public MappingField getFieldByLocalName(String localName) {
    if (this.fields == null || (this.fields.keySet().size() == 0)) {
      return null;
    }

    for (MappingField fld : this.fields.values()) {
      if (fld.getLocalName().equals(localName)) {
        return fld;
      }
    }

    return null;
  }

  public MappingField getFieldByName(String name) {
    return this.fields.get(name);
  }

  public boolean constantField(String localName) {
    MappingField fld = getFieldByLocalName(localName);

    if (fld == null) {
      return true; // leave it out
    }

    if (fld.getType() == FieldType.CONSTANT) {
      return true;
    } else
      return false;
  }

  public int getNumFields() {
    return this.fields.keySet().size();
  }

  public void addTable(String tblName, DatabaseTable tbl) {
    this.tables.addTable(tblName, tbl);
  }

  public DatabaseTable getTableByName(String name) {
    return this.tables.getTableByName(name);
  }

  public int getNumTables() {
    return this.tables.getNumTables();
  }

  public List<String> getTableNames() {
    return this.tables.getTableNames();
  }

  public List<String> getFieldNames() {
    return Arrays.asList(this.fields.keySet().toArray(new String[] { "" }));
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the defaultTable
   */
  public String getDefaultTable() {
    return this.tables.getDefaultTable();
  }

  /**
   * @param defaultTable
   *          the defaultTable to set
   */
  public void setDefaultTable(String defaultTable) {
    this.tables.setDefaultTable(defaultTable);
  }

}

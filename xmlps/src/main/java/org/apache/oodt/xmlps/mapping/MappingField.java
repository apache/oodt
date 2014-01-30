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

//OODT imports
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * <p>
 * A field within a mapping.xml file that defines the relationship between CDEs
 * and the underlying attributes of a local site's DBMS.
 * </p>
 * .
 */
public class MappingField {

  private String name;

  private FieldType type;

  private String dbName;

  private String tableName;

  private String constantValue;

  private FieldScope scope;

  private List<MappingFunc> funcs;

  private boolean string;

  /**
   * @param name
   * @param type
   * @param dbName
   * @param tableName
   * @param constantValue
   * @param scope
   * @param funcs
   * @param string
   * @param appendTableName
   */
  public MappingField(String name, FieldType type, String dbName,
      String tableName, String constantValue, FieldScope scope,
      List<MappingFunc> funcs, boolean string) {
    super();
    this.name = name;
    this.type = type;
    this.dbName = dbName;
    this.tableName = tableName;
    this.constantValue = constantValue;
    this.scope = scope;
    this.funcs = funcs;
    this.string = string;
  }

  /**
     *
     */
  public MappingField() {
    this.name = null;
    this.dbName = null;
    this.tableName = null;
    this.constantValue = null;
    this.scope = null;
    this.string = false;
    this.funcs = new Vector<MappingFunc>();
  }

  /**
   * @return the constantValue
   */
  public String getConstantValue() {
    return constantValue;
  }

  /**
   * @param constantValue
   *          the constantValue to set
   */
  public void setConstantValue(String constantValue) {
    this.constantValue = constantValue;
  }

  /**
   * @return the dbName
   */
  public String getDbName() {
    return dbName;
  }

  /**
   * @param dbName
   *          the dbName to set
   */
  public void setDbName(String dbName) {
    this.dbName = dbName;
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
   * @return the scope
   */
  public FieldScope getScope() {
    return scope;
  }

  /**
   * @param scope
   *          the scope to set
   */
  public void setScope(FieldScope scope) {
    this.scope = scope;
  }

  /**
   * @return the tableName
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * @param tableName
   *          the tableName to set
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * @return the type
   */
  public FieldType getType() {
    return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(FieldType type) {
    this.type = type;
  }

  /**
   * @return the funcs
   */
  public List<MappingFunc> getFuncs() {
    return funcs;
  }

  /**
   * @param funcs
   *          the funcs to set
   */
  public void setFuncs(List<MappingFunc> funcs) {
    this.funcs = funcs;
  }

  @Override
  public String toString() {
    StringBuffer rStr = new StringBuffer("[name=");
    rStr.append(this.name);
    rStr.append(",dbname=");
    rStr.append(this.dbName);
    rStr.append(",constant_value=");
    rStr.append(this.constantValue);
    rStr.append(",table_name=");
    rStr.append(this.tableName);
    rStr.append(",scope=");
    rStr.append(this.scope.equals(FieldScope.RETURN) ? "return" : "query");
    rStr.append(",type=");
    rStr.append(this.type.equals(FieldType.CONSTANT) ? "constant" : "dynamic");
    rStr.append(",funcs=");
    rStr.append(printClassNames(this.funcs));
    rStr.append(",string=");
    rStr.append(String.valueOf(this.string));
    rStr.append("]");
    return rStr.toString();
  }

  private String printClassNames(List<MappingFunc> funcs) {
    StringBuffer buf = new StringBuffer();

    if (funcs == null || (funcs != null && funcs.size() == 0)) {
      return "";
    } else {
      for (Iterator<MappingFunc> i = funcs.iterator(); i.hasNext();) {
        MappingFunc func = i.next();
        buf.append(func.getClass().getName());
        buf.append(",");
      }

      buf.deleteCharAt(buf.length() - 1);

      return buf.toString();
    }
  }

  /**
   * @return the string
   */
  public boolean isString() {
    return string;
  }

  /**
   * @param string
   *          the string to set
   */
  public void setString(boolean string) {
    this.string = string;
  }

  /**
   * If dbname exists and is not empty, it is used as the field name.
   * If the table exists and is not empty,
   * return tableName.fieldName, otherwise return fieldName.
   * @return the column name understood by the local db
   */
  public String getLocalName() {
    String dbColName = getName();
    if (getDbName() != null && !getDbName().isEmpty())
      dbColName = getDbName();
    if (getTableName() == null || getTableName().isEmpty())
      return dbColName;
    return getTableName() + "." + dbColName;
  }

}

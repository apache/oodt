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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * 
 * <p>
 * A collection of {@link DatabaseTable}s
 * </p>.
 */
public class DatabaseTableGroup {

    private Map<String, DatabaseTable> group;

    private List<DatabaseTable> orderedGroup;

    private String defaultTable;

    public DatabaseTableGroup() {
        this.group = new TreeMap<String, DatabaseTable>();
        this.orderedGroup = new Vector<DatabaseTable>();
    }

    public void addTable(String tblName, DatabaseTable tbl) {
        this.group.put(tblName, tbl);
        this.orderedGroup.add(tbl);
    }

    public DatabaseTable getTableByName(String name) {
        return this.group.get(name);
    }

    public int getNumTables() {
        return this.group.keySet().size();
    }

    public List<String> getTableNames() {
        List<String> names = new Vector<String>();
        for (DatabaseTable tbl : this.orderedGroup) {
            names.add(tbl.getName());
        }

        return names;
    }

    /**
     * @return the defaultTable
     */
    public String getDefaultTable() {
        return defaultTable;
    }

    /**
     * @param defaultTable
     *            the defaultTable to set
     */
    public void setDefaultTable(String defaultTable) {
        this.defaultTable = defaultTable;
    }

}

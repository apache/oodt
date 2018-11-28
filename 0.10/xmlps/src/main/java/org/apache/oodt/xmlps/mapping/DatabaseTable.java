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

/**
 * 
 * <p>
 * Description of a local site's database table that will be used as a model to
 * query against and select data from.
 * </p>.
 */
public class DatabaseTable {

    private String name;

    private String joinFieldName;

    private String defaultTableJoinFieldName;

    private String defaultTableJoin;

    /**
     * @param name
     * @param joinFieldName
     * @param defaultTableJoinFieldName
     * @param defaultTableJoin
     */
    public DatabaseTable(String name, String joinFieldName,
            String defaultTableJoinFieldName, String defaultTableJoin) {
        super();
        this.name = name;
        this.joinFieldName = joinFieldName;
        this.defaultTableJoinFieldName = defaultTableJoinFieldName;
        this.defaultTableJoin = defaultTableJoin;
    }

    /**
     * 
     */
    public DatabaseTable() {
    }

    /**
     * @return the joinFieldName
     */
    public String getJoinFieldName() {
        return joinFieldName;
    }

    /**
     * @param joinFieldName
     *            the joinFieldName to set
     */
    public void setJoinFieldName(String joinFieldName) {
        this.joinFieldName = joinFieldName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the defaultTableJoinFieldName
     */
    public String getDefaultTableJoinFieldName() {
        return defaultTableJoinFieldName;
    }

    /**
     * @param defaultTableJoinFieldName
     *            the defaultTableJoinFieldName to set
     */
    public void setDefaultTableJoinFieldName(String defaultTableJoinFieldName) {
        this.defaultTableJoinFieldName = defaultTableJoinFieldName;
    }

    /**
     * @return the defaultTableJoin
     */
    public String getDefaultTableJoin() {
        return defaultTableJoin;
    }

    /**
     * @param defaultTableJoin
     *            the defaultTableJoin to set
     */
    public void setDefaultTableJoin(String defaultTableJoin) {
        this.defaultTableJoin = defaultTableJoin;
    }

}

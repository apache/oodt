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
 * Met Keys for the {@link MappingReader}
 * </p>.
 */
public interface MappingReaderMetKeys {

    public static final String FIELD_TAG = "field";

    public static final String TABLES_OUTER_TAG = "tables";

    public static final String TABLE_TAG = "table";

    public static final String TABLE_ATTR_JOIN_FLD = "join";

    public static final String TABLE_ATTR_BASE_TBL_JOIN_TABLE = "to";

    public static final String TABLE_ATTR_BASE_TBL_JOIN_FLD = "tofld";

    public static final String TABLE_ATTR_NAME = "name";

    public static final String FIELD_ATTR_TYPE = "type";

    public static final String FIELD_ATTR_NAME = "name";

    public static final String FIELD_ATTR_DBNAME = "dbname";

    public static final String FIELD_ATTR_SCOPE = "scope";

    public static final String FIELD_ATTR_VALUE = "value";

    public static final String FIELD_ATTR_TABLE = "table";

    public static final String FIELD_ATTR_STRING = "string";

    public static final String FIELD_TRANSLATE_TAG = "translate";

    public static final String FUNC_TAG = "func";

    public static final String FUNC_ATTR_CLASS = "class";

    public static final String FIELD_ATTR_APPEND_TABLE_NAME = "appendTableName";

}

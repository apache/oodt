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

package org.apache.oodt.xmlps.queryparser;

/**
 *
 * Constant met keys used in parsing the XMLQuery.
 */
public interface ParseConstants {
    
    String XMLQUERY_LOGOP = "LOGOP";
    
    String XMLQUERY_AND = "AND";
    
    String XMLQUERY_OR = "OR";
    
    String XMLQUERY_RELOP = "RELOP";
    
    String XMLQUERY_EQUAL = "EQ";
    
    String XMLQUERY_LIKE = "LIKE";
    
    String XMLQUERY_GREATER_THAN = "GT";
    
    String XMLQUERY_GREATER_THAN_OR_EQUAL_TO = "GE";
    
    String XMLQUERY_LESS_THAN = "LT";
    
    String XMLQUERY_LESS_THAN_OR_EQUAL_TO = "LE";
    
    String XMLQUERY_LITERAL = "LITERAL";
    
    
    String SQL_LIKE = "LIKE";
    
    String SQL_EQUAL = "=";
    
    String SQL_AND = "AND";
    
    String SQL_OR = "OR";
    
    String SQL_GREATER_THAN_OR_EQUAL_TO = ">=";
    
    String SQL_GREATER_THAN = ">";
    
    String SQL_LESS_THAN = "<";
    
    String SQL_LESS_THAN_OR_EQUAL_TO = "<=";

}

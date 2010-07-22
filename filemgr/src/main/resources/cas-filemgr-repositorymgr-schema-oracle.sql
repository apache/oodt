/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

SET ECHO ON;
ALTER SESSION SET NLS_DATE_FORMAT='YYYY-MM-DD hh24:mi:ss';

CREATE TABLE PRODUCT_TYPES (
  PRODUCT_TYPE_ID number(11) NOT NULL ,
  PRODUCT_TYPE_NAME VARCHAR(255)  DEFAULT  ''NOT NULL, 
  PRODUCT_TYPE_DESCRIPTION VARCHAR(255)  DEFAULT  ''NOT NULL, 
  PRODUCT_TYPE_VERSIONER_CLASS VARCHAR(255) DEFAULT '',
  PRODUCT_TYPE_REPOSITORY_PATH VARCHAR(255)  DEFAULT  ''NOT NULL, 
  PRIMARY KEY  (PRODUCT_TYPE_ID)
);
 
CREATE INDEX PRODUCT_TYPES_IDX  ON  PRODUCT_TYPES(seqno);  

EXIT;

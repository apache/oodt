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

CREATE TABLE ELEMENTS (
  ELEMENT_ID number(11) NOT NULL ,
  ELEMENT_NAME VARCHAR(255)  DEFAULT  ''NOT NULL, 
  DATA_TYPE_ID number(11)  DEFAULT  '0'NOT NULL, 
  DC_ELEMENT VARCHAR(100) DEFAULT '',
  ELEMENT_DESCRIPTION VARCHAR(255)  DEFAULT  ''NOT NULL, 
  PRIMARY KEY  (ELEMENT_ID)
);

CREATE TABLE PRODUCT_TYPE_ELEMENT_MAP (
  PRODUCT_TYPE_ELEMENT_MAP_ID number(11) NOT NULL ,
  PRODUCT_TYPE_ID number(11)  DEFAULT  '0'NOT NULL, 
  ELEMENT_ID number(11)  DEFAULT  '0'NOT NULL, 
  PRIMARY KEY  (PRODUCT_TYPE_ELEMENT_MAP_ID)
);

CREATE TABLE SUB_TO_SUPER_MAP (
  PRODUCT_TYPE_ID number(11) DEFAULT '0'NOT NULL,
  PARENT_ID number(11) DEFAULT '0'NOT NULL,
  PRIMARY KEY (PRODUCT_TYPE_ID)
);	

CREATE INDEX ELEMENTS_IDX  ON  ELEMENTS(seqno);  
CREATE INDEX PRDCT_TYP_LMNT_MP_DX  ON  PRODUCT_TYPE_ELEMENT_MAP(seqno);  
CREATE INDEX SUB_TO_SUPER_MP_DX ON SUB_TO_SUPER_MAP(seqno);
EXIT;

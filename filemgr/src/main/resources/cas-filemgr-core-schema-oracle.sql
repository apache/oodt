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

CREATE TABLE PRODUCTS (
  PRODUCT_ID number(11) NOT NULL ,
  PRODUCT_STRUCTURE VARCHAR(20)  DEFAULT  ''NOT NULL, 
  PRODUCT_TYPE_ID number(11)  DEFAULT  '0'NOT NULL, 
  PRODUCT_NAME VARCHAR(255)  DEFAULT  ''NOT NULL, 
  PRODUCT_TRANSFER_STATUS VARCHAR(255) DEFAULT 'TRANSFERING' NOT NULL,
  PRIMARY KEY  (PRODUCT_ID)
);

CREATE INDEX PRODUCTS_idx ON PRODUCTS(product_id);  

EXIT;

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

CREATE TABLE CatalogServiceMapper (
  CAT_SERV_TRANS_ID VARCHAR2(255) NOT NULL ,
  CAT_SERV_TRANS_FACTORY VARCHAR2(255) NOT NULL ,
  CAT_TRANS_ID VARCHAR2(255) NOT NULL ,
  CAT_TRANS_FACTORY VARCHAR2(255) NOT NULL ,
  CAT_TRANS_DATE VARCHAR2(255) NOT NULL ,
  CATALOG_ID VARCHAR2(255) NOT NULL
);

CREATE INDEX CatalogServiceMapper_CSTId_idx ON CatalogServiceMapper(CAT_SERV_TRANS_ID);  
CREATE INDEX CatalogServiceMapper_CTId_idx ON CatalogServiceMapper(CAT_TRANS_ID);  
CREATE INDEX CatalogServiceMapper_CatId_idx ON CatalogServiceMapper(CATALOG_ID);  

EXIT;

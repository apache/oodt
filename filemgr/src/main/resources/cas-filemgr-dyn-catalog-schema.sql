--  Licensed to the Apache Software Foundation (ASF) under one or more
--  contributor license agreements.  See the NOTICE file distributed with
--  this work for additional information regarding copyright ownership.
--  The ASF licenses this file to You under the Apache License, Version 2.0
--  (the "License"); you may not use this file except in compliance with
--  the License.  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.


CREATE TABLE YourProductTypeName_metadata
(
  pkey int(10) unsigned primary KEY AUTO_INCREMENT, 
  product_id int NOT NULL,
  element_id varchar(1000) NOT NULL,
  metadata_value varchar(2500) NOT NULL
)

CREATE TABLE YourProductTypeName_reference
(
  pkey int(10) unsigned primary KEY AUTO_INCREMENT,
  product_id int NOT NULL,
  product_orig_reference varchar(2000) NOT NULL,
  product_datastore_reference varchar(2000), 
  product_reference_filesize int NOT NULL,
  product_reference_mimetype varchar(50)
)



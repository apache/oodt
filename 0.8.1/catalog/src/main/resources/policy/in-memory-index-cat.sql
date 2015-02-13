 -- Licensed to the Apache Software Foundation (ASF) under one or more
 -- contributor license agreements.  See the NOTICE file distributed with
 -- this work for additional information regarding copyright ownership.
 -- The ASF licenses this file to You under the Apache License, Version 2.0
 -- (the "License"); you may not use this file except in compliance with
 -- the License.  You may obtain a copy of the License at
 --
 --     http://www.apache.org/licenses/LICENSE-2.0
 --
 -- Unless required by applicable law or agreed to in writing, software
 -- distributed under the License is distributed on an "AS IS" BASIS,
 -- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 -- See the License for the specific language governing permissions and
 -- limitations under the License.
 
DROP TABLE transactions IF EXISTS;
DROP TABLE transaction_terms IF EXISTS;

CREATE TABLE transactions
(
  transaction_id varchar(256) NOT NULL,
  transaction_date varchar(256) NOT NULL
);

CREATE TABLE transaction_terms
(
  transaction_id varchar(256) NOT NULL,
  bucket_name varchar(256) NOT NULL,
  term_name varchar(256) NOT NULL,
  term_value varchar(1000) NOT NULL
);


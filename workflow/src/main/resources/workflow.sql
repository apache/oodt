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

CREATE TABLE workflows
(workflow_id int PRIMARY KEY NOT NULL,
workflow_name varchar(255));

CREATE TABLE workflow_condition_map
(workflow_id int NOT NULL,
workflow_condition_id int NOT NULL);

CREATE TABLE event_workflow_map
(workflow_id int NOT NULL,
event_name varchar(255) NOT NULL);

CREATE TABLE workflow_tasks
(workflow_task_id int NOT NULL PRIMARY KEY,
workflow_task_name varchar(255),
workflow_task_class varchar(255));

CREATE TABLE workflow_task_map
(workflow_id int NOT NULL,
workflow_task_id int NOT NULL,
task_order int NOT NULL);

CREATE TABLE workflow_instances
(workflow_instance_id int PRIMARY KEY NOT NULL,
workflow_instance_status varchar(255),
workflow_id int NOT NULL,
current_task_id int NOT NULL,
start_date_time varchar(255),
end_date_time varchar(255),
current_task_start_date_time varchar(255),
current_task_end_date_time varchar(255),
priority float,
times_blocked int DEFAULT 0);

-- use this definition if you would like
-- to use quoteFields (string versions of
-- current_task_id and workflow_id)
CREATE TABLE workflow_instances
(workflow_instance_id int PRIMARY KEY NOT NULL,
workflow_instance_status varchar(255),
workflow_id varchar(255) NOT NULL,
current_task_id varchar(255) NOT NULL,
start_date_time varchar(255),
end_date_time varchar(255),
current_task_start_date_time varchar(255),
current_task_end_date_time varchar(255),
priority float);

CREATE TABLE workflow_instance_metadata
(workflow_instance_id int NOT NULL,
workflow_met_key varchar(1000) NOT NULL,
workflow_met_val varchar(1000) NOT NULL);

CREATE TABLE workflow_conditions
(workflow_condition_id int NOT NULL PRIMARY KEY,
workflow_condition_name varchar(255) NOT NULL,
workflow_condition_class varchar(255) NOT NULL,
workflow_condition_timeout int,
workflow_optional boolean DEFAULT false);

CREATE TABLE  task_condition_map
(workflow_task_id int NOT NULL, 
workflow_condition_id int NOT NULL
condition_order int NOT NULL);

CREATE TABLE workflow_task_configuration
(workflow_task_id int NOT NULL,
property_name varchar(1000) NOT NULL,
property_value varchar(1000) NOT NULL);

CREATE TABLE workflow_condition_configuration
(workflow_condition_id int NOT NULL,
property_name varchar(1000) NOT NULL,
property_value varchar(1000) NOT NULL);

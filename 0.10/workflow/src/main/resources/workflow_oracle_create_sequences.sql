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

create sequence workflows_seq
start with 1
increment by 1
nomaxvalue;

create trigger workflows_trigger
before insert on workflows
for each row
begin
select workflows_seq.nextval into :new.workflow_id from dual;
end;

create sequence workflow_tasks_seq
start with 1
increment by 1
nomaxvalue;

create trigger workflow_tasks_trigger
before insert on workflow_tasks
for each row
begin
select workflow_tasks_seq.nextval into :new.workflow_task_id from dual;
end;

create sequence workflow_instances_seq
start with 1
increment by 1
nomaxvalue;

create trigger workflow_instances_trigger
before insert on workflow_instances
for each row
begin
select workflow_instances_seq.nextval into :new.workflow_instance_id from dual;
end;

create sequence workflow_conditions_seq
start with 1
increment by 1
nomaxvalue;

create trigger workflow_conditions_trigger
before insert on workflow_conditions
for each row
begin
select workflow_conditions_seq.nextval into :new.workflow_condition_id from dual;
end;

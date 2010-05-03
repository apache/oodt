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

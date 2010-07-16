create sequence products_seq
start with 1
increment by 1
nomaxvalue;

create trigger products_trigger
before insert on products
for each row
begin
select products_seq.nextval into :new.product_id from dual;
end;

create sequence product_types_seq
start with 1
increment by 1
nomaxvalue;

create trigger product_types_trigger
before insert on product_types
for each row
begin
select product_types_seq.nextval into :new.product_type_id from dual;
end;

create sequence elements_seq
start with 1
increment by 1
nomaxvalue;

create trigger elements_trigger
before insert on elements
for each row
begin
select elements_seq.nextval into :new.element_id from dual;
end;

create sequence product_type_element_map_seq
start with 1
increment by 1
nomaxvalue;

create trigger ptype_element_map_trigger
before insert on product_type_element_map
for each row
begin
select product_type_element_map_seq.nextval into :new.product_type_element_map_id from dual;
end;

create sequence data_types_seq
start with 1
increment by 1
nomaxvalue;

create trigger data_types_trigger
before insert on data_types
for each row
begin
select data_types_seq.nextval into :new.data_type_id from dual;
end;

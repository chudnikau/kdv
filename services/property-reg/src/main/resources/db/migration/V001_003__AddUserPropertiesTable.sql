create table if not exists user_properties
(
    property_id              integer references properties (id) not null,
    ref_property_type_id     integer                            not null,
    ref_property_category_id integer                            null,
    ref_user_id              integer                            not null
);

alter table properties
    drop column if exists ref_property_type_id;
alter table properties
    drop column if exists ref_property_category_id;

comment on column user_properties.ref_property_type_id is '_REFerence on field on another microservice';
comment on column user_properties.ref_property_category_id is '_REFerence on field on another microservice';
comment on column user_properties.ref_user_id is '_REFerence on field on another microservice';

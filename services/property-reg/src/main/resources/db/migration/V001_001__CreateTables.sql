create table if not exists properties
(
    id                       serial primary key,
    ref_property_type_id     integer   not null,
    ref_property_category_id integer   not null,
    property_json            varchar   not null,
    property_json_type       varchar   not null,
    created_date             timestamp not null default now(),
    modified_date            timestamp not null default now(),
    unique (ref_property_type_id, ref_property_category_id)
);

comment on column properties.ref_property_type_id is '_REFerence on field on another microservice';
comment on column properties.ref_property_category_id is '_REFerence on field on another microservice';
comment on column properties.property_json is 'JSON data of a property';
comment on column properties.property_json_type is 'Type of JSON data for property_json';
alter table properties add column is_active boolean not null default true;

comment on column properties.is_active is 'Property is either active or not';
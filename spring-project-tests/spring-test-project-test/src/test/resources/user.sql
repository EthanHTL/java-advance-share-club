// create schema if not exists testdb;
// set schema testdb;
create table IF NOT EXISTS  tuser (
    id bigint auto_increment primary key,
    name varchar(20) not null
);

insert into tuser(name) values ( '1231' );

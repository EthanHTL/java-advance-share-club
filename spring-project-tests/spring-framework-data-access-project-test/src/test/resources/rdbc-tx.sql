

create table if not exists people(
    `username` varchar(20) default '',
    `password` varchar(20) default ''
);

insert into people values ('jasonj','20');

select * from people;
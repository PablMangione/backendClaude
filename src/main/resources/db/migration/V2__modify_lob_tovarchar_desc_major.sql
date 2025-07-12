alter table majors
    modify description varchar(1000) null;
alter table subjects
    modify description varchar(1000) null;
alter table incidents
    modify description varchar(1000) not null;
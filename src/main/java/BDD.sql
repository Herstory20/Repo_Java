create table Annuaire (login varchar(50), ip varchar(32) primary key, port varchar(4));
create table Messages (id integer primary key references conversation(id), message varchar(500));
create table conversation (ip1 varchar(32) ,ip2 varchar(32) ,id integer, primary key (ip1,ip2))


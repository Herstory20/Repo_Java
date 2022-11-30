create table Annuaire (login varchar(50), ip varchar(32) primary key, port varchar(4));
create table Messages (id integer primary key, message varchar(500), ip1 varchar(32) ,ip2 varchar(32), foreign key (ip1,ip2) references Conversation(ip1,ip2));
create table Conversation (ip1 varchar(32) ,ip2 varchar(32), primary key (ip1,ip2), foreign key (ip1) references Annuaire(ip), foreign key (ip2) references Annuaire(ip));

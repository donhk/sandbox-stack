-- current hostport in use
create table if not exists hostport
(
  port   int primary key,
  status varchar(15) default 'FREE'
);
-- nat networks
create table if not exists nats
(
  network varchar(50) unique
);
-- table to keep track of the machines created
create table if not exists machines
(
  name    varchar(50) primary key,
  ipv4    varchar(50) default '', -- each machine has one IP, it can come from a generic NAT or a NAT Network
  state   varchar(50),
  vm      varchar(60) default '', --name of the vm
  created timestamp   default now(),
  updated timestamp   default now(),
  network varchar(50) default 'NAT' --NAT != NAT Network (NAT shared between n machines)
);
-- machine rules
create table if not exists rules
(
  name      varchar(50), --machine name
  rule_name varchar(100) unique,
  hostport  int,
  guestport int default 22,
  foreign key (name) references machines (name),
  foreign key (hostport) references hostport (port)
);
-- history
create table if not exists machines_hist
(
  name      varchar(50) unique,
  ipv4      varchar(50),
  network   varchar(50),
  rules     varchar(1000),
  created   timestamp,
  vm        varchar(60) default '',
  state     varchar(50) default '',
  destroyed timestamp   default now()
);
-- machines info cached
-- this table is only expected to keep the latest version
create table if not exists meta_digest
(
  digest  varchar(50) unique,
  created timestamp default now(),
  content clob -- this will be stores as base 64
);
-- requests
-- tracks all the update requests made by the users
create table if not exists history
(
  id        bigint primary key auto_increment,
  created   timestamp          default now(),
  operation varchar(1000)      default ''
);
-- meta meta info
create table if not exists machines_meta
(
  machine_prefix varchar(50) unique,
  machine_name   varchar(50),
  snapshot_name  varchar(50),
  cpu_count      varchar(50),
  memory         varchar(50),
  "user"         varchar(50),
  password       varchar(100),
  home           varchar(100),
  comments       varchar(1000)
);
-- messages table
create table if not exists messages
(
  id             bigint primary key auto_increment,
  machine_name   varchar(50),
  message        varchar(5000),
  record_time    timestamp   default now(),
  record_sent    varchar(10) default 'no'
);
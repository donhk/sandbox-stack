--
-- New Tables
--

-- table to keep track of the disks
create table if not exists vm_storage_units
(
    vm_id       VARCHAR(20) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    size_bytes  BIGINT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_storage_units_vm_id ON vm_storage_units(vm_id);


-- table to keep track of vm ports
create table if not exists vm_ports
(
    vm_id      VARCHAR(20) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    host_port  INT NOT NULL,
    vm_port    INT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_ports_vm_id ON vm_ports(vm_id);

-- table to keep track of the machines created
CREATE TABLE if not exists virtual_machines (
    id VARCHAR(20) UNIQUE,                  -- "mch-018"
    name VARCHAR(100) NOT NULL,            -- "Machine-R"
    seed_name VARCHAR(50),                 -- "seed-18"
    snapshot VARCHAR(50),                  -- "snap-018"
    network VARCHAR(50),                   -- "network3"
    network_type VARCHAR(50),              -- "network3"
    vm_ip_address VARCHAR(45),             -- "192.168.10.18"
    hostname VARCHAR(100),                 -- "local1.localhost:3999"
    vm_hostname VARCHAR(100),              -- "machineRow-r"
    machine_state VARCHAR(20),             -- "RUNNING"
    created_at TIMESTAMP,                  -- UTC recommended
    updated_at TIMESTAMP,                  -- UTC recommended
    locked BOOLEAN DEFAULT FALSE           -- false
);
--
-- Old Code
--

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
  ipv4    varchar(50) default '', -- each machineRow has one IP, it can come from a generic NAT or a NAT Network
  state   varchar(50),
  vm      varchar(60) default '', --name of the vm
  created timestamp   default now(),
  updated timestamp   default now(),
  network varchar(50) default 'NAT' --NAT != NAT Network (NAT shared between n machines)
);
-- machineRow rules
create table if not exists rules
(
  name      varchar(50), --machineRow name
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
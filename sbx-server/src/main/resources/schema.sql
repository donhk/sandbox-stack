--
-- New Tables
--

-- table to keep track of the disks
-- @formatter:off
create table if not exists vm_storage_units
(
    vm_id       VARCHAR (20) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    size_bytes  BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_vm_storage_units_vm_id ON vm_storage_units(vm_id);


-- table to keep track of vm ports
create table if not exists vm_ports
(
    vm_id       VARCHAR(20) NOT NULL,
    name        VARCHAR(50) NOT NULL,
    host_port   INT NOT NULL,
    vm_port     INT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_vm_ports_vm_id ON vm_ports(vm_id);

-- table to keep track of the machines created
CREATE TABLE if not exists virtual_machines
(
    id              VARCHAR(20)     UNIQUE,     -- "mch-018"
    name            VARCHAR(100)    NOT NULL,   -- "Machine-R"
    seed_name       VARCHAR(50),    -- "seed-18"
    snapshot        VARCHAR(50),    -- "snap-018"
    network         VARCHAR(50),    -- "network3"
    network_type    VARCHAR(50),    -- "network3"
    vm_ip_address   VARCHAR(45),    -- "192.168.10.18"
    hostname        VARCHAR(100),   -- "local1.localhost:3999"
    vm_hostname     VARCHAR(100),   -- "machineRow-r"
    machine_state   VARCHAR(20),    -- "RUNNING"
    created_at      TIMESTAMP,      -- UTC recommended
    updated_at      TIMESTAMP,      -- UTC recommended
    locked BOOLEAN  DEFAULT FALSE   -- false
);
-- @formatter:on

INSERT INTO virtual_machines (id, name, seed_name, snapshot, network, network_type, vm_ip_address, hostname, vm_hostname, machine_state, created_at, updated_at, locked)
VALUES
    ('mch-001', 'Machine-Alpha', 'seed-01', 'snap-001', 'network1', 'HOST_ONLY', '192.168.1.101', 'host1.local:3901', 'vm-alpha', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
    ('mch-002', 'Machine-Beta', 'seed-02', 'snap-002', 'network1', 'INTERNAL_NETWORK', '192.168.1.102', 'host2.local:3902', 'vm-beta', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
    ('mch-003', 'Machine-Gamma', 'seed-03', 'snap-003', 'network2', 'NAT', '192.168.1.103', 'host3.local:3903', 'vm-gamma', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE);

INSERT INTO vm_storage_units (vm_id, name, size_bytes)
VALUES
    ('mch-001', 'root-disk', 53687091200),   -- 50 GB
    ('mch-001', 'data-disk', 107374182400),  -- 100 GB
    ('mch-002', 'root-disk', 64424509440),   -- 60 GB
    ('mch-003', 'root-disk', 42949672960),   -- 40 GB
    ('mch-003', 'backup-disk', 214748364800);-- 200 GB


INSERT INTO vm_ports (vm_id, name, host_port, vm_port)
VALUES
    ('mch-001', 'ssh', 2222, 22),
    ('mch-001', 'http', 8080, 80),
    ('mch-002', 'ssh', 2223, 22),
    ('mch-003', 'ssh', 2224, 22),
    ('mch-003', 'https', 8443, 443);

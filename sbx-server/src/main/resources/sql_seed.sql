-- Insert 30 machines into virtual_machines
-- Insert 30 virtual machines
INSERT INTO virtual_machines (id, name, seed_name, snapshot, network, network_type, vm_ip_address, hostname,
                              vm_hostname, machine_state, created_at, updated_at, locked)
VALUES ('mch-001', 'Machine-Alpha', 'seed-01', 'snap-001', 'network1', 'HOST_ONLY', '192.168.1.101', 'host1.local:3901',
        'vm-alpha', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-002', 'Machine-Beta', 'seed-02', 'snap-002', 'network1', 'INTERNAL_NETWORK', '192.168.1.102',
        'host2.local:3902', 'vm-beta', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-003', 'Machine-Gamma', 'seed-03', 'snap-003', 'network2', 'NAT', '192.168.1.103', 'host3.local:3903',
        'vm-gamma', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-004', 'Machine-Delta', 'seed-04', 'snap-004', 'network2', 'NAT', '192.168.1.104', 'host4.local:3904',
        'vm-delta', 'SHUTTING_DOWN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-005', 'Machine-Epsilon', 'seed-05', 'snap-005', 'network3', 'HOST_ONLY', '192.168.1.105',
        'host5.local:3905', 'vm-epsilon', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-006', 'Machine-Zeta', 'seed-06', 'snap-006', 'network3', 'INTERNAL_NETWORK', '192.168.1.106',
        'host6.local:3906', 'vm-zeta', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-007', 'Machine-Eta', 'seed-07', 'snap-007', 'network4', 'NAT', '192.168.1.107', 'host7.local:3907',
        'vm-eta', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-008', 'Machine-Theta', 'seed-08', 'snap-008', 'network1', 'HOST_ONLY', '192.168.1.108', 'host8.local:3908',
        'vm-theta', 'FAILED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-009', 'Machine-Iota', 'seed-09', 'snap-009', 'network2', 'NAT', '192.168.1.109', 'host9.local:3909',
        'vm-iota', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-010', 'Machine-Kappa', 'seed-10', 'snap-010', 'network5', 'INTERNAL_NETWORK', '192.168.1.110',
        'host10.local:3910', 'vm-kappa', 'INITIALIZED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-011', 'Machine-Lambda', 'seed-11', 'snap-011', 'network5', 'NAT', '192.168.1.111', 'host11.local:3911',
        'vm-lambda', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-012', 'Machine-Mu', 'seed-12', 'snap-012', 'network6', 'HOST_ONLY', '192.168.1.112', 'host12.local:3912',
        'vm-mu', 'TERMINATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-013', 'Machine-Nu', 'seed-13', 'snap-013', 'network6', 'INTERNAL_NETWORK', '192.168.1.113',
        'host13.local:3913', 'vm-nu', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-014', 'Machine-Xi', 'seed-14', 'snap-014', 'network7', 'NAT', '192.168.1.114', 'host14.local:3914',
        'vm-xi', 'INITIALIZED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-015', 'Machine-Omicron', 'seed-15', 'snap-015', 'network7', 'HOST_ONLY', '192.168.1.115',
        'host15.local:3915', 'vm-omicron', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-016', 'Machine-Pi', 'seed-16', 'snap-016', 'network8', 'INTERNAL_NETWORK', '192.168.1.116',
        'host16.local:3916', 'vm-pi', 'FAILED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-017', 'Machine-Rho', 'seed-17', 'snap-017', 'network1', 'NAT', '192.168.1.117', 'host17.local:3917',
        'vm-rho', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-018', 'Machine-Sigma', 'seed-18', 'snap-018', 'network2', 'HOST_ONLY', '192.168.1.118',
        'host18.local:3918', 'vm-sigma', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-019', 'Machine-Tau', 'seed-19', 'snap-019', 'network3', 'INTERNAL_NETWORK', '192.168.1.119',
        'host19.local:3919', 'vm-tau', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-020', 'Machine-Upsilon', 'seed-20', 'snap-020', 'network3', 'NAT', '192.168.1.120', 'host20.local:3920',
        'vm-upsilon', 'TERMINATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-021', 'Machine-Phi', 'seed-21', 'snap-021', 'network4', 'HOST_ONLY', '192.168.1.121', 'host21.local:3921',
        'vm-phi', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-022', 'Machine-Chi', 'seed-22', 'snap-022', 'network4', 'NAT', '192.168.1.122', 'host22.local:3922',
        'vm-chi', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-023', 'Machine-Psi', 'seed-23', 'snap-023', 'network5', 'INTERNAL_NETWORK', '192.168.1.123',
        'host23.local:3923', 'vm-psi', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-024', 'Machine-Omega', 'seed-24', 'snap-024', 'network5', 'NAT', '192.168.1.124', 'host24.local:3924',
        'vm-omega', 'SHUTTING_DOWN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-025', 'Machine-Alpha2', 'seed-25', 'snap-025', 'network1', 'HOST_ONLY', '192.168.1.125',
        'host25.local:3925', 'vm-alpha2', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-026', 'Machine-Beta2', 'seed-26', 'snap-026', 'network2', 'INTERNAL_NETWORK', '192.168.1.126',
        'host26.local:3926', 'vm-beta2', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-027', 'Machine-Gamma2', 'seed-27', 'snap-027', 'network3', 'NAT', '192.168.1.127', 'host27.local:3927',
        'vm-gamma2', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE),
       ('mch-028', 'Machine-Delta2', 'seed-28', 'snap-028', 'network4', 'HOST_ONLY', '192.168.1.128',
        'host28.local:3928', 'vm-delta2', 'FAILED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-029', 'Machine-Epsilon2', 'seed-29', 'snap-029', 'network5', 'INTERNAL_NETWORK', '192.168.1.129',
        'host29.local:3929', 'vm-epsilon2', 'RUNNING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, FALSE),
       ('mch-030', 'Machine-Zeta2', 'seed-30', 'snap-030', 'network6', 'NAT', '192.168.1.130', 'host30.local:3930',
        'vm-zeta2', 'BOOTING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, TRUE);


-- Example disks for some VMs
INSERT INTO vm_storage_units (vm_id, name, size_bytes)
VALUES ('mch-001', 'root-disk', 53687091200),
       ('mch-001', 'data-disk', 107374182400),
       ('mch-002', 'root-disk', 64424509440),
       ('mch-003', 'root-disk', 42949672960),
       ('mch-003', 'backup-disk', 214748364800),
       ('mch-004', 'root-disk', 53687091200),
       ('mch-005', 'root-disk', 53687091200),
       ('mch-006', 'root-disk', 53687091200),
       ('mch-007', 'root-disk', 53687091200),
       ('mch-008', 'root-disk', 53687091200);



INSERT INTO vm_ports (vm_id, name, host_port, vm_port)
VALUES ('mch-001', 'ssh', 2222, 22),
       ('mch-001', 'http', 8080, 80),

       ('mch-002', 'ssh', 2223, 22),

       ('mch-003', 'ssh', 2224, 22),
       ('mch-003', 'https', 8443, 443),

       ('mch-004', 'ssh', 2225, 22),

       ('mch-005', 'ssh', 2226, 22),

       ('mch-006', 'ssh', 2227, 22),

       ('mch-007', 'ssh', 2228, 22),

       ('mch-008', 'ssh', 2229, 22),

       ('mch-009', 'ssh', 2230, 22),

       ('mch-010', 'ssh', 2231, 22),

       ('mch-011', 'ssh', 2232, 22),

       ('mch-012', 'ssh', 2233, 22),

       ('mch-013', 'ssh', 2234, 22),

       ('mch-014', 'ssh', 2235, 22),

       ('mch-015', 'ssh', 2236, 22),

       ('mch-016', 'ssh', 2237, 22),

       ('mch-017', 'ssh', 2238, 22),

       ('mch-018', 'ssh', 2239, 22),

       ('mch-019', 'ssh', 2240, 22),

       ('mch-020', 'ssh', 2241, 22),

       ('mch-021', 'ssh', 2242, 22),

       ('mch-022', 'ssh', 2243, 22),

       ('mch-023', 'ssh', 2244, 22),

       ('mch-024', 'ssh', 2245, 22),

       ('mch-025', 'ssh', 2246, 22),

       ('mch-026', 'ssh', 2247, 22),

       ('mch-027', 'ssh', 2248, 22),

       ('mch-028', 'ssh', 2249, 22),

       ('mch-029', 'ssh', 2250, 22),

       ('mch-030', 'ssh', 2251, 22);


INSERT INTO vm_seeds
(prefix, vm_user, vm_pass, home, snapshot_name, snapshot_cpus, snapshot_ram_mb, snapshot_comments)
VALUES ('windows', 'user1', 'password1', 'C:\users\user1', 'w1c1', 12, 12323, 'Special Snapshot Comments'),
       ('windows', 'user1', 'password1', 'C:\users\user1', 'w1c2', 6, 16000, 'Special Snapshot Comments'),

       ('linux1', 'user1', 'password1', '/home/user1', 'l1c1', 16, 18000, 'Special Snapshot Comments'),
       ('linux2', 'user1', 'password1', '/home/user1', 'l1c2', 8, 19000, 'Special Snapshot Comments3'),
       ('linux3', 'user1', 'password1', '/home/user1', 'l1c2', 8, 19000, 'Special Snapshot Comments2'),
       ('linux4', 'user1', 'password1', '/home/user1', 'l1c3', 9, 12000, 'Special Snapshot Comments1'),

       ('windows2', 'user1', 'password1', 'C:\users\user1', 'w1c1', 12, 12323, 'Special Snapshot Comments'),
       ('windows2', 'user1', 'password1', 'C:\users\user1', 'w1c2', 6, 16000, 'Special Snapshot Comments');

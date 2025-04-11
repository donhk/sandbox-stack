import {
    CButton,
    CCard, CCardBody, CCardHeader, CFormSwitch, CSpinner,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow
} from "@coreui/react";
import React, {useRef, useState} from "react";
import clsx from 'clsx';
import dayjs from 'dayjs';
import CIcon from "@coreui/icons-react";
import {cilDevices, cilLan, cilLibrary, cilPin, cilRectangle,} from "@coreui/icons";
import ModalWindow from "src/views/machines/ModalWindow";
import lockTable from "src/views/machines/utilities";

function formatPortMappings(mappings) {
    return mappings
        .map(({name, hostPort, vmPort}) => `${name}:${hostPort}:${vmPort}`)
        .join(', ');
}

function formatDiskSizes(disks) {
    const formatBytes = (bytes) => {
        const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
        let i = 0;
        let num = Number(bytes);

        while (num >= 1024 && i < units.length - 1) {
            num /= 1024;
            i++;
        }

        return `${Math.round(num)}${units[i]}`;
    };

    const totalBytes = disks.reduce((acc, {sizeBytes}) => acc + Number(sizeBytes), 0);
    return formatBytes(totalBytes);
}

const Machines = () => {

    const format = "MMM DD, hh:mm A";

    const [lockingVm, setLockingVm] = useState(new Map)
    const [lockedVm, setLockedVm] = useState(new Map)
    const [selectedMachine, setSelectedMachine] = useState(null);
    const [modalVisible, setModalVisible] = useState(false);
    const lastFocusedRef = useRef(null);
    const virtualMachines = [
        {
            "uuid": "mch-001",
            "name": "Machine-A",
            "seedName": "seed-01",
            "snapshot": "snap-001",
            "network": "network1",
            "vmIpAddress": "192.168.1.10",
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "ssh", "hostPort": 2222, "vmPort": 22},
                {"name": "http", "hostPort": 8080, "vmPort": 80}
            ],
            "vmHostname": "machine-a.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-01T10:00:00Z",
            "updatedAt": "2025-04-01T10:30:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "500000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-002",
            "name": "Machine-B",
            "seedName": "seed-02",
            "snapshot": "snap-002",
            "network": "network2",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "ssh", "hostPort": 2223, "vmPort": 22}
            ],
            "vmHostname": "machine-b.example.com",
            "machineState": "STOPPED",
            "createdAt": "2025-03-05T09:15:00Z",
            "updatedAt": "2025-04-01T09:45:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "1000000000000"},
                {"diskName": "sdb", "sizeBytes": "200000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-003",
            "name": "Machine-C",
            "seedName": "seed-03",
            "snapshot": "snap-003",
            "network": "network3",
            "vmIpAddress": "10.0.0.3",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-c.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-10T12:00:00Z",
            "updatedAt": "2025-04-01T12:05:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-004",
            "name": "Machine-D",
            "seedName": "seed-04",
            "snapshot": "snap-004",
            "network": "network4",
            "vmIpAddress": "172.16.0.5",
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "http", "hostPort": 8000, "vmPort": 80}
            ],
            "vmHostname": "machine-d.local",
            "machineState": "FAILED",
            "createdAt": "2025-02-25T14:00:00Z",
            "updatedAt": "2025-03-25T14:30:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "750000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-005",
            "name": "Machine-E",
            "seedName": "seed-05",
            "snapshot": "snap-005",
            "network": "network5",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-e",
            "machineState": "CREATING",
            "createdAt": "2025-04-01T00:00:00Z",
            "updatedAt": "2025-04-01T00:01:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-006",
            "name": "Machine-F",
            "seedName": "seed-06",
            "snapshot": "snap-006",
            "network": "network6",
            "vmIpAddress": "192.168.100.6",
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "ssh", "hostPort": 2226, "vmPort": 22}
            ],
            "vmHostname": "machine-f.domain",
            "machineState": "RUNNING",
            "createdAt": "2025-03-15T07:30:00Z",
            "updatedAt": "2025-04-01T07:45:00Z",
            "storageUnits": [
                {"diskName": "root", "sizeBytes": "320000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-007",
            "name": "Machine-G",
            "seedName": "seed-07",
            "snapshot": "snap-007",
            "network": "network6",
            "vmIpAddress": "10.10.10.7",
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "api", "hostPort": 9000, "vmPort": 9000}
            ],
            "vmHostname": "machine-g.cloud",
            "machineState": "UPDATING",
            "createdAt": "2025-03-22T18:00:00Z",
            "updatedAt": "2025-04-01T18:01:00Z",
            "storageUnits": [
                {"diskName": "boot", "sizeBytes": "10000000000"},
                {"diskName": "data", "sizeBytes": "1000000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-008",
            "name": "Machine-H",
            "seedName": "seed-08",
            "snapshot": "snap-008",
            "network": "network8",
            "vmIpAddress": "192.168.1.108",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-h",
            "machineState": "RUNNING",
            "createdAt": "2025-03-29T23:00:00Z",
            "updatedAt": "2025-04-01T23:15:00Z",
            "storageUnits": [
                {"diskName": "primary", "sizeBytes": "640000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-009",
            "name": "Machine-I",
            "seedName": "seed-09",
            "snapshot": "snap-009",
            "network": "network9",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-i.test",
            "machineState": "TERMINATED",
            "createdAt": "2025-01-01T00:00:00Z",
            "updatedAt": "2025-03-01T00:00:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-010",
            "name": "Machine-J",
            "seedName": "seed-10",
            "snapshot": "snap-010",
            "network": "network10",
            "vmIpAddress": "10.0.1.10",
            "hostname": "local1.localhost:3999",
            "ports": [
                {"name": "admin", "hostPort": 9999, "vmPort": 9999}
            ],
            "vmHostname": "machine-j.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-20T10:10:00Z",
            "updatedAt": "2025-04-01T10:20:00Z",
            "storageUnits": [
                {"diskName": "disk1", "sizeBytes": "800000000000"}
            ],
            "locked": false,
        },
        {
            "uuid": "mch-011",
            "name": "Machine-K",
            "seedName": "seed-11",
            "snapshot": "snap-011",
            "network": "network11",
            "vmIpAddress": "10.2.3.4",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-k",
            "machineState": "STOPPED",
            "createdAt": "2025-02-20T01:00:00Z",
            "updatedAt": "2025-04-01T01:10:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-012",
            "name": "Machine-L",
            "seedName": "seed-12",
            "snapshot": "snap-012",
            "network": "network3",
            "vmIpAddress": "192.168.0.212",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-l",
            "machineState": "RUNNING",
            "createdAt": "2025-02-14T08:00:00Z",
            "updatedAt": "2025-04-01T08:30:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-013",
            "name": "Machine-M",
            "seedName": "seed-13",
            "snapshot": "snap-013",
            "network": "network1",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-m",
            "machineState": "FAILED",
            "createdAt": "2025-01-10T03:00:00Z",
            "updatedAt": "2025-03-20T03:30:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-014",
            "name": "Machine-N",
            "seedName": "seed-14",
            "snapshot": "snap-014",
            "network": "network1",
            "vmIpAddress": "10.0.0.14",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-n",
            "machineState": "CREATING",
            "createdAt": "2025-03-28T20:00:00Z",
            "updatedAt": "2025-04-01T20:05:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-015",
            "name": "Machine-O",
            "seedName": "seed-15",
            "snapshot": "snap-015",
            "network": "network2",
            "vmIpAddress": "172.31.0.15",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-o",
            "machineState": "RUNNING",
            "createdAt": "2025-03-25T04:30:00Z",
            "updatedAt": "2025-04-01T04:45:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-016",
            "name": "Machine-P",
            "seedName": "seed-16",
            "snapshot": "snap-016",
            "network": "network2",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-p",
            "machineState": "STOPPED",
            "createdAt": "2025-02-18T11:00:00Z",
            "updatedAt": "2025-04-01T11:05:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-017",
            "name": "Machine-Q",
            "seedName": "seed-17",
            "snapshot": "snap-017",
            "network": "network3",
            "vmIpAddress": "10.4.5.17",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-q",
            "machineState": "UPDATING",
            "createdAt": "2025-03-10T05:00:00Z",
            "updatedAt": "2025-04-01T05:01:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-018",
            "name": "Machine-R",
            "seedName": "seed-18",
            "snapshot": "snap-018",
            "network": "network3",
            "vmIpAddress": "192.168.10.18",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-r",
            "machineState": "RUNNING",
            "createdAt": "2025-02-05T02:00:00Z",
            "updatedAt": "2025-04-01T02:20:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-019",
            "name": "Machine-S",
            "seedName": "seed-19",
            "snapshot": "snap-019",
            "network": "network20",
            "vmIpAddress": null,
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-s",
            "machineState": "TERMINATED",
            "createdAt": "2024-12-31T12:00:00Z",
            "updatedAt": "2025-01-31T12:01:00Z",
            "storageUnits": [],
            "locked": false,
        },
        {
            "uuid": "mch-020",
            "name": "Machine-T",
            "seedName": "seed-20",
            "snapshot": "snap-020",
            "network": "network3",
            "vmIpAddress": "10.0.2.20",
            "hostname": "local1.localhost:3999",
            "ports": [],
            "vmHostname": "machine-t",
            "machineState": "RUNNING",
            "createdAt": "2025-03-31T23:59:00Z",
            "updatedAt": "2025-04-01T00:00:00Z",
            "storageUnits": [
                {"diskName": "main", "sizeBytes": "960000000000"}
            ],
            "locked": false,
        }
    ];
    const handleSwitchChange = async (machine, event) => {
        const isChecked = event.target.checked
        // start loading
        setLockingVm(prev => new Map(prev).set(machine.uuid, true))

        try {
            const result = await lockTable(machine, isChecked) // pass checked state if needed
            setLockedVm(prev => new Map(prev).set(machine.uuid, result));
        } finally {
            setLockingVm(prev => new Map(prev).set(machine.uuid, false))
        }
    }

    const openModal = (machine, event) => {
        lastFocusedRef.current = event.currentTarget;
        setSelectedMachine(machine);
        setModalVisible(true);
    };
    const closeModal = () => {
        setModalVisible(false);
        setTimeout(() => {
            lastFocusedRef.current?.focus();
        }, 0);
    };
    const filterMachines = (machine) =>
        machine ? virtualMachines.filter((vm) => vm.network === machine.network) : [];

    return (
        <>
            <CCard>
                <CCardHeader>Sandboxer VMs</CCardHeader>
                <CCardBody>
                    <CTable align="middle" className="mb-0 border" hover responsive>
                        <CTableHead className="text-nowrap">
                            <CTableRow>
                                <CTableHeaderCell className="bg-body-tertiary text-center">
                                    <CIcon icon={cilDevices} title="UUID"/>
                                </CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Name</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary text-center">
                                    <CIcon icon={cilLibrary} title="Seed"/>
                                </CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Snapshot</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary text-jusify">
                                    <CIcon icon={cilLan} title="Network"/>
                                </CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Ip</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Ports</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">State</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Created</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary">Updated</CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary text-center">
                                    <CIcon icon={cilRectangle} title="Storage"/>
                                </CTableHeaderCell>
                                <CTableHeaderCell className="bg-body-tertiary text-justify">
                                    <CIcon icon={cilPin}
                                           title="Pin this machine to keep it running regardless of the client"/>
                                </CTableHeaderCell>
                            </CTableRow>
                        </CTableHead>

                        <CTableBody>
                            {virtualMachines.map((item, index) => (
                                <CTableRow key={index}>
                                    <CTableDataCell className="text-justify">
                                        {item.uuid}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.name}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.seedName}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify text-info">
                                        {item.snapshot}
                                    </CTableDataCell>
                                    <CTableDataCell
                                        className="text-justify"
                                        onClick={(e) => openModal(item, e)}
                                        tabIndex={0} // to allow focus
                                        role="button"
                                        style={{cursor: 'pointer'}}
                                        title="Click to view details"
                                    >
                                        <CButton color="success" variant="ghost">{item.network}</CButton>
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify text-primary">
                                        {item.vmIpAddress}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify text-info text-nowrap">
                                        {formatPortMappings(item.ports)}
                                    </CTableDataCell>
                                    <CTableDataCell
                                        className={clsx('text-justify', {
                                            'text-danger': item.machineState === 'FAILED',
                                            'text-success': item.machineState !== 'FAILED',
                                        })}
                                    >
                                        {item.machineState}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify text-primary">
                                        {dayjs(item.createdAt).format(format)}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.updatedAt}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify text-info">
                                        {formatDiskSizes(item.storageUnits)}
                                    </CTableDataCell>
                                    <CTableDataCell className="justify-content-center">
                                        {lockingVm.get(item.uuid) ? (
                                            <CSpinner color="info" variant="grow"/>
                                        ) : (
                                            <CFormSwitch
                                                onChange={(e) => handleSwitchChange(item, e)}
                                                size="lg"
                                                id={`m-keep-${index}`}
                                                checked={lockedVm.get(item.uuid) ?? item.locked}
                                                disabled={item.machineState === 'FAILED'}
                                            />
                                        )}
                                    </CTableDataCell>
                                </CTableRow>
                            ))}
                        </CTableBody>
                    </CTable>
                </CCardBody>
            </CCard>

            <ModalWindow
                visible={modalVisible}
                machines={filterMachines(selectedMachine)}
                onClose={closeModal}
                handleSwitchChange={handleSwitchChange}
                lockingVm={lockingVm}
                lockedVm={lockedVm}
            />
        </>
    );
};

export default Machines;

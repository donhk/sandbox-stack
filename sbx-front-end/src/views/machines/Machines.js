import {
    CCard, CCardBody, CCardHeader, CFormSwitch,
    CTable,
    CTableBody,
    CTableDataCell,
    CTableHead,
    CTableHeaderCell,
    CTableRow
} from "@coreui/react";
import React from "react";
import clsx from 'clsx';
import dayjs from 'dayjs';
import CIcon from "@coreui/icons-react";
import {cilDevices, cilLan, cilLibrary, cilPin, cilRectangle,} from "@coreui/icons";

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
    const virtualMachines = [
        {
            "uuid": "mch-001",
            "name": "Machine-A",
            "seedName": "seed-01",
            "snapshot": "snap-001",
            "network": "network1",
            "ipAddress": "192.168.1.10",
            "ports": [
                {"name": "ssh", "hostPort": 2222, "vmPort": 22},
                {"name": "http", "hostPort": 8080, "vmPort": 80}
            ],
            "hostname": "machine-a.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-01T10:00:00Z",
            "updatedAt": "2025-04-01T10:30:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "500000000000"}
            ]
        },
        {
            "uuid": "mch-002",
            "name": "Machine-B",
            "seedName": "seed-02",
            "snapshot": "snap-002",
            "network": "network2",
            "ipAddress": null,
            "ports": [
                {"name": "ssh", "hostPort": 2223, "vmPort": 22}
            ],
            "hostname": "machine-b.example.com",
            "machineState": "STOPPED",
            "createdAt": "2025-03-05T09:15:00Z",
            "updatedAt": "2025-04-01T09:45:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "1000000000000"},
                {"diskName": "sdb", "sizeBytes": "200000000000"}
            ]
        },
        {
            "uuid": "mch-003",
            "name": "Machine-C",
            "seedName": "seed-03",
            "snapshot": "snap-003",
            "network": "network3",
            "ipAddress": "10.0.0.3",
            "ports": [],
            "hostname": "machine-c.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-10T12:00:00Z",
            "updatedAt": "2025-04-01T12:05:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-004",
            "name": "Machine-D",
            "seedName": "seed-04",
            "snapshot": "snap-004",
            "network": "network4",
            "ipAddress": "172.16.0.5",
            "ports": [
                {"name": "http", "hostPort": 8000, "vmPort": 80}
            ],
            "hostname": "machine-d.local",
            "machineState": "FAILED",
            "createdAt": "2025-02-25T14:00:00Z",
            "updatedAt": "2025-03-25T14:30:00Z",
            "storageUnits": [
                {"diskName": "sda", "sizeBytes": "750000000000"}
            ]
        },
        {
            "uuid": "mch-005",
            "name": "Machine-E",
            "seedName": "seed-05",
            "snapshot": "snap-005",
            "network": "network5",
            "ipAddress": null,
            "ports": [],
            "hostname": "machine-e",
            "machineState": "CREATING",
            "createdAt": "2025-04-01T00:00:00Z",
            "updatedAt": "2025-04-01T00:01:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-006",
            "name": "Machine-F",
            "seedName": "seed-06",
            "snapshot": "snap-006",
            "network": "network6",
            "ipAddress": "192.168.100.6",
            "ports": [
                {"name": "ssh", "hostPort": 2226, "vmPort": 22}
            ],
            "hostname": "machine-f.domain",
            "machineState": "RUNNING",
            "createdAt": "2025-03-15T07:30:00Z",
            "updatedAt": "2025-04-01T07:45:00Z",
            "storageUnits": [
                {"diskName": "root", "sizeBytes": "320000000000"}
            ]
        },
        {
            "uuid": "mch-007",
            "name": "Machine-G",
            "seedName": "seed-07",
            "snapshot": "snap-007",
            "network": "network6",
            "ipAddress": "10.10.10.7",
            "ports": [
                {"name": "api", "hostPort": 9000, "vmPort": 9000}
            ],
            "hostname": "machine-g.cloud",
            "machineState": "UPDATING",
            "createdAt": "2025-03-22T18:00:00Z",
            "updatedAt": "2025-04-01T18:01:00Z",
            "storageUnits": [
                {"diskName": "boot", "sizeBytes": "10000000000"},
                {"diskName": "data", "sizeBytes": "1000000000000"}
            ]
        },
        {
            "uuid": "mch-008",
            "name": "Machine-H",
            "seedName": "seed-08",
            "snapshot": "snap-008",
            "network": "network8",
            "ipAddress": "192.168.1.108",
            "ports": [],
            "hostname": "machine-h",
            "machineState": "RUNNING",
            "createdAt": "2025-03-29T23:00:00Z",
            "updatedAt": "2025-04-01T23:15:00Z",
            "storageUnits": [
                {"diskName": "primary", "sizeBytes": "640000000000"}
            ]
        },
        {
            "uuid": "mch-009",
            "name": "Machine-I",
            "seedName": "seed-09",
            "snapshot": "snap-009",
            "network": "network9",
            "ipAddress": null,
            "ports": [],
            "hostname": "machine-i.test",
            "machineState": "TERMINATED",
            "createdAt": "2025-01-01T00:00:00Z",
            "updatedAt": "2025-03-01T00:00:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-010",
            "name": "Machine-J",
            "seedName": "seed-10",
            "snapshot": "snap-010",
            "network": "network10",
            "ipAddress": "10.0.1.10",
            "ports": [
                {"name": "admin", "hostPort": 9999, "vmPort": 9999}
            ],
            "hostname": "machine-j.local",
            "machineState": "RUNNING",
            "createdAt": "2025-03-20T10:10:00Z",
            "updatedAt": "2025-04-01T10:20:00Z",
            "storageUnits": [
                {"diskName": "disk1", "sizeBytes": "800000000000"}
            ]
        },
        {
            "uuid": "mch-011",
            "name": "Machine-K",
            "seedName": "seed-11",
            "snapshot": "snap-011",
            "network": "network11",
            "ipAddress": "10.2.3.4",
            "ports": [],
            "hostname": "machine-k",
            "machineState": "STOPPED",
            "createdAt": "2025-02-20T01:00:00Z",
            "updatedAt": "2025-04-01T01:10:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-012",
            "name": "Machine-L",
            "seedName": "seed-12",
            "snapshot": "snap-012",
            "network": "network12",
            "ipAddress": "192.168.0.212",
            "ports": [],
            "hostname": "machine-l",
            "machineState": "RUNNING",
            "createdAt": "2025-02-14T08:00:00Z",
            "updatedAt": "2025-04-01T08:30:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-013",
            "name": "Machine-M",
            "seedName": "seed-13",
            "snapshot": "snap-013",
            "network": "network1",
            "ipAddress": null,
            "ports": [],
            "hostname": "machine-m",
            "machineState": "FAILED",
            "createdAt": "2025-01-10T03:00:00Z",
            "updatedAt": "2025-03-20T03:30:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-014",
            "name": "Machine-N",
            "seedName": "seed-14",
            "snapshot": "snap-014",
            "network": "network1",
            "ipAddress": "10.0.0.14",
            "ports": [],
            "hostname": "machine-n",
            "machineState": "CREATING",
            "createdAt": "2025-03-28T20:00:00Z",
            "updatedAt": "2025-04-01T20:05:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-015",
            "name": "Machine-O",
            "seedName": "seed-15",
            "snapshot": "snap-015",
            "network": "network2",
            "ipAddress": "172.31.0.15",
            "ports": [],
            "hostname": "machine-o",
            "machineState": "RUNNING",
            "createdAt": "2025-03-25T04:30:00Z",
            "updatedAt": "2025-04-01T04:45:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-016",
            "name": "Machine-P",
            "seedName": "seed-16",
            "snapshot": "snap-016",
            "network": "network2",
            "ipAddress": null,
            "ports": [],
            "hostname": "machine-p",
            "machineState": "STOPPED",
            "createdAt": "2025-02-18T11:00:00Z",
            "updatedAt": "2025-04-01T11:05:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-017",
            "name": "Machine-Q",
            "seedName": "seed-17",
            "snapshot": "snap-017",
            "network": "network3",
            "ipAddress": "10.4.5.17",
            "ports": [],
            "hostname": "machine-q",
            "machineState": "UPDATING",
            "createdAt": "2025-03-10T05:00:00Z",
            "updatedAt": "2025-04-01T05:01:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-018",
            "name": "Machine-R",
            "seedName": "seed-18",
            "snapshot": "snap-018",
            "network": "network3",
            "ipAddress": "192.168.10.18",
            "ports": [],
            "hostname": "machine-r",
            "machineState": "RUNNING",
            "createdAt": "2025-02-05T02:00:00Z",
            "updatedAt": "2025-04-01T02:20:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-019",
            "name": "Machine-S",
            "seedName": "seed-19",
            "snapshot": "snap-019",
            "network": "network20",
            "ipAddress": null,
            "ports": [],
            "hostname": "machine-s",
            "machineState": "TERMINATED",
            "createdAt": "2024-12-31T12:00:00Z",
            "updatedAt": "2025-01-31T12:01:00Z",
            "storageUnits": []
        },
        {
            "uuid": "mch-020",
            "name": "Machine-T",
            "seedName": "seed-20",
            "snapshot": "snap-020",
            "network": "network21",
            "ipAddress": "10.0.2.20",
            "ports": [],
            "hostname": "machine-t",
            "machineState": "RUNNING",
            "createdAt": "2025-03-31T23:59:00Z",
            "updatedAt": "2025-04-01T00:00:00Z",
            "storageUnits": [
                {"diskName": "main", "sizeBytes": "960000000000"}
            ]
        }
    ];

    return (
        <CCard>
            <CCardHeader>Sandboxer VMs</CCardHeader>
            <CCardBody>
                <CTable align="middle" className="mb-0 border" hover responsive>

                    <CTableHead className="text-nowrap">
                        <CTableRow>
                            <CTableHeaderCell className="bg-body-tertiary text-center">
                                <CIcon icon={cilDevices}
                                       title="UUID"
                                />
                            </CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Name</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary text-center">
                                <CIcon icon={cilLibrary}
                                       title="Seed"
                                />
                            </CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Snapshot</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary text-center">
                                <CIcon icon={cilLan}
                                       title="Network"
                                />
                            </CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Ip</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Ports</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">State</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Created</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Updated</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary text-center">
                                <CIcon icon={cilRectangle}
                                       title="Storage"
                                />
                            </CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary text-center">
                                <CIcon icon={cilPin}
                                       title="Pin this machine to keep it running regardless of the client"
                                />
                            </CTableHeaderCell>
                        </CTableRow>
                    </CTableHead>

                    <CTableBody>
                        {virtualMachines.map((item, index) => (
                            <CTableRow v-for="item in tableItems" key={index}>
                                <CTableDataCell className="text-justify">
                                    {item.uuid}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify">
                                    {item.name}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify">
                                    {item.seedName}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify text-primary">
                                    {item.snapshot}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify">
                                    {item.network}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify text-primary">
                                    {item.ipAddress}
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
                                <CTableDataCell className="d-flex justify-content-center">
                                    <CFormSwitch size="lg" id="keep"/>
                                </CTableDataCell>
                            </CTableRow>
                        ))}
                    </CTableBody>

                </CTable>
            </CCardBody>
        </CCard>
    )
}

export default Machines;
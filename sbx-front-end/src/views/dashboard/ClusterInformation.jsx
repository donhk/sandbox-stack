import React, {useEffect, useState} from "react";
import {CCard, CCardBody, CCol, CHeader, CRow} from "@coreui/react";
import {ResourcesChart} from "src/views/dashboard/ResourcesCharts";
import {VMResourcesChart} from "src/views/dashboard/VMResourcesChart";

const remoteData = [
    {
        "vmHostname": "host1.localhost:4000",
        "totalMemoryMb": 454545,
        "usedMemoryMb": 45,
        "cpus": 122,
        "cpuUsage": 45,
        "netTrafficBytesIn": 45,
        "netTrafficBytesOut": 45,
        "totalStorageMb": 4511111,
        "usedStorageMb": 41111,
        "activeVMs": [
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
        ]
    },
    {
        "vmHostname": "host2.localhost:4000",
        "totalMemoryMb": 327680,
        "usedMemoryMb": 120000,
        "cpus": 64,
        "cpuUsage": 55,
        "netTrafficBytesIn": 12000,
        "netTrafficBytesOut": 18000,
        "totalStorageMb": 8000000,
        "usedStorageMb": 4000000,
        "activeVMs": [
            {
                "uuid": "mch-021",
                "name": "Machine-U",
                "seedName": "seed-21",
                "snapshot": "snap-021",
                "network": "network2",
                "vmIpAddress": "192.168.20.21",
                "hostname": "local2.localhost:3998",
                "ports": [],
                "vmHostname": "machine-u",
                "machineState": "RUNNING",
                "createdAt": "2025-02-10T08:00:00Z",
                "updatedAt": "2025-03-30T10:10:00Z",
                "storageUnits": [],
                "locked": false
            },
            {
                "uuid": "mch-022",
                "name": "Machine-V",
                "seedName": "seed-22",
                "snapshot": "snap-022",
                "network": "network2",
                "vmIpAddress": "10.0.2.22",
                "hostname": "local2.localhost:3998",
                "ports": [],
                "vmHostname": "machine-v",
                "machineState": "STOPPED",
                "createdAt": "2025-01-15T15:00:00Z",
                "updatedAt": "2025-02-15T15:30:00Z",
                "storageUnits": [],
                "locked": true
            }
        ]
    },
    {
        "vmHostname": "host3.localhost:4000",
        "totalMemoryMb": 655360,
        "usedMemoryMb": 300000,
        "cpus": 96,
        "cpuUsage": 70,
        "netTrafficBytesIn": 9000,
        "netTrafficBytesOut": 9500,
        "totalStorageMb": 10000000,
        "usedStorageMb": 9000000,
        "activeVMs": [
            {
                "uuid": "mch-023",
                "name": "Machine-W",
                "seedName": "seed-23",
                "snapshot": "snap-023",
                "network": "network5",
                "vmIpAddress": null,
                "hostname": "local3.localhost:3997",
                "ports": [],
                "vmHostname": "machine-w",
                "machineState": "TERMINATED",
                "createdAt": "2025-01-01T01:01:01Z",
                "updatedAt": "2025-03-01T02:02:02Z",
                "storageUnits": [],
                "locked": false
            },
            {
                "uuid": "mch-024",
                "name": "Machine-X",
                "seedName": "seed-24",
                "snapshot": "snap-024",
                "network": "network5",
                "vmIpAddress": "10.10.10.24",
                "hostname": "local3.localhost:3997",
                "ports": [],
                "vmHostname": "machine-x",
                "machineState": "RUNNING",
                "createdAt": "2025-02-14T14:14:14Z",
                "updatedAt": "2025-04-01T04:04:04Z",
                "storageUnits": [
                    {"diskName": "sda1", "sizeBytes": "500000000000"}
                ],
                "locked": false
            }
        ]
    },
    {
        "vmHostname": "host4.localhost:4000",
        "totalMemoryMb": 128000,
        "usedMemoryMb": 128000,
        "cpus": 32,
        "cpuUsage": 90,
        "netTrafficBytesIn": 2048,
        "netTrafficBytesOut": 4096,
        "totalStorageMb": 4000000,
        "usedStorageMb": 3999999,
        "activeVMs": [
            {
                "uuid": "mch-025",
                "name": "Machine-Y",
                "seedName": "seed-25",
                "snapshot": "snap-025",
                "network": "network6",
                "vmIpAddress": "192.168.50.25",
                "hostname": "local4.localhost:3996",
                "ports": [],
                "vmHostname": "machine-y",
                "machineState": "RUNNING",
                "createdAt": "2025-03-01T00:00:00Z",
                "updatedAt": "2025-04-01T00:00:00Z",
                "storageUnits": [],
                "locked": true
            }
        ]
    }
];

const fetchMockClusterData = async () => {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve(remoteData);
        }, 3000);
    });
};

const ClusterInformation = () => {
    const [cluster, setCluster] = useState([]);

    useEffect(() => {
        (async () => {
            const data = await fetchMockClusterData();
            setCluster(data);
        })();
    }, []);

    return (
        <CCard>
            <CHeader>
                <h4 id="resources" className="card-title mb-0">
                    Cluster Information - Nodes [{cluster.length}]
                </h4>
                <div className="small text-body-secondary">January - March 2025</div>
            </CHeader>
            <CCardBody>
                <CRow>
                    <CCol xs={6}>
                        <ResourcesChart/>
                    </CCol>
                    <CCol xs={6}>
                        <VMResourcesChart/>
                    </CCol>
                </CRow>
            </CCardBody>
        </CCard>
    );
};

export default ClusterInformation;

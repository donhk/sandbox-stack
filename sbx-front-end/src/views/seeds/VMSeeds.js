import {
    CCard,
    CCardBody,
    CCardHeader, CContainer, CTable, CTableBody, CTableDataCell, CTableHead, CTableHeaderCell, CTableRow,
} from "@coreui/react";
import React from "react";

const VMSeeds = () => {

    const vmSeeds = [
        {
            "prefix": "windows",
            "user": "user1",
            "pass": "password1",
            "home": "C:\\users\\user1",
            "snapshots": [
                {
                    "name": "w1c1",
                    "cpus": 12,
                    "ramMb": 12323,
                    "comments": "Special Snapshot Comments",
                },
                {
                    "name": "w1c2",
                    "cpus": 6,
                    "ramMb": 16000,
                    "comments": "Special Snapshot Comments",
                },
            ]
        },
        {
            "prefix": "linux",
            "user": "user1",
            "pass": "password1",
            "home": "/home/user1",
            "snapshots": [
                {
                    "name": "l1c1",
                    "cpus": 16,
                    "ramMb": 18000,
                    "comments": "Special Snapshot Comments",
                },
                {
                    "name": "l1c2",
                    "cpus": 8,
                    "ramMb": 19000,
                    "comments": "Special Snapshot Comments3",
                },
                {
                    "name": "l1c2",
                    "cpus": 8,
                    "ramMb": 19000,
                    "comments": "Special Snapshot Comments2",
                },
                {
                    "name": "l1c3",
                    "cpus": 9,
                    "ramMb": 12000,
                    "comments": "Special Snapshot Comments1",
                },
            ]
        },
        {
            "prefix": "windows2",
            "user": "user1",
            "pass": "password1",
            "home": "C:\\users\\user1",
            "snapshots": [
                {
                    "name": "w1c1",
                    "cpus": 12,
                    "ramMb": 12323,
                    "comments": "Special Snapshot Comments",
                },
                {
                    "name": "w1c2",
                    "cpus": 6,
                    "ramMb": 16000,
                    "comments": "Special Snapshot Comments",
                },
            ]
        },
    ];

    return (
        <CContainer>
            {vmSeeds.map((item, index) => (
                <CCard key={index} className="mb-4">
                    <CCardHeader className="d-flex flex-wrap gap-3">
                        <span><strong>Prefix:</strong> {item.prefix}</span>
                        <span><strong>Home:</strong> {item.home}</span>
                        <span><strong>User:</strong> {item.user}</span>
                        <span><strong>Pass:</strong> {item.pass}</span>
                    </CCardHeader>
                    <CCardBody>
                        <CTable align="middle" className="mb-0 border" hover responsive>
                            <CTableHead className="text-nowrap">
                                <CTableRow>
                                    <CTableHeaderCell>
                                        Name
                                    </CTableHeaderCell>
                                    <CTableHeaderCell>
                                        CPUs
                                    </CTableHeaderCell>
                                    <CTableHeaderCell>
                                        RAM(MB)
                                    </CTableHeaderCell>
                                    <CTableHeaderCell>
                                        Comments
                                    </CTableHeaderCell>
                                </CTableRow>
                            </CTableHead>
                            <CTableBody>
                                {item.snapshots.map((snapshot, i_index) => (
                                    <CTableRow v-for="item in tableItems" key={i_index}>
                                        <CTableDataCell className="text-justify">
                                            {snapshot.name}
                                        </CTableDataCell>
                                        <CTableDataCell className="text-justify">
                                            {snapshot.cpus}
                                        </CTableDataCell>
                                        <CTableDataCell className="text-justify">
                                            {snapshot.ramMb}
                                        </CTableDataCell>
                                        <CTableDataCell className="text-justify">
                                            {snapshot.comments}
                                        </CTableDataCell>
                                    </CTableRow>
                                ))}
                            </CTableBody>
                        </CTable>
                    </CCardBody>
                </CCard>
            ))}
        </CContainer>
    )
}

export default VMSeeds;
import {
    CCard,
    CCardBody,
    CCardHeader, CContainer, CTable, CTableBody, CTableDataCell, CTableHead, CTableHeaderCell, CTableRow,
} from "@coreui/react";
import React, {useEffect} from "react";
import axios from "axios";
import {config} from "src/config";
import {useDispatch, useSelector} from "react-redux";

const fetchVMSnapshots = async () => {
    try {
        return (await axios.get(config.baseUrl + '/api/vm-seeds/list')).data;
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
};


const VMSeeds = () => {

    const dispatch = useDispatch();
    const vmSnapshots = useSelector((state) => state.vmSnapshots);

    useEffect(() => {
        (async () => {
            const data = await fetchVMSnapshots();
            dispatch({type: 'set', vmSnapshots: data});
        })();
    }, [dispatch]);

    return (
        <CContainer>
            {vmSnapshots.map((item, index) => (
                <CCard key={index} className="mb-4">
                    <CCardHeader className="d-flex flex-wrap gap-3">
                        <span><strong>Prefix:</strong> {item.prefix}</span>
                        <span><strong>Home:</strong> {item.home}</span>
                        <span><strong>User:</strong> {item.vm_user}</span>
                        <span><strong>Pass:</strong> {item.vm_pass}</span>
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
                                <CTableRow key={`${item.snapshot_name}-${index}`}>
                                    <CTableDataCell className="text-justify">
                                        {item.snapshot_name}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.snapshot_cpus}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.snapshot_ram_mb}
                                    </CTableDataCell>
                                    <CTableDataCell className="text-justify">
                                        {item.snapshot_comments}
                                    </CTableDataCell>
                                </CTableRow>
                            </CTableBody>
                        </CTable>
                    </CCardBody>
                </CCard>
            ))}
        </CContainer>
    )
}

export default VMSeeds;
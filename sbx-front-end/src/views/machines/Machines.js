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
import React, {useEffect, useRef, useState} from "react";
import clsx from 'clsx';
import dayjs from 'dayjs';
import CIcon from "@coreui/icons-react";
import {cilDevices, cilLan, cilLibrary, cilPin, cilRectangle,} from "@coreui/icons";
import ModalWindow from "src/views/machines/ModalWindow";
import lockTable from "src/views/machines/utilities";
import {config} from 'src/config';
import axios from "axios";
import {useDispatch, useSelector} from "react-redux";

function formatTimestamp(timestampMs) {
    const now = Date.now();
    let diffSeconds = Math.floor((now - timestampMs) / 1000);

    if (diffSeconds < 0) {
        return 'just now';
    }

    const hours = Math.floor(diffSeconds / 3600);
    diffSeconds %= 3600;
    const minutes = Math.floor(diffSeconds / 60);
    const seconds = diffSeconds % 60;

    let parts = [];
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (seconds > 0 || parts.length === 0) parts.push(`${seconds}s`);

    return parts.join(' ') + ' ago';
}


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

const fetchVMs = async () => {
    try {
        return (await axios.get(config.baseUrl + '/api/machines/list')).data;
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
};

const Machines = () => {

    const dispatch = useDispatch();
    const virtualMachines = useSelector((state) => state.virtualMachines);

    useEffect(() => {
        (async () => {
            const data = await fetchVMs();
            dispatch({type: 'set', virtualMachines: data});
        })();
    }, [dispatch]);

    const format = "MMM DD, hh:mm A";

    const [lockingVm, setLockingVm] = useState(new Map)
    const [lockedVm, setLockedVm] = useState(new Map)
    const [selectedMachine, setSelectedMachine] = useState(null);
    const [modalVisible, setModalVisible] = useState(false);
    const lastFocusedRef = useRef(null);
    const handleSwitchChange = async (machineRow, event) => {
        const isChecked = event.target.checked
        // start loading
        setLockingVm(prev => new Map(prev).set(machineRow.uuid, true))

        try {
            const result = await lockTable(machineRow, isChecked)
            setLockedVm(prev => new Map(prev).set(machineRow.uuid, result));
        } finally {
            setLockingVm(prev => new Map(prev).set(machineRow.uuid, false))
        }
    }

    const openModal = (machineRow, event) => {
        lastFocusedRef.current = event.currentTarget;
        setSelectedMachine(machineRow);
        setModalVisible(true);
    };
    const closeModal = () => {
        setModalVisible(false);
        setTimeout(() => {
            lastFocusedRef.current?.focus();
        }, 0);
    };
    const filterMachines = (machineRow) =>
        machineRow ? virtualMachines.filter((vm) => vm.network.networkName === machineRow.network.networkName) : [];

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
                                           title="Pin this machineRow to keep it running regardless of the client"/>
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
                                        <CButton color="success" variant="ghost">{item.network.networkName}</CButton>
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
                                        {formatTimestamp(item.updatedAt)}
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

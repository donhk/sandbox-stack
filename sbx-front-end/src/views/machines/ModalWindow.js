import {
    CFormSwitch,
    CModal,
    CModalBody,
    CModalHeader,
    CModalTitle, CSpinner, CTable, CTableBody, CTableDataCell, CTableHead, CTableHeaderCell, CTableRow
} from "@coreui/react";
import React from "react";
import CIcon from "@coreui/icons-react";
import {cilPin} from "@coreui/icons";
import clsx from "clsx";

const ModalWindow = ({
                         visible,
                         machines,
                         onClose,
                         handleSwitchChange,
                         lockingVm,
                         lockedVm,
                     }) => {
    const width = 600;
    const height = 500;
    const radius = 180;
    const centerX = width / 2;
    const centerY = height / 2;

    const getCoords = (index, total) => {
        const angle = (index / total) * 2 * Math.PI;
        const x = centerX + radius * Math.cos(angle);
        const y = centerY + radius * Math.sin(angle);
        return {x, y};
    };

    const nodeRadius = 80;

    return (
        <CModal
            alignment="center"
            visible={visible}
            onClose={onClose}
            size="xl"
        >
            <CModalHeader>
                <CModalTitle>Machines in network: {machines[0]?.network || "Unknown"}</CModalTitle>
            </CModalHeader>
            <CModalBody>
                <div style={{textAlign: "center"}}>
                    <svg width={width} height={height} style={{background: "#1c1c1c", borderRadius: "10px"}}>
                        {/* Draw all lines between all pairs */}
                        {machines.map((m1, i) => {
                            const c1 = getCoords(i, machines.length);
                            return machines.map((m2, j) => {
                                if (i >= j) return null;
                                const c2 = getCoords(j, machines.length);
                                return (
                                    <line
                                        key={`${m1.uuid}-${m2.uuid}`}
                                        x1={c1.x}
                                        y1={c1.y}
                                        x2={c2.x}
                                        y2={c2.y}
                                        stroke="#555"
                                        strokeWidth={1}
                                    />
                                );
                            });
                        })}

                        {/* Draw each node */}
                        {machines.map((machineRow, index) => {
                            const {x, y} = getCoords(index, machines.length);
                            const portLines = machineRow.ports.map(
                                (p) => `${p.name}:${p.hostPort}->${p.vmPort}`
                            );

                            return (
                                <g key={machineRow.uuid}>
                                    <circle cx={x} cy={y} r={nodeRadius} fill="#007bff"/>
                                    <text
                                        x={x}
                                        y={y - 20}
                                        textAnchor="middle"
                                        fontSize="12"
                                        fill="white"
                                        fontWeight="bold"
                                    >
                                        {machineRow.name}
                                    </text>
                                    <text
                                        x={x}
                                        y={y - 5}
                                        textAnchor="middle"
                                        fontSize="12"
                                        fill="white"
                                    >
                                        {machineRow.vmIpAddress || "N/A"}
                                    </text>
                                    {portLines.map((line, i) => (
                                        <text
                                            key={i}
                                            x={x}
                                            y={y + 10 + i * 12}
                                            textAnchor="middle"
                                            fontSize="9"
                                            fill="white"
                                        >
                                            {line}
                                        </text>
                                    ))}
                                </g>
                            );
                        })}
                    </svg>
                </div>

                <br/>
                <CTable align="middle" className="mb-0 border" hover responsive>
                    <CTableHead className="text-nowrap">
                        <CTableRow>
                            <CTableHeaderCell className="bg-body-tertiary">Name</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">Ip</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary">State</CTableHeaderCell>
                            <CTableHeaderCell className="bg-body-tertiary text-justify">
                                <CIcon icon={cilPin}
                                       title="Pin this machineRow to keep it running regardless of the client"/>
                            </CTableHeaderCell>
                        </CTableRow>
                    </CTableHead>

                    <CTableBody>
                        {machines.map((item, index) => (
                            <CTableRow
                                key={index}
                            >
                                <CTableDataCell className="text-justify">
                                    {item.name}
                                </CTableDataCell>
                                <CTableDataCell className="text-justify text-primary">
                                    {item.vmIpAddress}
                                </CTableDataCell>
                                <CTableDataCell
                                    className={clsx('text-justify', {
                                        'text-danger': item.machineState === 'FAILED',
                                        'text-success': item.machineState !== 'FAILED',
                                    })}
                                >
                                    {item.machineState}
                                </CTableDataCell>
                                <CTableDataCell className="justify-content-center">
                                    {lockingVm.get(item.uuid) ? (
                                        <CSpinner size="sm" color="info" variant="grow"/>
                                    ) : (
                                        <CFormSwitch
                                            onChange={(e) => handleSwitchChange(item, e)}
                                            id={`keep-${index}`}
                                            checked={lockedVm.get(item.uuid) ?? item.locked}
                                            disabled={item.machineState === 'FAILED'}
                                        />
                                    )}
                                </CTableDataCell>
                            </CTableRow>
                        ))}
                    </CTableBody>
                </CTable>


            </CModalBody>
        </CModal>
    );
};

export default ModalWindow;

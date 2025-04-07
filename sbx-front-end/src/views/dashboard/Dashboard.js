import React from 'react'
import {
    CCard,
    CCardBody,
    CHeader,
} from '@coreui/react'
import {ResourcesChart} from "src/views/dashboard/ResourcesCharts";
import {VMResourcesChart} from "src/views/dashboard/VMResourcesChart";

const Dashboard = () => {

    return (
        <>
            <CCard className="mb-4">
                <CHeader>
                    <h4 id="resources" className="card-title mb-0">
                        Resource Utilization
                    </h4>
                    <div className="small text-body-secondary">January - March 2025</div>
                </CHeader>
                <CCardBody>
                    <ResourcesChart/>
                </CCardBody>
            </CCard>
            <CCard className="mb-4">
                <CHeader>
                    <h4 id="resources" className="card-title mb-0">
                        VMs History
                    </h4>
                    <div className="small text-body-secondary">January - March 2025</div>
                </CHeader>
                <CCardBody>
                    <VMResourcesChart/>
                </CCardBody>
            </CCard>
        </>
    )
}

export default Dashboard

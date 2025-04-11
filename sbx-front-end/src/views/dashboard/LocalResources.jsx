import {CCard, CCardBody, CCol, CHeader, CRow} from "@coreui/react";
import {ResourcesChart} from "src/views/dashboard/ResourcesCharts";
import {VMResourcesChart} from "src/views/dashboard/VMResourcesChart";
import React from "react";

const LocalResources = () => {
    return (
        <>
            <CCard>
                <CHeader>
                    <h4 id="resources" className="card-title mb-0">
                        Local Resource Utilization
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
        </>
    )
}

export default LocalResources;
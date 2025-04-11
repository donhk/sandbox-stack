import React from 'react'
import {
    CCard,
    CCardBody, CCol,
    CHeader, CRow,
} from '@coreui/react'
import {ResourcesChart} from "src/views/dashboard/ResourcesCharts";
import {VMResourcesChart} from "src/views/dashboard/VMResourcesChart";
import LocalResources from "src/views/dashboard/LocalResources";
import ClusterInformation from "src/views/dashboard/ClusterInformation";

const Dashboard = () => {

    return (
        <>
            <LocalResources/>
            <br/>
            <ClusterInformation/>
        </>
    )
}

export default Dashboard

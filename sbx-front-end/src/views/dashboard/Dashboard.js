import React from 'react'
import LocalResources from "src/views/dashboard/local/LocalResources";
import ClusterResources from "src/views/dashboard/cluster/ClusterResources";

const Dashboard = () => {

    return (
        <>
            <LocalResources/>
            <br/>
            <ClusterResources/>
        </>
    )
}

export default Dashboard

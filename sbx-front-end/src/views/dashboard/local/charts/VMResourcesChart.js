import React, {useEffect, useRef} from 'react'
import {getStyle} from '@coreui/utils'
import {CChart} from '@coreui/react-chartjs'
import axios from "axios";
import {config} from "src/config";
import {useDispatch, useSelector} from "react-redux";

const fetchLocalResources = async () => {
    try {
        return (await axios.get(config.baseUrl + '/api/local-vm-resources')).data;
    } catch (error) {
        console.error('Error:', error);
        return {};
    }
};

const options = {
    plugins: {
        legend: {
            labels: {
                color: getStyle('--cui-body-color'),
            },
        },
    },
    scales: {
        x: {
            grid: {
                color: getStyle('--cui-border-color-translucent'),
            },
            ticks: {
                color: getStyle('--cui-body-color'),
            },
            type: 'category',
        },
        y: {
            grid: {
                color: getStyle('--cui-border-color-translucent'),
            },
            ticks: {
                color: getStyle('--cui-body-color'),
            },
            beginAtZero: true,
        },
    },
    elements: {
        line: {
            tension: 0.3,
        },
        point: {
            radius: 2,
            hitRadius: 10,
            hoverRadius: 4,
            hoverBorderWidth: 3,
        },
    },
}

function convertDataToGraph(rawData) {
    return {
        labels: rawData.labels,
        datasets: [
            {
                label: 'VMs History',
                backgroundColor: 'rgb(0,236,180)',
                borderColor: 'rgb(0,236,180)',
                pointBackgroundColor: 'rgb(0,236,180)',
                pointBorderColor: '#fff',
                data: rawData.vmCounts,
                fill: false,
            },
        ],
    };
}

export const VMResourcesChart = () => {
    const dispatch = useDispatch();
    const localVmResources = useSelector((state) => state.localVmResources);
    useEffect(() => {
        (async () => {
            const rawData = await fetchLocalResources();
            dispatch({type: 'set', localVmResources: convertDataToGraph(rawData)});
        })();
    }, [dispatch]);
    const chartRef = useRef(null);

    return <CChart type="line" data={localVmResources} options={options} ref={chartRef}/>
}

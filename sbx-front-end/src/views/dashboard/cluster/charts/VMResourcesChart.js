import React, {useEffect, useRef} from 'react'
import {getStyle} from '@coreui/utils'
import {CChart} from '@coreui/react-chartjs'

export const VMResourcesChart = () => {
    const chartRef = useRef(null)

    useEffect(() => {
        const handleColorSchemeChange = () => {
            const chartInstance = chartRef.current
            if (chartInstance) {
                const {options} = chartInstance

                if (options.plugins?.legend?.labels) {
                    options.plugins.legend.labels.color = getStyle('--cui-body-color')
                }

                if (options.scales?.x) {
                    if (options.scales.x.grid) {
                        options.scales.x.grid.color = getStyle('--cui-border-color-translucent')
                    }
                    if (options.scales.x.ticks) {
                        options.scales.x.ticks.color = getStyle('--cui-body-color')
                    }
                }

                if (options.scales?.y) {
                    if (options.scales.y.grid) {
                        options.scales.y.grid.color = getStyle('--cui-border-color-translucent')
                    }
                    if (options.scales.y.ticks) {
                        options.scales.y.ticks.color = getStyle('--cui-body-color')
                    }
                }

                chartInstance.update()
            }
        }

        document.documentElement.addEventListener('ColorSchemeChange', handleColorSchemeChange)

        return () => {
            document.documentElement.removeEventListener('ColorSchemeChange', handleColorSchemeChange)
        }
    }, [])

    const data = {
        labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September'],
        datasets: [
            {
                label: 'VMs History',
                backgroundColor: 'rgb(0,236,180)',
                borderColor: 'rgb(0,236,180)',
                pointBackgroundColor: 'rgb(0,236,180)',
                pointBorderColor: '#fff',
                data: [40, 20, 12, 39, 10, 40, 39, 80, 40],
                fill: false,
            },
        ],
    }

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

    return <CChart type="line" data={data} options={options} ref={chartRef}/>
}

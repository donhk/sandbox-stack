import React from 'react'

const Dashboard = React.lazy(() => import('./views/dashboard/Dashboard'))
const Machines = React.lazy(() => import('./views/machines/Machines'))
const VMSeeds = React.lazy(() => import('./views/seeds/VMSeeds'))
const Settings = React.lazy(() => import('./views/settings/Settings'))

const routes = [
    {path: '/', exact: true, name: 'Home'},
    {path: '/dashboard', name: 'Dashboard', element: Dashboard},
    {path: '/machines', name: 'Machines', element: Machines},
    {path: '/vm-seeds', name: 'VMSeeds', element: VMSeeds},
    {path: '/settings', name: 'Settings', element: Settings},
]

export default routes

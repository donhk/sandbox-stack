import React from 'react'
import CIcon from '@coreui/icons-react'
import {
    cilFactory,
    cilSettings,
    cilSitemap,
    cilSpeedometer,
} from '@coreui/icons'
import {CNavItem, CNavTitle} from '@coreui/react'

const _nav = [
    {
        component: CNavTitle,
        name: 'Control',
    },
    {
        component: CNavItem,
        name: 'Home',
        to: '/dashboard',
        icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon"/>,
    },
    {
        component: CNavItem,
        name: 'Machines',
        to: '/machines',
        icon: <CIcon icon={cilSitemap} customClassName="nav-icon"/>,
    },
    {
        component: CNavItem,
        name: 'VM Seeds',
        to: '/vm-seeds',
        icon: <CIcon icon={cilFactory} customClassName="nav-icon"/>,
    },
    {
        component: CNavTitle,
        name: 'Sbx Settings',
    },
    {
        component: CNavItem,
        name: 'Settings',
        to: '/settings',
        icon: <CIcon icon={cilSettings} customClassName="nav-icon"/>,
    },
]

export default _nav

import React from 'react'
import {useSelector, useDispatch} from 'react-redux'

import {
    CImage,
    CSidebar,
    CSidebarHeader,
} from '@coreui/react'

import {AppSidebarNav} from './AppSidebarNav'

// sidebar nav config
import navigation from '../_nav'
import sbxLogo from '../assets/brand/sandboxer-logo.webp';
import {Link} from "react-router-dom";

const AppSidebar = () => {
    const dispatch = useDispatch()
    const sidebarShow = useSelector((state) => state.sidebarShow)

    return (
        <CSidebar
            className="border-end"
            colorScheme="dark"
            position="fixed"
            visible={sidebarShow}
            onVisibleChange={(visible) => {
                dispatch({type: 'set', sidebarShow: visible})
            }}
        >
            <CSidebarHeader className="border-bottom">
                <Link to="/dashboard">
                    <CImage
                        src={sbxLogo}
                        alt="Sandboxer Logo"
                        className="img-fluid"
                        style={{cursor: 'pointer'}}
                    />
                </Link>
            </CSidebarHeader>
            <AppSidebarNav items={navigation}/>
        </CSidebar>
    )
}

export default React.memo(AppSidebar)

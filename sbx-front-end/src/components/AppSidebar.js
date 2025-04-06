import React from 'react'
import {useSelector, useDispatch} from 'react-redux'

import {
    CSidebar,
    CSidebarHeader,
} from '@coreui/react'

import {AppSidebarNav} from './AppSidebarNav'

// sidebar nav config
import navigation from '../_nav'

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
                <a href="/#/dashboard">
                    <img
                        className="img-fluid"
                        src='src/assets/brand/sandboxer-logo.webp'
                        alt="Sandboxer Logo"/>
                </a>
            </CSidebarHeader>
            <AppSidebarNav items={navigation}/>
        </CSidebar>
    )
}

export default React.memo(AppSidebar)

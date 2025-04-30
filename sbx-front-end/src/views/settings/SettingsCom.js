import {CCard, CCardBody, CCardHeader, CCol, CFormInput, CRow, CSpinner} from "@coreui/react";
import {useEffect} from "react";
import axios from "axios";
import {config} from "src/config";
import {useDispatch, useSelector} from "react-redux";

const fetchSettings = async () => {
    try {
        return (await axios.get(config.baseUrl + '/api/sbx-settings/list')).data;
    } catch (error) {
        console.error('Error:', error);
        return [];
    }
};

const SettingsComp = () => {
    const dispatch = useDispatch();
    const sbxSettings = useSelector((state) => state.sbxSettings);

    useEffect(() => {
        (async () => {
            const data = await fetchSettings();
            dispatch({type: 'set', sbxSettings: data});
        })();
    }, [dispatch]);

    // Prevent render until settings are loaded
    if (!sbxSettings || !sbxSettings.dbName) {
        return (
            <>
                <CCard>
                    <CCardHeader>Settings</CCardHeader>
                    <CCardBody>
                        <div className="text-center">
                            <CSpinner color="info"/>
                        </div>
                    </CCardBody>
                </CCard>
            </>
        );
    }


    return (
        <>
            <CCard>
                <CCardHeader>Settings</CCardHeader>
                <CCardBody>
                    <CRow className="mb-3">
                        <CCol sm={2}>
                            <strong>Database Name:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.dbName}
                                readOnly
                                plainText
                            />
                        </CCol>
                        <CCol sm={2}>
                            <strong>Web Port:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.webPort}
                                readOnly
                                plainText
                            />
                        </CCol>
                        <CCol sm={2}>
                            <strong>TCP Port:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.tcpPort}
                                readOnly
                                plainText
                            />
                        </CCol>
                        <CCol sm={2}>
                            <strong>Service Port:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.sbxServicePort}
                                readOnly
                                plainText
                            />
                        </CCol>
                        <CCol sm={2}>
                            <strong>Service Low Port:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.sbxServiceLowPort}
                                readOnly
                                plainText
                            />
                        </CCol>
                        <CCol sm={2}>
                            <strong>Service High Port:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.sbxServiceHighPort}
                                readOnly
                                plainText
                            />
                        </CCol>
                    </CRow>
                    <CRow>
                        <CCol sm={12}>
                            <strong>Build Info:</strong>
                            <CFormInput
                                type="text"
                                value={sbxSettings.buildInfo}
                                readOnly
                                plainText
                            />
                        </CCol>
                    </CRow>
                </CCardBody>
            </CCard>
        </>
    );
};

export default SettingsComp;

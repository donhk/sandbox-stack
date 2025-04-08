import {CCard, CCardBody, CCardHeader, CCol, CFormInput, CFormLabel, CRow} from "@coreui/react";
import {useEffect, useState} from "react";

const SettingsComp = () => {
    const [settings1, setSettings1] = useState("");
    const [settings2, setSettings2] = useState("");

    const loadSettings = async () => {
        // Simulate fetching or loading settings
        setSettings1("Test Default Value1");
        setSettings2("Test Default Value2");
    };

    useEffect(() => {
        loadSettings().then(_r => {
        });
    }, []); // empty dependency array = run once on component mount

    return (
        <>
            <CCard>
                <CCardHeader>Settings</CCardHeader>
                <CCardBody>
                    <CRow className="mb-3">
                        <CFormLabel htmlFor="settings-1" className="col-sm-2 col-form-label">
                            Settings  example  1:
                        </CFormLabel>
                        <CCol sm={5}>
                            <CFormInput
                                type="text"
                                id="settings-1"
                                value={settings2}
                                readOnly
                                plainText
                            />
                        </CCol>
                    </CRow>
                    <CRow className="mb-3">
                        <CFormLabel htmlFor="settings-2" className="col-sm-2 col-form-label">
                            Settings1:
                        </CFormLabel>
                        <CCol sm={5}>
                            <CFormInput
                                type="text"
                                id="settings-2"
                                value={settings2}
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

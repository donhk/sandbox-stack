import {
    CButton,
    CCard,
    CCardBody,
    CCardHeader, CCloseButton,
    CFormInput,
    CInputGroup,
    CInputGroupText, COffcanvas,
    COffcanvasBody, COffcanvasHeader, COffcanvasTitle
} from "@coreui/react";
import {useState} from "react";
import WalkingPusheen from "../../components/WalkingPusheen";
import axios from "axios";
import {config} from "src/config";

const ControlPlane = () => {
    const [destroyElapsedMs, setDestroyElapsedMs] = useState('');
    const [showSuccessAlert, setShowSuccessAlert] = useState(false);
    const [vmNameToDestroy, setVmNameToDestroy] = useState('');
    const [loading, setLoading] = useState(false);
    const [showCat, setShowCat] = useState(false);

    const reloadVms = () => {
        setLoading(true);
        setShowCat(true); // start walking pusheen

        setTimeout(() => {
            setLoading(false);
            setShowCat(false); // stop pusheen when done
        }, 30000);
    };

    const godKill = () => {
        setLoading(true);
        setShowCat(true); // start walking pusheen

        setTimeout(() => {
            setLoading(false);
            setShowCat(false); // stop pusheen when done
        }, 30000);
    };

    const destroyVm = async () => {
        if (!vmNameToDestroy) {
            alert("Please enter a VM name to destroy.");
            return;
        }

        setLoading(true);
        setShowCat(true);

        try {
            const time = Date.now();
            const resp = (await axios.delete(config.baseUrl + `/api/machine/${vmNameToDestroy}`)).data;
            setDestroyElapsedMs(`✅ VM ${resp.uuid} successfully destroyed in ${Date.now() - time} ms`);
            setVmNameToDestroy('')
            // Show alert
            setShowSuccessAlert(true);
            setTimeout(() => {
                setShowSuccessAlert(false);
                setDestroyElapsedMs('')
            }, 10_000);

        } catch (error) {
            console.error('Error destroying VM:', error);
        } finally {
            setLoading(false);
            setShowCat(false);
        }
    };

    return (
        <>
            <WalkingPusheen visible={showCat}/>

            <COffcanvas placement="top" visible={showSuccessAlert} onHide={() => setShowSuccessAlert(false)}>
                <COffcanvasHeader>
                    <COffcanvasTitle>VM Destroy Operation</COffcanvasTitle>
                    <CCloseButton className="text-reset" onClick={() => setShowSuccessAlert(false)}/>
                </COffcanvasHeader>
                <COffcanvasBody>
                    {destroyElapsedMs}
                </COffcanvasBody>
            </COffcanvas>

            <CCard>
                <CCardHeader>Reload VMs Metadata</CCardHeader>
                <CCardBody>
                    <CButton color="primary" onClick={reloadVms} disabled={loading}>
                        Reload VMs
                    </CButton>
                </CCardBody>
            </CCard>
            <br/>
            <CCard>
                <CCardHeader>Destroy all VMs (God Kill)</CCardHeader>
                <CCardBody>
                    <CButton color="primary" onClick={godKill} disabled={loading}>
                        Destroy all VMs
                    </CButton>
                </CCardBody>
            </CCard>
            <br/>
            <CCard>
                <CCardHeader>Destroy VM</CCardHeader>
                <CCardBody>
                    <CInputGroup className="mb-sm-1">
                        <CInputGroupText id="basic-addon3">VM To Destroy</CInputGroupText>
                        <CFormInput id="basic-url"
                                    aria-describedby="basic-addon3"
                                    value={vmNameToDestroy}
                                    onChange={(e) => setVmNameToDestroy(e.target.value)}
                        />
                        <CButton
                            color="primary"
                            onClick={destroyVm}
                            disabled={loading || vmNameToDestroy.trim() === ''}
                        >
                            Destroy VM
                        </CButton>
                    </CInputGroup>
                </CCardBody>
            </CCard>
        </>
    );
};

export default ControlPlane;

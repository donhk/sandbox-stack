import {CButton, CCard, CCardBody, CCardHeader} from "@coreui/react";
import {useState} from "react";
import WalkingPusheen from "../../components/WalkingPusheen"; // update path if needed

const ControlPlane = () => {
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

    const destroyVm = () => {
        setLoading(true);
        setShowCat(true); // start walking pusheen

        setTimeout(() => {
            setLoading(false);
            setShowCat(false); // stop pusheen when done
        }, 30000);
    };

    return (
        <>
            <WalkingPusheen visible={showCat}/>

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
                    <CButton color="primary" onClick={destroyVm} disabled={loading}>
                        Destroy VM
                    </CButton>
                </CCardBody>
            </CCard>
        </>
    );
};

export default ControlPlane;

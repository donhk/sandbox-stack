import {config} from 'src/config';
import axios from "axios";

async function lockTable(machine, isChecked) {
    console.log("Switch is now:", isChecked ? "ON" : "OFF");
    console.log("locking uuid " + machine.uuid);
    console.log("locking hostname " + machine.hostname);
    let payload = {
        uuid: machine.uuid,
        name: machine.name,
        locked: isChecked,
        network: {
            networkType: machine.network.networkType,
            networkName: machine.network.networkName
        }
    }
    try {
        let response = await axios.put(config.baseUrl + '/api/machine/pin', payload);
        if (response.status === 200) {
            return response.data.locked;
        }
    } catch (error) {
        console.error('Error Locking VM:', error);
        return !isChecked;
    }
}


export default lockTable;
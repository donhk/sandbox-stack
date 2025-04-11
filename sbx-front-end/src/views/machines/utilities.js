function lockTable(machine, isChecked) {
    console.log("Switch is now:", isChecked ? "ON" : "OFF");
    console.log("locking uuid " + machine.uuid);
    console.log("locking hostname " + machine.hostname);

    return new Promise((resolve) => {
        setTimeout(() => {
            console.log("Finished locking...");
            resolve(isChecked);
        }, 5000);
    });
}


export default lockTable;
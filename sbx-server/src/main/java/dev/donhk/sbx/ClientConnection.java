package dev.donhk.sbx;

import dev.donhk.database.VMDataAccessService;
import dev.donhk.helpers.Utils;
import dev.donhk.pojos.*;
import dev.donhk.vbox.LaunchMode;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.donhk.helpers.Constants.*;

public class ClientConnection {

    private final Socket socket;
    private String name = "GUEST";
    private Thread reader = null;
    private PrintWriter output = null;
    private BufferedReader input = null;
    private boolean destroyMachineCalled = false;
    private boolean alive = false;         //this indicated whether this client connection is alive
    private final VBoxManager vBoxManager;
    private final VMDataAccessService VMDataAccessService;

    private ScheduledExecutorService poller = null;
    private TimeMark lastClientContact;
    private long MAX_TIME_WITHOUT_RESPONSE = 70; // seconds to wait before the machine has confirmed that it is up
    private static final long MAX_TIME_WITHOUT_RESPONSE_AFTER_BOOT_NO_NAT = 60 * 3; // seconds to wait after the machine confirmed it is up
    private static final long MAX_TIME_WITHOUT_RESPONSE_AFTER_BOOT_NAT_NET = 60 * 6; // seconds to wait after the machine confirmed it is up
    private boolean HAS_NAT_NETWORK = false;
    private String NAT_NETWORK = null;

    //test time details
    private int DEFAULT_EXEC_TIME_LIMIT = 60 * 24; // default 24 hours
    private int EXEC_TIME_THRESHOLD = 60 * 24 * 31; // in minutes, max 31 days

    //user ports
    private final Map<String, Integer> ports = new LinkedHashMap<>(); //ports used by this vm

    public ClientConnection(Socket socket, VirtualBoxManager boxManager, Connection conn) {
        this.socket = socket;
        this.vBoxManager = new VBoxManager(boxManager);
        this.VMDataAccessService = new VMDataAccessService(conn);
        //polling stuff
        ports.put("ssh", -1);
    }

    /***
     * Method invoked when the socket connection is established, this
     * will start the input/output streams and the thread in charge of
     * read from the user
     * @throws Exception on error
     */
    public void start() throws Exception {
        //output stream
        output = new PrintWriter(socket.getOutputStream(), true);
        //input stream
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //this is listing for instructions coming from the client
        reader = new Thread(() -> {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    Logger.info(name + " says " + line);
                    try {
                        processCmd(line);
                    } catch (Exception e) {
                        Logger.warn("Unexpected exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                Logger.info("Connection finished: " + name);
            } catch (Exception e) {
                //ignore
            }
        });
        reader.start();
        //set the current flag to active
        alive = true;
    }

    /**
     * This method contains the logic to process each client connection
     *
     * @param data instructions from the client
     */
    private void processCmd(String data) {
        //client name definition, this should be the very first thing
        if (data.startsWith(MY_NAME)) {
            registerMachine(data);
        }
        //if the default name has not be changed, the client has
        //not sent its credentials
        else if (name.equals("GUEST")) {
            output.println(ANONYMOUS_CLIENT_NOT_ALLOWED);
        }
        //get port
        else if (data.startsWith(CLONE_MACHINE)) {
            cloneMachine(data);
        }//get port
        else if (data.startsWith(GET_VM_IPV4)) {
            getMachineIPv4();
        }//get port
        else if (data.startsWith(CREATE_PORT_FORWARD_RULE)) {
            createPortForwardRule(data);
        }//get port
        else if (data.startsWith(GET_FREE_PORT)) {
            getFreePort();
        }
        //get the ssh port reserved in server
        else if (data.startsWith(GET_SSH_PORT)) {
            getSSHPort();
        }
        //start up machine
        else if (data.startsWith(START_UP_VM)) {
            startUpMachine();
        }
        //get remote base
        else if (data.startsWith(DESTROY_MACHINE)) {
            destroyMachine(CLEAN_END);
        }
        //change the default execution time
        else if (data.startsWith(OVERWRITE_EXEC_TIME)) {
            overwriteExecTime(data);
        }
        //refresh the rule to access to the vm
        else if (data.startsWith(UPDATE_FORWARD_RULE)) {
            updatePortForwardRule(data);
        }
        //update ssh por forward rule
        else if (data.startsWith(UPDATE_SSH_FORWARD_RULE)) {
            updateSSHPortForwardRule();
        }
        //refresh the rule to access to the vm
        else if (data.startsWith(CONFIRM_MACHINE_UP)) {
            confirmMachineUp();
        }
        //refresh the rule to access to the vm
        else if (data.startsWith(CREATE_NAT_NETWORK)) {
            createNatNetwork();
        }
        //create shared storage units
        else if (data.startsWith(CREATE_SHARED_STORAGE_UNITS)) {
            createSharedStorageUnits(data);
        }
        //attach shared storage units
        else if (data.startsWith(ATTACH_SHARED_STORAGE_UNITS)) {
            attachSharedStorageUnits(data);
        }
        //invalid command
        else {
            output.println(INVALID_COMMAND);
        }
    }

    /**
     * Execute the very first tasks, obtain a name from the user,
     * assign the name to the current session and pre-register the machine to
     * reserve the port for a future usage
     *
     * @param data user credential
     */
    private void registerMachine(String data) {
        String nameRequested = data.substring(data.indexOf("|") + 1);

        //it is only allowed to set the name once
        if (name.equals("GUEST")) {
            name = nameRequested;
        }
        //the client name was already defined
        else {
            output.println(CLIENT_NAME_ALREADY_DEFINED);
            return;
        }
        //register machine on db, at this point the machine has not claimed any IP
        //we are just adding to the database to allow the other registration steps to succeed
        try {
            //register machine in db
            VMDataAccessService.insertMachine(name, "", PREREGISTER, "NAT");

            configurePolling();

        } catch (SQLException e) {
            Logger.warn("sql exception " + e.getMessage());
            output.println(RUNTIME_ERR);
            abortAndClean(OPERATION_EXCEPTION);
        } catch (Exception e) {
            Logger.error("There was a problem establishing polling configuration, abort and clean " + name, e);
            output.println(RUNTIME_ERR);
            abortAndClean(OPERATION_EXCEPTION);
        }
    }

    private void configurePolling() throws SQLException {
        synchronized (ClientConnection.class) {
            //get another port for the ssh connections
            final int sshPort = VMDataAccessService.getHostPort();
            ports.put("ssh", sshPort);
            VMDataAccessService.updatePort(sshPort, HostPortStatus.BUSY);
            final String sshPortStatus = VMDataAccessService.getPortStatus(sshPort);
            Logger.info("ssh port " + sshPort + " marked as " + sshPortStatus + " for " + name);

            //register machine ssh port
            VMDataAccessService.insertRule(name, "ssh" + sshPort, sshPort, 22);
            Logger.info(name + " port ssh[" + sshPort + "]");
        }
        lastClientContact = new TimeMark(VMDataAccessService, name);
        //update the machine time for the very first time
        VMDataAccessService.pollMachine(name);
        //starting polling service
        poller = Executors.newSingleThreadScheduledExecutor();
        poller.scheduleAtFixedRate(() -> {
            final long secondsLastUpdate = lastClientContact.secondsSinceLastUpdate();
            //Logger.info(name + "last update was " + secondsLastUpdate + " seconds ago [limit: " + MAX_TIME_WITHOUT_RESPONSE + " seconds]");
            if (secondsLastUpdate > MAX_TIME_WITHOUT_RESPONSE) {
                Logger.info(name + " has not responded in " + secondsLastUpdate + " seconds, [limit: " + MAX_TIME_WITHOUT_RESPONSE + " seconds]. stopping");
                abortAndClean(RESPONSE_TIMEOUT);
            }
            //if this test takes more than DEFAULT_EXEC_TIME_LIMIT, it will be aborted
            //this will prevent hanging jobs
            if (destroyMachineCalled) {
                //if the destroy method was called, then there is no need to check for
                //default exec times or we will get MIN LocalDateTime as result
                return;
            }
            final long minSinceStart = lastClientContact.minutesSinceStart();
            if (minSinceStart > DEFAULT_EXEC_TIME_LIMIT) {
                Logger.info(name + " exceed its execution time, it will be destroyed after " + minSinceStart + " minutes");
                //we will let the client know that this execution was aborted due to the time exceed
                alive = false;
                //harakiri
                abortAndClean(TIME_EXEC_EXCEED);
            }
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    /**
     * Clone a given machine/snapshot and create a new one
     *
     * @param data input from the user with details about that to clone  CLONE_MACHINE|seedName|snapshot
     */
    private void cloneMachine(String data) {
        //     0           1        2      3
        //CLONE_MACHINE|seedName|snapshot|nat_network
        String[] parts = data.split("\\|");
        if (parts.length != 4) {
            //the user sent less than 3 args, abort and clean
            output.println(INVALID_ARG);
            abortAndClean(OPERATION_EXCEPTION);
        } else {
            try {
                String seedName = parts[1];
                String snapshot = parts[2];
                String natNetwork = parts[3];
                //review if this request is valid
                Logger.info(name + " requested to create a clone from: " + seedName + " " + snapshot);
                if (!VMDataAccessService.machineAndSnapExist(seedName, snapshot)) {
                    output.println(INVALID_ARG);
                    throw new Exception("Invalid machine name or snapshot name [" + name + "]");
                }
                Logger.info(name + " metadata provided is correct, a clone will be created");
                Logger.info(name + " wants its clone to be connected to " + natNetwork);
                if (!natNetwork.equalsIgnoreCase("nat")) {
                    NAT_NETWORK = natNetwork;
                    Logger.info("updating machines network information for " + name + " to use " + NAT_NETWORK);
                    VMDataAccessService.updateVmNetworkInfo(name, NAT_NETWORK);
                    HAS_NAT_NETWORK = true;
                }
                //for now lets use only NATs due to the dhcp issues
                Logger.info("Cloning " + name + " from " + seedName + " (" + snapshot + ") attached to " + NAT_NETWORK);
                vBoxManager.cloneMachineFromSeed(seedName, snapshot, name, NAT_NETWORK);
                VMDataAccessService.updateVmMeta(name, "CLONING", parts[1]);
                //if you need to created a shared dir, this is a
                //good point to do so
            } catch (Exception e) {
                Logger.warn(e.getMessage());
                output.println(RUNTIME_ERR);
                abortAndClean(OPERATION_EXCEPTION);
            }
        }
        output.println(OPERATION_SUCCESSFUL);
    }

    /**
     * returns the current port assigned to the current connection
     */
    private void getFreePort() {
        int freePort = -1;
        try {
            freePort = VMDataAccessService.getHostPort();
        } catch (SQLException e) {
            Logger.warn("error getting free port " + e.getMessage());
        }
        output.println(freePort);
    }

    private void getSSHPort() {
        output.println(ports.get("ssh"));
    }

    private void createSharedStorageUnits(String data) {
        List<String> response = new ArrayList<>();
        boolean error = false;
        //            0                1      2
        //CREATE_SHARED_STORAGE_UNITS|size|numDisks
        String[] parts = data.split("\\|");
        try {
            int size = Integer.parseInt(parts[1]);
            int numDisks = Integer.parseInt(parts[2]);
            response = vBoxManager.createSharedStorage(name, numDisks, size);
        } catch (Exception w) {
            error = true;
            Logger.error("Error creating shareable storage " + w.getMessage());
            w.printStackTrace();
        }
        if (error || response == null) {
            output.println(RUNTIME_ERR);
            abortAndClean(OPERATION_EXCEPTION);
        } else {
            output.println(response.toString());
        }
    }

    private void attachSharedStorageUnits(String data) {
        //            0                          1
        //ATTACH_SHARED_STORAGE_UNITS|[path_1, path_2[, path_n]]
        String[] parts = data.split("\\|");
        List<String> disks = Utils.deserializeList(parts[1]);
        if (disks == null) {
            Logger.error("Error deserializing list of disks, input was " + data);
            return;
        }
        String result = OPERATION_SUCCESSFUL;
        try {
            vBoxManager.addSharedStorageToMachine(name, disks);
            Logger.info("Operation successful fot " + name);
        } catch (Exception e) {
            Logger.error("Error attaching disks to " + name);
            e.printStackTrace();
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result.equals(RUNTIME_ERR)) {
            abortAndClean(result);
        }
    }

    /**
     * returns the ip claimed by the current machine or null if the property has not been
     * assigned yet
     */
    private void getMachineIPv4() {
        /*
        The error management for this function lives in the client, since it is not possible to know when
        a machine started up completely, we will defer this task to the client as it has the details
        to establish a SSH connection which will indicate us when the machine is ready for business
         */
        String result;
        try {
            result = vBoxManager.getMachineIPv4(name);
        } catch (Exception e) {
            Logger.warn("Problem getting vm ipv4 for " + name);
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result != null && result.equals(RUNTIME_ERR)) {
            abortAndClean(OPERATION_EXCEPTION);
        }
    }

    private void createPortForwardRule(String data) {
        //    0                       1       2       3
        //CREATE_PORT_FORWARD_RULE|hostPort|vmPort|ruleName
        String result = OPERATION_SUCCESSFUL;
        String[] parts = data.split("\\|");
        if (parts.length != 4) {
            result = INVALID_ARG;
        } else {
            try {
                Logger.info("Creating custom port forward rule " + name);
                String ruleName = name + "_" + parts[3];
                int hostPort = Integer.parseInt(parts[1]);
                int guestPort = Integer.parseInt(parts[2]);
                if (HAS_NAT_NETWORK) {
                    vBoxManager.addNATNetworkPortForwardRule(NAT_NETWORK, hostPort, guestPort, name, ruleName);
                } else {
                    vBoxManager.addNATPortForwardRule(hostPort, guestPort, name, ruleName);
                }
                //this might have issue on concurrent environments, watch out
                //as one port might be attempted to use by 2 clients
                VMDataAccessService.insertRule(name, ruleName, hostPort, guestPort);
                //save the port in the map for later clean up
                ports.put(ruleName, hostPort);
                Logger.info("Custom port forward rule " + name + " created " + hostPort + "->" + guestPort + " [" + ruleName + "]");
            } catch (Exception e) {
                result = INVALID_ARG;
                Logger.info(e.getMessage(), e);
            }
        }
        output.println(result);
        if (result.equals(INVALID_ARG)) {
            abortAndClean(INVALID_ARG);
        }
    }

    private void updatePortForwardRule(String data) {
        //    0                    1      2        3          4
        //UPDATE_FORWARD_RULE|hostPort|vmPort|oldRuleName|newRuleName
        String result = OPERATION_SUCCESSFUL;
        String[] parts = data.split("\\|");
        if (parts.length != 5) {
            result = INVALID_ARG;
        } else {
            try {
                int hostPort = Integer.parseInt(parts[1]);
                int guestPort = Integer.parseInt(parts[2]);
                String oldRuleName = name + "_" + parts[3];
                String newRuleName = name + "_" + parts[4];
                Logger.info("Updating custom port forward rule " + name + " " + hostPort + "->" + guestPort);
                if (HAS_NAT_NETWORK) {
                    vBoxManager.rmNATNetworkPortForwardRule(NAT_NETWORK, oldRuleName);
                    vBoxManager.addNATNetworkPortForwardRule(NAT_NETWORK, hostPort, guestPort, name, newRuleName);
                } else {
                    vBoxManager.rmNATPortForwardRule(name, oldRuleName);
                    vBoxManager.addNATPortForwardRule(hostPort, guestPort, name, newRuleName);
                }
                //this might have issue on concurrent environments, watch out
                //as one port might be attempted to use by 2 clients
                VMDataAccessService.updateRule(name, oldRuleName, newRuleName, hostPort, guestPort);
                //save the port in the map for later clean up
                ports.remove(oldRuleName);
                ports.put(newRuleName, hostPort);
                Logger.info("Update successful of custom port forward rule " + name + " " + hostPort + "->" + guestPort + " [" + newRuleName + "]");
            } catch (Exception e) {
                result = INVALID_ARG;
                Logger.info(e.getMessage(), e);
            }
        }
        output.println(result);
        if (result.equals(INVALID_ARG)) {
            abortAndClean(INVALID_ARG);
        }
    }

    private void updateSSHPortForwardRule() {
        String result = OPERATION_SUCCESSFUL;
        Logger.info("Updating forward rule for " + name);
        try {
            Logger.info("Removing old forward rule for " + name);
            Rule sshRule = VMDataAccessService.geSSHRule(name);
            //remove old rule
            Logger.info("Creating new forward rule for " + name);
            if (HAS_NAT_NETWORK) {
                vBoxManager.rmNATNetworkPortForwardRule(NAT_NETWORK, sshRule.rule_name);
                vBoxManager.addNATNetworkPortForwardRule(NAT_NETWORK, ports.get("ssh"), 22, name, sshRule.rule_name);
            } else {
                vBoxManager.rmNATPortForwardRule(name, sshRule.rule_name);
                vBoxManager.addNATPortForwardRule(ports.get("ssh"), 22, name, sshRule.rule_name);
            }
            VMDataAccessService.updateMachine(name, vBoxManager.getMachineIPv4(name), "WAITING_IP");
        } catch (Exception e) {
            e.printStackTrace();
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result.equals(RUNTIME_ERR)) {
            abortAndClean(OPERATION_EXCEPTION);
        }
    }

    private void startUpMachine() {
        String result = OPERATION_SUCCESSFUL;
        try {
            VMDataAccessService.updateVmState(name, "STARTING_UP");
            vBoxManager.launchMachine(name, LaunchMode.headless);
            //update machine register with the new IP
            VMDataAccessService.updateMachine(name, vBoxManager.getMachineIPv4(name), "WAITING_IP");
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result.equals(RUNTIME_ERR)) {
            abortAndClean(OPERATION_EXCEPTION);
        } else {
            //if the machine is up, increase the max time without response
            if (HAS_NAT_NETWORK) {
                MAX_TIME_WITHOUT_RESPONSE = MAX_TIME_WITHOUT_RESPONSE_AFTER_BOOT_NAT_NET;
            } else {
                MAX_TIME_WITHOUT_RESPONSE = MAX_TIME_WITHOUT_RESPONSE_AFTER_BOOT_NO_NAT;
            }
        }
    }

    private void confirmMachineUp() {
        String result = OPERATION_SUCCESSFUL;
        try {
            VMDataAccessService.updateMachine(name, vBoxManager.getMachineIPv4(name), "UP+IP ASSIGNED");
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result.equals(RUNTIME_ERR)) {
            abortAndClean(OPERATION_EXCEPTION);
        }
    }

    private void createNatNetwork() {
        String result = name;
        try {
            Logger.info(name + " requested NAT Network");
            VMDataAccessService.insertNATNetwork(name);
            if (vBoxManager.createNatNetwork(name)) {
                Logger.info("NAT Network " + name + " created successfully");
            } else {
                Logger.info("NAT Network creation failed [" + name + "]");
                result = RUNTIME_ERR;
            }
        } catch (Exception e) {
            Logger.warn(e.getMessage());
            result = RUNTIME_ERR;
        }
        output.println(result);
        if (result.equals(RUNTIME_ERR)) {
            abortAndClean(OPERATION_EXCEPTION);
        }
    }

    private void destroyMachine(String lastState) {
        Logger.info("Destroy machine called for " + name);
        destroyMachineCalled = true;
        final DestroyVM terminator =
                DestroyVM.newInstance(
                        VMDataAccessService,
                        ports,
                        HAS_NAT_NETWORK,
                        NAT_NETWORK,
                        name,
                        lastState,
                        vBoxManager
                );
        terminator.destroy();
    }

    private void overwriteExecTime(String data) {
        try {
            final String newTime = data.substring(data.indexOf("|") + 1);
            int time = Integer.parseInt(newTime);
            if (time >= 5 && time <= EXEC_TIME_THRESHOLD) {
                DEFAULT_EXEC_TIME_LIMIT = time;
                output.println(OPERATION_SUCCESSFUL);
            } else {
                output.println(INVALID_ARG);
            }
        } catch (Exception e) {
            output.println(INVALID_ARG);
        }
    }

    /**
     * Close active connections
     * and drop any file created with this connection
     *
     * @param lastState new state for the db
     */
    public void abortAndClean(String lastState) {
        Logger.info("abort and clean called for " + name);
        //stop polling related threads
        //thread reader
        if (poller != null) {
            Logger.info("Stopping polling thread of " + name);
            poller.shutdownNow();
            Logger.info("Is polling shutdown? " + name + " " + poller.isShutdown());
            Logger.info("Is polling terminated? " + name + " " + poller.isTerminated());
        }
        //alive flag
        alive = false;
        //main socket reader
        if (reader != null) {
            reader.interrupt();
        }
        if (output != null) {
            output.close();
        }
        try {
            socket.close();
        } catch (Exception e) {
            Logger.warn("There was an error closing the connection to client " + name);
            e.printStackTrace();
        }

        //drop everything
        if (!destroyMachineCalled) {
            destroyMachine(lastState);
        } else {
            Logger.info("second call ignored " + name + " as it was already called before");
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public String getName() {
        return name;
    }

}

package dev.donhk.vbox;

import dev.donhk.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_7_1.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VBoxManager {
    /*
        # create NatNetwork
        vboxmanage natnetwork add --netname sandbox1NAT --network "10.0.0.0/8" --enable --dhcp on
        # add dhcp server to the sandbox1NAT
        vboxmanage dhcpserver add --netname sandbox1NAT --ip 10.0.0.3 --lowerip 10.10.0.1 --upperip 10.10.0.254 --netmask 255.0.0.0
        vboxmanage dhcpserver modify --netname sandbox1NAT --enable

        # create hostonly interface
        VBoxManage hostonlyif create
        VBoxManage hostonlyif ipconfig vboxnet0 --ip 172.16.0.1 --netmask 255.240.0.0

        # add dhcp server to the hostonly interface
        VBoxManage dhcpserver add --ifname vboxnet0 --ip 172.16.0.3 --lowerip 172.16.10.1 --upperip 172.16.10.254 --netmask 255.240.0.0
        VBoxManage dhcpserver modify --ifname vboxnet0 --enable

        # remove dhcpserver
        vboxmanage dhcpserver remove --netname name

        vboxmanage list dhcpservers
        vboxmanage list natnets
     */

    private final Logger logger = LoggerFactory.getLogger(VBoxManager.class);
    private final VirtualBoxManager boxManager;
    private final IVirtualBox vbox;
    private final static List<String> groups = Collections.singletonList("/sandboxer/sbx-clients");
    private IProgress progress;

    public VBoxManager(VirtualBoxManager boxManager) {
        this.boxManager = boxManager;
        vbox = boxManager.getVBox();
    }

    public String getVBoxVersion() {
        return vbox.getVersion();
    }

    /**
     * Retrieves a list of VirtualBox machines that are currently in the specified state.
     *
     * <p>This method filters the available VirtualBox machines and returns only those
     * whose current state matches the given {@link MachineState}.</p>
     *
     * @param state the desired {@link MachineState} to filter machines by
     * @return a list of {@link IMachine} instances that match the specified state
     */
    public List<IMachine> getMachines(MachineState state) {
        List<IMachine> iMachines = new ArrayList<>();
        for (IMachine machine : vbox.getMachines()) {
            if (machine.getState() == state) {
                iMachines.add(machine);
            }
        }
        return iMachines;
    }

    /**
     * Clones a vm from a given seed machine and a snapshot, also depending on the value of natNetwork
     * it will virtually 'attach' the vm to a NAT Network, if the  natNetwork is null, it will add the machine
     * to a NAT where the machine won't have access to other vms
     *
     * @param seedName    name of the vm to clone from
     * @param snapshot    snapshot which will be used as reference for the cloning
     * @param machineName new name for this machine
     * @param natNetwork  network to attach the machine to, null to leave it alone
     * @return true if the machine was successfully created, false otherwise
     */
    public boolean cloneMachineFromSeed(String seedName, String snapshot, String machineName, String natNetwork) {
        if (!machineExists(seedName)) {
            return false;
        }
        final IMachine seedMachine = vbox.findMachine(seedName);
        if (!snapshotExists(seedMachine, snapshot)) {
            return false;
        }

        final ISnapshot iSnapshot = seedMachine.findSnapshot(snapshot);

        final IMachine sourceMachine = iSnapshot.getMachine();

        //create a new empty machine container
        final IMachine newMachine = vbox.createMachine(
                seedMachine.getSettingsFilePath(),//wstring settingsFile
                machineName,//wstring name
                seedMachine.getPlatform().getArchitecture(),//PlatformArchitecture platform
                groups,//wstring groups
                seedMachine.getOSTypeId(),//wstring osTypeId
                "forceOverwrite=1",//wstring flags
                null,// The cipher. It should be empty if encryption is not required
                null,// The password id. It should be empty if encryption is not required.
                null// The password. It should be empty if encryption is not required.
        );

        try {
            final long firstAdapter = 0L;
            //prepare setting to clone this machine
            List<CloneOptions> options = new ArrayList<>();
            //we want it to be a clone to save space
            options.add(CloneOptions.Link);
            //start cloning
            IProgress progress = sourceMachine.cloneTo(newMachine, CloneMode.MachineState, options);
            wait(progress);
            //if the nat network was not provided, make this vm to use a NAT
            if (natNetwork == null) {
                logger.info(machineName + " will use a NAT");
                //make this machine a NAT one (isolated network)
                INetworkAdapter net = newMachine.getNetworkAdapter(firstAdapter);
                net.setAttachmentType(NetworkAttachmentType.NAT);
                net.setEnabled(true);
            } else {
                logger.info(machineName + " will use a NAT Network " + natNetwork);
                //set this machine to a NAT Network (this can be used for other VMs)
                //The total number of adapters per machine is defined by the
                //ISystemProperties::getMaxNetworkAdapters() property, so the maximum slot number is one less
                //than that property's value
                final ISystemProperties systemProperties = vbox.getSystemProperties();
                final IPlatformProperties platformProperties = systemProperties.getPlatform();
                final ChipsetType chipset = newMachine.getPlatform().getChipsetType();
                final long maxNetworkAdapters = platformProperties.getMaxNetworkAdapters(chipset) - 4;
                final String inetName = natNetwork.substring(natNetwork.indexOf("_") + 1);
                for (long adapter = firstAdapter; adapter < maxNetworkAdapters; adapter++) {
                    final INetworkAdapter networkAdapter = newMachine.getNetworkAdapter(adapter);
                    //the first adapter is for NAT Network
                    //required for port forwarding
                    final NetworkAttachmentType attachmentType;
                    if (adapter == firstAdapter) {
                        attachmentType = NetworkAttachmentType.NATNetwork;
                        networkAdapter.setAttachmentType(attachmentType);
                        networkAdapter.setNATNetwork(natNetwork);
                    } else {
                        attachmentType = NetworkAttachmentType.Internal;
                        networkAdapter.setAttachmentType(attachmentType);
                        networkAdapter.setInternalNetwork(inetName);
                    }
                    logger.info("Enabling adapter " + adapter + " as " + attachmentType.name() + " for " + machineName);
                    networkAdapter.setEnabled(true);
                }
            }
        } catch (Exception e) {
            logger.error("Fatal error " + e.getMessage(), e);
        } finally {
            //save changes and register
            newMachine.saveSettings();
            vbox.registerMachine(newMachine);
        }
        return true;
    }

    public boolean addSharedDirectory(String machineName, String dirName, String hostPath) {
        if (!machineExists(machineName)) {
            return false;
        }
        IMachine machine = vbox.findMachine(machineName);
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Write);
        try {
            //TODO fix ;) 190118 I know you can do it
            session.getMachine().createSharedFolder(dirName, hostPath, true, true, "shared"/*autoMountPoint*/);
            session.getMachine().saveSettings();
        } finally {
            waitToUnlock(session, machine);
        }
        return true;
    }

    /**
     * Startups a given machine on a desired mode
     *
     * @param machineName machine to start
     * @param mode        mode un which the database should be started
     * @return true if the startup was successful
     */
    public boolean launchMachine(String machineName, LaunchMode mode) {
        if (!machineExists(machineName)) {
            return false;
        }
        IMachine machine = vbox.findMachine(machineName);
        ISession session = boxManager.getSessionObject();
        try {
            IProgress progress = machine.launchVMProcess(session, mode.name(), null);
            wait(progress);
        } finally {
            session.unlockMachine();
        }
        return true;
    }

    /**
     * Adds a TCP port forwarding rule to a VirtualBox NAT Network, redirecting a port on the host
     * to a specified port on a guest machine within the internal NAT Network.
     *
     * <p>This method resolves the internal IPv4 address of the target guest machine, verifies that
     * the specified NAT Network exists, and adds a rule to forward traffic from the host to the guest.</p>
     *
     * @param networkName the name of the NAT Network where the rule will be applied
     * @param hostPort    the TCP port on the host that should be forwarded
     * @param guestPort   the TCP port on the guest machine to receive the forwarded traffic
     * @param machineName the name of the VirtualBox machine (guest) to which the traffic will be forwarded
     * @param ruleName    a unique identifier for the forwarding rule
     * @return {@code true} if the rule was successfully created; {@code false} if the machine IP could not be resolved
     * or the specified NAT Network does not exist
     */
    public boolean addNATNetworkPortForwardRule(String networkName, int hostPort, int guestPort, String machineName, String ruleName) {
        String ipv4 = getMachineIPv4(machineName);
        if (ipv4 == null) {
            return false;
        }
        if (!natNetworkExists(networkName)) {
            return false;
        }
        INATNetwork natNet = vbox.findNATNetworkByName(networkName);
        natNet.addPortForwardRule(
                /*isIpv6*/false,
                /*ruleName*/ruleName,
                /*proto*/NATProtocol.TCP,
                /*hostIP*/"0.0.0.0",
                /*hostPort*/hostPort,
                /*guestIP*/ipv4,
                /*guestPort*/guestPort
        );
        return true;
    }

    public boolean addNATPortForwardRule(int hostPort, int guestPort, String machineName, String ruleName) {
        String ipv4 = getMachineIPv4(machineName);
        if (ipv4 == null) {
            return false;
        }
        IMachine machine = vbox.findMachine(machineName);
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            IMachine xMachine = session.getMachine();
            INATEngine inatEngine = xMachine.getNetworkAdapter(0L).getNATEngine();
            inatEngine.addRedirect(
                    /*name*/ruleName,
                    /*proto*/NATProtocol.TCP,
                    /*hostIP*/"0.0.0.0",
                    /*hostPort*/hostPort,
                    /*guestIP*/ipv4,
                    /*guestPort*/guestPort
            );
        } finally {
            session.unlockMachine();
        }
        return true;
    }

    /**
     * Creates given number of virtual sharable hard disks and returns their location on the host
     *
     * @param machineName machine name whose hard disk location will be ued as reference
     * @param numDisks    number of disks to create, it should be between 1 and 28
     * @param size        size in GB, this size will be applied to all te disks
     * @return list of hard disks locations
     */
    public List<String> createSharedStorage(String machineName, int numDisks, int size) {
        if (!machineExists(machineName)) {
            return null;
        }
        //vm hard limit
        if (numDisks <= 0 || numDisks >= 28) {
            logger.error("Invalid number of disks specified [" + numDisks + "]");
            return null;
        }
        List<String> disks = new ArrayList<>();
        String format = "vdi";
         /*
            We will use VM_HOME directory to create the disks, this because we can't use
            a temp location since the partitions on the host could have leave it too small,
            that is why we will use the user defined storage for this operation
         */
        ///home/donhk/VirtualBoxVMs
        IMachine machine = vbox.findMachine(machineName);
        //        VM_HOME           | VM_FOLDER | settings
        // /home/donhk/VirtualBoxVMs/VMNAME/VMNAME.vbox
        String locationBase = new File(machine.getSettingsFilePath()).getParentFile().getParentFile().getAbsolutePath();

        for (int i = 0; i < numDisks; i++) {
            // /location/base/machine_name_#.vdi
            String disk = locationBase + File.separator + machineName + "_" + i + "." + format;
            disks.add(disk);
            logger.info("Media will be created on :" + disk);
            //https://www.virtualbox.org/sdkref/interface_i_system_properties.html#a3fddf22466361f98b6dc9fc4458d1049
            IMedium medium;
            try {
                medium = vbox.createMedium(
                        /*format*/format,
                        /*location*/disk,
                        /*accessMode*/AccessMode.ReadWrite,
                        /*aDeviceTypeType*/DeviceType.HardDisk
                );
            } catch (Exception e) {
                logger.error("Failed to create medium", e);
                return null;
            }
        /*
            The actual storage unit is not created by this method. In order to do it,
            and before you are able to attach the created medium to virtual machines, you must
            call one of the following methods to allocate a format-specific storage unit at the specified location:

            IMedium::createBaseStorage
            IMedium::createDiffStorage
         */
            IProgress progress = medium.createBaseStorage(
                    /*bytes*/1024L * 1_000_000L * size, //GB
                    List.of(MediumVariant.Fixed)
            );
            //wait to allocate
            wait(progress);
            try {
                medium = vbox.openMedium(
                        /*location*/disk,
                        /*DeviceType deviceType*/DeviceType.HardDisk,
                        /* AccessMode accessMode*/AccessMode.ReadWrite,
                        /* boolean forceNewUuid*/false
                );
                //Allow using this medium concurrently by several machines.
                medium.setType(MediumType.Shareable);
            } catch (Exception e) {
                logger.error("Error creating storage unit {}", disk, e);
                return null;
            }
        }
        return disks;
    }

    /**
     * Attaches one or more shared storage disks to a VirtualBox virtual machine using a SATA controller.
     *
     * <p>If the machine already has a SATA controller, the disks are added to the next available ports.
     * If no SATA controller exists, one is created and used. This method ensures the controller has enough
     * ports to support all provided disks.</p>
     *
     * <p>Each disk is opened in read-write mode and attached to the VM. If a new SATA controller is created,
     * it is named after the machine.</p>
     *
     * @param machineName        the name of the VirtualBox VM to which shared disks will be added
     * @param sharedStorageDisks a list of absolute paths to the shared storage disk files (usually `.vdi`, `.vhd`, etc.)
     * @return {@code true} if the disks were successfully attached; {@code false} if the machine does not exist
     */
    public boolean addSharedStorageToMachine(String machineName, List<String> sharedStorageDisks) {
        if (!machineExists(machineName)) {
            return false;
        }
        final IMachine machine = vbox.findMachine(machineName);
        final ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            final IMachine xMachine = session.getMachine();
            try {
                final IStorageController storageController;
                final boolean hasSATAControllerAlready = hasSATAControllerAlready(xMachine); // only one is allowed
                final String controllerName;
                int port; // index of ports of SATA connection
                if (hasSATAControllerAlready) {
                    controllerName = StorageBus.SATA.name();
                    storageController = xMachine.getStorageControllerByName(controllerName); //default name
                    port = countFreeSATAPorts(xMachine.getMediumAttachmentsOfController(controllerName)) + 1; // some ports might be already in use
                    logger.info("busy ports from [0-" + port + "] (exclusive) for " + machineName);
                } else {
                    controllerName = machineName; // use the machine name as controller name
                    storageController = xMachine.addStorageController(controllerName, StorageBus.SATA);
                    port = 0; // start from the port zero
                    logger.info("free ports from 0 for " + machineName);
                }
                //total ports needed  =  in_use + required
                final long portsNeeded = port + sharedStorageDisks.size();
                //enable another port on the vm
                logger.info("MaxPortCount .............. " + storageController.getMaxPortCount() + " for " + machineName);
                logger.info("MinPortCount .............. " + storageController.getMinPortCount() + " for " + machineName);
                logger.info("(before update) PortCount . " + storageController.getPortCount() + " for " + machineName);
                logger.info("updating port count to .... " + portsNeeded + " for " + machineName);
                storageController.setPortCount(portsNeeded);
                logger.info("(after update) PortCount .. " + storageController.getPortCount() + " for " + machineName);
                //for each disk, open it up and mount it
                for (String sharedStorageDisk : sharedStorageDisks) {
                    //open the media
                    final IMedium medium = vbox.openMedium(
                            /*location*/sharedStorageDisk,
                            /*DeviceType deviceType*/DeviceType.HardDisk,
                            /* AccessMode accessMode*/AccessMode.ReadWrite,
                            /* boolean forceNewUuid*/true
                    );
                    //attach it to the vm
                    xMachine.attachDevice(
                            /*wstring*/controllerName, //https://www.perkin.org.uk/posts/create-virtualbox-vm-from-the-command-line.html
                            /*controllerPort*/port++, //for a SATA controller, from 0 to 29;
                            /*device*/0, /* This is only relevant for IDE controllers, for which 0
                                                    specifies the master device and 1 specifies the slave device.
                                                    For all other controller types, this must be 0.  */
                            /* DeviceType type*/DeviceType.HardDisk,
                            /*IMedium medium*/medium
                    );
                }
            } finally {
                xMachine.saveSettings();
            }
        } finally {
            session.unlockMachine();
        }
        return true;
    }

    /**
     * Checks whether the specified virtual machine already has a SATA storage controller.
     *
     * <p>This method iterates over all storage controllers attached to the given {@link IMachine}
     * and returns {@code true} if any of them use the {@link StorageBus#SATA} bus type.</p>
     *
     * @param xMachine the virtual machine to inspect
     * @return {@code true} if a SATA controller is present; {@code false} otherwise
     */
    private boolean hasSATAControllerAlready(IMachine xMachine) {
        for (IStorageController controller : xMachine.getStorageControllers()) {
            if (controller.getBus() == StorageBus.SATA) {
                return true;
            }
        }
        return false;
    }

    private int countFreeSATAPorts(List<IMediumAttachment> attachments) {
        int port = 0;
        for (IMediumAttachment attachment : attachments) {
            logger.info("[" + attachment.getPort() + "] [" + attachment.getController() + "] " + attachment.getType().name());
            port = attachment.getPort();
        }
        return port;
    }

    public void rmNATPortForwardRule(String machineName, String ruleName) {
        if (!machineExists(machineName)) {
            return;
        }
        IMachine machine = vbox.findMachine(machineName);
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            IMachine xMachine = session.getMachine();
            INATEngine inatEngine = xMachine.getNetworkAdapter(0L).getNATEngine();
            for (String red : inatEngine.getRedirects()) {
                if (red.startsWith(ruleName)) {
                    inatEngine.removeRedirect(ruleName);
                }
            }
        } finally {
            session.unlockMachine();
        }
    }

    public List<String> getPortForwardRules(String networkName) {
        List<String> rules = new LinkedList<>();
        if (!natNetworkExists(networkName)) {
            return rules;
        }
        INATNetwork natNet = vbox.findNATNetworkByName(networkName);
        return natNet.getPortForwardRules4();
    }

    public void rmNATNetworkPortForwardRule(String networkName, String ruleName) {
        if (!natNetworkExists(networkName)) {
            return;
        }
        INATNetwork natNet = vbox.findNATNetworkByName(networkName);
        //make sure the rule exists before attempt to drop it
        for (String rule : natNet.getPortForwardRules4()) {
            if (rule.startsWith(ruleName)) {
                natNet.removePortForwardRule(false, ruleName);
            }
        }
    }

    public String getMachineIPv4(String machineName) {
        if (!machineExists(machineName)) {
            return null;
        }
        IMachine machine = vbox.findMachine(machineName);

        //scan the machine properties looking for its ip, once
        //we get it, we can assemble the command to add the new rule
        Holder<List<String>> keys = new Holder<>();
        Holder<List<String>> values = new Holder<>();
        Holder<List<Long>> timestamps = new Holder<>();
        Holder<List<String>> flags = new Holder<>();
        machine.enumerateGuestProperties(null, keys, values, timestamps, flags);
        String ipv4 = null;
        for (int i = 0; i < keys.value.size(); i++) {
            String key = keys.value.get(i);
            String val = values.value.get(i);
            if (key.contains("GuestInfo/Net/0/V4/IP")) {
                ipv4 = val;
                break;
            }
        }
        //if this property was not found, we can't continue
        return ipv4;
    }

    public void cleanUpVM(String machineName) {
        if (!machineExists(machineName)) {
            return;
        }
        IMachine machine = vbox.findMachine(machineName);
        MachineState state = machine.getState();
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            if (state.value() >= MachineState.FirstOnline.value() && state.value() <= MachineState.LastOnline.value()) {
                IProgress progress = session.getConsole().powerDown();
                wait(progress);
            }
        } finally {
            waitToUnlock(session, machine);
            logger.info("Deleting machine " + machineName);
            List<IMedium> media = machine.unregister(CleanupMode.DetachAllReturnHardDisksOnly);
            machine.deleteConfig(media);
        }
    }

    public IProgress getProgress() {
        return progress;
    }

    /**
     * +---------[powerDown()] <- Stuck <--[failure]-+
     * V                                             |
     * +-> PoweredOff --+-->[powerUp()]--> Starting --+      | +-----[resume()]-----+
     * |                |                             |      | V                    |
     * |   Aborted -----+                             +--> Running --[pause()]--> Paused
     * |                                              |      ^ |                   ^ |
     * |   Saved -----------[powerUp()]--> Restoring -+      | |                   | |
     * |     ^                                               | |                   | |
     * |     |     +-----------------------------------------+-|-------------------+ +
     * |     |     |                                           |                     |
     * |     |     +- OnlineSnapshotting <--[takeSnapshot()]<--+---------------------+
     * |     |                                                 |                     |
     * |     +-------- Saving <--------[saveState()]<----------+---------------------+
     * |                                                       |                     |
     * +-------------- Stopping -------[powerDown()]<----------+---------------------+
     *
     * @param machineName target machine
     */
    private void shutdownMachine(String machineName) {
        if (!machineExists(machineName)) {
            return;
        }
        IMachine machine = vbox.findMachine(machineName);
        MachineState state = machine.getState();
        ISession session = boxManager.getSessionObject();
        machine.lockMachine(session, LockType.Shared);
        try {
            if (state.value() >= MachineState.FirstOnline.value() && state.value() <= MachineState.LastOnline.value()) {
                IProgress progress = session.getConsole().powerDown();
                wait(progress);
            }
        } finally {
            waitToUnlock(session, machine);
        }
    }

    /**
     * Attempts to unlock the given VirtualBox machine session and waits until the session state becomes {@code Unlocked}.
     *
     * <p>This method first tries to unlock the session, ignoring any exceptions that might occur. Then, it polls the
     * session state every 200 milliseconds, waiting up to 120 seconds for the session to become fully unlocked.
     * If the timeout is reached, it forcefully terminates the virtual machine using  Utils.killVMHardWay.</p>
     *
     * @param session the {@link ISession} object associated with the locked machine
     * @param machine the {@link IMachine} whose session state should be monitored
     */
    private void waitToUnlock(ISession session, IMachine machine) {
        try {
            session.unlockMachine();
        } catch (Exception ignored) {
        }
        SessionState sessionState = machine.getSessionState();
        final long maxWaitTime = 120_000L; // 120 seconds
        final long lookup = 200; // 200 milliseconds
        long elapsed = 0;
        while (!SessionState.Unlocked.equals(sessionState)) {
            sessionState = machine.getSessionState();
            logger.info("Waiting for session unlock...[{}][{}]", sessionState.name(), machine.getName());
            try {
                TimeUnit.MILLISECONDS.sleep(lookup);
                elapsed += lookup;
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for session to be unlocked {} is {}", machine.getName(), machine.getSessionState().name());
            }
            if (elapsed > maxWaitTime) {
                logger.warn("max wait time for session unlock reached for " + machine.getName() + " " + machine.getSessionState().name());
                Utils.killVMHardWay(machine.getName(), logger);
            }
        }
    }

    /**
     * Waits for the given VirtualBox operation to complete.
     *
     * <p>This method blocks until the provided {@link IProgress} task finishes. If the result code
     * indicates a failure (non-zero), an error message is logged containing the detailed error information.</p>
     *
     * <p>Possible result codes include:</p>
     * <ul>
     *   <li><b>E_UNEXPECTED</b>: Virtual machine not registered.</li>
     *   <li><b>E_INVALIDARG</b>: Invalid session type.</li>
     *   <li><b>VBOX_E_OBJECT_NOT_FOUND</b>: No machine matching the specified ID was found.</li>
     *   <li><b>VBOX_E_INVALID_OBJECT_STATE</b>: Session is already open or in the process of opening.</li>
     *   <li><b>VBOX_E_IPRT_ERROR</b>: Failed to launch process for the machine.</li>
     *   <li><b>VBOX_E_VM_ERROR</b>: Failed to assign the machine to the session.</li>
     * </ul>
     *
     * @param progress the {@link IProgress} object representing the task to wait for
     */
    private void wait(IProgress progress) {
        //make this available for the caller
        this.progress = progress;
        progress.waitForCompletion(-1);
        if (progress.getResultCode() != 0) {
            logger.error("Operation failed: {}", progress.getErrorInfo().getText());
        }
    }

    /**
     * Checks whether a VirtualBox machine with the specified name exists.
     *
     * <p>This method avoids directly using {@code findMachine}, which throws a {@link VBoxException}
     * if the machine is not found. Instead, it iterates through all registered machines and compares
     * their names to determine existence.</p>
     *
     * @param machineName the name of the VirtualBox machine to look for
     * @return {@code true} if a machine with the given name exists; {@code false} if the name is null or no match is found
     * @throws IllegalArgumentException if an unexpected error occurs while retrieving the list of machines
     */
    public boolean machineExists(String machineName) {
        ///VBOX_E_OBJECT_NOT_FOUND
        //kind of "exists"
        if (machineName == null) {
            return false;
        }
        //since the method findMachine returns org.virtualbox_7_1.VBoxException

        //if the machine doesn't exist we will need to find it by
        //ourselves iterating over all the machines
        try {
            List<IMachine> machines = vbox.getMachines();
            for (IMachine machine : machines) {
                if (machine.getName().equals(machineName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Checks whether a snapshot with the specified name exists for the given VirtualBox machine.
     *
     * <p>This method uses {@link IMachine#findSnapshot(String)} to locate the snapshot by name.
     * If the snapshot is found, the method returns {@code true}; otherwise, it returns {@code false}.</p>
     *
     * <p>Note: If the snapshot does not exist, {@code findSnapshot} returns {@code null} instead of
     * throwing {@code VBoxException}.</p>
     *
     * @param machine  the {@link IMachine} to check for the snapshot
     * @param snapshot the name of the snapshot to look for
     * @return {@code true} if the snapshot exists; {@code false} otherwise
     */
    private boolean snapshotExists(IMachine machine, String snapshot) {
        ///VBOX_E_OBJECT_NOT_FOUND
        return machine.findSnapshot(snapshot) != null;
    }

    /**
     * Checks whether a NAT Network with the specified name exists in the VirtualBox environment.
     *
     * <p>This method iterates through all registered NAT Networks and compares their names
     * with the given {@code networkName}. If a match is found, it returns {@code true}.</p>
     *
     * @param networkName the name of the NAT Network to search for
     * @return {@code true} if a NAT Network with the specified name exists; {@code false} if the name is {@code null} or not found
     */
    private boolean natNetworkExists(String networkName) {
        if (networkName == null) {
            return false;
        }
        for (INATNetwork network : vbox.getNATNetworks()) {
            if (network.getNetworkName().equals(networkName)) {
                return true;
            }
        }
        return false;
    }

    public boolean createNatNetwork(String networkName) {
        /*
            vboxmanage list dhcpservers
            vboxmanage list natnets
         */
        //ensure this network doesn't exist
        if (natNetworkExists(networkName)) {
            return false;
        }
        logger.info("Creating nat network " + networkName);
        INATNetwork natNetwork = vbox.createNATNetwork(networkName);
        natNetwork.setIPv6Enabled(false);
        /*
         http://www.calculator.net/ip-subnet-calculator.html?cclass=c&csubnet=28&cip=10.0.6.0&ctype=ipv4&printit=0&x=85&y=22

         IPv4 addresses of gateway (low address + 1) and DHCP server (= low address + 2)

                     IP Address: 10.0.6.0
                Network Address: 10.0.6.0
           Usable Host IP Range: 10.0.6.1 - 10.0.6.14
              Broadcast Address: 10.0.6.15
          Total Number of Hosts: 16
         Number of Usable Hosts: 14
                    Subnet Mask: 255.255.255.240
                  Wildcard Mask: 0.0.0.15
                       IP Class: C
        */
        natNetwork.setNetwork("10.0.6.0/28");
        natNetwork.setNeedDhcpServer(true);
        logger.info("Creating dhcp " + networkName);
        IDHCPServer dhcp = vbox.createDHCPServer(networkName);
        dhcp.setConfiguration(
                /*IPAddress*/ "10.0.6.3",
                /*networkMask*/"255.255.255.240",
                /*FromIPAddress*/"10.0.6.4",
                /*ToIPAddress*/"10.0.6.14"
        );
        //start the dhcp server
        dhcp.start(
                /*trunkName*/null,
                /*trunkType*/null
        );
        dhcp.setEnabled(true);
        return true;
    }

    public boolean removeNatNetwork(String networkName) {
        //ensure this network exists
        if (!natNetworkExists(networkName)) {
            return false;
        }
        try {
            logger.info("looking dhcp for " + networkName);
            IDHCPServer dhcp = null;
            for (IDHCPServer server : vbox.getDHCPServers()) {
                if (server.getNetworkName().equals(networkName)) {
                    dhcp = server;
                    break;
                }
            }
            if (dhcp != null) {
                dhcp.stop();
                dhcp.setEnabled(false);
                logger.info("Removing dhcp " + networkName);
                vbox.removeDHCPServer(dhcp);
            }
            logger.info("looking natNet for " + networkName);
            INATNetwork natNet = null;
            for (INATNetwork network : vbox.getNATNetworks()) {
                if (network.getNetworkName().equals(networkName)) {
                    natNet = network;
                }
            }
            if (natNet != null) {
                natNet.stop();
                natNet.setEnabled(false);
                logger.info("Removing nat network " + networkName);
                vbox.removeNATNetwork(natNet);
            }
        } catch (Exception e) {
            logger.error("failed to remove nat network {}", networkName, e);
        }
        return true;
    }

    public List<INATNetwork> getNatNetworks() {
        return vbox.getNATNetworks();
    }
}

package dev.donhk.actor;

import org.apache.pekko.actor.AbstractActor;
import dev.donhk.actor.impl.*;
import dev.donhk.helpers.Constants;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;
import org.virtualbox_7_2.VirtualBoxManager;

public class VBoxActor extends AbstractActor {

    private static final String SOAP_ENDPOINT = "http://127.0.0.1:18083";
    /// Unique identifier
    public static String id = "VBoxActor";
    ///  VirtualBoxManager instance that depends on the native bindings
    private VirtualBoxManager virtualBoxManager;
    /// VBoxManager adaptor to interact with VirtualBoxManager
    private VBoxManager boxManager;

    @Override
    public void preStart() {
        this.virtualBoxManager = VirtualBoxManager.createInstance(null);
        if (Constants.isWindows) {
            virtualBoxManager.connect(SOAP_ENDPOINT, null, null);
        }
        this.boxManager = new VBoxManager(this.virtualBoxManager);
        Logger.info("API version {}", virtualBoxManager.getVBox().getAPIVersion());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(VBoxMessage.PingRequest.class, msg -> {
                    getSender().tell(new VBoxMessage.PingResponse(msg.name(), msg.age()), getSelf());
                })
                .match(VBoxMessage.PingRequest2.class, msg -> {
                    getSender().tell(new VBoxMessage.PingResponse2(msg.name(), msg.age()), getSelf());
                })
                .match(VBoxMessage.ListMachinesRequest.class, request ->
                        getSender().tell(new ListMachines(this.boxManager).dispatch(), getSelf()))
                .match(VBoxMessage.DelDanglingNetsRequest.class, request ->
                        getSender().tell(new DelDanglingNets(this.boxManager, request.activeMachineRows()).dispatch(), getSelf()))
                .match(VBoxMessage.GetVBoxVersionRequest.class, request ->
                        getSender().tell(new GetVBoxVersion(this.boxManager).dispatch(), getSelf()))
                .match(VBoxMessage.CloneMachineRequest.class, request ->
                        getSender().tell(new CloneMachine(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.LaunchMachineRequest.class, request ->
                        getSender().tell(new LaunchMachine(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.AddSharedDirectoryRequest.class, request ->
                        getSender().tell(new AddSharedDirectory(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.CreateSharedStorageRequest.class, request ->
                        getSender().tell(new CreateSharedStorage(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.AddSharedStorageToMachineRequest.class, request ->
                        getSender().tell(new AddSharedStorageToMachine(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.AddNATNetworkPortForwardRuleRequest.class, request ->
                        getSender().tell(new AddNATNetworkPortForwardRule(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.AddNATPortForwardRuleRequest.class, request ->
                        getSender().tell(new AddNATPortForwardRule(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.RmNATPortForwardRuleRequest.class, request ->
                        getSender().tell(new RmNATPortForwardRule(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.RmNATNetworkPortForwardRuleRequest.class, request ->
                        getSender().tell(new RmNATNetworkPortForwardRule(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.GetPortForwardRulesRequest.class, request ->
                        getSender().tell(new GetPortForwardRules(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.GetMachineIPv4Request.class, request ->
                        getSender().tell(new GetMachineIPv4(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.CleanUpVMRequest.class, request ->
                        getSender().tell(new CleanUpVM(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.MachineExistsRequest.class, request ->
                        getSender().tell(new MachineExists(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.CreateNatNetworkRequest.class, request ->
                        getSender().tell(new CreateNatNetwork(this.boxManager, request).dispatch(), getSelf()))
                .match(VBoxMessage.RemoveNatNetworkRequest.class, request ->
                        getSender().tell(new RemoveNatNetwork(this.boxManager, request).dispatch(), getSelf()))
                .build();
    }

    @Override
    public void postStop() {
        Logger.info("Bye Bye API version {}", virtualBoxManager.getVBox().getAPIVersion());
    }
}

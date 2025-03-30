package dev.donhk.actor;

import akka.actor.AbstractActor;
import dev.donhk.actor.impl.DelDanglingNets;
import dev.donhk.actor.impl.ListMachines;
import dev.donhk.helpers.Constants;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;
import org.virtualbox_7_1.VirtualBoxManager;

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
                    System.out.println("Received1: " + msg.name() + " " + msg.age());
                    getSender().tell(new VBoxMessage.PingResponse("b", 1), getSelf());
                })
                .match(VBoxMessage.PingRequest2.class, msg -> {
                    System.out.println("Received2: " + msg.name() + " " + msg.age());
                    getSender().tell(new VBoxMessage.PingResponse2("a", 1), getSelf());
                })
                .match(VBoxMessage.ListMachinesRequest.class, request -> getSender().tell(new ListMachines(this.boxManager).dispatch(), getSelf()))
                .match(VBoxMessage.DelDanglingNetsRequest.class, request -> getSender().tell(new DelDanglingNets(this.boxManager, request.activeMachineRows()).dispatch(), getSelf()))
                .build();
    }

    @Override
    public void postStop() {
        Logger.info("Bye Bye API version {}", virtualBoxManager.getVBox().getAPIVersion());
    }
}

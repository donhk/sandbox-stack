package dev.donhk.actor;

import akka.actor.AbstractActor;
import dev.donhk.helpers.Constants;
import dev.donhk.pojos.MachineMeta;
import dev.donhk.vbox.MetaExtractor;
import dev.donhk.vbox.VBoxManager;
import org.tinylog.Logger;
import org.virtualbox_7_1.VirtualBoxManager;

import java.util.List;

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
                .match(VBoxMessage.ListMachinesRequest.class, request -> {
                    Logger.info("ListMachinesRequest");
                    final MetaExtractor metaExtractor = new MetaExtractor(this.boxManager);
                    final List<MachineMeta> machines = metaExtractor.genMetaInfo();
                    final VBoxMessage.ListMachinesResponse response = new VBoxMessage.ListMachinesResponse(machines);
                    getSender().tell(response, getSelf());
                })
                .build();
    }

    @Override
    public void postStop() {
        Logger.info("Bye Bye API version {}", virtualBoxManager.getVBox().getAPIVersion());
    }
}

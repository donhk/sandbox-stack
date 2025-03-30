package dev.donhk.actor;

import akka.actor.AbstractActor;
import dev.donhk.helpers.Constants;
import org.virtualbox_7_1.VirtualBoxManager;


public class VBoxActor extends AbstractActor {

    public static String id = "VBoxActor";
    private VirtualBoxManager boxManager;

    @Override
    public void preStart() {
        boxManager = VirtualBoxManager.createInstance(null);
        if (Constants.isWindows) {
            boxManager.connect("http://127.0.0.1:18083", null, null);
        }
        System.out.println("API version " + boxManager.getVBox().getAPIVersion());
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
                .build();
    }

    @Override
    public void postStop() {
        System.out.println("Bye Bye API version " + boxManager.getVBox().getAPIVersion());
    }
}

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import dev.donhk.actor.VBoxActor;
import dev.donhk.actor.VBoxMessage;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TestActor {

    @Test
    public void actor_should_respond() {
        // Create actor system
        ActorSystem system = ActorSystem.create("vBoxActor");

        // Create VBoxActor
        ActorRef vboxActor = system.actorOf(Props.create(VBoxActor.class), "vBoxActor");

        // Prepare message and timeout
        VBoxMessage.PingRequest request = new VBoxMessage.PingRequest("Alice", 30);
        Duration timeout = Duration.ofSeconds(1);

        // Send typed message and await response
        CompletionStage<VBoxMessage.PingResponse> typedFuture =
                Patterns.ask(vboxActor, request, timeout)
                        .thenApply(response -> (VBoxMessage.PingResponse) response);

        typedFuture.thenAccept(pr -> System.out.println("Got response: " + pr.name() + ", " + pr.age())).toCompletableFuture().join();
    }


    @Test
    public void actor_test() throws InterruptedException {
        // Create an ActorSystem
        ActorSystem system = ActorSystem.create("vBoxActor");

        // Create an ActorRef for vBoxActor
        ActorRef vboxActor = system.actorOf(Props.create(VBoxActor.class), "vBoxActor");

        // Send an order message
        vboxActor.tell(new VBoxMessage.PingRequest("ping", 20), ActorRef.noSender());
        vboxActor.tell(new VBoxMessage.PingRequest2("ping2", 20), ActorRef.noSender());

        TimeUnit.MILLISECONDS.sleep(100);

        // Shutdown the system
        system.terminate();

        system.getWhenTerminated().toCompletableFuture().join();
    }
}

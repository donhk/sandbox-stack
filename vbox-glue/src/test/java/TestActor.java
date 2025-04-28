import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.donhk.actor.VBoxActor;
import dev.donhk.actor.VBoxMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class TestActor {

    public static ActorSystem system;
    public static ActorRef vboxActor;

    @BeforeAll
    public static void setup() {
        Config config = ConfigFactory.parseString("""
                    akka.log-dead-letters = off
                    akka.log-dead-letters-during-shutdown = off
                """);
        system = ActorSystem.create("vBoxActor", config);
        vboxActor = system.actorOf(Props.create(VBoxActor.class), "vBoxActor");
    }

    @Test
    public void actor_should_respond() {
        // Prepare message and timeout
        VBoxMessage.PingRequest request = new VBoxMessage.PingRequest("Alice", 30);
        Duration timeout = Duration.ofSeconds(1);

        // Send typed message and await response
        CompletionStage<VBoxMessage.PingResponse> typedFuture = Patterns.ask(vboxActor, request, timeout).thenApply(response -> (VBoxMessage.PingResponse) response);

        typedFuture.thenAccept(pr -> {
            Assertions.assertEquals(30, pr.age());
            Assertions.assertEquals("Alice", pr.name());
        }).toCompletableFuture().join();
    }


    @Test
    public void actor_test() throws InterruptedException {
        // Send an order message
        vboxActor.tell(new VBoxMessage.PingRequest("ping", 20), ActorRef.noSender());
        vboxActor.tell(new VBoxMessage.PingRequest2("ping2", 20), ActorRef.noSender());

        TimeUnit.MILLISECONDS.sleep(100);
    }

    @AfterAll
    public static void down() {
        // Shutdown the system
        system.terminate();
        system.getWhenTerminated().toCompletableFuture().join();
    }
}

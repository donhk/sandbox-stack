import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.donhk.actor.VBoxActor;
import dev.donhk.actor.VBoxMessage;
import dev.donhk.vbox.LaunchMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
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

    @Test
    public void getVBoxVersion_should_respond() {
        Duration timeout = Duration.ofSeconds(5);
        CompletionStage<VBoxMessage.GetVBoxVersionResponse> future = Patterns.ask(vboxActor, new VBoxMessage.GetVBoxVersionRequest(), timeout)
                .thenApply(response -> (VBoxMessage.GetVBoxVersionResponse) response);
        future.thenAccept(r -> Assertions.assertNotNull(r.version()))
                .toCompletableFuture().join();
    }

    @Test
    public void machineExists_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        CompletionStage<VBoxMessage.MachineExistsResponse> future = Patterns.ask(vboxActor, new VBoxMessage.MachineExistsRequest("__no_such_vm__"), timeout)
                .thenApply(response -> (VBoxMessage.MachineExistsResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.exists()))
                .toCompletableFuture().join();
    }

    @Test
    public void getMachineIPv4_should_return_null_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        CompletionStage<VBoxMessage.GetMachineIPv4Response> future = Patterns.ask(vboxActor, new VBoxMessage.GetMachineIPv4Request("__no_such_vm__"), timeout)
                .thenApply(response -> (VBoxMessage.GetMachineIPv4Response) response);
        future.thenAccept(r -> Assertions.assertNull(r.ipv4()))
                .toCompletableFuture().join();
    }

    @Test
    public void cloneMachine_should_return_false_for_unknown_seed() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.CloneMachineRequest request = new VBoxMessage.CloneMachineRequest("__no_seed__", "snap", "__new_vm__", null);
        CompletionStage<VBoxMessage.CloneMachineResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.CloneMachineResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void launchMachine_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.LaunchMachineRequest request = new VBoxMessage.LaunchMachineRequest("__no_such_vm__", LaunchMode.headless);
        CompletionStage<VBoxMessage.LaunchMachineResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.LaunchMachineResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void addSharedDirectory_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.AddSharedDirectoryRequest request = new VBoxMessage.AddSharedDirectoryRequest("__no_such_vm__", "shared", "/tmp");
        CompletionStage<VBoxMessage.AddSharedDirectoryResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.AddSharedDirectoryResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void createSharedStorage_should_return_null_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.CreateSharedStorageRequest request = new VBoxMessage.CreateSharedStorageRequest("__no_such_vm__", 1, 1);
        CompletionStage<VBoxMessage.CreateSharedStorageResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.CreateSharedStorageResponse) response);
        future.thenAccept(r -> Assertions.assertNull(r.diskPaths()))
                .toCompletableFuture().join();
    }

    @Test
    public void addSharedStorageToMachine_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.AddSharedStorageToMachineRequest request = new VBoxMessage.AddSharedStorageToMachineRequest("__no_such_vm__", List.of());
        CompletionStage<VBoxMessage.AddSharedStorageToMachineResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.AddSharedStorageToMachineResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void addNATNetworkPortForwardRule_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.AddNATNetworkPortForwardRuleRequest request = new VBoxMessage.AddNATNetworkPortForwardRuleRequest("__no_net__", 8080, 80, "__no_such_vm__", "rule1");
        CompletionStage<VBoxMessage.AddNATNetworkPortForwardRuleResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.AddNATNetworkPortForwardRuleResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void addNATPortForwardRule_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        VBoxMessage.AddNATPortForwardRuleRequest request = new VBoxMessage.AddNATPortForwardRuleRequest(8080, 80, "__no_such_vm__", "rule1");
        CompletionStage<VBoxMessage.AddNATPortForwardRuleResponse> future = Patterns.ask(vboxActor, request, timeout)
                .thenApply(response -> (VBoxMessage.AddNATPortForwardRuleResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void getPortForwardRules_should_return_empty_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        CompletionStage<VBoxMessage.GetPortForwardRulesResponse> future = Patterns.ask(vboxActor, new VBoxMessage.GetPortForwardRulesRequest("__no_net__"), timeout)
                .thenApply(response -> (VBoxMessage.GetPortForwardRulesResponse) response);
        future.thenAccept(r -> Assertions.assertTrue(r.rules().isEmpty()))
                .toCompletableFuture().join();
    }

    @Test
    public void removeNatNetwork_should_return_false_for_unknown() {
        Duration timeout = Duration.ofSeconds(5);
        CompletionStage<VBoxMessage.RemoveNatNetworkResponse> future = Patterns.ask(vboxActor, new VBoxMessage.RemoveNatNetworkRequest("__no_net__"), timeout)
                .thenApply(response -> (VBoxMessage.RemoveNatNetworkResponse) response);
        future.thenAccept(r -> Assertions.assertFalse(r.success()))
                .toCompletableFuture().join();
    }

    @Test
    public void createAndRemoveNatNetwork_round_trip() {
        String testNet = "sbx_unit_test_tmp";
        Duration timeout = Duration.ofSeconds(10);

        // ensure clean state
        Patterns.ask(vboxActor, new VBoxMessage.RemoveNatNetworkRequest(testNet), timeout)
                .toCompletableFuture().join();

        VBoxMessage.CreateNatNetworkResponse createResp = Patterns.ask(vboxActor, new VBoxMessage.CreateNatNetworkRequest(testNet), timeout)
                .thenApply(r -> (VBoxMessage.CreateNatNetworkResponse) r)
                .toCompletableFuture().join();
        Assertions.assertTrue(createResp.success());

        VBoxMessage.RemoveNatNetworkResponse removeResp = Patterns.ask(vboxActor, new VBoxMessage.RemoveNatNetworkRequest(testNet), timeout)
                .thenApply(r -> (VBoxMessage.RemoveNatNetworkResponse) r)
                .toCompletableFuture().join();
        Assertions.assertTrue(removeResp.success());
    }

    @Test
    public void actor_test_void_commands() throws InterruptedException {
        vboxActor.tell(new VBoxMessage.CleanUpVMRequest("__no_such_vm__"), ActorRef.noSender());
        vboxActor.tell(new VBoxMessage.RmNATPortForwardRuleRequest("__no_such_vm__", "rule"), ActorRef.noSender());
        vboxActor.tell(new VBoxMessage.RmNATNetworkPortForwardRuleRequest("__no_net__", "rule"), ActorRef.noSender());

        TimeUnit.MILLISECONDS.sleep(100);
    }

    @AfterAll
    public static void down() {
        // Shutdown the system
        system.terminate();
        system.getWhenTerminated().toCompletableFuture().join();
    }
}

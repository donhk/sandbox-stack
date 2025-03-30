package dev.donhk.actors;

import akka.actor.ActorRef;
import akka.pattern.Patterns;

import java.time.Duration;

public class Utilities {
    public static <T> T askSync(ActorRef actor, Object msg, Duration timeout) {
        return (T) Patterns.ask(actor, msg, timeout).toCompletableFuture().join();
    }

    public static <T> T askSync(ActorRef actor, Object msg) {
        return askSync(actor, msg, Duration.ofSeconds(120));
    }
}

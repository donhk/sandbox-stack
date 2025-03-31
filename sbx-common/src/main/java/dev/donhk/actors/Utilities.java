package dev.donhk.actors;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import org.tinylog.Logger;

import java.time.Duration;
import java.time.Instant;

public class Utilities {

    @SuppressWarnings("unchecked")
    public static <T> T askSync(ActorRef actor, Object msg, Duration timeout) {
        return (T) Patterns.ask(actor, msg, timeout).toCompletableFuture().join();
    }

    public static <T> T askSync(ActorRef actor, Object msg) {
        final Instant start = Instant.now();
        String msgType = msg.getClass().getSimpleName();
        try {
            return askSync(actor, msg, Duration.ofMinutes(32));
        } finally {
            long elapsedMillis = Duration.between(start, Instant.now()).toMillis();
            Logger.info("askSync [{}] elapsed {}ms", msgType, elapsedMillis);
        }
    }
}

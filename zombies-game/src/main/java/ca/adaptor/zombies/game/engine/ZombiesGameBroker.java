package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_WS;
import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.VAR_WS_BROKER_ID;

@NoArgsConstructor
public class ZombiesGameBroker implements ZombiesGameBrokerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameBroker.class);
    private static final int TIMEOUT_S = 30;

    @Getter
    private final UUID brokerId = UUID.randomUUID();
    @Getter
    private UUID playerId;

    //----- ZombiesGameBrokerInterface -----//

    public void initialize(@NotNull UUID playerId) throws IllegalStateException {
        this.playerId = playerId;
    }

    public void requestRoll() {
//        var outMessage = new RequestRollMessage();
//        var latch = new CountDownLatch(1);
//        MessageHandler.Whole<RequestRollMessage> handler = message -> {
//            if(Objects.equals(outMessage.getRequestId(), message.getRequestId())) {
//                LOGGER.trace("Received reply from player (" + playerId + "): " + message);
//                latch.countDown();
//            }
//        };
//        session.addMessageHandler(RequestRollMessage.class, handler);
//        try {
//            LOGGER.trace("Requesting roll from player (" + playerId + ")");
//            session.getAsyncRemote().sendObject(outMessage);
//            LOGGER.trace("Waiting for player (" + playerId + ")");
//            // TODO: Should note that this timed out and do something with that knowledge
//            latch.await(TIMEOUT_S, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) { /* NOOP */ }
//        finally { session.removeMessageHandler(handler); }
    }
    public boolean requestUseBullets() {
//        var ret = new AtomicBoolean(false);
//
//        var outMessage = new RequestUseBulletsMessage();
//        var latch = new CountDownLatch(1);
//        MessageHandler.Whole<RequestUseBulletsMessage> handler = message -> {
//            if(Objects.equals(outMessage.getRequestId(), message.getRequestId())) {
//                LOGGER.trace("Received reply from player (" + playerId + "): " + message);
//                ret.set(message.isUsingBullets());
//                latch.countDown();
//            }
//        };
//        session.addMessageHandler(RequestUseBulletsMessage.class, handler);
//        try {
//            LOGGER.trace("Requesting bullets use from player ("+ playerId +")");
//            session.getAsyncRemote().sendObject(outMessage);
//            LOGGER.trace("Waiting for player (" + playerId + ")");
//            // TODO: Should note that this timed out and do something with that knowledge
//            latch.await(TIMEOUT_S, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) { /* NOOP */ }
//        finally { session.removeMessageHandler(handler); }
//
//        return ret.get();
        throw new RuntimeException();
    }
    @Nullable
    public ZombiesDirection requestPlayerMovement() {
//        var ret = new AtomicReference<ZombiesDirection>();
//
//        var outMessage = new RequestPlayerMovementMessage();
//        var latch = new CountDownLatch(1);
//        MessageHandler.Whole<RequestPlayerMovementMessage> handler = message -> {
//            if(Objects.equals(outMessage.getRequestId(), message.getRequestId())) {
//                LOGGER.trace("Received reply from player (" + playerId + "): " + message);
//                ret.set(message.getDirection());
//                latch.countDown();
//            }
//        };
//        session.addMessageHandler(RequestPlayerMovementMessage.class, handler);
//        try {
//            LOGGER.trace("Requesting movement from player ("+ playerId +")");
//            session.getAsyncRemote().sendObject(outMessage);
//            LOGGER.trace("Waiting for player (" + playerId + ")");
//            // TODO: Should note that this timed out and do something with that knowledge
//            latch.await(TIMEOUT_S, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) { /* NOOP */ }
//        finally { session.removeMessageHandler(handler); }
//
//        return ret.get();
        throw new RuntimeException();
    }
    @NotNull
    public Set<UUID> requestPlayerEventCardDiscards() {
//        var ret = new AtomicReference<Set<UUID>>();
//
//        var outMessage = new RequestDiscardsMessage();
//        var latch = new CountDownLatch(1);
//        MessageHandler.Whole<RequestDiscardsMessage> handler = message -> {
//            if(Objects.equals(outMessage.getRequestId(), message.getRequestId())) {
//                LOGGER.trace("Received reply from player (" + playerId + "): " + message);
//                ret.set(message.getCardIds());
//                latch.countDown();
//            }
//        };
//        session.addMessageHandler(RequestDiscardsMessage.class, handler);
//        try {
//            LOGGER.trace("Requesting discards from player ("+ playerId +")");
//            session.getAsyncRemote().sendObject(outMessage);
//            LOGGER.trace("Waiting for player (" + playerId + ")");
//            // TODO: Should note that this timed out and do something with that knowledge
//            latch.await(TIMEOUT_S, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e) { /* NOOP */ }
//        finally { session.removeMessageHandler(handler); }
//
//        return ret.get();
        throw new RuntimeException();
    }

    public void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update) {
//        LOGGER.trace("Sending update to player ("+ playerId +"): " + update);
//        session.getAsyncRemote().sendObject(update);
    }
}

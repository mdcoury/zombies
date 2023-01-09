package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestDiscards;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestMovement;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestRoll;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestUseBullets;
import ca.adaptor.zombies.game.messages.ZombiesGameUpdateMessage;
import ca.adaptor.zombies.game.model.ZombiesDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@NoArgsConstructor
public class ZombiesGameBroker implements IZombiesGameBroker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameBroker.class);

    @Getter
    private final UUID brokerId = UUID.randomUUID();
    @Getter
    private IMessageHandler messageHandler = null;
    // TODO: This is a bit clunky...
    private final Lock callbackLock = new ReentrantLock();
    private Runnable callbackFn = null;

//region ZombiesGameBrokerInterface

    @Override
    public void testForHandler(@Nullable Runnable callbackFn) {
        callbackLock.lock();
        try {
            if (messageHandler == null) {
                assert this.callbackFn == null;
                this.callbackFn = callbackFn;
            }
        }
        finally {
            callbackLock.unlock();
        }
    }

    @Override
    public void setMessageHandler(@NotNull IMessageHandler messageHandler) {
        if(this.messageHandler != null) {
            throw new IllegalStateException();
        }

        callbackLock.lock();
        try {
            this.messageHandler = messageHandler;
            if (callbackFn != null) {
                callbackFn.run();
            }
        }
        finally {
            callbackLock.unlock();
        }
    }

    @Override
    public void requestRoll() {
        LOGGER.trace("Requesting roll from broker (" + brokerId+ ")");
        messageHandler.send(new RequestRoll(), brokerId);
    }
    @Override
    public boolean requestUseBullets() {
        LOGGER.trace("Requesting bullets use from broker (" + brokerId+")");
        var response = messageHandler.send(new RequestUseBullets(), brokerId);
        return response.isUsingBullets();
    }
    @Override
    @Nullable
    public ZombiesDirection requestPlayerMovement() {
        LOGGER.trace("Requesting movement from broker (" + brokerId+")");
        var response = messageHandler.send(new RequestMovement(), brokerId);
        return response.getDirection();
    }
    @Override
    @NotNull
    public Set<UUID> requestPlayerEventCardDiscards() {
        LOGGER.trace("Requesting discards from broker (" + brokerId+")");
        var response = messageHandler.send(new RequestDiscards(), brokerId);
        return response.getCardIds();
    }
    @Override
    public void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update) {
        LOGGER.trace("Sending update to broker (" + brokerId+"): " + update);
        messageHandler.send(update, brokerId);
    }

//endregion
}

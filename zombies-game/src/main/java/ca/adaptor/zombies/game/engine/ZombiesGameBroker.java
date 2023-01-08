package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestDiscardsMessage;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestPlayerMovementMessage;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestRollMessage;
import ca.adaptor.zombies.game.messages.AbstractZombiesWsMessage.RequestUseBulletsMessage;
import ca.adaptor.zombies.game.model.ZombiesDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
public class ZombiesGameBroker implements IZombiesGameBroker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameBroker.class);

    @Getter
    private final UUID brokerId = UUID.randomUUID();
    @Getter @Setter
    private UUID playerId;
    @Getter @Setter
    private IMessageHandler messageHandler;

    //----- ZombiesGameBrokerInterface -----//

    public void requestRoll() {
        LOGGER.trace("Requesting roll from player (" + playerId + ")");
        messageHandler.send(new RequestRollMessage(), brokerId);
    }
    public boolean requestUseBullets() {
        LOGGER.trace("Requesting bullets use from player ("+ playerId +")");
        var response = messageHandler.send(new RequestUseBulletsMessage(), brokerId);
        return response.isUsingBullets();
    }
    @Nullable
    public ZombiesDirection requestPlayerMovement() {
        LOGGER.trace("Requesting movement from player ("+ playerId +")");
        var response = messageHandler.send(new RequestPlayerMovementMessage(), brokerId);
        return response.getDirection();
    }
    @NotNull
    public Set<UUID> requestPlayerEventCardDiscards() {
        LOGGER.trace("Requesting discards from player ("+ playerId +")");
        var response = messageHandler.send(new RequestDiscardsMessage(), brokerId);
        return response.getCardIds();
    }

    public void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update) {
        LOGGER.trace("Sending update to player ("+ playerId +"): " + update);
        messageHandler.send(update, brokerId);
    }
}

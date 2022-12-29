package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class ZombiesGameBroker implements ZombiesGameBrokerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameBroker.class);

    @Getter
    private final UUID playerId;

    public void requestRoll() {
        LOGGER.trace("Requesting roll from player ("+ playerId +")");
        throw new RuntimeException("Not implemented yet");
    }
    public boolean requestUseBullets() {
        LOGGER.trace("Requesting bullets use from player ("+ playerId +")");
        throw new RuntimeException("Not implemented yet");
    }
    @Nullable
    public ZombiesDirection requestPlayerMovement() {
        LOGGER.trace("Requesting movement from player ("+ playerId +")");
        throw new RuntimeException("Not implemented yet");
    }
    @NotNull
    public Set<UUID> requestPlayerEventCardDiscards() {
        LOGGER.trace("Requesting discards from player ("+ playerId +")");
        throw new RuntimeException("Not implemented yet");
    }

    public void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update) {
        LOGGER.trace("Sending update to player ("+ playerId +"): " + update);
        throw new RuntimeException("Not implemented yet");
    }
}

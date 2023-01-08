package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import ca.adaptor.zombies.game.model.ZombiesPlayer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Set;
import java.util.UUID;

public interface ZombiesGameBrokerInterface {
    UUID getBrokerId();
    /**
     * @param playerId the {@link UUID} of the {@link ZombiesPlayer} for this
     *                 broker.
     * @throws IllegalStateException if this method is called multiple times
     */
    void initialize(@NotNull UUID playerId) throws IllegalStateException;
    /** BLocking call to the player/client for them to "roll" the dice */
    void requestRoll();
    /**
     * Blocking call to the player/client on whether to use bullets for their
     * combat roll
     */
    boolean requestUseBullets();
    /**
     * Blocking call to the player/client for a single move
     * @return the {@link ZombiesDirection} to move one space, or <tt>null</tt>
     *          iff the player is ending their movement early
     */
    @Nullable ZombiesDirection requestPlayerMovement();
    /**
     * Blocking call to the player/client for which, if any, cards to discard
     * from their hand
     * @return the {@link UUID}s of the cards to be discarded, or an empty
     *          {@link Set} if the user does not want to discard any cards.
     */
    @NotNull Set<UUID> requestPlayerEventCardDiscards();
    /**
     * Non-blocking call to send an update to the player/client
     * @param update the {@link ZombiesGameUpdateMessage} to send
     */
    void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update);
}

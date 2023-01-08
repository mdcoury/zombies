package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.messages.IZombiesWsMessage;
import ca.adaptor.zombies.game.model.ZombiesDirection;
import ca.adaptor.zombies.game.model.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface IZombiesGameBroker {
    interface IMessageHandler {
        // TODO: Seems a bit clunky having the broker id in here...
        @NotNull <M extends IZombiesWsMessage> M send(@NotNull M message, @NotNull UUID brokerId);
        void send(@NotNull ZombiesGameUpdateMessage update, @NotNull UUID brokerId);
    }

    @NotNull UUID getBrokerId();

    void setMessageHandler(@NotNull IZombiesGameBroker.IMessageHandler messageHandler);
    @Nullable IZombiesGameBroker.IMessageHandler getMessageHandler();
    /** @param playerId the {@link UUID} of the {@link ZombiesPlayer} for this broker. */
    void setPlayerId(@NotNull UUID playerId);
    @Nullable UUID getPlayerId();
    /** Blocking call to the player/client for them to "roll" the dice */
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

package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface ZombiesGameBrokerInterface {
    void requestRoll();
    boolean requestUseBullets();
    @Nullable
    ZombiesDirection requestPlayerMovement();
    @NotNull
    Set<UUID> requestPlayerEventCardDiscards();

    void sendGameUpdate(@NotNull ZombiesGameUpdateMessage update);
}

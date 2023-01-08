package ca.adaptor.zombies.game.messages;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IZombiesWsMessage {
    enum Type {
        HELLO, GAME
    }

    @NotNull UUID getMessageId();
    @NotNull Type getType();
}

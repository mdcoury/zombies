package ca.adaptor.zombies.game.messages;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IZombiesWsMessage {
    String HELLO = "Hello";
    String GOODBYE = "Goodbye";
    String REQUEST_ROLL = "RequestRoll";
    String REQUEST_USE_BULLETS = "RequestUseBullets";
    String REQUEST_MOVEMENT = "RequestMovement";
    String REQUEST_DISCARDS = "RequestDiscards";

    enum Type {
        HELLO, REQUEST, REPLY, UPDATE, BYE
    }

    @NotNull UUID getMessageId();
    @NotNull Type getType();
}

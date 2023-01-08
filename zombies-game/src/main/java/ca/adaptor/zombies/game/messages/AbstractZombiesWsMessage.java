package ca.adaptor.zombies.game.messages;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.HelloMessage.class, name = "HelloMessage"),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestRollMessage.class, name = "RequestRollMessage"),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestUseBulletsMessage.class, name = "RequestUseBulletsMessage"),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestPlayerMovementMessage.class, name = "RequestPlayerMovementMessage"),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestDiscardsMessage.class, name = "RequestDiscardsMessage"),
})
public abstract class AbstractZombiesWsMessage implements IZombiesWsMessage {
    @Getter @Setter
    private UUID messageId = UUID.randomUUID();

    @NoArgsConstructor
    public static class HelloMessage extends AbstractZombiesWsMessage {
        @Getter @Setter
        private UUID playerId;
        @Getter @Setter
        private UUID gameId;
        @Getter @Setter
        private Type type = Type.HELLO;
    }

    public static abstract class GameMessage extends AbstractZombiesWsMessage {
        @Getter @Setter
        private Type type = Type.GAME;
    }
    @NoArgsConstructor
    public static class RequestRollMessage extends GameMessage {

    }
    @NoArgsConstructor
    public static class RequestUseBulletsMessage extends GameMessage {
        @Getter @Setter
        private boolean usingBullets;
    }
    @NoArgsConstructor
    public static class RequestPlayerMovementMessage extends GameMessage {
        @Getter @Setter
        private ZombiesDirection direction;
    }
    @NoArgsConstructor
    public static class RequestDiscardsMessage extends GameMessage {
        @Getter @Setter
        private Set<UUID> cardIds;
    }

}

package ca.adaptor.zombies.game.messages;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.util.Set;
import java.util.UUID;

import static ca.adaptor.zombies.game.messages.IZombiesWsMessage.*;

@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true, allowSetters = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.Hello.class, name = HELLO),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.Goodbye.class, name = GOODBYE),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestRoll.class, name = REQUEST_ROLL),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestUseBullets.class, name = REQUEST_USE_BULLETS),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestMovement.class, name = REQUEST_MOVEMENT),
        @JsonSubTypes.Type(value = AbstractZombiesWsMessage.RequestDiscards.class, name = REQUEST_DISCARDS),
})
public abstract class AbstractZombiesWsMessage implements IZombiesWsMessage {
    @Getter @Setter
    private UUID messageId = UUID.randomUUID();

    public static abstract class AbstractRequestMessage extends AbstractZombiesWsMessage {
        @Getter @Setter
        private Type type = Type.REQUEST;
    }

    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Hello extends AbstractZombiesWsMessage {
        @Getter @Setter
        private UUID playerId;
        @Getter @Setter
        private UUID gameId;
        @Getter @Setter
        private Type type = Type.HELLO;
    }

    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class Goodbye extends AbstractZombiesWsMessage {
        @Getter @Setter
        private UUID playerId;
        @Getter @Setter
        private UUID gameId;
        @Getter @Setter
        private Type type = Type.BYE;
    }
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class RequestRoll extends AbstractRequestMessage {

    }
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class RequestUseBullets extends AbstractRequestMessage {
        @Getter @Setter
        private boolean usingBullets;
    }
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class RequestMovement extends AbstractRequestMessage {
        @Getter @Setter
        private ZombiesDirection direction;
    }
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    public static class RequestDiscards extends AbstractRequestMessage {
        @Getter @Setter
        private Set<UUID> cardIds;
    }

}

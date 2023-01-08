package ca.adaptor.zombies.game.messages;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

public abstract class AbstractWsMessage implements WsMessage {
    @Getter @Setter
    private UUID messageId = UUID.randomUUID();

    public class HelloMessage extends AbstractWsMessage {
        @Getter @Setter
        private UUID playerId;
        @Getter @Setter
        private UUID gameId;
        @Getter @Setter
        private Type type = Type.HELLO;
    }

    public abstract class GameMessage extends AbstractWsMessage {
        @Getter @Setter
        private Type type = Type.GAME;
    }

    @NoArgsConstructor
    public class RequestRollMessage extends GameMessage {

    }
    @NoArgsConstructor
    public class RequestUseBulletsMessage extends GameMessage {
        @Getter @Setter
        private boolean usingBullets;
    }
    @NoArgsConstructor
    public class RequestPlayerMovementMessage extends GameMessage {
        @Getter @Setter
        private ZombiesDirection direction;
    }
    @NoArgsConstructor
    public class RequestDiscardsMessage extends GameMessage {
        @Getter @Setter
        private Set<UUID> cardIds;
    }

}

package ca.adaptor.zombies.game.messages;

import ca.adaptor.zombies.game.model.ZombiesCoordinate;
import ca.adaptor.zombies.game.model.ZombiesGameData;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Getter @Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class ZombiesGameUpdateMessage implements IZombiesWsMessage {
    public enum Phase {
        COMBAT,
        DRAW_CARDS,
        MOVEMENT,
        ZOMBIES,
        DISCARD_CARDS
    }
    private final UUID messageId = UUID.randomUUID();
    private final Type type = Type.UPDATE;
    private final UUID gameId;
    private final long serialNumber;
    private final int turn;
    private final Phase phase;

    private ZombiesGameData playerData;
    private Map<ZombiesCoordinate, ZombiesCoordinate> zombieMovements;
    private Set<ZombiesCoordinate> zombieKills;
    private Integer roll;
}

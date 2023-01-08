package ca.adaptor.zombies.game.engine;

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
public class ZombiesGameUpdateMessage {
    public enum Phase {
        COMBAT,
        DRAW_CARDS,
        MOVEMENT,
        ZOMBIES
    }
    private final UUID messageId = UUID.randomUUID();
    private final UUID gameId;
    private final UUID engineId;
    private final long serialNumber;
    private final int turn;
    private final Phase phase;

    private ZombiesGameData playerData;
    private Map<ZombiesCoordinate, ZombiesCoordinate> zombieMovements;
    private Set<ZombiesCoordinate> zombieKills;
    private Integer roll;
}

package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesGameData;
import ca.adaptor.zombies.game.model.ZombiesCoordinate;
import lombok.*;

import java.util.*;

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
    @Getter
    private final UUID messageId = UUID.randomUUID();
    @Getter
    private final UUID gameId;
    @Getter
    private final UUID engineId;
    @Getter
    private final int turn;
    @Getter
    private final long serialNumber;
    @Getter
    private final Phase phase;

    @Getter @Setter
    private ZombiesGameData playerData = null;
    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private Map<ZombiesCoordinate, ZombiesCoordinate> zombieMovements = new HashMap<>();
    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private Set<ZombiesCoordinate> zombieKills = new HashSet<>();
    @Getter @Setter
    private Integer roll = null;
}

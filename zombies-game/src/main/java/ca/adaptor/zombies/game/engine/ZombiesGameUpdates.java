package ca.adaptor.zombies.game.engine;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ZombiesGameUpdates {
    private final ConcurrentMap<UUID, Queue<ZombiesGameUpdateMessage>> updatesByGameId = new ConcurrentHashMap<>();
}

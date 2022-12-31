package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.ZombiesDirection;
import ca.adaptor.zombies.game.model.ZombiesGame;
import ca.adaptor.zombies.game.model.ZombiesPlayer;
import ca.adaptor.zombies.game.repositories.*;
import ca.adaptor.zombies.game.util.ZombiesMapGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Random;

@SpringBootTest
public class ZombieGameEngineTest {
    private static final Random RNG = new Random(1);

    @Autowired
    private ZombiesMapRepository mapRepository;
    @Autowired
    private ZombiesTileRepository tileRepository;
    @Autowired
    private ZombiesMapTileRepository mapTileRepository;
    @Autowired
    private ZombiesGameRepository gameRepository;
    @Autowired
    private ZombiesPlayerRepository playerRepository;

    @Test
    public void engineTest() {
        var player = playerRepository.save(new ZombiesPlayer());
        var map = ZombiesMapGenerator.createAndSave(tileRepository, mapRepository, mapTileRepository, RNG);
        var game = gameRepository.save(new ZombiesGame(map));
        game.addPlayer(player);
        gameRepository.save(game);

        var engine = new ZombiesGameEngine(game, () -> {
            var mock = Mockito.mock(ZombiesGameBrokerInterface.class);
            Mockito.when(mock.requestPlayerMovement()).thenReturn(ZombiesDirection.values()[RNG.nextInt(4)]);
            Mockito.when(mock.requestUseBullets()).thenReturn(RNG.nextBoolean());
            Mockito.when(mock.requestPlayerEventCardDiscards()).thenReturn(new HashSet<>());
            return mock;
        });

        engine.runGame();
    }
}

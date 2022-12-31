package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZombiesMapTest {
    @Autowired
    private ZombiesTileRepository tRepo;
    @Autowired
    private ZombiesMapRepository mRepo;
    @Autowired
    private ZombiesMapTileRepository mtRepo;

    @Test
    public void mapSaveTest1() {
        var tile = tRepo.findByName("_elbow_1");
        var map = new ZombiesMap();
        var mt = map.add(0, 0, tile, ZombiesMapTile.TileRotation.ROT_0);
        mtRepo.save(mt);
        mRepo.save(map);
    }
    @Test
    public void mapSaveTest2() {
        var tile = tRepo.findByName("_elbow_1");
        var map = new ZombiesMap();
        mRepo.save(map);
        var mt = map.add(0, 0, tile, ZombiesMapTile.TileRotation.ROT_0);
        mtRepo.save(mt);
        mRepo.save(map);
    }
}

package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ZombiesMapTest {
    @Autowired
    private ZombiesTileRepository tRepo;
    @Autowired
    private ZombiesMapRepository mRepo;
    @Autowired
    private ZombiesMapTileRepository mtRepo;
    private ZombiesTile testTile;

    @BeforeEach
    public void setup() {
        testTile = tRepo.findByName("_elbow_1").orElseThrow();
    }

    private UUID createMap() {
        var mt = new ZombiesMapTile(testTile, new ZombiesCoordinate(0,0), ZombiesMapTile.TileRotation.ROT_0);
        mt = mtRepo.save(mt);
        var map = new ZombiesMap();
        var added = map.add(mt);
        Assertions.assertTrue(added);
        Assertions.assertNull(map.getId());
        map = mRepo.save(map);
        Assertions.assertNotNull(map.getId());

        mtRepo.flush();
        mRepo.flush();

        return map.getId();
    }

    @Test
    public void mapSaveTest1() {
        createMap();
    }

    @Test
    public void mapSaveAndRetrieveTest1() {
        var mapId = createMap();
        var mapOpt = mRepo.findById(mapId);
        Assertions.assertFalse(mapOpt.isEmpty());
        var map = mapOpt.get();
        Assertions.assertEquals(mapId, map.getId());

        Assertions.assertEquals(1, map.getMapTileIds().size());
        var zc = new ZombiesCoordinate(0,0);
        var mtId = map.getMapTileId(zc);
        Assertions.assertNotNull(mtId);

        var mtOpt = mtRepo.findById(mtId);
        Assertions.assertFalse(mtOpt.isEmpty());
        var mt = mtOpt.get();
        Assertions.assertEquals(testTile, mt.getTile());
    }
}

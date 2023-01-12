package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.util.ZombiesEntityManagerHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class ZombiesMapTest {
    @Autowired
    private ZombiesEntityManagerHelper entityManagerHelper;
    private ZombiesTile testTile;

    @BeforeEach
    public void setup() {
        testTile = entityManagerHelper.findByName("_elbow_1", ZombiesTile.class).orElseThrow();
    }

    private UUID createMap() {
        var mt = new ZombiesMapTile(testTile, new ZombiesCoordinate(0,0), ZombiesMapTile.TileRotation.ROT_0);
        mt = entityManagerHelper.save(mt);
        var map = new ZombiesMap();
        var added = map.add(mt);
        Assertions.assertTrue(added);
        Assertions.assertNull(map.getId());
        map = entityManagerHelper.save(map);
        Assertions.assertNotNull(map.getId());

        return map.getId();
    }

    @Test
    public void mapSaveTest1() {
        createMap();
    }

    @Test
    public void mapSaveAndRetrieveTest1() {
        var mapId = createMap();
        var mapOpt = entityManagerHelper.findById(mapId, ZombiesMap.class);
        Assertions.assertFalse(mapOpt.isEmpty());
        var map = mapOpt.get();
        Assertions.assertEquals(mapId, map.getId());

        Assertions.assertEquals(1, map.getMapTileIds().size());
        var zc = new ZombiesCoordinate(0,0);
        var mtId = map.getMapTileId(zc);
        Assertions.assertNotNull(mtId);

        var mtOpt = entityManagerHelper.findById(mtId, ZombiesMapTile.class);
        Assertions.assertFalse(mtOpt.isEmpty());
        var mt = mtOpt.get();
        Assertions.assertEquals(testTile.getId(), mt.getTileId());
    }
}

package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static ca.adaptor.zombies.game.model.ZombiesTile.SquareType.IMPASSABLE;
import static ca.adaptor.zombies.game.model.ZombiesTile.SquareType.ROAD;

@SpringBootTest
public class ZombiesMapTileTest {
    @Autowired
    private ZombiesTileRepository tRepo;
    @Autowired
    private ZombiesMapTileRepository mtRepo;

    @Test
    public void mapTileSimpleSaveTest() {
        var mapTile = new ZombiesMapTile(
                tRepo.findByName("_elbow_1").orElseThrow(),
                new ZombiesCoordinate(0,0),
                ZombiesMapTile.TileRotation.ROT_270
        );
        Assertions.assertNull(mapTile.getId());
        mapTile = mtRepo.save(mapTile);
        Assertions.assertNotNull(mapTile.getId());
    }

    @Test
    public void tileRotationTest_0() {
        var tile = new ZombiesMapTile(
                tRepo.findByName("_elbow_1").orElseThrow(),
                new ZombiesCoordinate(0,0),
                ZombiesMapTile.TileRotation.ROT_0
        );
        Assertions.assertNotNull(tile);
        Assertions.assertEquals(IMPASSABLE, tile.get(0,0));
        Assertions.assertEquals(ROAD, tile.get(1,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,1));
        Assertions.assertEquals(ROAD, tile.get(1,1));
        Assertions.assertEquals(ROAD, tile.get(2,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(1,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,2));
    }

    @Test
    public void tileRotationTest_90() {
        var tile = new ZombiesMapTile(
                tRepo.findByName("_elbow_1").orElseThrow(),
                new ZombiesCoordinate(0,0),
                ZombiesMapTile.TileRotation.ROT_90
        );
        Assertions.assertNotNull(tile);

        Assertions.assertEquals(IMPASSABLE, tile.get(0,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(1,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,1));
        Assertions.assertEquals(ROAD, tile.get(1,1));
        Assertions.assertEquals(ROAD, tile.get(2,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,2));
        Assertions.assertEquals(ROAD, tile.get(1,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,2));
    }

    @Test
    public void tileRotationTest_180() {
        var tile = new ZombiesMapTile(
                tRepo.findByName("_elbow_1").orElseThrow(),
                new ZombiesCoordinate(0,0),
                ZombiesMapTile.TileRotation.ROT_180
        );
        Assertions.assertNotNull(tile);

        Assertions.assertEquals(IMPASSABLE, tile.get(0,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(1,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,0));
        Assertions.assertEquals(ROAD, tile.get(0,1));
        Assertions.assertEquals(ROAD, tile.get(1,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,2));
        Assertions.assertEquals(ROAD, tile.get(1,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,2));
    }

    @Test
    public void tileRotationTest_270() {
        var tile = new ZombiesMapTile(
                tRepo.findByName("_elbow_1").orElseThrow(),
                new ZombiesCoordinate(0,0),
                ZombiesMapTile.TileRotation.ROT_270
        );
        Assertions.assertNotNull(tile);

        Assertions.assertEquals(IMPASSABLE, tile.get(0,0));
        Assertions.assertEquals(ROAD, tile.get(1,0));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,0));
        Assertions.assertEquals(ROAD, tile.get(0,1));
        Assertions.assertEquals(ROAD, tile.get(1,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,1));
        Assertions.assertEquals(IMPASSABLE, tile.get(0,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(1,2));
        Assertions.assertEquals(IMPASSABLE, tile.get(2,2));
    }
}

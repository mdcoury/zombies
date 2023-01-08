package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.ZombiesGameRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZombiesGameTest {
    @Autowired
    private ZombiesTileRepository tRepo;
    @Autowired
    private ZombiesMapRepository mRepo;
    @Autowired
    private ZombiesMapTileRepository mtRepo;
    @Autowired
    private ZombiesGameRepository gRepo;

//    @Test
//    public void gameSaveTest() {
//        var tile = tRepo.findByName("_elbow_1");
//        var map = new ZombiesMap();
//        mRepo.save(map);
//        var mt = map.add(0, 0, tile, ZombiesMapTile.TileRotation.ROT_0);
//        mtRepo.save(mt);
//        var game = new ZombiesGame(map);
//        gRepo.save(game);
//    }
}

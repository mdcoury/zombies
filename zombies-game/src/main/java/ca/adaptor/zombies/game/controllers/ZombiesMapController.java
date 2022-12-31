package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesMap;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import ca.adaptor.zombies.game.util.ZombiesMapGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_MAPS;

@RestController
@RequestMapping(PATH_MAPS)
public class ZombiesMapController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapController.class);

//    @Value("zombies.rng.seed")
    private long rngSeed = System.currentTimeMillis();

    private final Random rng = new Random(rngSeed);

    @Autowired
    private ZombiesMapRepository mapRepository;
    @Autowired
    private ZombiesTileRepository tileRepository;
    @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    public ZombiesMap createMap() {
        var map = ZombiesMapGenerator.create(tileRepository, mapTileRepository, rng);
        map = mapRepository.saveAndFlush(map);
        LOGGER.debug("Created new map: " + map);
        return map;
    }

    @PostMapping
    public UUID create() {
        return createMap().getId();
    }

    @GetMapping
    public List<UUID> getAllIds() {
        var ret = mapRepository.findAllIds();
        LOGGER.debug("Retrieving all maps... found " + ret.size());
        return ret;
    }
}

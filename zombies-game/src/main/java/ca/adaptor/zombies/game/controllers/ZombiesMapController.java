package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesMap;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import ca.adaptor.zombies.game.util.ZombiesMapGenerator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_MAPS;

@RestController
@RequestMapping(PATH_MAPS)
public class ZombiesMapController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapController.class);

    private final long rngSeed = Long.parseLong(System.getProperty("zombies.rng.seed", String.valueOf(System.currentTimeMillis())));
    private final Random rng = new Random(rngSeed);

    @Autowired
    private ZombiesMapRepository mapRepository;
    @Autowired
    private ZombiesTileRepository tileRepository;
    @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    @Autowired
    private AutowireCapableBeanFactory autowireFactory;

    @NotNull
    ZombiesMap createMap() {
        LOGGER.trace("Creating new map...");
        var map = ZombiesMapGenerator.create(tileRepository, mapTileRepository, autowireFactory, rng);
        map = mapRepository.saveAndFlush(map);
        LOGGER.debug("Created map: " + map.getId());
        return map;
    }

    @PostMapping
    public ResponseEntity<UUID> create() {
        return ResponseEntity.ok(createMap().getId());
    }

    @GetMapping(path = "{mapId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZombiesMap> getMap(@PathVariable UUID mapId) {
        var mapOpt = mapRepository.findById(mapId);
        if(mapOpt.isPresent()) {
            var map = mapOpt.get();
            LOGGER.debug("Retrieving map: " + map.getId());
            return ResponseEntity.ok(map);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

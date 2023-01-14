package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.util.ZombiesEntityManagerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_TILES;

@RestController
@RequestMapping(PATH_TILES)
public class ZombiesTileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesTileController.class);
    @Autowired
    private ZombiesEntityManagerHelper entityManager;

    @GetMapping
    public ResponseEntity<List<UUID>> getAllIds() {
        var ret = entityManager.findAllIds(ZombiesTile.class);
        LOGGER.debug("Retrieving all tile-IDs... found " + ret.size());
        return ResponseEntity.ok(ret);
    }

    @GetMapping(value = "{tileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZombiesTile> getTile(@PathVariable UUID tileId) {
        var tileOpt = entityManager.findById(tileId, ZombiesTile.class);
        if(tileOpt.isPresent()) {
            var tile = tileOpt.get();
            LOGGER.debug("Retrieving tile: " + tile.getId());
            return ResponseEntity.ok(tile);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

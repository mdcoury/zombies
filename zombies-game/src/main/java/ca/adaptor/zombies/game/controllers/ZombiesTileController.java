package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private ZombiesTileRepository tileRepository;

    @GetMapping
    public ResponseEntity<List<UUID>> getAllIds() {
        var retOpt = tileRepository.findAllIds();
        LOGGER.debug("Retrieving all tile-IDs... found " + retOpt.map(List::size).orElse(0));
        return retOpt.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("{tileId}")
    public ResponseEntity<ZombiesTile> getTile(@PathVariable UUID tileId) {
        var retOpt = tileRepository.findById(tileId);
        LOGGER.debug("Retrieving tile: " + retOpt);
        return retOpt.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

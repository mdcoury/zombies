package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesMapTile;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
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

import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_MAP_TILES;

@RestController
@RequestMapping(PATH_MAP_TILES)
public class ZombiesMapTileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapTileController.class);
    @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    @GetMapping(path = "{mapTileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZombiesMapTile> getMapTile(@PathVariable UUID mapTileId) {
        var mapTileOpt = mapTileRepository.findById(mapTileId);
        if(mapTileOpt.isPresent()) {
            var mapTile = mapTileOpt.get();
            LOGGER.debug("Retrieving map-tile: " + mapTile.getId());
            return ResponseEntity.ok(mapTile);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}

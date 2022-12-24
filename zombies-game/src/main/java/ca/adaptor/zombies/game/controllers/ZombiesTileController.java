package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_TILES;

@RestController
@RequestMapping(PATH_TILES)
public class ZombiesTileController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesTileController.class);
    @Autowired
    private ZombiesTileRepository repository;

    @GetMapping
    public List<ZombiesTile> retrieveAll() {
        var ret = repository.findAll();
        LOGGER.debug("Retrieving all tiles... found " + ret.size());
        return ret;
    }

    @GetMapping("{id}")
    public Optional<ZombiesTile> retrieve(@PathVariable UUID id) {
        var ret = repository.findById(id);
        LOGGER.debug("Retrieving tile: " + ret);
        return ret;
    }
}

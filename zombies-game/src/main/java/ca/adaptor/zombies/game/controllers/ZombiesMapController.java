package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesMap;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.util.ZombiesMapGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_MAPS;

@RestController
@RequestMapping(PATH_MAPS)
public class ZombiesMapController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapController.class);

    @Autowired
    private ZombiesMapRepository repository;

    @PostMapping
    public UUID create(
            @RequestParam int width,
            @RequestParam int height
    ) {
        var map = ZombiesMapGenerator.create(width, height);
        LOGGER.debug("Created new ("+width+"x"+height+") map: " + map.dump());
        repository.save(map);
        return map.getUuid();
    }

    @GetMapping
    public List<ZombiesMap> retrieveAll() {
        var ret = repository.findAll();
        LOGGER.debug("Retrieving all maps... found " + ret.size());
        return ret;
    }
}

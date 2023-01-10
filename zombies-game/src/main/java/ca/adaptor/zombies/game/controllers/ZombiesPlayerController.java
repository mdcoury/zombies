package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesPlayer;
import ca.adaptor.zombies.game.repositories.ZombiesPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_PLAYERS;

@RestController
@RequestMapping(PATH_PLAYERS)
public class ZombiesPlayerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesPlayerController.class);
    @Autowired
    private ZombiesPlayerRepository repository;

    @PostMapping
    public ResponseEntity<UUID> create() {
        var player = repository.saveAndFlush(new ZombiesPlayer());
        LOGGER.debug("Created player: " + player.getId());
        return ResponseEntity.ok(player.getId());
    }
}

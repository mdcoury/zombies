package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.model.ZombiesGame;
import ca.adaptor.zombies.game.repositories.ZombiesGameRepository;
import ca.adaptor.zombies.game.repositories.ZombiesPlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_GAMES;

@RestController
@RequestMapping(PATH_GAMES)
public class ZombiesGameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameController.class);
    @Autowired
    private ZombiesGameRepository gameRepository;
    @Autowired
    private ZombiesPlayerRepository playerRepository;
    @Autowired
    private ZombiesMapController mapController;

    @PostMapping
    public ResponseEntity<UUID> create() {
        var map = mapController.createMap();
        var game = gameRepository.saveAndFlush(new ZombiesGame(map));
        LOGGER.debug("Created game: " + game);
        return ResponseEntity.ok(game.getId());
    }

    @GetMapping
    public List<ZombiesGame> getAll() {
        var ret = gameRepository.findAll();
        LOGGER.debug("Retrieving all maps... found " + ret.size());
        return ret;
    }

    @GetMapping(path = "{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZombiesGame> getGame(@PathVariable UUID gameId) {
        var gameOpt = gameRepository.findById(gameId);
        LOGGER.debug("Retrieving game: " + gameOpt);
        return gameOpt.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{gameId}/join/{playerId}")
    public ResponseEntity<Boolean> joinGame(@PathVariable UUID gameId, @PathVariable UUID playerId) {
        var gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()) {
            var game = gameOpt.get();
            var playerOpt = playerRepository.findById(playerId);
            if(playerOpt.isPresent()) {
                var player = playerOpt.get();
                LOGGER.debug("Player (" + player + ") joining game (" + game + ")");
                var joined = game.addPlayer(player);
                gameRepository.saveAndFlush(game);
                return ResponseEntity.ok(joined);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("{gameId}/init")
    public ResponseEntity<Boolean> initializeGame(@PathVariable UUID gameId) {
        var gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()) {
            var game = gameOpt.get();
            if(!game.isInitialized()) {
                LOGGER.debug("Starting game: " + game);
                var initialized = game.initialize();
                gameRepository.saveAndFlush(game);
                return ResponseEntity.ok(initialized);
            }
            return ResponseEntity.ok(false);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

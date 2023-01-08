package ca.adaptor.zombies.game.controllers;

import ca.adaptor.zombies.game.engine.ZombiesGameBroker;
import ca.adaptor.zombies.game.engine.ZombiesGameEngine;
import ca.adaptor.zombies.game.model.*;
import ca.adaptor.zombies.game.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ca.adaptor.zombies.game.controllers.ZombiesControllerConstants.PATH_GAMES;

@RestController
@RequestMapping(PATH_GAMES)
public class ZombiesGameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameController.class);
    private static final ExecutorService theExecutor = Executors.newWorkStealingPool();

    @Autowired
    private ZombiesGameRepository gameRepository;
    @Autowired
    private ZombiesGameDataRepository gameDataRepository;
    @Autowired
    private ZombiesPlayerRepository playerRepository;
    @Autowired
    private ZombiesMapRepository mapRepository;
    @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    @Autowired
    private ZombiesMapController mapController;
    @Autowired
    private ZombiesWsController wsController;

    @Autowired
    private AutowireCapableBeanFactory autowireFactory;

    /** @return the {@link UUID} of the created {@link ZombiesGame} */
    @PostMapping
    public ResponseEntity<UUID> create() {
        var map = mapController.createMap();
        var game = gameRepository.saveAndFlush(new ZombiesGame(map.getId()));
        LOGGER.debug("Created game: " + game.getId());
        return ResponseEntity.ok(game.getId());
    }

    @GetMapping
    public ResponseEntity<List<UUID>> getAllIds() {
        var retOpt = gameRepository.findAllIds();
        LOGGER.debug("Retrieving all game-IDs... found " + retOpt.map(List::size).orElse(0));
        return retOpt.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ZombiesGame> getGame(@PathVariable UUID gameId) {
        var gameOpt = gameRepository.findById(gameId);
        LOGGER.debug("Retrieving game: " + gameOpt);
        return gameOpt.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /** @return the {@link UUID} of the player's {@link ZombiesGameData} */
    @PutMapping("{gameId}/join/{playerId}")
    public ResponseEntity<UUID> joinGame(@PathVariable UUID gameId, @PathVariable UUID playerId) {
        var gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()) {
            var game = gameOpt.get();
            game.autowire(autowireFactory);

            var playerOpt = playerRepository.findById(playerId);
            if(playerOpt.isPresent()) {
                var player = playerOpt.get();
                var playerDataId = game.addPlayer(player.getId());
                if(playerDataId != null) {
                    LOGGER.debug("Player (" + player.getId() + ") joined game (" + game.getId() + ")");
                    gameRepository.saveAndFlush(game);
                }
                return ResponseEntity.ok(playerDataId);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("{gameId}/populate")
    public ResponseEntity<Boolean> populateGame(@PathVariable UUID gameId) {
        var gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()) {
            var game = gameOpt.get();
            game.autowire(autowireFactory);
            if(!game.isPopulated()) {
                LOGGER.debug("Populating game: " + game.getId());
                var populated = game.populate();
                gameRepository.saveAndFlush(game);
                return ResponseEntity.ok(populated);
            }
            return ResponseEntity.ok(false);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /** @return the {@link UUID} of the {@link ZombiesGameEngine} */
    @PutMapping("{gameId}/start")
    public ResponseEntity<UUID> startGame(@PathVariable UUID gameId) {
        var gameOpt = gameRepository.findById(gameId);
        if(gameOpt.isPresent()) {
            var game = gameOpt.get();
            game.autowire(autowireFactory);
            var engine = ZombiesGameEngine.getInstance(game, ZombiesGameBroker::new);
            // TODO: Chance for race-condition here
            if(!game.isRunning()) {
                engine.autowire(autowireFactory);
                theExecutor.submit(engine::runGame);

            }
            return ResponseEntity.ok(engine.getGameEngineId());
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}

package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.model.*;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Supplier;

import static ca.adaptor.zombies.game.engine.ZombiesGameUpdateMessage.Phase.*;
import static ca.adaptor.zombies.game.model.ZombiesGame.MAX_NUM_EVENT_CARDS;

public class ZombiesGameEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameEngine.class);
    public static final int MIN_WINNING_ROLL = 4;

    //    @Value("zombies.rng.seed")
    private long rngSeed = System.currentTimeMillis();

    // TODO: Pass a seed into here, perhaps from application.properties
    private final Random rng = new Random(rngSeed);
    @Getter
    private final UUID gameEngineId = UUID.randomUUID();
    @Getter
    private final ZombiesGame theGame;
    @Getter
    private long serialNumberCtr = 0;
    @Getter
    private boolean running = false;

    private final Map<UUID, ZombiesGameBrokerInterface> theBrokersById = new HashMap<>();
    private final ZombiesMapRepository mapRepository;
    private final ZombiesMapTileRepository mapTileRepository;
    private ZombiesMap theMap;

    public <B extends ZombiesGameBrokerInterface> ZombiesGameEngine(
            @NotNull ZombiesGame game,
            @NotNull Supplier<B> supplier,
            @NotNull ZombiesMapRepository mapRepository,
            @NotNull ZombiesMapTileRepository mapTileRepository
    ) {
        theGame = game;
        this.mapRepository = mapRepository;
        this.mapTileRepository = mapTileRepository;
        for(var playerId : theGame.getPlayerIds()) {
            theBrokersById.put(playerId, supplier.get());
        }
        LOGGER.debug("Created engine (id="+gameEngineId+") for game (id="+theGame.getId()+")");
    }

    public void runGame() {
        if(running) {
            throw new IllegalStateException("The game's (id="+theGame.getId()+") engine (id="+gameEngineId+") is already running!)");
        }
        if(!theGame.isInitialized()) {
            boolean initialized = theGame.initialize();
            if(!initialized) {
                throw new IllegalStateException("The game (id="+theGame.getId()+") failed to initialize!");
            }
        }

        theMap = mapRepository.findById(theGame.getMapId()).orElseThrow();

        running = true;
        while(running) {
            //----- During a turn, players must perform the following steps.
            theGame.incrementTurn();
            for(var playerId : theGame.getPlayerIds()) {
                LOGGER.debug("Running... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");

                var playerData = theGame.getPlayerData(playerId);
                //  1. Draw a tile from the map deck and place it on the table.
                /* NOOP */
                //  2. Combat any zombies on your current space. (Please see Combat Rules section.)
                if(theGame.isZombieAtLocation(theGame.getPlayerData(playerId).getLocation())) {
                    resolveCombat(playerId);
                    if(playerData.isPlayerDead()) {
                        //----- reset the player and then continue
                        LOGGER.trace("Player (" + playerId + ") has died!");
                        resetPlayer(playerId);
                        continue;
                    }
                }
                //  3. Draw back up to three event cards, if you have less than three.
                var eventCardIds = theGame.getPlayerData(playerId).getEventCardIds();
                while(eventCardIds.size() < MAX_NUM_EVENT_CARDS) {
                    LOGGER.debug("Drawing card... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
                    eventCardIds.add(drawCard());
                }
                var update = createUpdateMessage(DRAW_CARDS);
                update.setPlayerData(theGame.getPlayerData(playerId));
                theBrokersById.get(playerId).sendGameUpdate(update);
                //  4. Make a movement roll. (Please see Movement Rules section.)
                //  5. Move up to the number of spaces indicated by the movement roll. You must stop
                //     and combat on any space occupied by a zombie. You may continue your movement
                //     after defeating a zombie up to your movement total.
                resolveMovement(playerId);
                if(playerData.isPlayerDead()) {
                    //----- reset the player and then continue
                    LOGGER.trace("Player (" + playerId + ") has died!");
                    resetPlayer(playerId);
                    continue;
                }
                //  6. After moving, roll a six-sided die. You must move that number of zombies,
                //     one space each, if able.
                resolveZombieMovement(playerId);
                //  7. At the end of the turn, you may discard one event card from your hand.
                //     Play then proceeds clockwise around the table
                resolveEventCardDiscards(playerId);
            }
        }
    }

    private void resetPlayer(@NotNull UUID playerId) {
        throw new RuntimeException("Not implemented yet");
    }

    private int rollD6() {
        var ret = rng.nextInt(6);
        LOGGER.trace("Rolled D6: " + ret);
        return ret;
    }

    private int requastRoll(@NotNull ZombiesGameBrokerInterface broker, ZombiesGameUpdateMessage.Phase phase) {
        broker.requestRoll();
        int roll = rollD6();
        var rollUpdate = createUpdateMessage(phase);
        rollUpdate.setRoll(roll);
        broadcastUpdateMessage(rollUpdate);
        return roll;
    }

    /**
     * Combat Rules
     * • Any time you begin your turn on the same space as a zombie or you land on a space
     *      occupied by a zombie during movement, combat ensues.
     * • Combat is resolved by rolling a six-sided die. If you roll a four, five or six, you win
     *      and the zombie is added to your collection; if you roll a one, two or three, you lose
     *      and must either, forfeit a life token or spend enough bullet tokens to raise the roll
     *      enough to make it successful. For example, if you rolled a two, you could discard a
     *      life token and roll again or spend two bullet tokens to raise the total from two to four.
     * • Combat continues until the player wins or runs out of life tokens. A player can never
     *      voluntarily leave combat.
     * • When a player runs out of life tokens, movement stops and he must move his pawn
     *      back to the center of town square. Additionally he forfeits the rest of his movement,
     *      loses half (rounded up) of the zombies he has collected and any weapon cards he
     *      has in play. The rest of the turn continues as normal.
     * • Whenever a player dies and is forced to start again, he begins his next turn with
     *      three life and three bullet tokens.
     */
    private void resolveCombat(@NotNull UUID playerId) {
        var data = theGame.getPlayerData(playerId);
        var location = data.getLocation();

        assert theGame.isZombieAtLocation(location);
        LOGGER.trace("Resolving combat... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");

        var broker = theBrokersById.get(playerId);
        var stillResolving = true;
        while(stillResolving) {
            //----- Ask the player to "roll" the dice
            int roll = requastRoll(broker, COMBAT);
            if(roll < MIN_WINNING_ROLL) {
                //----- If the player has enough bullets
                if(data.getNumBullets() + roll >= MIN_WINNING_ROLL) {
                    //----- Ask the player if they want to use their bullets
                    boolean use = broker.requestUseBullets();
                    if(use) {
                       var nb = MIN_WINNING_ROLL - roll;
                       for(int i = 0; i < nb; i++) {
                           var used = data.decrementBullets();
                           assert used;
                           roll++;
                       }
                    }
                }
            }
            stillResolving = resolveCombatRoll(data, roll);
        }
    }

    private boolean resolveCombatRoll(
            @NotNull ZombiesGameData data,
            int roll
    ) {
        boolean ret;
        var combatUpdate = createUpdateMessage(COMBAT);
        combatUpdate.setPlayerData(data);
        if(roll >= MIN_WINNING_ROLL) {
            //----- This means the player killed the zombie
            ret = false;
            assert theGame.getZombieLocations().contains(data.getLocation());
            theGame.getZombieLocations().remove(data.getLocation());
            combatUpdate.getZombieKills().add(data.getLocation());
        }
        else {
            //----- The player loses a life :( decrementLife() returns false if the player's health is 0
            ret = data.decrementLife();
        }
        broadcastUpdateMessage(combatUpdate);
        return ret;
    }

    @NotNull
    private ZombiesGameUpdateMessage createUpdateMessage(ZombiesGameUpdateMessage.Phase phase) {
        return new ZombiesGameUpdateMessage(theGame.getId(), gameEngineId, theGame.getTurn(), serialNumberCtr++, phase);
    }

    private void broadcastUpdateMessage(@NotNull ZombiesGameUpdateMessage update) {
        LOGGER.trace("Broadcasting game-update: " + update);
        for(var broker : theBrokersById.values()) {
            broker.sendGameUpdate(update);
        }
    }

    @NotNull
    private UUID drawCard() {
        throw new RuntimeException("Not implemented yet");
    }

    @NotNull
    private ZombiesCoordinate getDestination(@NotNull ZombiesCoordinate location, @NotNull ZombiesDirection direction) {
        ZombiesCoordinate ret;
        switch(direction) {
            case NORTH -> ret = new ZombiesCoordinate(location.getX(), location.getY() - 1);
            case EAST -> ret = new ZombiesCoordinate(location.getX() + 1, location.getY());
            case SOUTH -> ret = new ZombiesCoordinate(location.getX(), location.getY() + 1);
            case WEST -> ret = new ZombiesCoordinate(location.getX() - 1, location.getY());
            default -> throw new IllegalArgumentException();
        }
        return ret;
    }

    /**
     * Player Movement Rules
     * • Movement amount is determined by rolling a six-sided die.
     * • No diagonal movement is allowed.
     * • Players may choose not to use the entire movement amount and may stop at any
     *      time. Players may only move on to road or named building spaces.
     * • Any zombies encountered while moving must be fought before movement continues.
     *      Please see the Combat section for more details. (Event cards such as “Alternate
     *      Food Source” override this rule.)
     * • If a player moves onto a space containing a life or bullet token and no zombie, the
     *      token is immediately added to that player’s collection.
     * • Zombies and players may move in and out of “named” buildings
     *      only and they must use the door spaces indicated. No other
     *      buildings or parking lots may be entered.
     * • All spaces in “named” buildings are used. These spaces are
     *      indicated by the lines in the buildings. For Example, the “Sporting
     *      Goods Store” to the right has seven interior spaces and one road
     *      space
     */
    private void resolveMovement(@NotNull UUID playerId) {
        LOGGER.trace("Resolving movement... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
        var location = theGame.getPlayerData(playerId).getLocation();

        var broker = theBrokersById.get(playerId);
        int roll = requastRoll(broker, MOVEMENT);

        for(int i = 0; i < roll; i++) {
            var direction = broker.requestPlayerMovement();
            if(direction == null) {
                //----- This indicates that the player does not want to use all of their movement, so we break out of
                //      the loop early
                LOGGER.trace("Player (" + playerId + ") has stopped moving early");
                break;
            }

            var destination = getDestination(location, direction);
            var data = theGame.getPlayerData(playerId);

            var update = createUpdateMessage(MOVEMENT);
            update.setPlayerData(data);

            var mapTileId = theMap.getMapTileId(destination);
            assert mapTileId != null;
            var mapTile = mapTileRepository.findById(mapTileId).orElseThrow();
            if(mapTile.getSquareType(destination) != ZombiesTile.SquareType.IMPASSABLE) {
                LOGGER.trace("Moving player ("+playerId+"): ("+data.getLocation()+") -> ("+destination+")");
                data.setLocation(destination);
                //----- See if there's a zombie where the player has moved to
                if(theGame.isZombieAtLocation(destination)) {
                    resolveCombat(playerId);
                    if(data.isPlayerDead()) {
                        break;
                    }
                }
                if(theGame.isBulletAtLocation(destination)) {
                    LOGGER.trace("Player ("+playerId+") found a bullet!");
                    theGame.getBulletLocations().remove(destination);
                    data.incrementBullets();
                }
                if(theGame.isLifeAtLocation(destination)) {
                    LOGGER.trace("Player ("+playerId+") found a life!");
                    theGame.getLifeLocations().remove(destination);
                    data.incrementLife();
                }
            }
            else {
                // TODO: Should handle this case more elegantly
                LOGGER.trace("Player has attempted an invalid move. Allowing them to try again...");
                i--;
            }
        }
    }

    /**
     * Zombie Movement Rules
     * • At the end of a turn, a six-sided die is rolled and that number of zombies is moved
     *      one space each, if possible.
     * • Zombies may not be moved diagonally.
     * • Each space may have only one zombie on it at a time, ever.
     * • Spaces containing a zombie may also contain a life or bullet token but, if the zombie
     *      moves, the token does not
     */
    private void resolveZombieMovement(@NotNull UUID playerId) {
        LOGGER.trace("Resolving zombie movement... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");

        var update = createUpdateMessage(ZOMBIES);

        var zombieLocations = new ArrayList<>(theGame.getZombieLocations());
        Collections.shuffle(zombieLocations, rng);

        int numZombiesToMove = rollD6();
        int i = -1;
        while(numZombiesToMove > 0 && ++i < zombieLocations.size()) {
            var zombieLocation = zombieLocations.get(i);
            var directions = Arrays.asList(ZombiesDirection.values());
            Collections.shuffle(directions);
            var zombieMoved = false;
            for(var direction : directions) {
                var destination = getDestination(zombieLocation, direction);
                var mapTileId = theMap.getMapTileId(destination);
                assert mapTileId != null;
                var mapTile = mapTileRepository.findById(mapTileId).orElseThrow();
                //----- This zombie can move to the destination iff the destination is not impassable, and...
                if(mapTile.getSquareType(destination) != ZombiesTile.SquareType.IMPASSABLE
                        //----- there is not already a zombie there
                        && !theGame.getZombieLocations().contains(destination)
                ) {
                    zombieMoved = true;
                    numZombiesToMove--;
                    LOGGER.trace("Moving zombie: ("+zombieLocation+") -> ("+destination+")");
                    // TODO: This is clunky...
                    theGame.getZombieLocations().remove(zombieLocation);
                    theGame.getZombieLocations().add(destination);
                    update.getZombieMovements().put(zombieLocation, destination);
                    break;
                }
            }

            if(!zombieMoved) {
                LOGGER.trace("No viable move for zombie at (" + zombieLocation + ")");
            }
        }
        broadcastUpdateMessage(update);
    }

    private void resolveEventCardDiscards(@NotNull UUID playerId) {
        LOGGER.trace("Resolving event-card discards... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
        var cardIdsToDiscard = theBrokersById.get(playerId).requestPlayerEventCardDiscards();
        var playerCardIds = theGame.getPlayerData(playerId).getEventCardIds();
        for(var cardId : cardIdsToDiscard) {
            LOGGER.trace("Player (" + playerId + ") discarding card: id= " + cardId);
            playerCardIds.removeIf(x -> x.equals(cardId));
        }
    }
}

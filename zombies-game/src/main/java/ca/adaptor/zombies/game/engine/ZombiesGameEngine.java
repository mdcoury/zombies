package ca.adaptor.zombies.game.engine;

import ca.adaptor.zombies.game.messages.ZombiesGameUpdateMessage;
import ca.adaptor.zombies.game.model.*;
import ca.adaptor.zombies.game.util.ZombiesEntityManagerHelper;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static ca.adaptor.zombies.game.messages.ZombiesGameUpdateMessage.Phase.*;
import static ca.adaptor.zombies.game.model.ZombiesGame.MAX_NUM_EVENT_CARDS;

public class ZombiesGameEngine {
//region Static methods and fields
    private static class Registry {
        private final ReadWriteLock registryLock = new ReentrantReadWriteLock();

        /** Maps a engine/game-ID to a game-engine */
        private final Map<UUID, ZombiesGameEngine> theEngineRegistry = new HashMap<>();
        /**
         * A {@link ZombiesGame} id is in this {@link Set} iff there is a game
         * -engine for it currently active; ie, in {@link #theEngineRegistry}.
         */
        private final Map<UUID, UUID> theGameRegistry = new HashMap<>();

        boolean containsGame(@NotNull UUID gameId) {
            registryLock.readLock().lock();
            try {
                return theGameRegistry.containsKey(gameId);
            }
            finally {
                registryLock.readLock().unlock();
            }
        }
        boolean containsEngine(@NotNull UUID engineId) {
            registryLock.readLock().lock();
            try {
                return theEngineRegistry.containsKey(engineId);
            }
            finally {
                registryLock.readLock().unlock();
            }
        }
        @Nullable
        ZombiesGameEngine getEngineById(@NotNull UUID engineId) {
            registryLock.readLock().lock();
            try {
                return theEngineRegistry.get(engineId);
            }
            finally {
                registryLock.readLock().unlock();
            }
        }
        @Nullable
        ZombiesGameEngine getEngineByGameId(@NotNull UUID gameId) {
            registryLock.readLock().lock();
            try {
                return theEngineRegistry.get(theGameRegistry.get(gameId));
            }
            finally {
                registryLock.readLock().unlock();
            }
        }
        void addEngine(@NotNull ZombiesGameEngine engine) {
            registryLock.writeLock().lock();
            assert !theRegistry.containsEngine(engine.getGameEngineId());
            try {
                LOGGER.trace("Adding game-engine to registry: " + engine.getGameEngineId());
                theEngineRegistry.put(engine.getGameEngineId(), engine);
                theGameRegistry.put(engine.getTheGame().getId(), engine.getGameEngineId());
            }
            finally {
                registryLock.writeLock().unlock();
            }
        }
        void removeEngine(@NotNull ZombiesGameEngine engine) {
            removeEngineById(engine.getGameEngineId());
        }
        void removeEngineById(@NotNull UUID engineId) {
            registryLock.writeLock().lock();
            try {
                LOGGER.trace("Removing game-engine from registry: " + engineId);
                var engine = theEngineRegistry.remove(engineId);
                if(engine != null) {
                    theGameRegistry.remove(engine.getTheGame().getId());
                }
            }
            finally {
                registryLock.writeLock().unlock();
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGameEngine.class);
    public static final int MIN_WINNING_ROLL = 4;

    private static final Registry theRegistry = new Registry();

    private static final Lock mutex = new ReentrantLock();

    @NotNull
    public static ZombiesGameEngine getInstance(@NotNull UUID gameId) {
        return Objects.requireNonNull(
                theRegistry.getEngineByGameId(gameId)
        );
    }

    @NotNull
    public static <B extends IZombiesGameBroker> ZombiesGameEngine getInstance(
            @NotNull ZombiesGame game,
            @NotNull Supplier<B> supplier
    ) {
        assert game.getId() != null;

        mutex.lock();
        try {
            if (!theRegistry.containsGame(game.getId())) {
                theRegistry.addEngine(new ZombiesGameEngine(game, supplier));
            }
        }
        finally {
            mutex.unlock();
        }
        return getInstance(game.getId());
    }

    public static void releaseInstance(@NotNull UUID engineId) {
        theRegistry.removeEngineById(engineId);
    }
//endregion

    private final long rngSeed = Long.parseLong(System.getProperty("zombies.rng.seed", String.valueOf(System.currentTimeMillis())));
    private final Random rng = new Random(rngSeed);
    @Getter
    private final UUID gameEngineId = UUID.randomUUID();
    @Getter
    private final ZombiesGame theGame;
    @Getter
    private long serialNumberCtr = 0;

    private final Map<UUID, IZombiesGameBroker> theBrokersByPlayerId = new HashMap<>();
    @Autowired
    private ZombiesEntityManagerHelper entityManager;
    private ZombiesMap theMap;

    private <B extends IZombiesGameBroker> ZombiesGameEngine(
            @NotNull ZombiesGame game,
            @NotNull Supplier<B> supplier
    ) {
        theGame = game;
        for(var playerId : theGame.getPlayerIds()) {
            var broker = supplier.get();
            theBrokersByPlayerId.put(playerId, broker);
            LOGGER.trace("Created broker (id="+broker.getBrokerId()+") for player ("+playerId+")");
        }
        LOGGER.debug("Created engine (id="+gameEngineId+") for game (id="+theGame.getId()+")");
    }

    @Nullable
    public IZombiesGameBroker getBroker(@NotNull UUID playerId) {
        return theBrokersByPlayerId.get(playerId);
    }

    public void autowire(@NotNull AutowireCapableBeanFactory autowireFactory) {
        autowireFactory.autowireBean(this);
    }

    public void runGame() {
        assert entityManager != null;

        if(!theGame.isPopulated()) {
            throw new IllegalStateException("The game (id="+theGame.getId()+") is not initialized!");
        }
        if(theGame.isRunning()) {
            throw new IllegalStateException("The game's (id="+theGame.getId()+") engine (id="+gameEngineId+") is already running!)");
        }

        theMap = entityManager.findById(theGame.getMapId(), ZombiesMap.class).orElseThrow();
        //----- Wait until all of the players have connected to the web-socket
        try {
            waitForPlayersToConnect();
        }
        catch (InterruptedException e) {
            LOGGER.error("An exception occurred while waiting for players to connect", e);
            return;
        }

        theGame.setRunning(true);
        while(theGame.isRunning()) {
            //----- During a turn, players must perform the following steps.
            theGame.incrementTurn();
            for(var playerId : theGame.getPlayerIds()) {
                LOGGER.debug("Running... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
                var playerData = theGame.getPlayerData(playerId);

                //  1. Draw a tile from the map deck and place it on the table.
                /* NOOP */
                //  2. Combat any zombies on your current space. (Please see Combat Rules section.)
                if(theGame.isZombieAtLocation(playerData.getLocation())) {
                    resolveCombat(playerId);
                    if(playerData.isPlayerDead()) {
                        //----- reset the player and then continue
                        resetPlayer(playerId);
                        continue;
                    }
                }
                //  3. Draw back up to three event cards, if you have less than three.
                var eventCardIds = playerData.getEventCardIds();
                while(eventCardIds.size() < MAX_NUM_EVENT_CARDS) {
                    broadcastUpdateMessage(createUpdateMessage(DRAW_CARDS));
                    LOGGER.debug("Drawing card... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
                    eventCardIds.add(drawCard());
                }
                var update = createUpdateMessage(DRAW_CARDS);
                update.setPlayerData(playerData);
                theBrokersByPlayerId.get(playerId).sendGameUpdate(update);
                //  4. Make a movement roll. (Please see Movement Rules section.)
                //  5. Move up to the number of spaces indicated by the movement roll. You must stop
                //     and combat on any space occupied by a zombie. You may continue your movement
                //     after defeating a zombie up to your movement total.
                broadcastUpdateMessage(createUpdateMessage(MOVEMENT));
                resolveMovement(playerId);
                if(playerData.isPlayerDead()) {
                    //----- reset the player and then continue
                    resetPlayer(playerId);
                    continue;
                }
                //  6. After moving, roll a six-sided die. You must move that number of zombies,
                //     one space each, if able.
                broadcastUpdateMessage(createUpdateMessage(ZOMBIES));
                resolveZombieMovement(playerId);
                //  7. At the end of the turn, you may discard one event card from your hand.
                //     Play then proceeds clockwise around the table
                if(playerData.getEventCardIds().size() > 0) {
                    broadcastUpdateMessage(createUpdateMessage(DISCARD_CARDS));
                    resolveEventCardDiscards(playerId);
                }
                // TODO: Should verify that the game-state is being updated in the DB
            }
        }
    }

    private void waitForPlayersToConnect() throws InterruptedException {
        var latch = new CountDownLatch(theGame.getNumberOfPlayers());
        for(var broker : theBrokersByPlayerId.values()) {
            broker.testForHandler(latch::countDown);
        }
        // TODO: Add timeout
        latch.await();
        LOGGER.trace("All players have connected to game (engine-id="+gameEngineId+")");
    }

    private void resetPlayer(@NotNull UUID playerId) {
        throw new RuntimeException("Not implemented yet");
    }

    private int rollD6() {
        var ret = rng.nextInt(6) + 1;
        LOGGER.trace("Rolled D6: " + ret);
        return ret;
    }

    private int requastRoll(@NotNull IZombiesGameBroker broker, ZombiesGameUpdateMessage.Phase phase) {
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
        broadcastUpdateMessage(createUpdateMessage(COMBAT));
        var data = theGame.getPlayerData(playerId);
        var location = data.getLocation();

        assert theGame.isZombieAtLocation(location);
        LOGGER.trace("Resolving combat... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");

        var broker = theBrokersByPlayerId.get(playerId);
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
            if(combatUpdate.getZombieKills() == null) {
                combatUpdate.setZombieKills(new HashSet<>());
            }
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
        return new ZombiesGameUpdateMessage(theGame.getId(), serialNumberCtr++, theGame.getTurn(), phase);
    }

    private void broadcastUpdateMessage(@NotNull ZombiesGameUpdateMessage update) {
        LOGGER.trace("Broadcasting game-update: " + update);
        for(var broker : theBrokersByPlayerId.values()) {
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
            case NORTH  -> ret = new ZombiesCoordinate(location.getX(),     location.getY() - 1 );
            case EAST   -> ret = new ZombiesCoordinate(location.getX() + 1, location.getY()     );
            case SOUTH  -> ret = new ZombiesCoordinate(location.getX(),     location.getY() + 1 );
            case WEST   -> ret = new ZombiesCoordinate(location.getX() - 1, location.getY()     );
            default     -> throw new IllegalArgumentException();
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

        var broker = theBrokersByPlayerId.get(playerId);
        int roll = requastRoll(broker, MOVEMENT);
        var data = theGame.getPlayerData(playerId);

        for(int i = 0; i < roll; i++) {
            var direction = broker.requestPlayerMovement();
            if(direction == null) {
                //----- This indicates that the player does not want to use all of their movement, so we break out of
                //      the loop early
                LOGGER.trace("Player (" + playerId + ") has stopped moving early");
                break;
            }

            var destination = getDestination(data.getLocation(), direction);

            var update = createUpdateMessage(MOVEMENT);
            update.setPlayerData(data);

            var mapTileId = theMap.getMapTileId(destination);
            assert mapTileId != null;
            var mapTile = entityManager.findById(mapTileId, ZombiesMapTile.class).orElseThrow();
            if(mapTile.getSquareType(destination) != ZombiesTile.SquareType.IMPASSABLE) {
                LOGGER.trace("Moving player (" + playerId + "): (" + data.getLocation() + ") -> (" + destination + ")");
                data.setLocation(destination);
                //----- See if there's a zombie where the player has moved to
                if(theGame.isZombieAtLocation(destination)) {
                    resolveCombat(playerId);
                    if(data.isPlayerDead()) {
                        LOGGER.debug("Player (" + playerId + ") died!");
                        break;
                    }
                }
                if(theGame.isBulletAtLocation(destination)) {
                    LOGGER.trace("Player (" + playerId + ") found a bullet!");
                    theGame.getBulletLocations().remove(destination);
                    data.incrementBullets();
                }
                if(theGame.isLifeAtLocation(destination)) {
                    LOGGER.trace("Player (" + playerId + ") found a life!");
                    theGame.getLifeLocations().remove(destination);
                    data.incrementLife();
                }
            }
            else {
                // TODO: Should handle this case more elegantly
                LOGGER.trace("Player has attempted an invalid move. Allowing them to try again...");
                i--;
            }
            // TODO: Probably don't always want to braodcast this, eg PvP
            broadcastUpdateMessage(update);
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
        update.setZombieMovements(new HashMap<>());

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
                if(mapTileId != null) {
                    var mapTile = entityManager.findById(mapTileId, ZombiesMapTile.class).orElseThrow();
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
            }

            if(!zombieMoved) {
                LOGGER.trace("No viable move for zombie at (" + zombieLocation + ")");
            }
        }
        broadcastUpdateMessage(update);
    }

    private void resolveEventCardDiscards(@NotNull UUID playerId) {
        LOGGER.trace("Resolving event-card discards... (Game=" + theGame.getId() + ", Engine=" + gameEngineId + ", Player=" + playerId + ",Turn=" + theGame.getTurn() + ")");
        var cardIdsToDiscard = theBrokersByPlayerId.get(playerId).requestPlayerEventCardDiscards();
        var playerCardIds = theGame.getPlayerData(playerId).getEventCardIds();
        for(var cardId : cardIdsToDiscard) {
            LOGGER.trace("Player (" + playerId + ") discarding card: id= " + cardId);
            playerCardIds.removeIf(x -> x.equals(cardId));
        }
    }
}

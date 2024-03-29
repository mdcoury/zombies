package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.util.ZombiesEntityManagerHelper;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_GAME)
@Table(name = TABLE_GAME)
public class ZombiesGame implements IZombieModelObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGame.class);

    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 6;
    // TODO: Update to 3 when cards are implemented!
    public static final int MAX_NUM_EVENT_CARDS = 0;

    @Getter
    @Id
    @GeneratedValue
    @Column(name = COLUMN_ID, updatable = false, nullable = false)
    private UUID id;
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> bulletLocations = new HashSet<>();
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> lifeLocations = new HashSet<>();
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> zombieLocations = new HashSet<>();
    /** Maps a player-id to a game-data-id */
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<UUID, UUID> playerDataIds = new ConcurrentHashMap<>();
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<UUID> playerIds = new HashSet<>();
    @Getter
    @Column(name = COLUMN_MAP_ID, nullable = false, updatable = false, unique = true)
    private UUID mapId;
    @Getter
    @Column(name = COLUMN_POPULATED, nullable = false)
    private boolean populated = false;
    @Getter
    @Column(name = COLUMN_TURN, nullable = false)
    private int turn = 0;
    @Getter @Setter
    @Column(name = COLUMN_RUNNING, nullable = false)
    private boolean running = false;

    @Autowired @Transient
    private ZombiesEntityManagerHelper entityManager;

    @Transient
    private final Map<UUID, ZombiesGameData> gameDataCache = new HashMap<>();
    @Transient
    private ZombiesMap theMap;

    public ZombiesGame(@NotNull UUID mapId) {
        this.mapId = mapId;
    }

    public int getNumberOfPlayers() {
        assert playerDataIds.size() == playerIds.size();
        return playerIds.size();
    }

    @NotNull
    public ZombiesGameData getPlayerData(@NotNull UUID playerId) {
        if(!gameDataCache.containsKey(playerId)) {
            var playerData = entityManager.findById(playerDataIds.get(playerId), ZombiesGameData.class).orElseThrow();
            gameDataCache.put(playerId, playerData);
        }
        return gameDataCache.get(playerId);
    }

    public void incrementTurn() { turn++; }

    public boolean isZombieAtLocation(@NotNull ZombiesCoordinate coord) {
        return zombieLocations.contains(coord);
    }
    public boolean isBulletAtLocation(@NotNull ZombiesCoordinate coord) {
        return bulletLocations.contains(coord);
    }
    public boolean isLifeAtLocation(@NotNull ZombiesCoordinate coord) {
        return lifeLocations.contains(coord);
    }

    private ZombiesMap getMap() {
        assert entityManager != null;
        assert mapId != null;

        if(theMap == null) {
            theMap = entityManager.findById(mapId, ZombiesMap.class).orElseThrow();
        }
        return theMap;
    }

    private void populateMap() {
        assert populated;

        var map = getMap();
        //----- Go through all of the map-tiles and place its items
        var mapTiles = entityManager.findAllById(map.getMapTileIds().values(), ZombiesMapTile.class);
        for(var mapTile : mapTiles) {
            //----- The town-square starts with _no_ zombies...
            if(!mapTile.isTownSquare()) {
                //----- See if it's a building...
                if (mapTile.isBuilding()) {
                    //----- If it is, then it will have bullets and/or life and/or zombies, which should get placed into
                    //      the building
                    populateBuilding(mapTile);
                } else if (mapTile.isHelipad()) {
                    //----- The helipad starts with a zombie on every square
                    for (int i = 0; i < mapTile.getNumZombies(); i++) {
                        zombieLocations.add(new ZombiesCoordinate(
                                mapTile.getTopLeft().getX() + (i % TILE_SIZE),
                                mapTile.getTopLeft().getY() + (i / TILE_SIZE)
                        ));
                    }
                } else {
                    //----- This is a street piece -- ie, not a building -- so it should have one zombie at each exit,
                    //      no bullets and no life
                    populateStreet(mapTile);
                }
            }
        }
    }

    private void populateStreet(@NotNull ZombiesMapTile mapTile) {
        assert populated;

        // TODO: These need to be rotated!!
        var exits = mapTile.getExits();
        for(var exit : exits) {
            switch(exit) {
                case NORTH -> zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 1,
                        mapTile.getTopLeft().getY()
                ));
                case SOUTH -> zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 1,
                        mapTile.getTopLeft().getY() + 2
                ));
                case EAST -> zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 2,
                        mapTile.getTopLeft().getY() + 1
                ));
                case WEST -> zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX(),
                        mapTile.getTopLeft().getY() + 1
                ));
            }
        }
    }

    private void populateBuilding(@NotNull ZombiesMapTile mapTile) {
        assert populated;

        // TODO: These need to be rotated!!
        var buildingSquares = mapTile.getBuildingSquares();
        Collections.shuffle(buildingSquares);

        int nb = mapTile.getNumBullets();
        int nl = mapTile.getNumLife();
        int nz = mapTile.getNumZombies();

        for(var buildingSquare : buildingSquares) {
            if(nz > 0) {
                zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nz -= 1;
            }
            //----- A tile can have either a bullet or a life, not both
            if (nb > 0) {
                bulletLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nb -= 1;
            }
            else if(nl > 0) {
                lifeLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nl -= 1;
            }
        }
    }

    @Nullable
    public UUID addPlayer(@NotNull UUID playerId) {
        if(!isRunning()) {
            if(!playerIds.contains(playerId)) {
                assert !playerDataIds.containsKey(playerId);
                if (playerIds.size() < MAX_PLAYERS) {
                    LOGGER.debug("[game=" + getId() + "] Adding player: " + playerId);
                    playerIds.add(playerId);
                    var map = getMap();
                    var playerData = new ZombiesGameData(map.getTownSquareLocation());
                    playerData = entityManager.update(playerData);
                    playerDataIds.put(playerId, playerData.getId());
                    return playerData.getId();
                }
                LOGGER.debug("[game=" + getId() + "] Game is full: unable to add player: " + playerId);
            }
            else {
                assert playerDataIds.containsKey(playerId);
                LOGGER.debug("[game=" + getId() + "] Player has already joined this game: " + playerId);
            }
        }
        else {
            LOGGER.debug("[game=" + getId() + "] Game is already running! Unable to add player: " + playerId);
        }
        return null;
    }

    public void autowire(@NotNull AutowireCapableBeanFactory autowireFactory) {
        autowireFactory.autowireBean(this);
    }

    public boolean populate() {
        assert entityManager != null;

        if(populated) {
            throw new IllegalStateException("This game (" + getId() + ") is already populated!");
        }

        populated = true;
        var tic = System.currentTimeMillis();
        populateMap();
        LOGGER.trace("[game=" + getId() + "] Populating game took " + (System.currentTimeMillis()-tic) + "ms ");
        return true;
    }

}

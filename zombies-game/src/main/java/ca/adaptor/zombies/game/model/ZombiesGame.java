package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.*;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_GAME)
@Table(name = TABLE_GAME)
public class ZombiesGame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGame.class);

    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 6;

    public static final int MAX_NUM_EVENT_CARDS = 3;

    @Transient @Autowired
    private ZombiesGameRepository gameRepository;
    @Transient @Autowired
    private ZombiesGameDataRepository gameDataRepository;
    @Transient @Autowired
    private ZombiesPlayerRepository playerRepository;
    @Transient @Autowired
    private ZombiesMapRepository mapRepository;
    @Transient @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    @Getter
    @Id
    @GeneratedValue
    @Column(name = COLUMN_GAME_ID, updatable = false, nullable = false)
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
    private Map<UUID, UUID> playerDataId = new HashMap<>();
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> playerIds = new ArrayList<>();
    @Getter
    @Column(name = COLUMN_GAME_MAP_ID, nullable = false, updatable = false, unique = true)
    private UUID mapId;
    @Getter
    @Column(name = COLUMN_GAME_INITIALIZED, nullable = false)
    private boolean initialized = false;
    @Getter
    @Column(name =  COLUMN_GAME_TURN, nullable = false)
    private int turn = 0;

    public ZombiesGame(@NotNull ZombiesMap map) {
        this.mapId = map.getId();
    }

    @NotNull
    public ZombiesGameData getPlayerData(@NotNull UUID playerId) {
        return gameDataRepository.findById(playerId).orElseThrow();
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

    private void populateMap() {
        assert initialized;

        var map = mapRepository.findById(mapId).orElseThrow();
        LOGGER.trace("[game=" + getId() + "] Populating map (" + mapId + ")...");
        //----- Go through all of the map-tiles and place its items
        for(var mapTileId : map.getMapTileIds().values()) {
            var mapTile = mapTileRepository.findById(mapTileId).orElseThrow();
            var tile = mapTile.getTile();

            //----- The town-square starts with _no_ zombies...
            if(!tile.getName().equals(ZombiesTile.TOWN_SQUARE)) {
                //----- See if it's a building...
                if (tile.isBuilding()) {
                    //----- If it is, then it will have bullets and/or life and/or zombies, which should get placed into
                    //      the building
                    populateBuilding(mapTile);
                } else if (tile.getName().equals(ZombiesTile.HELIPAD)) {
                    //----- The helipad starts with a zombie on every square
                    for (int i = 0; i < tile.getNumZombies(); i++) {
                        zombieLocations.add(new ZombiesCoordinate(
                                mapTile.getTopLeft().getX() + (i % TILE_SIZE),
                                mapTile.getTopLeft().getY() + (i / TILE_SIZE)
                        ));
                    }
                } else {
                    //----- This is a street piece -- ie, not a building -- so it should have one zombie at each exit, no
                    //      bullets and no life
                    populateStreet(mapTile);
                }
            }
        }
    }

    private void populateStreet(@NotNull ZombiesMapTile mapTile) {
        assert initialized;

        LOGGER.trace("[game=" + getId() + "] Populating street: " + mapTile.getTile().getName());

        var tile = mapTile.getTile();
        var exits = tile.getExits();
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
        assert initialized;

        LOGGER.trace("[game=" + getId() + "] Populating building: " + mapTile.getTile().getName());

        var tile = mapTile.getTile();
        var buildingSquares = tile.getBuildingSquares();
        Collections.shuffle(buildingSquares);

        int nb = tile.getNumBullets();
        int nl = tile.getNumLife();
        int nz = tile.getNumZombies();

        for(var buildingSquare : buildingSquares) {
            if(nz > 0) {
                zombieLocations.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nz -= 1;
            }

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
            else {
                break;
            }
        }
    }

    public boolean addPlayer(@NotNull ZombiesPlayer player) {
        if(!isInitialized()) {
            if (playerIds.size() < MAX_PLAYERS) {
                LOGGER.debug("[game=" + getId() + "] Adding player: " + player);
                playerIds.add(player.getId());
                return true;
            }
            LOGGER.debug("[game=" + getId() + "] Game is full: unable to add player " + player);
        }
        else {
            LOGGER.debug("[game=" + getId() + "] Game is already started: unable to add player " + player);
        }
        return false;
    }

    public boolean initialize() {
        if(initialized) {
            throw new IllegalStateException("This game (" + getId() + ") is already initialized!");
        }
        if(playerIds.size() < MIN_PLAYERS) {
            LOGGER.warn("[game=" + getId() + "] Not enough players in game!");
            return false;
        }

        LOGGER.debug("[game=" + getId() + "] Initializing game...");

        initialized = true;
        populateMap();
        return true;
    }
}

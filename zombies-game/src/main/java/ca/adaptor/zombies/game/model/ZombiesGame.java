package ca.adaptor.zombies.game.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_PLAYER_ID;

public class ZombiesGame {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesGame.class);
    private static final int MIN_PLAYERS = 1;
    private static final int MAX_PLAYERS = 6;

    private UUID gameId = UUID.randomUUID();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> theBullets = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> theLife = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<ZombiesCoordinate> theZombies = new HashSet<>();
    @JoinColumn(name = COLUMN_PLAYER_ID, nullable = false, updatable = false)
    @OneToMany(fetch = FetchType.EAGER)
    private List<ZombiesPlayer> thePlayers = new ArrayList<>();
    private ZombiesMap theMap;

    private boolean started = false;

    public ZombiesGame(@NotNull ZombiesMap map) {
        this.theMap = map;
    }

    private void populateMap() {
        if(!started) {
            throw new IllegalStateException();
        }
        LOGGER.trace("Populating map...");
        //----- Go through all of the map-tiles and place its items
        for(var mapTile : theMap.getMapTiles()) {
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
                        theZombies.add(new ZombiesCoordinate(
                                mapTile.getTopLeft().getX() + (i % 3),
                                mapTile.getTopLeft().getY() + (i / 3)
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
        LOGGER.trace("Populating street: " + mapTile.getTile().getName());

        var tile = mapTile.getTile();
        var exits = tile.getExits();
        for(var exit : exits) {
            switch(exit) {
                case NORTH -> theZombies.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 1,
                        mapTile.getTopLeft().getY()
                ));
                case SOUTH -> theZombies.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 1,
                        mapTile.getTopLeft().getY() + 2
                ));
                case EAST -> theZombies.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + 2,
                        mapTile.getTopLeft().getY() + 1
                ));
                case WEST -> theZombies.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX(),
                        mapTile.getTopLeft().getY() + 1
                ));
            }
        }
    }

    private void populateBuilding(@NotNull ZombiesMapTile mapTile) {
        LOGGER.trace("Populating building: " + mapTile.getTile().getName());

        var tile = mapTile.getTile();
        var buildingSquares = tile.getBuildingSquares();
        Collections.shuffle(buildingSquares);

        int nb = tile.getNumBullets();
        int nl = tile.getNumLife();
        int nz = tile.getNumZombies();

        for(var buildingSquare : buildingSquares) {
            if(nz > 0) {
                theZombies.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nz--;
            }

            if (nb > 0) {
                theBullets.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nb--;
            }
            else if(nl > 0) {
                theLife.add(new ZombiesCoordinate(
                        mapTile.getTopLeft().getX() + buildingSquare.getX(),
                        mapTile.getTopLeft().getY() + buildingSquare.getY()
                ));
                nl--;
            }
            else {
                break;
            }
        }
    }

    public boolean addPlayer(@NotNull ZombiesPlayer player) {
        if(thePlayers.size() < MAX_PLAYERS) {
            LOGGER.debug("Adding player: " + player);
            thePlayers.add(player);
            return true;
        }

        LOGGER.debug("Game is full: unable to add player " + player);
        return false;
    }

    public boolean start() {
        if(started) {
            throw new IllegalStateException();
        }
        if(thePlayers.size() < MIN_PLAYERS) {
            return false;
        }

        LOGGER.debug("Starting game: " + gameId);

        started = true;
        populateMap();
        return true;
    }
}

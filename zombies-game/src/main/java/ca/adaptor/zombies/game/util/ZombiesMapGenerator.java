package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static ca.adaptor.zombies.game.model.ZombiesTile.NUM_SIDES;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

public class ZombiesMapGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapGenerator.class);

    @NotNull
    public static ZombiesMap create(
            @NotNull ZombiesEntityManagerHelper entityManager,
            @NotNull AutowireCapableBeanFactory autowireFactory,
            @NotNull Random rng
    ) {
        var ret = new ZombiesMap();
        ret.autowire(autowireFactory);
        var deck = shuffleTiles(entityManager, rng);

        var exits = new ArrayList<ZombiesCoordinate>();
        placeTile(entityManager.findTileByName(ZombiesTile.TOWN_SQUARE).orElseThrow(),
                ret, exits, entityManager, rng);
        for(var tile : deck) {
            placeTile(tile, ret, exits, entityManager, rng);
        }
//        LOGGER.trace(ret.dump());
        return ret;
    }

    private static void placeTile(
            ZombiesTile tile,
            ZombiesMap map,
            List<ZombiesCoordinate> exits,
            ZombiesEntityManagerHelper entityManager,
            Random rng
    ) {
        if(exits.size() == 0) {
            if(tile.getName().equals(ZombiesTile.TOWN_SQUARE)) {
                // TODO: Do the math so this is centred at (0,0)
                var topLeft = new ZombiesCoordinate(90,90);
                var mapTile = new ZombiesMapTile(tile, topLeft, ZombiesMapTile.TileRotation.ROT_0);
                entityManager.save(mapTile);
                var added = map.add(mapTile);
                if(added) {
                    addExits(map, tile, exits, topLeft, ZombiesMapTile.TileRotation.ROT_0);
                }
                else {
                    throw new IllegalStateException();
                }
            }
            else {
                throw new IllegalStateException();
            }
        }
        else {
            //----- We'll go through the remaining exits randomly
            Collections.shuffle(exits, rng);
            ZombiesCoordinate exitToRemove = null;
            for(var exit : exits) {
                //----- Where is the exit inside the tile
                var exitOffset = getOffset(exit);
                //----- What is the top-left coordinate of this exit's tile
                var exitTopLeft = getTopLeft(exit);
                //----- Which side is the exit on
                var exitSide = (exitOffset.getX() == 1)
                        ? ((exitOffset.getY() == 0) ? ZombiesDirection.NORTH : ZombiesDirection.SOUTH)
                        : ((exitOffset.getX() == 0) ? ZombiesDirection.WEST : ZombiesDirection.EAST);
                //----- What is the top-left of the tile we are exiting to; ie, this is where we are looking to place
                //      the new tile
                var targetTopLeft = getNeighbourTopLeft(exitTopLeft, exitSide);

                //----- Try all four rotations and accept iff one of them works
                for(var rotation : ZombiesMapTile.TileRotation.values()) {
                    //----- Check to see if this is a valid placement; ie, there is not already a tile there (shouldn't
                    //      happen) and all of the new tile's exits align either with an existing exit or are open
                    var mapTile = new ZombiesMapTile(tile, targetTopLeft, rotation);
                    if(checkValidPlacement(targetTopLeft, mapTile, map, entityManager)) {
                        entityManager.save(mapTile);
                        var added = map.add(mapTile);
                        if(added) {
                            //----- Remove the exit we aligned to
                            exitToRemove = exit;
                            addExits(map, tile, exits, targetTopLeft, rotation);
                            break;
                        }
                        else {
                            throw new IllegalStateException();
                        }
                    }
                }
                if(exitToRemove != null) {
                    break;
                }
                //----- Else, go on to the next one
            }
            if(exitToRemove != null) {
                exits.remove(exitToRemove);
            }
            else {
                LOGGER.debug("No exit to remove for this tile!");
            }
        }
    }

    private static void addExits(
            @NotNull ZombiesMap map,
            @NotNull ZombiesTile tile,
            @NotNull List<ZombiesCoordinate> exits,
            @NotNull ZombiesCoordinate tileTopLeft,
            @NotNull ZombiesMapTile.TileRotation rotation
    ) {
        var tileExits = tile.getExits();
        for(var tileExit : tileExits) {
            var exit = ZombiesDirection.values()[
                    (tileExit.ordinal() + rotation.ordinal()) % NUM_SIDES
                    ];

            //----- Only add exits that are not already attached to another tile
            ZombiesCoordinate tileExitCoord, alignedExitCoord;
            switch(exit) {
                case NORTH -> {
                    tileExitCoord = new ZombiesCoordinate(tileTopLeft.getX() + 1, tileTopLeft.getY());
                    alignedExitCoord = new ZombiesCoordinate((tileTopLeft.getX()) + 1, tileTopLeft.getY() - 1);
                }
                case SOUTH -> {
                    tileExitCoord = new ZombiesCoordinate(tileTopLeft.getX() + 1, tileTopLeft.getY() + 2);
                    alignedExitCoord = new ZombiesCoordinate((tileTopLeft.getX()) + 1, tileTopLeft.getY() + 3);
                }
                case EAST  -> {
                    tileExitCoord = new ZombiesCoordinate(tileTopLeft.getX() + 2, tileTopLeft.getY() + 1);
                    alignedExitCoord = new ZombiesCoordinate((tileTopLeft.getX()) + 3, tileTopLeft.getY() + 1);
                }
                case WEST  -> {
                    tileExitCoord = new ZombiesCoordinate(tileTopLeft.getX(), tileTopLeft.getY() + 1);
                    alignedExitCoord = new ZombiesCoordinate((tileTopLeft.getX()) - 1, tileTopLeft.getY() + 1);
                }
                default -> throw new IllegalArgumentException();
            }

            var mapTileId = map.getMapTileId(alignedExitCoord);
            if(mapTileId == null) {
                exits.add(tileExitCoord);
            }
        }
    }

    private static ZombiesCoordinate getNeighbourTopLeft(ZombiesCoordinate coord, ZombiesDirection dir) {
        switch(dir) {
            case NORTH -> {
                return new ZombiesCoordinate(coord.getX(), coord.getY() - TILE_SIZE);
            }
            case SOUTH -> {
                return new ZombiesCoordinate(coord.getX(), coord.getY() + TILE_SIZE);
            }
            case EAST -> {
                return new ZombiesCoordinate(coord.getX() + TILE_SIZE, coord.getY());
            }
            case WEST -> {
                return new ZombiesCoordinate(coord.getX() - TILE_SIZE, coord.getY());
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private static ZombiesCoordinate getTopLeft(ZombiesCoordinate coord) {
        return new ZombiesCoordinate(
                coord.getX() - (coord.getX() % TILE_SIZE),
                coord.getY() - (coord.getY() % TILE_SIZE)
        );
    }
    private static ZombiesCoordinate getOffset(ZombiesCoordinate coord) {
        return new ZombiesCoordinate(
                coord.getX() % TILE_SIZE, coord.getY() % TILE_SIZE
        );
    }

    private static List<ZombiesTile> shuffleTiles(
            ZombiesEntityManagerHelper entityManager,
            Random rng
    ) {
        // TODO: Create query to load all the tiles /but/ town square and helipad
        var ret = entityManager.findAll(ZombiesTile.class);

        var townSquare = entityManager.findTileByName(ZombiesTile.TOWN_SQUARE).orElseThrow();
        ret.remove(townSquare);
        var helipad = entityManager.findTileByName(ZombiesTile.HELIPAD).orElseThrow();
        ret.remove(helipad);

        Collections.shuffle(ret, rng);

        int ncards = ret.size();
        var index = rng.nextInt(ncards/2) + ncards/2;
        ret.add(index, helipad);

        return ret;
    }
    private static boolean checkValidPlacement(
            @NotNull ZombiesCoordinate xy,
            @NotNull ZombiesMapTile mapTile,
            @NotNull ZombiesMap map,
            @NotNull ZombiesEntityManagerHelper entityManager
    ) {
        return checkValidPlacement(xy.getX(), xy.getY(), mapTile, map, entityManager);
    }
    private static boolean checkValidPlacement(
            int x,
            int y,
            @NotNull ZombiesMapTile testTile,
            @NotNull ZombiesMap map,
            @NotNull ZombiesEntityManagerHelper entityManager
    ) {
        if(x%TILE_SIZE == 0 && y%TILE_SIZE == 0) {
            var topLeft = new ZombiesCoordinate(x, y);
            //----- Ensure that there is not already a tile here
            if (!map.getMapTileIds().containsKey(topLeft)) {
                //----- Ensure that all of this tile's exits align either with an exit or empty space
                return
                        // NORTH
                        testAdjacent(map, x+1, y-1, testTile.get(1,0) == ZombiesTile.SquareType.ROAD, entityManager)
                                // EAST
                                && testAdjacent(map, x+3, y+1, testTile.get(2,1) == ZombiesTile.SquareType.ROAD, entityManager)
                                // WEST
                                && testAdjacent(map, x-1, y+1, testTile.get(0,1) == ZombiesTile.SquareType.ROAD, entityManager)
                                // SOUTH
                                && testAdjacent(map, x+1, y+3, testTile.get(1,2) == ZombiesTile.SquareType.ROAD, entityManager)
                        ;
            }
        }
        return false;
    }
    private static boolean testAdjacent(ZombiesMap map, int x, int y, boolean isRoad, ZombiesEntityManagerHelper entityManager) {
        ZombiesTile.SquareType targetSquare = null;
        var targetTileId = map.getMapTileId(new ZombiesCoordinate(x,y));
        if(targetTileId != null) {
            var targetTile = entityManager.findById(targetTileId, ZombiesMapTile.class).orElseThrow();
            targetSquare = targetTile.getSquareType(x,y);
        }

        if(isRoad) {
            return targetSquare == null || targetSquare == ZombiesTile.SquareType.ROAD;
        }
        return targetSquare != ZombiesTile.SquareType.ROAD;
    }
}

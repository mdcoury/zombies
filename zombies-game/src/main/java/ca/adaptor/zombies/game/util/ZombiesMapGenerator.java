package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.ZombiesCoordinate;
import ca.adaptor.zombies.game.model.ZombiesMap;
import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.*;

public class ZombiesMapGenerator {
    private static Logger LOGGER = LoggerFactory.getLogger(ZombiesMapGenerator.class);

    private static Random RNG = new Random();
    private static int NUM_SIDES = 4;

    @NonNull
    public static ZombiesMap create(
            ZombiesTileRepository zombiesTileRepository,
            ZombiesMapRepository zombiesMapRepository,
            ZombiesMapTileRepository zombiesMapTileRepository
    ) {
        var ret = new ZombiesMap();
        var deck = shuffleTiles(zombiesTileRepository);

        var exits = new ArrayList<ZombiesCoordinate>();
        placeTile(zombiesTileRepository.findByName(ZombiesTile.TOWN_SQUARE), ret, exits, zombiesMapTileRepository);
        for(var tile : deck) {
            placeTile(tile, ret, exits, zombiesMapTileRepository);
        }

        LOGGER.debug("\n" + ret.dump());
        zombiesMapRepository.saveAndFlush(ret);
        return ret;
    }

    private static void placeTile(
            ZombiesTile tile,
            ZombiesMap map,
            List<ZombiesCoordinate> exits,
            ZombiesMapTileRepository repository
    ) {
        if(exits.size() == 0) {
            if(tile.getName().equals(ZombiesTile.TOWN_SQUARE)) {
                var topLeft = new ZombiesCoordinate(90,90);
                var mapTile = map.add(topLeft, tile, ZombiesTile.TileRotation.ROT_0);
                repository.save(mapTile);
                addExits(map, tile, exits, topLeft, ZombiesTile.TileRotation.ROT_0);
            }
            else {
                throw new IllegalStateException();
            }
        }
        else {
            //----- We'll go through the remaining exits randomly
            Collections.shuffle(exits, RNG);
            ZombiesCoordinate exitToRemove = null;
            for(var exit : exits) {
                //----- Where is the exit inside the tile
                var exitOffset = getOffset(exit);
                //----- What is the top-left coordinate of this exit's tile
                var exitTopLeft = getTopLeft(exit);
                //----- Which side is the exit on
                var exitSide = (exitOffset.getX() == 1)
                        ? ((exitOffset.getY() == 0) ? ZombiesTile.TileSide.NORTH : ZombiesTile.TileSide.SOUTH)
                        : ((exitOffset.getX() == 0) ? ZombiesTile.TileSide.WEST : ZombiesTile.TileSide.EAST);
                //----- What is the top-left of the tile we are exiting to; ie, this is where we are looking to place
                //      the new tile
                var targetTopLeft = getNeighbourTopLeft(exitTopLeft, exitSide);

                //----- Try all four rotations and accept iff one of them works
                for(var rotation : ZombiesTile.TileRotation.values()) {
                    //----- Check to see if this is a valid placement; ie, there is not already a tile there (shouldn't
                    //      happen) and all of the new tile's exits align either with an existing exit or are open
                    if(map.checkValidPlacement(targetTopLeft, tile, rotation)) {
                        var mapTile = map.add(targetTopLeft, tile, rotation);
                        repository.save(mapTile);
                        //----- Remove the exit we aligned to
                        exitToRemove = exit;
                        addExits(map, tile, exits, targetTopLeft, rotation);
                        break;
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
            ZombiesMap map,
            ZombiesTile tile,
            List<ZombiesCoordinate> exits,
            ZombiesCoordinate tileTopLeft,
            ZombiesTile.TileRotation rotation
    ) {
        var tileExits = tile.getExits();
        for(var tileExit : tileExits) {
            var exit = ZombiesTile.TileSide.values()[
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
                default    -> throw new IllegalArgumentException();
            }

            if(map.get(getTopLeft(alignedExitCoord)) == null) {
                exits.add(tileExitCoord);
            }
        }
    }

    private static ZombiesCoordinate getNeighbourTopLeft(ZombiesCoordinate coord, ZombiesTile.TileSide dir) {
        switch(dir) {
            case NORTH -> {
                return new ZombiesCoordinate(coord.getX(), coord.getY() - 3);
            }
            case SOUTH -> {
                return new ZombiesCoordinate(coord.getX(), coord.getY() + 3);
            }
            case EAST -> {
                return new ZombiesCoordinate(coord.getX() + 3, coord.getY());
            }
            case WEST -> {
                return new ZombiesCoordinate(coord.getX() - 3, coord.getY());
            }
            default -> throw new IllegalArgumentException();
        }
    }

    private static ZombiesCoordinate getTopLeft(ZombiesCoordinate coord) {
        return new ZombiesCoordinate(
                coord.getX() - (coord.getX()%3),
                coord.getY() - (coord.getY()%3)
        );
    }
    private static ZombiesCoordinate getOffset(ZombiesCoordinate coord) {
        return new ZombiesCoordinate(
                coord.getX() % 3, coord.getY() % 3
        );
    }

    private static List<ZombiesTile> shuffleTiles(ZombiesTileRepository repository) {
        var ret = repository.findAll();
        int ncards = ret.size();

        var townSquare = repository.findByName(ZombiesTile.TOWN_SQUARE);
        ret.remove(townSquare);
        var helipad = repository.findByName(ZombiesTile.HELIPAD);
        ret.remove(helipad);

        Collections.shuffle(ret, RNG);

        var index = RNG.nextInt(ncards/2) + ncards/2;
        ret.add(index, helipad);

        return ret;
    }
}

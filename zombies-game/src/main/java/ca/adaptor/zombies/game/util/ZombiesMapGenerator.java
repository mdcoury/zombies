package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.ZombiesCoordinate;
import ca.adaptor.zombies.game.model.ZombiesDirection;
import ca.adaptor.zombies.game.model.ZombiesMap;
import ca.adaptor.zombies.game.model.ZombiesTile;
import ca.adaptor.zombies.game.repositories.ZombiesMapRepository;
import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
import ca.adaptor.zombies.game.repositories.ZombiesTileRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static ca.adaptor.zombies.game.model.ZombiesTile.NUM_SIDES;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

public class ZombiesMapGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMapGenerator.class);

    @NonNull
    public static ZombiesMap create(
            @NonNull ZombiesTileRepository zombiesTileRepository,
            @NonNull ZombiesMapRepository zombiesMapRepository,
            @NonNull ZombiesMapTileRepository zombiesMapTileRepository,
            @NonNull Random rng
    ) {
        var ret = new ZombiesMap();
        zombiesMapRepository.saveAndFlush(ret);
        var deck = shuffleTiles(zombiesTileRepository, rng);

        var exits = new ArrayList<ZombiesCoordinate>();
        placeTile(zombiesTileRepository.findByName(ZombiesTile.TOWN_SQUARE), ret, exits, zombiesMapTileRepository, rng);
        for(var tile : deck) {
            placeTile(tile, ret, exits, zombiesMapTileRepository, rng);
        }

        zombiesMapRepository.saveAndFlush(ret);
        return ret;
    }

    private static void placeTile(
            ZombiesTile tile,
            ZombiesMap map,
            List<ZombiesCoordinate> exits,
            ZombiesMapTileRepository repository,
            Random rng
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
            @NotNull ZombiesMap map,
            @NotNull ZombiesTile tile,
            @NotNull List<ZombiesCoordinate> exits,
            @NotNull ZombiesCoordinate tileTopLeft,
            @NotNull ZombiesTile.TileRotation rotation
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

            if(map.getSquareType(alignedExitCoord) == null) {
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
            ZombiesTileRepository repository,
            Random rng
    ) {
        var ret = repository.findAll();
        int ncards = ret.size();

        var townSquare = repository.findByName(ZombiesTile.TOWN_SQUARE);
        ret.remove(townSquare);
        var helipad = repository.findByName(ZombiesTile.HELIPAD);
        ret.remove(helipad);

        Collections.shuffle(ret, rng);

        var index = rng.nextInt(ncards/2) + ncards/2;
        ret.add(index, helipad);

        return ret;
    }
}

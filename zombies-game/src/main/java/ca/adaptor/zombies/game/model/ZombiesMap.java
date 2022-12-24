package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;

@EqualsAndHashCode
@ToString
@Entity
@Table
public class ZombiesMap {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMap.class);
    public enum TileRotation {
        ROT_0, ROT_90, ROT_180, ROT_270
    }

    @Getter
    @Id
    @Column(name = COLUMN_MAP_ID, updatable = false, nullable = false)
    private UUID uuid;
    @ElementCollection
    private Map<ZombiesCoordinate, ZombiesMapTile> mapTiles = new HashMap<>();

    public ZombiesMap() {
        this.uuid = UUID.randomUUID();
    }

    public boolean check(ZombiesCoordinate xy, ZombiesTile tile, TileRotation rotation) {
        return check(xy.getX(), xy.getY(), tile, rotation);
    }
    private boolean testAdjacent(int x, int y, boolean isRoad) {
        var target = get(x,y);
        if(isRoad) {
            return target == null || target == ZombiesTile.SquareType.ROAD;
        }
        return target != ZombiesTile.SquareType.ROAD;
    }
    public boolean check(int x, int y, ZombiesTile tile, TileRotation rotation) {
        if(x%3 == 0 && y%3 == 0) {
            var topLeft = new ZombiesCoordinate(x, y);
            var testTile = new ZombiesMapTile(tile, rotation);
            //----- Ensure that there is not already a tile here
            if (!mapTiles.containsKey(topLeft)) {
                //----- Ensure that all of this tile's exits align either with an exit or empty space
                return
                    // NORTH
                       testAdjacent(x+1, y-1, testTile.get(1,0) == ZombiesTile.SquareType.ROAD)
                    // EAST
                    && testAdjacent(x+3, y+1, testTile.get(2,1) == ZombiesTile.SquareType.ROAD)
                    // WEST
                    && testAdjacent(x-1, y+1, testTile.get(0,1) == ZombiesTile.SquareType.ROAD)
                    // SOUTH
                    && testAdjacent(x+1, y+3, testTile.get(1,2) == ZombiesTile.SquareType.ROAD)
                    ;
            }
        }
        return false;
    }

    public ZombiesMapTile add(int x, int y, ZombiesTile tile, TileRotation rotation) {
        return add(new ZombiesCoordinate(x,y), tile, rotation);
    }
    public ZombiesMapTile add(ZombiesCoordinate topLeft, ZombiesTile tile, TileRotation rotation) {
        LOGGER.trace("Placed " + tile.getName() + " at " + topLeft + ", " + rotation);
        var mapTile = new ZombiesMapTile(tile, rotation);
        mapTiles.put(topLeft, mapTile);
        return mapTile;
    }

    @Nullable
    public ZombiesTile.SquareType get(int x, int y) {
        return get(new ZombiesCoordinate(x, y));
    }

    @Nullable
    public ZombiesTile.SquareType get(ZombiesCoordinate xy) {
        //----- Get the top-left coordinate for xy's tile
        var tl = new ZombiesCoordinate(
                xy.getX() - (xy.getX()%3),
                xy.getY() - (xy.getY()%3)
        );
        //----- Find the MapTile for this coordinate
        if(mapTiles.containsKey(tl)) {
            var tile = mapTiles.get(tl);
            var square = tile.get(xy.getX() - tl.getX(), xy.getY() - tl.getY());
            return square;
        }
        return null;
    }

    @NonNull
    public String dump() {
        var ret = new StringBuilder("Map dump:\n");

        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;

        for(var key : mapTiles.keySet()) {
            minx = Math.min(minx, key.getX());
            maxx = Math.max(maxx, key.getX());
            miny = Math.min(miny, key.getY());
            maxy = Math.max(maxy, key.getY());
        }
        maxx += 3;
        maxy += 3;

        var map = new Integer[maxy - miny][maxx - minx];
        for(var entry : mapTiles.entrySet()) {
            int tlx = entry.getKey().getX() + -minx;
            int tly = entry.getKey().getY() + -miny;

            map[tly  ][tlx  ] = entry.getValue().get(0,0).ordinal();
            map[tly  ][tlx+1] = entry.getValue().get(1,0).ordinal();
            map[tly  ][tlx+2] = entry.getValue().get(2,0).ordinal();
            map[tly+1][tlx  ] = entry.getValue().get(0,1).ordinal();
            map[tly+1][tlx+1] = entry.getValue().get(1,1).ordinal();
            map[tly+1][tlx+2] = entry.getValue().get(2,1).ordinal();
            map[tly+2][tlx  ] = entry.getValue().get(0,2).ordinal();
            map[tly+2][tlx+1] = entry.getValue().get(1,2).ordinal();
            map[tly+2][tlx+2] = entry.getValue().get(2,2).ordinal();
        }

        for (var row : map) {
            for (var sqr : row) {
                if (sqr != null) {
                    ret.append(sqr);
                } else {
                    ret.append(' ');
                }
            }
            ret.append('\n');
        }

        return ret.toString();
    }
}

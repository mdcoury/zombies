package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;
import static org.hibernate.annotations.CascadeType.*;

@EqualsAndHashCode
@ToString
@Entity(name = TABLE_MAP)
@Table(name = TABLE_MAP)
public class ZombiesMap {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesMap.class);

    @Getter
    @Id
    @GeneratedValue
    @Column(name = COLUMN_MAP_ID, updatable = false, nullable = false)
    private UUID id;
    @ElementCollection(fetch = FetchType.LAZY)
    private Map<ZombiesCoordinate, ZombiesMapTile> mapTiles;
    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = COLUMN_X, column = @Column(name = COLUMN_TOWN_SQUARE_X)),
            @AttributeOverride(name = COLUMN_Y, column = @Column(name = COLUMN_TOWN_SQUARE_Y)),
    })
    private ZombiesCoordinate townSquareLocation;
    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = COLUMN_X, column = @Column(name = COLUMN_HELIPAD_X)),
            @AttributeOverride(name = COLUMN_Y, column = @Column(name = COLUMN_HELIPAD_Y)),
    })
    private ZombiesCoordinate helipadLocation;

    public ZombiesMap() {
        mapTiles = new HashMap<>();
    }

    private boolean testAdjacent(int x, int y, boolean isRoad) {
        var targetSquare = getSquareType(x,y);
        if(isRoad) {
            return targetSquare == null || targetSquare == ZombiesTile.SquareType.ROAD;
        }
        return targetSquare != ZombiesTile.SquareType.ROAD;
    }
    public boolean checkValidPlacement(
            @NotNull ZombiesCoordinate xy,
            @NotNull ZombiesTile tile,
            ZombiesMapTile.TileRotation rotation
    ) {
        return checkValidPlacement(xy.getX(), xy.getY(), tile, rotation);
    }
    public boolean checkValidPlacement(int x, int y, @NotNull ZombiesTile tile, ZombiesMapTile.TileRotation rotation) {
        if(x%TILE_SIZE == 0 && y%TILE_SIZE == 0) {
            var topLeft = new ZombiesCoordinate(x, y);
            var testTile = new ZombiesMapTile(tile, topLeft, rotation);
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

    @NotNull
    public ZombiesMapTile add(int x, int y, @NotNull ZombiesTile tile, ZombiesMapTile.TileRotation rotation) {
        return add(new ZombiesCoordinate(x,y), tile, rotation);
    }
    @NotNull
    public ZombiesMapTile add(@NotNull ZombiesCoordinate topLeft, @NotNull ZombiesTile tile, ZombiesMapTile.TileRotation rotation) {
        LOGGER.trace("[map=" + getId() + "] Placed " + tile.getName() + " at " + topLeft + ", " + rotation);
        var mapTile = new ZombiesMapTile(tile, topLeft, rotation);
        mapTiles.put(topLeft, mapTile);

        if(tile.getName().equals(ZombiesTile.TOWN_SQUARE)) {
            assert townSquareLocation == null;
            townSquareLocation = new ZombiesCoordinate(topLeft.getX() + 1, topLeft.getY() + 1);
        }
        if(tile.getName().equals(ZombiesTile.HELIPAD)) {
            assert helipadLocation == null;
            helipadLocation = new ZombiesCoordinate(topLeft.getX() + 1, topLeft.getY() + 1);
        }
        return mapTile;
    }

    @NotNull
    public Collection<ZombiesMapTile> getMapTiles() {
        return mapTiles.values();
    }

    @Nullable
    public ZombiesMapTile getMapTile(@NotNull ZombiesCoordinate xy) {
        //----- Get the top-left coordinate for xy's tile
        var tl = new ZombiesCoordinate(
                xy.getX() - (xy.getX() % TILE_SIZE),
                xy.getY() - (xy.getY() % TILE_SIZE)
        );
        //----- Find the MapTile for this coordinate
        if(mapTiles.containsKey(tl)) {
            return mapTiles.get(tl);
        }
        return null;
    }

    @Nullable
    public ZombiesTile.SquareType getSquareType(int x, int y) {
        return getSquareType(new ZombiesCoordinate(x, y));
    }

    @Nullable
    public ZombiesTile.SquareType getSquareType(@NotNull ZombiesCoordinate xy) {
        var mapTile = getMapTile(xy);
        if(mapTile != null) {
            return mapTile.get(xy.getX() % TILE_SIZE, xy.getY() % TILE_SIZE);
        }
        return null;
    }

    @NotNull
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

package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.*;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

@NoArgsConstructor
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
    @Getter
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<ZombiesCoordinate, UUID> mapTileIds = new HashMap<>();
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

    @Transient
    private ZombiesMapTileRepository mapTileRepository;

    public ZombiesMap(@Autowired ZombiesMapTileRepository mapTileRepository) {
        this.mapTileRepository = mapTileRepository;
    }

    public void add(@NotNull ZombiesMapTile mapTile) {
        LOGGER.trace("[map=" + getId() + "] Placed " + mapTile);
        mapTileIds.put(mapTile.getTopLeft(), mapTile.getId());

        var tile = mapTile.getTile();
        if(tile.getName().equals(ZombiesTile.TOWN_SQUARE)) {
            assert townSquareLocation == null;
            townSquareLocation = new ZombiesCoordinate(mapTile.getTopLeft().getX() + 1, mapTile.getTopLeft().getY() + 1);
        }
        if(tile.getName().equals(ZombiesTile.HELIPAD)) {
            assert helipadLocation == null;
            helipadLocation = new ZombiesCoordinate(mapTile.getTopLeft().getX() + 1, mapTile.getTopLeft().getY() + 1);
        }
    }

    @Nullable
    public UUID getMapTileId(@NotNull ZombiesCoordinate xy) {
        //----- Get the top-left coordinate for xy's tile
        var tl = new ZombiesCoordinate(
                xy.getX() - (xy.getX() % TILE_SIZE),
                xy.getY() - (xy.getY() % TILE_SIZE)
        );
        //----- Find the MapTile for this coordinate
        if(mapTileIds.containsKey(tl)) {
            return mapTileIds.get(tl);
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

        for(var key : mapTileIds.keySet()) {
            minx = Math.min(minx, key.getX());
            maxx = Math.max(maxx, key.getX());
            miny = Math.min(miny, key.getY());
            maxy = Math.max(maxy, key.getY());
        }
        maxx += 3;
        maxy += 3;

        var map = new Integer[maxy - miny][maxx - minx];
        for(var entry : mapTileIds.entrySet()) {
            int tlx = entry.getKey().getX() + -minx;
            int tly = entry.getKey().getY() + -miny;

            var mapTile = mapTileRepository.findById(entry.getValue()).orElseThrow();
            map[tly  ][tlx  ] = mapTile.get(0,0).ordinal();
            map[tly  ][tlx+1] = mapTile.get(1,0).ordinal();
            map[tly  ][tlx+2] = mapTile.get(2,0).ordinal();
            map[tly+1][tlx  ] = mapTile.get(0,1).ordinal();
            map[tly+1][tlx+1] = mapTile.get(1,1).ordinal();
            map[tly+1][tlx+2] = mapTile.get(2,1).ordinal();
            map[tly+2][tlx  ] = mapTile.get(0,2).ordinal();
            map[tly+2][tlx+1] = mapTile.get(1,2).ordinal();
            map[tly+2][tlx+2] = mapTile.get(2,2).ordinal();
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

package ca.adaptor.zombies.game.model;

import ca.adaptor.zombies.game.repositories.ZombiesMapTileRepository;
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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Comparator;
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
    @Getter
    @Column
    private int minx = Integer.MAX_VALUE;
    @Getter
    @Column
    private int miny = Integer.MAX_VALUE;

    @Transient @Autowired
    private ZombiesMapTileRepository mapTileRepository;

    public void autowire(@NotNull AutowireCapableBeanFactory autowireFactory) {
        autowireFactory.autowireBean(this);
    }

    public boolean add(@NotNull ZombiesMapTile mapTile) {
        assert mapTile.getId() != null;

        var topLeft = mapTile.getTopLeft();
        if(!mapTileIds.containsKey(topLeft)) {
            mapTileIds.put(topLeft, mapTile.getId());
            minx = Math.min(minx, topLeft.getX());
            miny = Math.min(miny, topLeft.getY());

            if (mapTile.isTownSquare()) {
                assert townSquareLocation == null;
                townSquareLocation = new ZombiesCoordinate(topLeft.getX() + 1, topLeft.getY() + 1);
            } else if (mapTile.isHelipad()) {
                assert helipadLocation == null;
                helipadLocation = new ZombiesCoordinate(topLeft.getX() + 1, topLeft.getY() + 1);
            }
            return true;
        }
        return false;
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
